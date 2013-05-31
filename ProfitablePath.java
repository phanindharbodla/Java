
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
/**
 *
 * @author j1013563
 */
public class ProfitablePath {
   private static boolean visitedVertex [] = new boolean[100];
   private static Integer [][] cost;
   private static Queue theQueue;

    private static long adjcentNonvisitedCity(long v1, Integer[][] cost1, Integer total) {
        for(int j=1;j<=total;j++)
                {
                    if(cost[(int)v1][j]!=0) {
                        if(!visitedVertex[j]) {
                            return j;
                        }
                    }
                }
        return 0;
    }
   class Queue
   {
   private int maxSize;
   private long[] queArray;
   private int front;
   private int rear;
   private int nItems;
   public Queue(int s)          // constructor
      {
      maxSize = s;
      queArray = new long[maxSize];
      front = 0;
      rear = -1;
      nItems = 0;
      }
   public void insert(long j)   // put item at rear of queue
      {
      if(rear == maxSize-1) {
              rear = -1;
          }
      queArray[++rear] = j;         // increment rear and insert
      nItems++;                     // one more item
      }
   public long remove()         // take item from front of queue
      {
      long temp = queArray[front++]; // get value and incr front
      if(front == maxSize) {
              front = 0;
          }
      nItems--;                      // one less item
      return temp;
      }
   public long peekFront()      // peek at front of queue
      {
      return queArray[front];
      }
   public boolean isEmpty()    // true if queue is empty
      {
      return (nItems==0);
      }
   public boolean isFull()     // true if queue is full
      {
      return (nItems==maxSize);
      }
   public int size()           // number of items in queue
      {
      return nItems;
      }
   }
     private static Integer bfs(Integer[][] cost, Integer source, Integer destination, Integer total) 
    {
        visitedVertex[source]= true;
        System.out.println(source);
        long  v1,v2;
        for(int i=1;i<=total;i++)
        {
            theQueue.insert(source);
            while(!theQueue.isEmpty())
            {
                v1=theQueue.remove();
                while((v2=adjcentNonvisitedCity(v1,cost,total))!=0)
                {
                    visitedVertex[(int)v2] = true;
                    System.out.println(v2);
                    theQueue.insert(v2);
                }
                
            }
        }
        return 0 ;
    }
    @SuppressWarnings("empty-statement")
    public static void main(String args[]) throws IOException
    {
        Integer cities,weights,from,to,limit,optimalCost,source,destination,target;
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String input=console.readLine();
        System.out.println(input);
        StringTokenizer stk=new StringTokenizer(input);
        cities=Integer.parseInt(stk.nextToken(input));//Integer.parseInt(stk.nextToken(input));
        weights=Integer.parseInt(stk.nextToken(input));
        Integer cost[][]=new Integer[cities+1][cities+1];
        for(int i=0;i<weights;i++)
        {
            input=console.readLine();
            stk=new StringTokenizer(input);
            from=Integer.parseInt(stk.nextToken(input));
            to=Integer.parseInt(stk.nextToken(input));
            limit=Integer.parseInt(stk.nextToken(input))-1;
            cost[from][to]=limit;
            cost[to][from]=limit;
        }
        input=console.readLine();
        stk=new StringTokenizer(input);
        source=Integer.parseInt(stk.nextToken(input));
        destination=Integer.parseInt(stk.nextToken(input));
        target=Integer.parseInt(stk.nextToken(input));
        optimalCost=bfs(cost,source,destination,cities);
    }    
}