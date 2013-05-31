package codejam.SmartTravelAgent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class SmartTravelAgent 
{
	private static double noOfPessengers = 0;
	private static int stPoint = 0;
	private static int endPoint = 0;
	private static int noOfCities = 0;
	private static int noOfRoutes = 0;
	private static String inputFileName = "";
	private static String outputFileName = "";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		inputFileName = args[0];
		outputFileName = args[1];
		HashMap<Integer,HashMap<Integer,Integer>> routeMap = new HashMap<Integer, HashMap<Integer,Integer>>();
		routeMap = readInput(inputFileName);
		List<List<Integer>> allPossibleRoutes = findAllPossibleRoutes(routeMap,stPoint,endPoint);
		
		HashMap<Integer, List<Integer>> capRouteMap  = findMaxPersonPerTrip(allPossibleRoutes,routeMap);
		double noOfTrips = findNoOfTrips(capRouteMap);
		
		writeOutput(noOfTrips,capRouteMap,outputFileName);
		
	}
	
	private static HashMap<Integer,HashMap<Integer,Integer>>  readInput(String fileName)
	{
		 DataInputStream in=null;
		 HashMap<Integer,HashMap<Integer,Integer>> routeMap =  null;
		try 
		{
			routeMap = new HashMap<Integer, HashMap<Integer,Integer>>();
			FileInputStream fstream = new FileInputStream("Input\\" + fileName);

			  in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new   InputStreamReader(in));
			  String strLine;
			  int i =0;

			  while ((strLine = br.readLine()) != null)
			  {
				  String[] firstLine = strLine.split(" ");
				  if(i==0)
				  {
					  noOfCities = Integer.parseInt(firstLine[0]);
					  noOfRoutes = Integer.parseInt(firstLine[1]);
				  }
				  else if(i==noOfRoutes+1)
				  {
					  stPoint =  Integer.parseInt(firstLine[0]);
					  endPoint =  Integer.parseInt(firstLine[1]);
					  noOfPessengers =  Integer.parseInt(firstLine[2]);
				  }
				  else
				  {
					  HashMap<Integer,Integer> capMap = new HashMap<Integer, Integer>(); 
					  int srcCity = Integer.parseInt(firstLine[0]);
					  int destinationCity = Integer.parseInt(firstLine[1]);
					  int capacity = Integer.parseInt(firstLine[2]);
					  Iterator iter = routeMap.keySet().iterator();
					  while(iter.hasNext())
					  {
						  int city = (Integer)iter.next();
						  if(routeMap.get(city).containsKey(srcCity))
						  {
							  capMap.put(city, (routeMap.get(city).get(srcCity)));
						  }
					  }
					  if(routeMap.containsKey(srcCity))
					  {
						  HashMap<Integer,Integer> innerMap = routeMap.get(srcCity);
						  innerMap.put(destinationCity, capacity);
					  }
					  else
					  {
						  capMap.put(destinationCity, capacity);
						  routeMap.put(srcCity, capMap);
					  }
				  }
				  
				  i++;
			  }
		}
		catch (FileNotFoundException e) 
		{
			writeError("Input File not found");
		}
		catch (IOException e) 
		{
			writeError("Exception caught while reading the file");
		}
		finally
		{
			try
			{
				in.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return routeMap;
		
	}
	private static void writeOutput(double noOfTrips,HashMap<Integer, List<Integer>> capRouteMap,String outputFileName) 
	{
		BufferedWriter output  = null;
		try 
		{
			FileWriter fstream = new FileWriter("Output\\" + outputFileName);
		    output = new BufferedWriter(fstream);
			output.write(String.valueOf((int)noOfTrips));
			Iterator iter = capRouteMap.keySet().iterator();
			int cap = 0;
			while(iter.hasNext())
			{
				cap = (Integer)iter.next();
			}
			List<Integer> route = new ArrayList<Integer>();
			route = capRouteMap.get(cap);
			StringBuffer routeStr = new StringBuffer();
			for (int i = 0; i < route.size(); i++) 
			{
				routeStr.append(route.get(i));
			}
			output.newLine();
			output.write(routeStr.toString());
		}
		catch (FileNotFoundException e) 
		{
			writeError("Output file not found");
		} 
		catch (IOException e)
		{
			writeError("Exception caught in writeOutput method");
		}		
		finally
		{
			try
			{
				output.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param errorMsg
	 */
	private static void writeError(String errorMsg) 
	{
		BufferedWriter out  = null;
		try 
		{
			FileWriter fstream = new FileWriter("Output\\" + outputFileName);
			out = new BufferedWriter(fstream);
			out.write(errorMsg);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			   try 
			   {
				   if(out!=null)
				   {
					   out.close();
				   }
			   } 
			   catch (Exception e)
			   {
				e.printStackTrace();
			   }
		}
	}
	private static double findNoOfTrips(HashMap<Integer, List<Integer>> capRouteMap) 
	{
		double noOfTrips=0;
		try 
		{
			Iterator iter = capRouteMap.keySet().iterator();
			noOfTrips = 0;
			double maxCapacity = 0;
			while(iter.hasNext())
			{
				maxCapacity = (Integer)iter.next();
				noOfTrips = noOfPessengers/maxCapacity;
				noOfTrips = Math.ceil(noOfTrips);
			}
			if((noOfTrips*maxCapacity)<(noOfPessengers + noOfTrips))
			{
				noOfTrips += Math.ceil(((noOfPessengers + noOfTrips)-(noOfTrips*maxCapacity))/maxCapacity);
			}
		} 
		catch (Exception e) 
		{
			writeError("Exception caught in findNoOfTrips method");
		}
		return noOfTrips;
	}
	private static HashMap<Integer, List<Integer>> findMaxPersonPerTrip(List<List<Integer>> allPossibleRoutes, HashMap<Integer, HashMap<Integer, Integer>> map) 
	{
		int noOfRoutes = allPossibleRoutes.size();
		int maxPerson = 0;
		HashMap<Integer,List<Integer>> capRouteMap = null;
		try
		{
			for (int index = 0; index < noOfRoutes; index++) 
			{
				int maxPersonForThisRoute=0;
				List<Integer> route  = allPossibleRoutes.get(index);
				maxPersonForThisRoute = findMaxCapacity(route,map);
				if(maxPersonForThisRoute > maxPerson)
				{
					maxPerson = maxPersonForThisRoute;
					capRouteMap = new HashMap<Integer, List<Integer>>();
					capRouteMap.put(maxPerson, route);
				}
			}
		}
		catch (Exception e)
		{
			writeError("Exception caught in findMaxPersonPerTrip method");
		}
		return capRouteMap;
	}
	private static int findMaxCapacity(List<Integer> route,HashMap<Integer, HashMap<Integer, Integer>> map) 
	{
		
		int max=0;
		try 
		{
			int routeLen = route.size();
			max = 99999;
			for (int i = 0; i < routeLen-1; i++)
			{
				int capacity = map.get(route.get(i)).get(route.get(i+1));
				if(capacity < max)
				{
					max = capacity;
				}
			}
		}
		catch (Exception e) 
		{
			writeError("Exception caught in findMaxCapacity method");
		}
		return max;
	}
	private static List<List<Integer>> findAllPossibleRoutes(HashMap<Integer, HashMap<Integer, Integer>> map, int stPoint1,int endPoint1) 
	{
			List<List<Integer>>possibleRoutes = new ArrayList<List<Integer>>();
			HashMap<Integer,Integer> innerMap = new HashMap<Integer, Integer>();
			try 
			{
				innerMap = map.get(stPoint1);
				Iterator iter = innerMap.keySet().iterator();
				while(iter.hasNext())
				{
					List<Integer> visitedCities = new ArrayList<Integer>();
					List<List<Integer>> allRoutes = new ArrayList<List<Integer>>();
					visitedCities.add(stPoint1);
					Integer cityReached  = (Integer)iter.next();
					visitedCities.add(cityReached);
					allRoutes.addAll(findRoute(map,cityReached,endPoint1,visitedCities));
					possibleRoutes.addAll(allRoutes);
				}
			} 
			catch (Exception e)
			{
				writeError("Exception caught in findAllPossibleRoutes method");
			}
			return possibleRoutes;
	}
	private static List<List<Integer>> findRoute(HashMap<Integer, HashMap<Integer, Integer>> map, int stPoint2,int endPoint2,List<Integer> visitedCities) 
	{
		HashMap<Integer,Integer> innerMap = new HashMap<Integer, Integer>();
		innerMap = map.get(stPoint2);
		List<Integer> route = new ArrayList<Integer>();
		List<List<Integer>> totalRoutes = new ArrayList<List<Integer>>();
		try 
		{
			Iterator iter = innerMap.keySet().iterator();
			
			while(iter.hasNext())
			{
				List<Integer> coveredCities = new ArrayList<Integer>();
				coveredCities.addAll(visitedCities);
				int cityReached = (Integer)iter.next();
				if(visitedCities.contains(cityReached) || cityReached==stPoint2)
				{
					continue;
				}
				if(cityReached==endPoint)
				{
					List<Integer> newList = new ArrayList<Integer> ();
					newList.addAll(visitedCities);
					newList.add(cityReached);
					totalRoutes.add(newList);
					continue;
				}
				else
				{
					coveredCities.add(cityReached);
					totalRoutes.addAll(findRoute(map, cityReached, endPoint,coveredCities));
				}
			}
		} 
		catch (Exception e) 
		{
			writeError("Exception caught in findRoute method");
		}
			return totalRoutes;
	}
}
