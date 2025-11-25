/* Daniel Rangosch
   Dr. Steinberg
   COP3503 Fall 2025
   Programming Assignment 5
*/

import java.io.*;
import java.util.*;

public class KnightNet {

    // Clase interna para representar aristas
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

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    private HashMap<String, ArrayList<Edge>> graph; 
    private HashSet<String> decoyNodes;
    private ArrayList<Edge> mstEdges;

    // Constructor: carga archivo y construye grafo
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

    // Retorna nodos reales
    public HashSet<String> getRealNodes() {
        HashSet<String> realNodes = new HashSet<>(graph.keySet());
        realNodes.removeAll(decoyNodes);
        return realNodes;
    }

   // Calcula MST usando Prim: solo nodos reales + aristas válidas
public int computeMSTCost(String startNode, int maxVisibility) {

    HashSet<String> realNodes = getRealNodes();

    if (!realNodes.contains(startNode)) {
        return -1;
    }

    mstEdges.clear();
    int totalCost = 0;

    HashSet<String> visited = new HashSet<>();
    PriorityQueue<Edge> pq = new PriorityQueue<>();

    visited.add(startNode);

    // Paso 1: Inicializa la cola de prioridad con las aristas del nodo de inicio
    for (Edge e : graph.getOrDefault(startNode, new ArrayList<>())) {
        // Comprueba validez ANTES de agregar
        if (!e.isDecoy && e.visibility <= maxVisibility) {

            String neighbor = visited.contains(e.from) ? e.to : e.from;

            // Solo añade si el vecino es un nodo real
            if (realNodes.contains(neighbor)) {
                pq.add(e);
            }
        }
    }

    while (!pq.isEmpty() && visited.size() < realNodes.size()) {
        Edge e = pq.poll();

        // Determinar el nodo al que se dirige y comprobar si ya ha sido visitado
        String next = visited.contains(e.from) ? e.to : e.from;

        // Se espera que la arista sea válida y el nodo sea real gracias al filtrado al agregar
        if (e.isDecoy || e.visibility > maxVisibility || !realNodes.contains(next)) {
            continue;
        }

        if (visited.contains(next)) {
            continue; // Si ya se visitó, salta esta arista
        }

        // ¡Esta es la arista que se añade al MST!
        mstEdges.add(e);
        totalCost += e.cost;
        visited.add(next);

        // Paso 2: Agrega las nuevas aristas del nodo 'next'
        for (Edge nextEdge : graph.getOrDefault(next, new ArrayList<>())) {
            if (nextEdge.isDecoy || nextEdge.visibility > maxVisibility) {
                continue;
            }

            String neighbor = visited.contains(nextEdge.from) ? nextEdge.to : nextEdge.from;

            // Solo añade si el vecino es un nodo real Y NO VISITADO
            if (realNodes.contains(neighbor) && !visited.contains(neighbor)) {
                pq.add(nextEdge);
            }
        }
    }

    // Comprueba si todos los nodos reales fueron visitados
    if (visited.size() != realNodes.size()) {
        return -1;
    }

    return totalCost;
}

  // Mostrar aristas del MST elegidas por Prim
public void displayEdges() {
    if (mstEdges.isEmpty()) {
        return;
    }

    mstEdges.sort((a, b) -> {
        String a1 = (a.from.compareTo(a.to) < 0) ? a.from : a.to;
        String a2 = (a.from.compareTo(a.to) < 0) ? a.to : a.from;
        String b1 = (b.from.compareTo(b.to) < 0) ? b.from : b.to;
        String b2 = (b.from.compareTo(b.to) < 0) ? b.to : b.from;

        int cmp = a1.compareTo(b1);
        if (cmp != 0) return cmp;
        return a2.compareTo(b2);
    });

    for (Edge e : mstEdges) {
        String node1 = (e.from.compareTo(e.to) < 0) ? e.from : e.to;
        String node2 = (e.from.compareTo(e.to) < 0) ? e.to : e.from;
        System.out.println(node1 + " - " + node2 +
            " | Cost: " + e.cost +
            " | Visibility: " + e.visibility);
    }
}


    // Elimina nodo totalmente del grafo
    public void removeNode(String node) {
        graph.remove(node);
        decoyNodes.remove(node);

        for (String key : graph.keySet()) {
            graph.get(key).removeIf(e -> e.from.equals(node) || e.to.equals(node));
        }
    }
}
