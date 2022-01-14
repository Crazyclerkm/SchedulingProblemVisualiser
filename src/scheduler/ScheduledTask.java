package scheduler;

import java.util.Objects;

public class ScheduledTask implements Comparable<ScheduledTask>{
    private int startTime;
    private int processor;
    private int weight;

    public ScheduledTask(int startTime, int processor, int weight) {
        this.startTime = startTime;
        this.processor = processor;
        this.weight = weight;
    }

    public int getProcessor() {
        return processor;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getWeight() {return weight; }

    @Override
    public String toString() {
        return this.startTime + "_" + this.weight + "_" + this.processor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledTask that = (ScheduledTask) o;
        return startTime == that.startTime &&
                processor == that.processor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, processor);
    }

    @Override
    public int compareTo(ScheduledTask o) {
        return Integer.compare(this.startTime, o.startTime);
    }
}
