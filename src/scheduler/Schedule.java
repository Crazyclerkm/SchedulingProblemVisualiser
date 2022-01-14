package scheduler;

import java.util.*;

/**
 * Note: This class has a natural ordering that is inconsistent with equals
 */
public class Schedule implements Cloneable, Comparable<Schedule> {
    private int estimatedTime;
    private int[] procLastTaskEndTime;
    private int[] procStartTime;
    private int numProcessors;
    private HashMap<Task, ScheduledTask> tasks;
    private int taskCount;
    private int length;

    public Schedule(int processorNo, int allTaskWeight, int taskCount) {
        procLastTaskEndTime = new int[processorNo];
        procStartTime = new int[processorNo];
        Arrays.fill(procStartTime, -1);
        tasks = new HashMap<>();
        this.numProcessors = processorNo;
        this.estimatedTime = allTaskWeight;
        this.taskCount = taskCount;
        this.length = 0;
    }

    public Schedule clone() {
        Schedule clonedSchedule;

        try {
            clonedSchedule = (Schedule) super.clone();
        } catch (CloneNotSupportedException e) {
            //If we're here, things are very wrong
            System.out.println("Cloneable interface not cloneable");
            System.exit(1);
            return null;
        }

        clonedSchedule.tasks = (HashMap<Task, ScheduledTask>)this.tasks.clone();
        clonedSchedule.procLastTaskEndTime = this.procLastTaskEndTime.clone();
        return clonedSchedule;
    }

    public Schedule add(Task task, int processorNo) throws TaskNotSchedulableException {
        //  Check if all dependencies are already scheduled and if so, schedule given task at earliest start time
        //  on the specified processor, accounting for communication time from dependencies on other processors
        Schedule alteredSchedule = this.clone();

        if (isSchedulable(task)) {
            int startTime = earliestStartTime(task, processorNo);

            if(procStartTime[processorNo] == -1) {
                procStartTime[processorNo] = startTime;
            }

            int endTime = startTime + task.getWeight();
            int newIdleTime = startTime - alteredSchedule.procLastTaskEndTime[processorNo];
            ScheduledTask scheduledTask = new ScheduledTask(startTime, processorNo, task.getWeight());
            alteredSchedule.tasks.put(task, scheduledTask);
            alteredSchedule.procLastTaskEndTime[processorNo] = endTime;
            alteredSchedule.estimatedTime += newIdleTime;

            if (endTime > alteredSchedule.length) {
                alteredSchedule.length = endTime;
            }

            if (alteredSchedule.isComplete()) {
                alteredSchedule.estimatedTime = alteredSchedule.length * this.procLastTaskEndTime.length;
            }
        } else {
            throw new TaskNotSchedulableException(task.getName() + "was not schedulable");
        }
        return alteredSchedule;
    }

    public boolean isSchedulable(Task task) {
        if (tasks.containsKey(task)) {
            return false;
        }

        for (Dependency dependency : task.getDependencies()) {
            Task dependeeTask = dependency.getDependency();

            if (!tasks.containsKey(dependeeTask)) return false;

        }
        return true;
    }

    public int earliestStartTime(Task task, int processorNo) {
        int latestDependencyFinish = procLastTaskEndTime[processorNo];

        for (Dependency dependency : task.getDependencies()) {
            Task dependeeTask = dependency.getDependency();
            if (tasks.containsKey(dependeeTask)) {
                ScheduledTask dependeeScheduledTask = tasks.get(dependeeTask);
                int dependeeEndTime = dependeeScheduledTask.getStartTime() + dependeeTask.getWeight();

                if (tasks.get(dependeeTask).getProcessor() != processorNo) {
                    dependeeEndTime += dependency.getCommTime();
                }

                if (dependeeEndTime > latestDependencyFinish) latestDependencyFinish = dependeeEndTime;
            }
        }

        return latestDependencyFinish;
    }

    public ArrayList<Task> getSchedulableChildren(Task task) {
        List<Dependency> candidates = task.getDependents();
        ArrayList<Task> schedulableChildren = new ArrayList<>();

        for(Dependency dependency : candidates) {
            if(isSchedulable(dependency.getDependency())) {
                schedulableChildren.add(dependency.getDependency());
            }
        }

        return schedulableChildren;
    }

    public HashMap<Task, ScheduledTask> getTasks() {
        return tasks;
    }

    public boolean isComplete() {
        return this.tasks.size() >= this.taskCount;
    }

    public int getCost() {
        return this.estimatedTime;
    }

    public int[] getProcStartTime() {
        return procStartTime;
    }

    public int getLength() {
        return this.length;
    }

    public boolean isProcessorEmpty(int i) {
        return this.procLastTaskEndTime[i] == 0;
    }

    @Override
    public int compareTo(Schedule o) {
        return this.estimatedTime < o.estimatedTime ? -1 : (this.estimatedTime == o.estimatedTime ? 0 : 1);
    }

    @Override
    public String toString() {
        return this.tasks.toString() + this.estimatedTime + "_" + this.length + Arrays.toString(this.procLastTaskEndTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return estimatedTime == schedule.estimatedTime &&
                taskCount == schedule.taskCount &&
                length == schedule.length &&
                Arrays.equals(procLastTaskEndTime, schedule.procLastTaskEndTime) &&
                tasks.equals(schedule.tasks);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(estimatedTime, tasks, taskCount, length);
        result = 31 * result + Arrays.hashCode(procLastTaskEndTime);
        return result;
    }

    public int getNumProcessors() {
        return numProcessors;
    }

    public List<ScheduledTask> getProcessorTaskList(int processorNo) {
        List<ScheduledTask> taskList = new ArrayList<>();

        for(ScheduledTask s : tasks.values()) {
            if(s.getProcessor() == processorNo) taskList.add(s);

        }

        taskList.sort(ScheduledTask::compareTo);

        return taskList;
    }
}
