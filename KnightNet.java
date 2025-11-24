/* Daniel Rangosch
Dr. Steinberg
COP3503 Fall 2025
Programming Assignment 5
*/
import java.io.;
import java.util.*;

public class KnightNet {

// Helper class: representa una arista no dirigida
private static class Edge implements Comparable<Edge> {
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

    // Orden por costo, luego por etiquetas para determinismo
    @Override
    public int compareTo(Edge other) {
        int c = Integer.compare(this.cost, other.cost);
        if (c != 0) return c;
        String thisPair = (this.from.compareTo(this.to) <= 0) ? this.from + this.to : this.to + this.from;
        String otherPair = (other.from.compareTo(other.to) <= 0) ? other.from + other.to : other.to + other.from;
        int s = thisPair.compareTo(otherPair);
        if (s != 0) return s;
        return Integer.compare(this.visibility, other.visibility);
    }
}

private HashMap<String, ArrayList<Edge>> graph; // grafo original (lista de adyacencia)
private HashSet<String> decoyNodes;             // nodos que son decoys (si aparecen en arista decoy)
private ArrayList<Edge> mstEdges;               // aristas resultantes del último MST calculado
private int maxVisibilityField;                 // valor por defecto almacenado (no obligatorio usarlo)

/* Lee archivo y construye el grafo; detecta decoy nodes */
public KnightNet(String filename, int maxVisibility) throws IOException {
    graph = new HashMap<>();
    decoyNodes = new HashSet<>();
    mstEdges = new ArrayList<>();
    this.maxVisibilityField = maxVisibility;

    BufferedReader br = new BufferedReader(new FileReader(filename));
    String line;
    while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;
        String[] parts = line.split(",");
        if (parts.length < 5) continue;
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

/* Devuelve nodos que no son decoy */
public HashSet<String> getRealNodes() {
    HashSet<String> real = new HashSet<>(graph.keySet());
    real.removeAll(decoyNodes);
    return real;
}

/* Ejecuta Prim sobre subgrafo válido; retorna costo o -1 si imposible */
public int computeMSTCost(String startNode, int maxVisibility) {
    mstEdges.clear();

    HashSet<String> realNodes = getRealNodes();
    if (realNodes.size() < 2) return -1;

    if (!realNodes.contains(startNode)) {
        // driver should pass a real node; defensivamente devolvemos -1
        return -1;
    }

    // Construir subgrafo limpio: solo nodos reales y aristas válidas
    HashMap<String, ArrayList<Edge>> clean = new HashMap<>();
    for (String n : realNodes) clean.put(n, new ArrayList<>());

    for (String n : realNodes) {
        for (Edge e : graph.getOrDefault(n, new ArrayList<>())) {
            // considerar solo aristas entre nodos reales, no-decoy y visibilidad OK
            if (!realNodes.contains(e.from) || !realNodes.contains(e.to)) continue;
            if (e.isDecoy) continue;
            if (e.visibility > maxVisibility) continue;

            // Añadir arista únicamente al listado del extremo 'from'
            // (ya que el grafo original las tiene duplicadas por ambos extremos,
            //  aquí evitamos crear objetos nuevos; usar la misma referencia está bien)
            clean.get(e.from).add(e);
        }
    }

    // Prim
    HashSet<String> visited = new HashSet<>();
    PriorityQueue<Edge> pq = new PriorityQueue<>();

    visited.add(startNode);
    for (Edge e : clean.getOrDefault(startNode, new ArrayList<>())) pq.add(e);

    int totalCost = 0;

    while (!pq.isEmpty() && visited.size() < realNodes.size()) {
        Edge e = pq.poll();

        boolean fromVisited = visited.contains(e.from);
        boolean toVisited = visited.contains(e.to);

        if (fromVisited && toVisited) continue;
        if (!fromVisited && !toVisited) continue; // edge between two unvisited nodes; skip

        String newNode = fromVisited ? e.to : e.from;

        // Añadir arista al MST (usar la copia original)
        mstEdges.add(e);
        totalCost += e.cost;
        visited.add(newNode);

        // Agregar aristas adyacentes del nuevo nodo que conecten a no-visitados
        for (Edge next : clean.getOrDefault(newNode, new ArrayList<>())) {
            boolean aV = visited.contains(next.from);
            boolean bV = visited.contains(next.to);
            if ((aV && !bV) || (!aV && bV)) {
                pq.add(next);
            }
        }
    }

    if (visited.size() < realNodes.size()) return -1;
    return totalCost;
}

/* Muestra las aristas del MST con orden y formato requerido */
public void displayEdges() {
    List<Edge> sorted = new ArrayList<>(mstEdges);
    sorted.sort((a, b) -> {
        String aSrc = a.from.compareTo(a.to) <= 0 ? a.from : a.to;
        String aDst = a.from.compareTo(a.to) <= 0 ? a.to : a.from;
        String bSrc = b.from.compareTo(b.to) <= 0 ? b.from : b.to;
        String bDst = b.from.compareTo(b.to) <= 0 ? b.to : b.from;

        int cmp = aSrc.compareTo(bSrc);
        if (cmp != 0) return cmp;
        return aDst.compareTo(bDst);
    });

    for (Edge e : sorted) {
        String n1 = e.from.compareTo(e.to) <= 0 ? e.from : e.to;
        String n2 = e.from.compareTo(e.to) <= 0 ? e.to : e.from;
        System.out.println(n1 + " - " + n2 + " | Cost: " + e.cost + " | Visibility: " + e.visibility);
    }
}

/* Elimina físicamente un nodo y todas sus aristas del grafo */
public void removeNode(String node) {
    graph.remove(node);
    decoyNodes.remove(node);

    for (String key : new HashSet<>(graph.keySet())) {
        ArrayList<Edge> list = graph.get(key);
        if (list != null) {
            list.removeIf(e -> e.from.equals(node) || e.to.equals(node));
        }
    }
}
}