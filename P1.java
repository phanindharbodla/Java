import java.io.*;
class P1
{
		public static boolean valid(long n)
		{
			int m=0;
			for(int i=0;n!=0;i++)
			{
				if(m>(m^=(1<<(n%10))))
				return false;
				else
				n/=10;
			}
			return true;
		}
		public static void main(String args[]) throws IOException
		{
		final long max=9876543210L;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		long input=Integer.parseInt(br.readLine());
		for(long i=1;i<=max;i++)
		{
				if(valid(i))
				if(valid(i*input))
				System.out.println((i*input)+"/"+i);
		}
		}		
}