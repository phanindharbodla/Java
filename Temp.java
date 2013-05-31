//~--- JDK imports ------------------------------------------------------------

import java.sql.*;

class Temp {
    public static void main(String args[]) {
        System.out.println("1: Hello World now..!!");
        System.out.println("Welcome to the Evaluation..!!");
        System.out.println("2:Time to swap ..!! (No methods can be used for Primitive Data types..:) )");

        int a = 10;
        int b = 12;

        System.out.println(a + " " + b);
        a = a ^ b;
        b = a ^ b;
        a = a ^ b;
        System.out.println(a + " " + b);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
