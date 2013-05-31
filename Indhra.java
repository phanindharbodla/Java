import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
class Vertex
{
    Map<Integer,Integer> edge ;
    Vertex() 
    {
    this.edge = new TreeMap<Integer,Integer>();   
    }
}
public class Indhra {
    private static boolean trace [] = new boolean [100] ;
    /*private static void algo(Vertex[] city, Integer source, Integer destination) {
        Vertex current=city[source];
        trace[source]=true;
        if(!current.edge.isEmpty())
        {
            for(MapIterator i=current.edge.;;);
        }
    }*/
    //
	public static void main(String[] args) throws IOException {
       BufferedReader br= new BufferedReader(new InputStreamReader(System.in));
       String str=(br.readLine());
       StringTokenizer stk=new StringTokenizer(str);
       int nodes=Integer.parseInt(stk.nextToken(str));
       int edges=Integer.parseInt(stk.nextToken(str));
       
       Vertex [] city =new Vertex[nodes+1];
       Integer from,to,weight,source,destination,input;
       for(int i=1;i<nodes+1;i++)
       {
           city[i]=new Vertex();
       }
       for(int i=0;i<nodes;i++)
       {
           str=(br.readLine());
           stk=new StringTokenizer(str);
           from=Integer.parseInt(stk.nextToken(str));
           to=Integer.parseInt(stk.nextToken(str));
           weight=Integer.parseInt(stk.nextToken(str))-1;
           city[from].edge.put(to, weight);
           city[to].edge.put(from, weight);
       }
       str=(br.readLine());
       stk=new StringTokenizer(str);
       source=Integer.parseInt(stk.nextToken(str));
       destination=Integer.parseInt(stk.nextToken(str));
       input=Integer.parseInt(stk.nextToken(str));
       algo(city , source,destination);
    }
}