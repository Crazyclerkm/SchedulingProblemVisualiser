package parser;


import scheduler.Dependency;
import scheduler.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InputParser {
    private List<Task> tasks;
    private String name;

    public InputParser(String fileName) throws IOException, ParseException {
        Lexer lexer = new Lexer(fileName);

        Parser parser = new Parser();
        Graph graph = parser.parse(lexer);
        name = graph.name;

        Map<String, Node> nodes = graph.getNodes();
        Map<String, Map<String, Edge>> edges = graph.getEdges();

        // Map each <String, Node> (e) into <String, Task>
        Map<String, Task> tasksDict = graph.getNodes().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> new Task(
                                e.getKey(),
                                Integer.parseInt(e.getValue().getAttribute("Weight"))
                        )
                ));

        for (Map.Entry<String, Map<String, Edge>> edgeEntry : edges.entrySet()) {
            String from = edgeEntry.getKey();
            Map<String, Edge> toList = edgeEntry.getValue();
            for (Map.Entry<String, Edge> entry: toList.entrySet()) {
                String to = entry.getKey();
                int weight = Integer.parseInt(entry.getValue().getAttribute("Weight"));

                Task fromTask = tasksDict.get(from);
                Task toTask = tasksDict.get(to);
                Dependency dependency = new Dependency(toTask, fromTask, weight);
                toTask.addDependency(fromTask, weight);
            }
        }

        tasks = new ArrayList<>(tasksDict.values());
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getName() {
        return this.name;
    }
}
