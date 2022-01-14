package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Task {
    private String name;
    private int weight;
    private List<Dependency> dependents;
    private List<Dependency> dependencies;

    public Task(String name, int weight) {
        this.name = name;
        this.weight = weight;
        dependents = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public List<Dependency> getDependents() {
        return dependents;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void addDependency(Task dependency, int commTime) {
        Dependency dep = new Dependency(this, dependency, commTime);
        dependencies.add(dep);
        dependency.dependents.add(dep);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return weight == task.weight &&
                name.equals(task.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
