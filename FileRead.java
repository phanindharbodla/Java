import java.io.*;

import java.util.*;

class FileRead {
    public static void main(String args[]) {
        try {
            File           f        = new File("TargetDay2.txt");    // Creating the File passing path to the constructor..!!
            BufferedReader br       = new BufferedReader(new FileReader(f));    //
            String         strLine  = " ";
            String         filedata = "";

            while ((strLine = br.readLine()) != null) {
                filedata += strLine + " ";
            }

            StringTokenizer stk   = new StringTokenizer(filedata);
            Vector<String>  token = new Vector<String>();

            while (stk.hasMoreTokens()) {
                token.add(stk.nextToken());
            }

            Collections.sort(token);
            System.out.println(token);
            br.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}