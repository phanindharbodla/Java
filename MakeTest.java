import java.io.*;

import java.util.*;
class MakeTest {
    public static void main(String args[]) {
        try {
            File           f        = new File("temp.lp");//Give your lp file name here in " "
            BufferedReader br       = new BufferedReader(new FileReader(f));
            String         strLine  = " ";
			BufferedWriter out = new BufferedWriter(new FileWriter("test.lp"));//the test.lp is your output file
            while ((strLine = br.readLine()) != null) {
				out.write(strLine);
				out.newLine();
				}
			out.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}