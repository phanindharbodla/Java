import java.util.HashMap;
public abstract class Records {
	private static int uniqueId=0;// Only to ensure that unique id is for any newly created object is same..!!
	public String name="";
	int prductId;
	private static int generateUniqueId() // all we need is any id for ANY derived class..!!
	{
		return ++uniqueId;
	}
	public void getDeails(int productId);
	public void Records(String str)
	{
	this.productId=generateUniqueId();
	this.name=str;
	}
}
class Book extends Records{
	String author ;
	Book(String name,String aut);
	{
	this.author=aut;
	super(name);
	}
	public void getDetails(int id)
	{
		System.out.println("Name :"+this.name+"  Author :"+this.author);
	}
}
class MyGoods
{
	public static void main(String args[])
	{
	//Map<Integer,Book   >  BookHash	 = new HashMap<Integer, Book   >();
	
	}
}
