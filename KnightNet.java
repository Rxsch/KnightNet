/* Daniel Rangosch
   Dr. Steinberg
   COP3503 Fall 2025
   Programming Assignment 5
*/

import java.io.*;
import java.util.*;

public class KnightNet {

    // Represents a graph edge
    private class Edge implements Comparable<Edge> {
        String from;
        String to;
        int cost;
        int visibility;
        boolean isDecoy;

        Edge(String from, String to, int cost, int visibility, boolean isDecoy) {
            this.from = from;
            this.to = to;
            this.cost = cost;
            this.visibility = visibility;
            this.isDecoy = isDecoy;
        }

        public int compareTo(Edge other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    private HashMap<String, ArrayList<Edge>> graph; // adjacency list
    private HashSet<String> decoyNodes;             // set of decoy nodes
    private ArrayList<Edge> mstEdges;               // MST edges list

    // Constructor: read file and build graph (include decoys in graph)
public KnightNet(String filename, int maxVisibility) throws IOException {
    graph = new HashMap<>();
    decoyNodes = new HashSet<>();
    mstEdges = new ArrayList<>();

    BufferedReader br = new BufferedReader(new FileReader(filename));
    String line;
    while ((line = br.readLine()) != null) {
        String[] parts = line.split(",");
        String nodeA = parts[0].trim();
        String nodeB = parts[1].trim();
        int cost = Integer.parseInt(parts[2].trim());
        int visibility = Integer.parseInt(parts[3].trim());
        boolean isDecoy = Boolean.parseBoolean(parts[4].trim());

        Edge e = new Edge(nodeA, nodeB, cost, visibility, isDecoy);

        // Mark nodes as decoy if edge is decoy
        if (isDecoy) {
            decoyNodes.add(nodeA);
            decoyNodes.add(nodeB);
        }

        // Always add edge to adjacency list (even if it’s a decoy)
        graph.putIfAbsent(nodeA, new ArrayList<>());
        graph.putIfAbsent(nodeB, new ArrayList<>());
        graph.get(nodeA).add(e);
        graph.get(nodeB).add(e);
    }
    br.close();
}

// Print the entire graph including decoy edges
public void printGraph() {
    System.out.println("Graph (including decoys):");
    for (String node : graph.keySet()) {
        System.out.print(node + " -> ");
        ArrayList<Edge> edges = graph.get(node);
        ArrayList<String> edgeDescriptions = new ArrayList<>();
        for (Edge e : edges) {
            // Determine the other node in the edge
            String other = e.from.equals(node) ? e.to : e.from;
            String desc = other + " (Cost: " + e.cost + ", Vis: " + e.visibility;
            if (e.isDecoy) desc += ", DECOY";
            desc += ")";
            edgeDescriptions.add(desc);
        }
        System.out.println(edgeDescriptions);
    }
    System.out.println();
}

    // Return non-decoy nodes
    public HashSet<String> getRealNodes() {
        //Use the normal from the previous comment
        HashSet<String> realNodes = new HashSet<>(graph.keySet());
        realNodes.removeAll(decoyNodes);
        return realNodes;
    }

    // Compute MST using Prim's algorithm
    public int computeMSTCost(String startNode, int maxVisibility) {
        mstEdges.clear();

        HashSet<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        HashSet<String> realNodes = getRealNodes();

        if (!realNodes.contains(startNode)) return -1;

        visited.add(startNode);

        // Add edges connected to startNode
        for (Edge e : graph.getOrDefault(startNode, new ArrayList<>())) {
            if (!e.isDecoy && e.visibility <= maxVisibility &&
                realNodes.contains(e.from) && realNodes.contains(e.to)) {
                pq.add(e);
            }
        }

        int totalCost = 0;

        while (!pq.isEmpty()) {
            Edge e = pq.poll();

            String nextNode = null;
            boolean fromVisited = visited.contains(e.from);
            boolean toVisited = visited.contains(e.to);

            // Must have exactly one visited endpoint
            if (fromVisited && !toVisited) nextNode = e.to;
            else if (!fromVisited && toVisited) nextNode = e.from;
            else continue;

            visited.add(nextNode);
            mstEdges.add(e);
            totalCost += e.cost;

            // Add edges of newly visited node
            for (Edge edge : graph.getOrDefault(nextNode, new ArrayList<>())) {
                if (!visited.contains(edge.from) || !visited.contains(edge.to)) {
                    if (!edge.isDecoy && edge.visibility <= maxVisibility &&
                        realNodes.contains(edge.from) && realNodes.contains(edge.to)) {
                        pq.add(edge);
                    }
                }
            }
        }

        // Check if MST spans all real nodes
        if (visited.size() < realNodes.size()) return -1;

        return totalCost;
    }

    // Display MST edges in required format
    public void displayEdges() {
        List<Edge> sortedEdges = new ArrayList<>(mstEdges);

        sortedEdges.sort((a, b) -> {
            String srcA = a.from.compareTo(a.to) < 0 ? a.from : a.to;
            String dstA = a.from.compareTo(a.to) < 0 ? a.to : a.from;
            String srcB = b.from.compareTo(b.to) < 0 ? b.from : b.to;
            String dstB = b.from.compareTo(b.to) < 0 ? b.to : b.from;

            int cmp = srcA.compareTo(srcB);
            if (cmp != 0) return cmp;
            return dstA.compareTo(dstB);
        });

        for (Edge e : sortedEdges) {
            String node1 = e.from.compareTo(e.to) < 0 ? e.from : e.to;
            String node2 = e.from.compareTo(e.to) < 0 ? e.to : e.from;
            System.out.println(node1 + " - " + node2 + " | Cost: " + e.cost + " | Visibility: " + e.visibility);
        }
    }

    // Remove node and all edges connected to it
    public void removeNode(String node) {
        graph.remove(node);
        decoyNodes.remove(node);

        for (String key : graph.keySet()) {
            graph.get(key).removeIf(e -> e.from.equals(node) || e.to.equals(node));
        }
    }
}
