package parser;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    public Graph parse(TokenStream str) throws ParseException {
        Graph graph;
        Map<String, String> defEdge = new HashMap<>();
        Map<String, String> defNode = new HashMap<>();

        Token t;

        //Make sure it's a digraph
        t = str.nextToken();

        if (t.type != TokenType.ID || !t.data.equals("digraph")) {
            throw new ParseException();
        }

        t = str.nextToken();

        //Check for graph name
        if (t.type == TokenType.ID) {
            graph = new Graph(t.data);
        }
        else {
            graph = new Graph();
        }

        //Make sure we have beginning of element list
        if (str.nextToken().type != TokenType.OPEN_CURLY) {
            throw new ParseException();
        }

        while (true) {
            Element ele;
            String id;

            t = str.nextToken();

            //Make sure we have either an id or end of element list
            if (t.type != TokenType.ID && t.type != TokenType.CLOSE_CURLY) {
                throw new ParseException();
            }

            //End of element list
            if (t.type == TokenType.CLOSE_CURLY) {
                break;
            }

            //If we're here we had an ID
            id = t.data;
            t = str.nextToken();

            //Check for right arrow (= edge)
            if (t.type == TokenType.RIGHT_ARROW) {
                t = str.nextToken();

                //Check for second ID
                if (t.type != TokenType.ID) {
                    throw new ParseException();
                }

                ele = new Edge(id, t.data);
                t = str.nextToken();
            } else {
                ele = new Node(id);
            }

            //Make sure we have attribute list
            if (t.type != TokenType.OPEN_SQUARE) {
                throw new ParseException();
            }

            while (true) {
                String attrName;
                t = str.nextToken();

                //Make sure we have either id or end of attribute list
                if (t.type != TokenType.ID && t.type != TokenType.CLOSE_SQUARE) {
                    throw new ParseException();
                }

                //End of attribute list
                if (t.type == TokenType.CLOSE_SQUARE) {
                    break;
                }

                //We have an ID
                attrName = t.data;

                //Make sure we have an equals
                if (str.nextToken().type != TokenType.EQUALS) {
                    throw new ParseException();
                }

                t = str.nextToken();

                //Make sure we have an ID
                if (t.type != TokenType.ID) {
                    throw new ParseException();
                }

                ele.addAttribute(attrName, t.data);
                t = str.nextToken();

                //Make sure we have either separator or end of attribute list
                if (t.type != TokenType.SEMICOLON && t.type != TokenType.COMMA && t.type != TokenType.CLOSE_SQUARE) {
                    throw new ParseException();
                }

                //Element of attribute list
                if (t.type == TokenType.CLOSE_SQUARE) {
                    break;
                }
            }

            if (ele instanceof Node) {
                 if (((Node)ele).name.equals("graph")) {
                     //Do nothing since we don't support graph attributes
                 } else if (((Node)ele).name.equals("node")) {
                     defNode.putAll(ele.getAttributes());
                 } else if (((Node)ele).name.equals("edge")) {
                     defEdge.putAll(ele.getAttributes());
                 } else {
                     for (Map.Entry<String, String> attr : defNode.entrySet()) {
                         ele.addAttribute(attr.getKey(), attr.getValue());
                     }

                     graph.add(ele);
                 }
            } else {
                for (Map.Entry<String, String> attr : defEdge.entrySet()) {
                    ele.addAttribute(attr.getKey(), attr.getValue());
                }

                graph.add(ele);
            }

            t = str.nextToken();

            //Make sure we have either separator or end of element list
            if (t.type != TokenType.SEMICOLON && t.type != TokenType.CLOSE_CURLY) {
                throw new ParseException();
            }

            //End of element list
            if (t.type == TokenType.CLOSE_CURLY) {
                break;
            }
        }

        //Make sure that was everything
        if (str.nextToken().type != TokenType.EOF) {
            throw new ParseException();
        }

        return graph;
    }
}
