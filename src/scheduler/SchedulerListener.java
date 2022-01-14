package scheduler;

import java.util.List;

public interface SchedulerListener {
    void notifyInit(Schedule bound, int threadCount);
    void notifyBound(int thread, Schedule bound);
    void notifyConnections(int thread, Schedule parent, List<Schedule> children);
    void notifyPop(int thread, Schedule schedule);
}
