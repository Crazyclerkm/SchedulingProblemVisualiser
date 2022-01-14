package scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

public class Scheduler {
    private List<Task> taskList;
    private int processorNo;
    private int allTaskWeight;

    public Scheduler(List<Task> taskList, int processorNo) {
        this.taskList = taskList;
        this.processorNo = processorNo;
        this.allTaskWeight = 0;

        for (Task t : this.taskList) {
            this.allTaskWeight += t.getWeight();
        }
    }

    private Schedule listSchedule() {
        Schedule schedule = new Schedule(processorNo, this.allTaskWeight, this.taskList.size());
        List<Task> tasks = getTopologicalOrder();

        for (Task task : tasks) {
            int earliestStartTime = schedule.earliestStartTime(task, 0);
            int processor = 0;
            for (int i = 1; i < processorNo; i++) {
                int processorStartTime = schedule.earliestStartTime(task, i);
                if (processorStartTime < earliestStartTime) {
                    earliestStartTime = processorStartTime;
                    processor = i;
                }
            }
            try {
                schedule = schedule.add(task, processor);
            } catch (TaskNotSchedulableException e) {
                System.err.println("Task " + task.getName() + " was not schedulable on processor" + processor);
                System.exit(1);
            }
        }
        return schedule;
    }

    private List<Task> getTopologicalOrder() {
        Map<Task, Integer> inDegree = new HashMap<>();
        List<Task> candidate = new ArrayList<>();

        for(Task t : taskList) {
            int count = t.getDependencies().size();

            if (count == 0) {
                candidate.add(t);
            } else {
                inDegree.put(t, count);
            }
        }

        List<Task> s = new ArrayList<>();

        while (!candidate.isEmpty()) {
            Task v = candidate.remove(0);
            s.add(v);

            for (Dependency d : v.getDependents()) {
                Task dependent = d.getTask();
                int count = inDegree.get(dependent) - 1;
                inDegree.put(dependent, count);

                if (count == 0) {
                    candidate.add(dependent);
                    inDegree.remove(dependent);
                }
            }
        }

        return s;
    }

    // DFS branch & bound implemented with parallelisation
    public Schedule findBestSchedule(int threadCount, SchedulerListener listener) {
        long start = System.nanoTime();

        //Create initial state
        SchedulerState initState = new SchedulerState(new Semaphore(2), null);
        ThreadState initThread = new ThreadState(threadCount);
        initThread.boundSchedule = this.listSchedule();

        if (!initThread.boundSchedule.isComplete()) {
            System.err.println("Initial schedule not complete");
            System.exit(1);
            return null;
        }

        //Notify UI
        if (listener != null) {
            listener.notifyInit(initThread.boundSchedule, threadCount);
        }

        //Generate initial roots for each thread
        if (threadCount != 1) {
            findThreadSchedule(0, initThread, initState, new Schedule(this.processorNo, this.allTaskWeight, this.taskList.size()), threadCount);

            //Did we finish
            if (initState.done.count < 2) {
                return initThread.boundSchedule;
            }
        } else {
            initThread.roots[0] = new Schedule(this.processorNo, this.allTaskWeight, this.taskList.size());
        }

        SchedulerState schedState = new SchedulerState(new Semaphore(threadCount), listener);
        ThreadState[] states = new ThreadState[threadCount];

        for (int i = 0; i < threadCount; i++) {
            states[i] = new ThreadState(0);
            states[i].boundSchedule = initThread.boundSchedule;
            Schedule root = initThread.roots[i];

            if (root == null) {
                break;
            }

            int id = i;
            new Thread(() -> findThreadSchedule(id, states[id], schedState, root,0)).start();
        }

        schedState.done.waitFor();
        Schedule best = initThread.boundSchedule;

        for (int i = 0; i < threadCount; i++) {
            if (states[i] == null) {
                break;
            }

            if (states[i].boundSchedule.getLength() < best.getLength()) {
                best = states[i].boundSchedule;
            }
        }

        return best;
    }

    private static void findThreadSchedule(int id, ThreadState state, SchedulerState scheduler, Schedule root, int max) {
        LinkedBlockingDeque<Schedule> stack = new LinkedBlockingDeque<>();
        stack.add(root);
        Schedule schedule;

        while (!stack.isEmpty()) {
            schedule = stack.poll();

            //Notify UI
            if (scheduler.listener != null) {
                scheduler.listener.notifyPop(id, schedule);
            }

            List<Schedule> children = new ArrayList<>();

            for (Task t : scheduler.scheduler.taskList) {
                if (schedule.isSchedulable(t)) {
                    for (int i = 0; i < scheduler.scheduler.processorNo; i++) {
                        Schedule child;
                        boolean wasEmpty = schedule.isProcessorEmpty(i);

                        try {
                            child = schedule.add(t, i);
                        } catch (TaskNotSchedulableException e) {
                            System.err.println("Task " + t.getName() + " was not schedulable on processor" + i);
                            System.exit(1);
                            return;
                        }

                        children.add(child);

                        //Only include those smaller than bound
                        if (child.getCost() < state.boundSchedule.getCost() && child.getLength() < state.boundSchedule.getLength() && !stack.contains(child)) {
                            stack.addFirst(child);

                            //Did we get enough to parallelise
                            if (max != 0 && stack.size() == max) {
                                for (int j = 0; j < max; j++) {
                                    state.roots[j] = stack.poll();
                                }

                                return;
                            }

                            //If we found a complete schedule lower the bound
                            if (child.isComplete()) {
                                state.boundSchedule = child;

                                //Notify UI
                                if (scheduler.listener != null) {
                                    scheduler.listener.notifyBound(id, child);
                                }
                            }
                        }

                        //All empty processors are the same
                        if (wasEmpty) {
                            break;
                        }
                    }
                }
            }

            //Notify UI
            if (scheduler.listener != null) {
                scheduler.listener.notifyConnections(id, schedule, children);
            }
        }

        scheduler.done.decrement();
    }

    public Schedule getRootSchedule() {
        return new Schedule(this.processorNo, this.allTaskWeight, this.taskList.size());
    }

    private class ThreadState {
        public Schedule boundSchedule;
        public final Schedule[] roots;

        public ThreadState(int count) {
            this.roots = new Schedule[count];
        }
    }

    private class SchedulerState {
        public final Scheduler scheduler;
        public final Semaphore done;
        public final SchedulerListener listener;

        public SchedulerState(Semaphore done, SchedulerListener listener) {
            this.scheduler = Scheduler.this;
            this.done = done;
            this.listener = listener;
        }
    }

    private class Semaphore {
        private volatile int count;

        public Semaphore(int initCount) {
            this.count = initCount;
        }

        private void decrement() {
            synchronized (this) {
                this.count--;

                if (this.count == 0) {
                    this.notify();
                }
            }
        }

        private void waitFor() {
            synchronized (this) {
                while (this.count != 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        //Don't worry about it
                    }
                }
            }
        }
    }
}
