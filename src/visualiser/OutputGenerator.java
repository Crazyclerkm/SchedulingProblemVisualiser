package visualiser;

import scheduler.Dependency;
import scheduler.Schedule;
import scheduler.ScheduledTask;
import scheduler.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/*
  Input file name is in the format of: path/to/file/filename.dot
  Output file name can be anything
 */
public class OutputGenerator {
    static public void scheduleToDot(Schedule schedule, File outputFile, String graphName) throws IOException {
        FileWriter outputWriter = new FileWriter(outputFile);
        outputWriter.write("digraph " + graphName + " {" + System.lineSeparator());

        //Write nodes
        for (Map.Entry<Task, ScheduledTask> t : schedule.getTasks().entrySet()) {
            outputWriter.write("\t" + t.getKey().getName() + "\t[Weight=" + t.getKey().getWeight() + ",Start=" + t.getValue().getStartTime() + ",Processor=" + (t.getValue().getProcessor() + 1) + "];" + System.lineSeparator());
        }

        //Write edges
        for (Map.Entry<Task, ScheduledTask> t : schedule.getTasks().entrySet()) {
            for (Dependency d : t.getKey().getDependents()) {
                outputWriter.write("\t" + d.getDependency().getName() + " -> " + d.getTask().getName() +  "\t[Weight=" + d.getCommTime() + "];" + System.lineSeparator());
            }
        }

        outputWriter.write("}" + System.lineSeparator());
        outputWriter.close();
    }
}
