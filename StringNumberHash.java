import java.io.*;

import java.util.*;

@SuppressWarnings("unchecked")
class StringNumberHash {
    public static void main(String args[]) throws IOException {
        System.out.print("Java test : ");

        BufferedReader  br                    = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer stk                   = new StringTokenizer(br.readLine());
        String          indexStoredAsString[] = {
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
            "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"
        };
        Vector<String> token = new Vector<String>();

        while (stk.hasMoreTokens()) {
            token.add(stk.nextToken());
        }

        String               s1          = (String) token.get(0),
                             s2          = (String) token.get(1);
        Map<String, Integer> intInString = new HashMap<String, Integer>();
        int                  num1, num2;

        for (int i = 0; i < 20; i++) {
            intInString.put(indexStoredAsString[i], (Integer) i);
        }

        num1 = intInString.get(s1);
        num2 = intInString.get(s2);

        try {
            System.out.println(num1 / num2);
        } catch (ArithmeticException e) {    // catch divide-by-zero error
            System.out.println("Division by zero.");
        }
    }
}
