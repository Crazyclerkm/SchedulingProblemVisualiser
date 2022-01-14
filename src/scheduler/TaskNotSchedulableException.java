package scheduler;

public class TaskNotSchedulableException extends Exception {
    public TaskNotSchedulableException() {
        super();
    }

    public TaskNotSchedulableException(String message) {
        super(message);
    }
}
