# SchedulingProblemVisualiser
Java application to optimally solve the scheduling of tasks onto a set of processors along with visualisations.

## Install
Download SchedulingProblemVisualiser.jar from the [v1.0.0 release](https://github.com/Crazyclerkm/SchedulingProblemVisualiser/releases/tag/v1.0.0).

## Usage
Open SchedulingProblemVisualiser.jar

Example task graphs can be found [here](https://github.com/Crazyclerkm/SchedulingProblemVisualiser/tree/master/examples)

![Default screen](https://puu.sh/ICGRx/7485f43fa9.png)

You can select an input .dot file that represents a task graph using the "Select Task Graph" button located in the top left.

![Loaded task graph](https://puu.sh/ICGZN/9c8d576711.png)

After selecting a task graph an image representation of it will be loaded.

You can then specify the number of processors for the task graph to be scheduled on, as well as enabling/disabling multithreading.

When ready you can use the "Find Optimal Schedule" button in the bottom left to begin finding the best schedule.

![Calculating Optimal Schedule](https://puu.sh/ICGRg/d403012ced.png)

From here you can select a schedule from the list of schedules in order to display it on the right. You can also save the selected schedule to a .dot file.

When finding the optimal schedule you can see RAM usage as well as time taken.
