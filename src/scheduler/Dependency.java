package scheduler;

/*
  Dependency represents the dependency of 2 tasks.
  `dependency` needs to finish before `task` can start.
 */
public class Dependency {
    private Task task;
    private Task dependency;
    private int commTime;

    public Dependency(Task task, Task dependency, int commTime) {
        this.task = task;
        this.dependency = dependency;
        this.commTime = commTime;
    }

    public Task getTask() {
        return task;
    }

    public Task getDependency() {
        return dependency;
    }

    public int getCommTime() {
        return commTime;
    }
}
