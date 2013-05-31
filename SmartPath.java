//~--- JDK imports ------------------------------------------------------------

import java.io.*;

import java.util.*;

class SmartPath {
	public static int min(int a,int b)
		{
		if(a<b)return a;
		else return b;
		}
    public static void main(String args[]) throws IOException {
        BufferedReader  br          = new BufferedReader(new InputStreamReader(System.in));
        String          str         = (br.readLine());
        StringTokenizer stk         = new StringTokenizer(str);
        int             totalCities = Integer.parseInt(stk.nextToken());
        int             limits      = Integer.parseInt(stk.nextToken());
        int             from, to, weight,
                        totalTrips  = 0;
        int             limit[][]   = new int[totalCities][totalCities];
        int             visited[]   = new int[totalCities];
		Vector<Vector<int>>  limit1 = new Vector<Vector<int>>();
        for (int s = 0; s < limits; s++) {
            stk             = new StringTokenizer(br.readLine());
            from            = (Integer.parseInt(stk.nextToken())) - 1;
            to              = (Integer.parseInt(stk.nextToken())) - 1;
            weight          = (Integer.parseInt(stk.nextToken())) - 1;
            limit[from][to] = weight;
            limit[to][from] = weight;
            stk             = null;
        }
		 
        for (int i1 = 0; i1 <totalCities ; i1++, System.out.println()) {
            for (int i2 = 0; i2 < totalCities; i2++) {
                System.out.print(limit[i1][i2] + " ");
            }
        }

        stk    = new StringTokenizer(br.readLine());
        from   = (Integer.parseInt(stk.nextToken())) - 1;
        to     = (Integer.parseInt(stk.nextToken())) - 1;
        weight = (Integer.parseInt(stk.nextToken()));
		
		for (int i = from; i < totalCities; i++)
		for (int j = 0; j < totalCities; j++) 
		{
		
		}

        System.out.println(totalTrips);
    }
}
