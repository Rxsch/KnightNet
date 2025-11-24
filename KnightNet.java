/* Daniel Rangosch 
   Dr. Steinberg
   COP3503 Fall 2025
   Programming Assignment 5
*/
import java.io.*;
import java.util.*;

public class KnightNet {

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
    private HashSet<String> decoyNodes;
    private ArrayList<Edge> mstEdges;
    private int maxVisibility;

    // Reads file and initializes graph and decoy nodes
    public KnightNet(String filename, int maxVisibility) throws IOException {
        this.graph = new HashMap<>();
        this.decoyNodes = new HashSet<>();
        this.mstEdges = new ArrayList<>();
        this.maxVisibility = maxVisibility;

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

            if (isDecoy) {
                decoyNodes.add(nodeA);
                decoyNodes.add(nodeB);
            }

            graph.putIfAbsent(nodeA, new ArrayList<>());
            graph.putIfAbsent(nodeB, new ArrayList<>());
            graph.get(nodeA).add(e);
            graph.get(nodeB).add(e);
        }
        br.close();
    }

    // Returns non-decoy nodes
    public HashSet<String> getRealNodes() {
        HashSet<String> realNodes = new HashSet<>(graph.keySet());
        realNodes.removeAll(decoyNodes);
        return realNodes;
    }

    // Computes MST cost using Prim's algorithm
    public int computeMSTCost(String startNode, int maxVisibility) {
        mstEdges.clear();

        HashSet<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>();

        visited.add(startNode);
        for (Edge e : graph.getOrDefault(startNode, new ArrayList<>())) {
            if (!e.isDecoy && e.visibility <= maxVisibility)
                pq.add(e);
        }

        int totalCost = 0;

        while (!pq.isEmpty()) {
            Edge e = pq.poll();

            String nextNode = null;
            if (visited.contains(e.from) && !visited.contains(e.to)) {
                nextNode = e.to;
            } else if (visited.contains(e.to) && !visited.contains(e.from)) {
                nextNode = e.from;
            } else {
                continue; // skip if both visited or both unvisited
            }

            visited.add(nextNode);
            mstEdges.add(e);
            totalCost += e.cost;

            for (Edge edge : graph.getOrDefault(nextNode, new ArrayList<>())) {
                if (!visited.contains(edge.from) || !visited.contains(edge.to)) {
                    if (!edge.isDecoy && edge.visibility <= maxVisibility)
                        pq.add(edge);
                }
            }
        }

        // check if MST spans all real nodes
        if (visited.size() < getRealNodes().size()) {
            return -1; // MST not possible
        }

        return totalCost;
    }

    // Displays MST edges sorted properly
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
            System.out.println(node1 + " " + node2 + " Cost: " + e.cost + " | Visibility: " + e.visibility);
        }
    }

    // Removes node and all connected edges
    public void removeNode(String node) {
        graph.remove(node);
        decoyNodes.remove(node);

        for (String key : graph.keySet()) {
            graph.get(key).removeIf(e -> e.from.equals(node) || e.to.equals(node));
        }
    }
}
