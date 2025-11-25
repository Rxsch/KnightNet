/* Daniel Rangosch 
   Dr. Steinberg
   COP3503 Fall 2025
   Programming Assignment 5
*/

import java.io.*;
import java.util.*;

public class KnightNetDriver 
{
    public static void main(String[] args) throws IOException 
	{
        Random random = new Random(0); //making sure we pick the same node
		int maxVisibility [] = {7, 7, 6, 6, 6};
		String inputs [] = {"knightNet1.txt", "knightNet2.txt", "knightNet3.txt", "knightNet4.txt", "knightNet5.txt",};
		
		//each test case
		for(int testCase = 0; testCase < 5; ++testCase)
		{
			System.out.println("+------------------------ Test Case " + (testCase + 1) + " ------------------------+");
			KnightNet graph = new KnightNet(inputs[testCase], maxVisibility[testCase]);
			
			HashSet<String> realNodes = graph.getRealNodes(); //determine nondecoy nodes
			graph.printGraphRealOnly(maxVisibility[testCase]);// Esto no deberia existir
			if(realNodes.size() > 1)
			{
				String startNode = realNodes.iterator().next();
				
				System.out.println("+++++++++++++ Initial MST +++++++++++++");
				int initialCost = graph.computeMSTCost(startNode, maxVisibility[testCase]);
				
				graph.displayEdges();
				
				System.out.println("Total Cost: " + initialCost);
			}
			else
			{
				System.out.println("No real nodes available for MST computation.");
				System.out.println("+--------------------------------------------------------------------+");
				continue;
			}
			
			ArrayList<String> nodeList = new ArrayList<>(realNodes);
			Collections.sort(nodeList);
			String breachedNode = nodeList.get(random.nextInt(nodeList.size()));
			System.out.println("\n*** ALERT: Node breached: " + breachedNode + " ***");
			graph.removeNode(breachedNode);
			
			HashSet<String> remainingNodes = graph.getRealNodes();
			if (realNodes.size() > 1) 
			{
				String newStart = remainingNodes.iterator().next();
				System.out.println("\n+++++++++++++ Reconnected MST (after breach) +++++++++++++");
				int newCost = graph.computeMSTCost(newStart, maxVisibility[testCase]);
				graph.displayEdges();
				System.out.println("New Total Cost: " + newCost);
			} 
			else 
			{
				System.out.println("Network cannot be reconnected. All real nodes removed.");
			}
			
			System.out.println("+--------------------------------------------------------------------+");
		}
    }
}