package davis_putnam;

import java.util.*;
import java.io.*;


public class DavisPutnam {
	
	ArrayList<ArrayList<Literal>> clauses = new ArrayList<ArrayList<Literal>>();//propositional clauses
	ArrayList<Integer> literalValues = new ArrayList<Integer>();//a list of the literal values
	ArrayList<String> backMatter = new ArrayList<String>();//used by the back end
	Boolean[] truthValues;//the result of running the DP algorithm

	public static void main(String[] args) throws Exception
	{
		DavisPutnam dp = new DavisPutnam();
		dp.read(); //read clauses and literal values from input file
		dp.truthValues = new Boolean[dp.literalValues.size()];//initialize the Boolean values to null
		dp.truthValues = dp.dp(dp.clauses, dp.truthValues, dp.literalValues);//run the Davis-Putnam algorithm
		dp.write();//write the results to the output file
	}
	
	//read clauses from the input file
	void read() throws Exception
	{
		System.out.print("Enter path to input file:\n");
		Scanner kbd = new Scanner(System.in);
		String path = kbd.next();
		File file = new File(path);
		Scanner sc = new Scanner(file);
		String line = null;
		outerloop:
		while(sc.hasNextLine())
		{
			line = sc.nextLine();
			String[] str = line.split("\\s");
			if(Integer.parseInt(str[0]) == 0)
			{
				while(sc.hasNextLine())
				{
					backMatter.add(sc.nextLine());
				}
				break outerloop;
			}
			ArrayList<Literal> clause = new ArrayList<Literal>();
			for(int i = 0; i < str.length; i++)
			{
				clause.add(new Literal(Integer.parseInt(str[i])));
				literalValues.add(Math.abs(Integer.parseInt(str[i])));
			}
			clauses.add(clause);
		}
		
		//sort and dedupe a list of each unique numerical literal value
		Collections.sort(literalValues);
		int i = 1;
		int prev = literalValues.get(0);
		while(i < literalValues.size())
		{
			while((literalValues.get(i) == prev))
			{
				literalValues.remove(i);
				if(i == literalValues.size())
				{
					break;
				}
			}
			if(i< literalValues.size())
			{
				prev = literalValues.get(i);
			}
			i++;
		}
	}
	
	//write the truth value results to the output file
	void write() throws Exception
	{
		System.out.print("Enter path to output file:\n");
		Scanner kbd = new Scanner(System.in);
		String path = kbd.next();
		File file = new File(path);
		if(!file.exists())
		{
			file.createNewFile();
		}
		
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i = 0; i < truthValues.length; i++)
		{
			if(truthValues[i] == null)
			{
				bw.write("NO SOLUTION");
				bw.newLine();
				break;
			}
			bw.write((i + 1) + " ");
			if(truthValues[i])
			{
				bw.write("T");
			}
			else
			{
				bw.write("F");
			}
			bw.newLine();
		}
		bw.write("0");
		bw.newLine();
		for(int i = 0; i < backMatter.size(); i++)
		{
			bw.write(backMatter.get(i));
			bw.newLine();
		}
		bw.close();
	}
	
	//the Davis-Putnam algorithm
	Boolean[] dp(ArrayList<ArrayList<Literal>> clauses, Boolean[] truthValues, ArrayList<Integer> literalValues)
	{
		//make deep copies of the parameters
		Boolean[] tempTruthValues = new Boolean[truthValues.length];
		System.arraycopy(truthValues, 0, tempTruthValues, 0, truthValues.length);
		ArrayList<ArrayList<Literal>> tempClauses = new ArrayList<ArrayList<Literal>>();
		for(int i = 0; i < clauses.size(); i++)
		{
			tempClauses.add(new ArrayList<Literal>());
			for(int j = 0; j < clauses.get(i).size(); j++)
			{
				Literal literal = new Literal(clauses.get(i).get(j));
				tempClauses.get(i).add(literal);
			}
		}
		
		//begin with the easy cases
		int easyCase = 1;
		while(easyCase > 0)
		{
			easyCase = 0;
			
			//check if tempClauses is empty
			if(tempClauses.size() == 0)
			{
				for(int i = 0; i < tempTruthValues.length; i++)
				{
					if(tempTruthValues[i] == null)
					{
						tempTruthValues[i] = true;
					}
				}
				return tempTruthValues;
			}
			
			//check for an empty clause
			for(int i = 0; i < tempClauses.size(); i++)
			{
				if(tempClauses.get(i).size() == 0)
				{
					return new Boolean[1];
				}
			}
			
			//check for pure literals
			boolean pure;
			Boolean negative;
			outerloop:
			for(int i = 0; i < literalValues.size(); i++)
			{
				pure = true;
				negative = null;
				int val = literalValues.get(i);
				for(int j = 0; j < tempClauses.size(); j++)
				{
					for(int k = 0; k < tempClauses.get(j).size(); k++)
					{
						Literal literal = new Literal(tempClauses.get(j).get(k));
						if(literal.value == val)
						{
							negative = literal.negative;
							break;
						}
					}
				}
				if(negative != null)
				{
					thisloop:
					for(int j = 0; j < tempClauses.size(); j++)
					{
						for(int k = 0; k < tempClauses.get(j).size(); k++)
						{
							Literal literal = tempClauses.get(j).get(k);
							if(literal.value == val)
							{
								if(literal.negative != negative)
								{
									pure = false;
									break thisloop;
								}
							}
						}
					}
				}
				else
				{
					pure = false;
				}
				if(pure)
				{
					Literal literal = new Literal(literalValues.get(i));
					literal.negative = negative;
					if(negative)
					{
						tempTruthValues[i] = false;
					}
					else
					{
						tempTruthValues[i] = true;
					}
					tempClauses = pureLiteralAssign(literal, tempClauses);
					easyCase++;
					break outerloop;
				}
			}

			//check for unit clauses
			outerloop:
			for(int i = 0; i < tempClauses.size(); i++)
			{
				if(tempClauses.get(i).size() == 1)
				{
					if(tempClauses.get(i).get(0).negative == true)
					{
						tempTruthValues[tempClauses.get(i).get(0).value - 1] = false;
					}
					else
					{
						tempTruthValues[tempClauses.get(i).get(0).value - 1] = true;
					}
					Literal literal = new Literal(tempClauses.get(i).get(0));
					literal.negative = !tempTruthValues[tempClauses.get(i).get(0).value - 1];
					tempClauses = unitPropagate(literal, tempClauses);
					easyCase++;
					break outerloop;
				}
			}
		}		
		
		//begin recursion
		//assign a random literal
		Literal literal = new Literal(chooseLiteral(tempClauses));
		literal.negative = false;
		tempTruthValues[literal.value-1] = true;
		ArrayList<Literal> l = new ArrayList<Literal>();
		l.add(literal);
		tempClauses.add(l);
		Boolean[] tempVals = dp(tempClauses, tempTruthValues, literalValues);
		if(tempVals[0] != null)
		{
			return tempVals;
		}
		tempClauses.remove(tempClauses.size() - 1);
		l.remove(l.size() - 1);
		literal.negative = true;
		tempTruthValues[literal.value-1] = false;
		l.add(literal);
		tempClauses.add(l);
		return dp(tempClauses, tempTruthValues, literalValues);
	}
	
	//chooses a literal to assign a truth value to
	Literal chooseLiteral(ArrayList<ArrayList<Literal>> tempClauses)
	{
		int i;
		outerloop:
		for(i = 0; i < tempClauses.size(); i++)
		{
			if(tempClauses.get(i).size() > 0)
			{
				break outerloop;
			}
		}
		Literal literal = new Literal(tempClauses.get(i).get(0));
		return literal;
	}
	
	//removes the clauses that are satisfied after a pure literal is detected
	ArrayList<ArrayList<Literal>> pureLiteralAssign(Literal literal, ArrayList<ArrayList<Literal>> clauses)
	{
		for(int i = 0; i < clauses.size(); i++)
		{
			thisloop:
			for(int j = 0; j < clauses.get(i).size(); j++)
			{
				if(clauses.get(i).get(j).value == literal.value)
				{
					clauses.remove(i);
					i--;
					break thisloop;
				}
			}
		}
		return clauses;
	}
	
	//propagate when a singleton clause is detected
	ArrayList<ArrayList<Literal>> unitPropagate(Literal literal, ArrayList<ArrayList<Literal>> clauses)
	{
		//update clauses for literal assignment
		//removes a clause if satisfied, removes matching literals with mismatched signs
		for(int i = 0; i < clauses.size(); i++)
		{
			thisloop:
			for(int j = 0; j < clauses.get(i).size(); j++)
			{
				if(clauses.get(i).get(j).value == literal.value)
				{
					if(clauses.get(i).get(j).negative == true)
					{
						if(literal.negative == true)
						{
							clauses.remove(i);
							i--;
							break thisloop;
						}
						else
						{
							clauses.get(i).remove(j);
							j--;
						}
					}
					else if(clauses.get(i).get(j).negative == false)
					{
						if(literal.negative == false)
						{
							clauses.remove(i);
							i--;
							break thisloop;
						}
						else
						{
							clauses.get(i).remove(j);
							j--;
						}
					}
				}
			}
		}
		return clauses;
	}
}

//an object to represent a literal
class Literal
{
	int value;
	boolean negative;
	
	//constructors
	public Literal(int value)
	{
		this.value = Math.abs(value);
		if(value < 0)
		{
			this.negative = true;
		}
		else
		{
			this.negative = false;
		}
	}
	
	public Literal(Literal literal)
	{
		this.value = literal.value;
		this.negative = literal.negative;
	}
}