//~--- JDK imports ------------------------------------------------------------

import java.io.*;

class P5 {
    public static void main(String args[]) throws IOException {
        BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
        double         a[] = new double[8];
        double         input;

        for (int i = 0; i < 8; i++) {
            a[i] = Double.parseDouble(br.readLine());
        }

        java.util.Arrays.sort(a);
        System.out.format("Second Minimum number is %-10.3f  %n", a[1]);
        System.out.format("Second Maximum number is %-10.3f  %n", a[6]);
        System.out.format("%n ");
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
