package parser;

public class Edge extends Element {
    public final String from;
    public final String to;

    Edge(String from, String to) {
        this.from = from;
        this.to = to;
    }
}
