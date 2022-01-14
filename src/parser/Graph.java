package parser;

import java.util.Map;
import java.util.HashMap;

public class Graph {
    public final String name;
    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Map<String, Edge>> edges = new HashMap<>();

    Graph() {
        this.name = "";
    }

    Graph(String name) {
        this.name = name;
    }

    public Map<String, Node> getNodes() {
        Map<String, Node> nodes = new HashMap<String, Node>();
        nodes.putAll(this.nodes);
        return nodes;
    }

    public Map<String, Map<String, Edge>> getEdges() {
        Map<String, Map<String, Edge>> edges = new HashMap<>();

        for (Map.Entry<String, Map<String, Edge>> e : this.edges.entrySet()) {
            Map<String, Edge> map = new HashMap<>(e.getValue());
            edges.put(e.getKey(), map);
        }

        return edges;
    }

    public void add(Element e) {
        if (e instanceof Node) {
            this.nodes.put(((Node)e).name, (Node)e);
        } else if (e instanceof Edge) {
            Map<String, Edge> edgeMap = this.edges.get(((Edge)e).from);

            if (edgeMap == null) {
                edgeMap = new HashMap<>();
                this.edges.put(((Edge)e).from, edgeMap);
            }

            edgeMap.put(((Edge)e).to, (Edge)e);
        } else {
            throw new ClassCastException("Element not Edge or Node");
        }
    }
}
