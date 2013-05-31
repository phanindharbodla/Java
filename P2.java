import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

class P2 {
    public static void main(String args[]) throws IOException {
        final int max   = 100;
        int       sum[] = new int[max + 1];

        for (int i = 1; i <= max; i++) {
            for (int j = 2; (j * i) <= max; j++) {
                sum[j * i] += i;
            }
        }

        BufferedReader br    = new BufferedReader(new InputStreamReader(System.in));
        int            input = Integer.parseInt(br.readLine());

        while (input != 0) {
            if (input == sum[input]) {
                System.out.format("%-3d  %3d   Perfect%n", input, sum[input]);
            } else if (input < sum[input]) {
                System.out.format("%-3d  %3d   Abundant%n", input, sum[input]);
            } else {
                System.out.format("%-3d  %3d   Deficient%n", input, sum[input]);
            }

            input = Integer.parseInt(br.readLine());
        }
    }
}
