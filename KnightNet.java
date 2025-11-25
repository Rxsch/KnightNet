/* Daniel Rangosch
   Dr. Steinberg
   COP3503 Fall 2025
   Programming Assignment 5
*/

import java.io.*;
import java.util.*;

public class KnightNet {

	// Stores connection details between two nodes
	private class Edge implements Comparable<Edge> {
		String from;
		String to;
		int cost;
		int visibility;
		boolean isDecoy;

		// Saves link data coming from input file
		Edge(String from, String to, int cost, int visibility, boolean isDecoy) {
			this.from = from;
			this.to = to;
			this.cost = cost;
			this.visibility = visibility;
			this.isDecoy = isDecoy;
		}

		// Sort edges by cost
		public int compareTo(Edge other) {
			return Integer.compare(this.cost, other.cost);
		}
	}

	private HashMap<String, ArrayList<Edge>> graph; // adjacency list per node
	private HashSet<String> decoyNodes; // tracks decoy nodes
	private ArrayList<Edge> mstEdges; // stores MST chosen edges

	// Reads file and builds graph structure
	public KnightNet(String filename, int maxVisibility) throws IOException {
		graph = new HashMap<>();
		decoyNodes = new HashSet<>();
		mstEdges = new ArrayList<>();

		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			// Split each CSV line into parts
			String[] parts = line.split(",");
			String nodeA = parts[0].trim();
			String nodeB = parts[1].trim();
			int cost = Integer.parseInt(parts[2].trim());
			int visibility = Integer.parseInt(parts[3].trim());
			boolean isDecoy = Boolean.parseBoolean(parts[4].trim());

			// Create one shared edge object
			Edge e = new Edge(nodeA, nodeB, cost, visibility, isDecoy);

			// Mark nodes as decoy if needed
			if (isDecoy) {
				decoyNodes.add(nodeA);
				decoyNodes.add(nodeB);
			}

			// Ensure nodes exist in adjacency list
			graph.putIfAbsent(nodeA, new ArrayList<>());
			graph.putIfAbsent(nodeB, new ArrayList<>());

			// Add edge to both endpoints
			graph.get(nodeA).add(e);
			graph.get(nodeB).add(e);
		}
		br.close();
	}

	// Returns nodes not marked as decoys
	public HashSet<String> getRealNodes() {
		HashSet<String> realNodes = new HashSet<>(graph.keySet());
		realNodes.removeAll(decoyNodes);
		return realNodes;
	}

	// Prints allowed edges and calculates total cost
	// Uses visibility + decoy filtering
	public int computeMSTCost(String startNode, int maxVisibility) {
		HashSet<String> realNodes = getRealNodes();
		HashSet<String> printedEdges = new HashSet<>();

		int total = 0;

		// Loop through only real node neighbors
		for (String node : realNodes) {
			for (Edge e : graph.getOrDefault(node, new ArrayList<>())) {

				// Skip edges with fake nodes
				if (e.isDecoy) continue;

				// Enforce max visibility limit
				if (e.visibility > maxVisibility) continue;

				// Get the opposite endpoint
				String other = e.from.equals(node) ? e.to : e.from;

				// Skip if neighbor is fake
				if (!realNodes.contains(other)) continue;

				// Prevent edge duplicate prints
				String key = node.compareTo(other) < 0 ?
				             node + "-" + other : other + "-" + node;
				if (printedEdges.contains(key)) continue;
				printedEdges.add(key);

				// Show valid communication edge
				System.out.println(node + " - " + other +
				                   " | Cost: " + e.cost +
				                   " | Visibility: " + e.visibility);

				total += e.cost;
			}
		}

		System.out.println();
		return total;
	}

	// Shows stored MST edges alphabetically
	public void displayEdges() {
		List<Edge> sortedEdges = new ArrayList<>(mstEdges);

		// Sort by node names to standardize printing
		sortedEdges.sort((a, b) -> {
			String srcA = a.from.compareTo(a.to) < 0 ? a.from : a.to;
			String dstA = a.from.compareTo(a.to) < 0 ? a.to : a.from;
			String srcB = b.from.compareTo(b.to) < 0 ? b.from : b.to;
			String dstB = b.from.compareTo(b.to) < 0 ? b.to : b.from;

			int cmp = srcA.compareTo(srcB);
			if (cmp != 0) return cmp;
			return dstA.compareTo(dstB);
		});

		// Print formatted result
		for (Edge e : sortedEdges) {
			String node1 = e.from.compareTo(e.to) < 0 ? e.from : e.to;
			String node2 = e.from.compareTo(e.to) < 0 ? e.to : e.from;
			System.out.println(node1 + " - " + node2 +
			                   " | Cost: " + e.cost +
			                   " | Visibility: " + e.visibility);
		}
	}

	// Removes a node and all connected edges
	public void removeNode(String node) {
		graph.remove(node);       // Remove node key entirely
		decoyNodes.remove(node);  // Keep decoy list consistent

		// Delete any edge pointing to removed node
		for (String key : graph.keySet()) {
			graph.get(key).removeIf(e -> e.from.equals(node) || e.to.equals(node));
		}
	}
}