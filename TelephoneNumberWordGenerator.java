import java.io.*;

class TelephoneNumberWordGenerator {
    public static void main(String args[]) throws IOException {
        FileOutputStream cout;
        PrintStream      toMyFile;
        BufferedReader   br      = new BufferedReader(new InputStreamReader(System.in));
        String           map[][] = {
            { "A", "B", "C" }, { "D", "E", "F" }, { "G", "H", "I" }, { "J", "K", "L" }, 
			{ "M", "N", "O" }, { "P", "R", "S" }, { "T", "U", "V" }, { "W", "X", "Y" }
        };
        int    number = Integer.parseInt(br.readLine());
        String result;
        int    digit[] = new int[7];

        for (int i = 0; number != 0; i++) {
            digit[i] = (number % 10) - 2;
            number   = number / 10;
        }

        try {
            cout     = new FileOutputStream("TeleWord.txt");
            toMyFile = new PrintStream(cout);

            for (int i1 = 0; i1 < 3; i1++) {
                for (int i2 = 0; i2 < 3; i2++) {
                    for (int i3 = 0; i3 < 3; i3++) {
                        for (int i4 = 0; i4 < 3; i4++) {
                            for (int i5 = 0; i5 < 3; i5++) {
                                for (int i6 = 0; i6 < 3; i6++) {
                                    for (int i7 = 0; i7 < 3; i7++) {
                                        result = map[digit[0]][i1];
                                        result = result + map[digit[1]][i2];
                                        result = result + map[digit[2]][i3];
                                        result = result + map[digit[3]][i4];
                                        result = result + map[digit[4]][i5];
                                        result = result + map[digit[5]][i6];
                                        result = result + map[digit[6]][i7];
                                        toMyFile.println(result);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Write successfully");
            toMyFile.close();
        } catch (Exception e) {
            System.err.println("Error in writing to file..!!");
            System.err.println("Verify your input please..!!");
            System.err.println("May be 1 ,0 are in your number..!!");
        }
    }
}