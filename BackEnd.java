import java.io.*;
import java.util.*;

public class BackEnd {
	
	//store the truth values so we can translate them into propositional atoms
	ArrayList<String[]> truthValues = new ArrayList<String[]>();
	ArrayList<String[]> atomValues = new ArrayList<String[]>();

	public static void main(String[] args) throws Exception
	{
		BackEnd backend = new BackEnd();
		backend.read();
		backend.write();
	}
	
	//read from the input file
	void read() throws Exception
	{
		System.out.print("Enter path to input file:\n");
		Scanner kbd = new Scanner(System.in);
		String path = kbd.next();
		File file = new File(path);
		Scanner sc = new Scanner(file);
		String line = null;
		while(sc.hasNextLine())
		{
			while(!(line = sc.nextLine()).equals("0"))
			{
				String[] str = line.split("\\s");
				truthValues.add(str);
			}
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] str = line.split("\\s");
				atomValues.add(str);
			}
		}
	}
	
	//writes the correct path to the output file
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
		int index;
		for(int i = 0; i < truthValues.size(); i++)
		{
			if(truthValues.size() == 1)
			{
				bw.write(truthValues.get(0)[0] + " ");
				bw.write(truthValues.get(0)[1]);
				bw.newLine();
			}
		    else if(truthValues.get(i)[1].equals("T"))
			{
				index = Integer.parseInt(truthValues.get(i)[0]) - 1;
				bw.write(atomValues.get(index)[1]);
				bw.newLine();
			}
		}
		bw.close();
	}
}
