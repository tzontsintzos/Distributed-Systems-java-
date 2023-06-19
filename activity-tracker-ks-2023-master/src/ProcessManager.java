package src;

import src.master.Master;
import src.worker.Worker;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//It takes a command line argument representing the number of worker processes to spawn,
//and creates a new Master process and the requested number of Worker processes.
//It then waits for all processes to complete before terminating.

public class ProcessManager {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Required argument: number of workers (int)");
            System.exit(2);
        }
        try {
            Integer.valueOf(args[0]);
        } catch (Exception e) {
            System.err.println("Argument worker count is not integer");
            System.exit(2);
        }

        LinkedList<Process> children = new LinkedList<>();  //keep track of the child processes (i.e. Worker processes)

        Process master = execProcess(Master.class, args);
        for (int i = 0; i < Integer.parseInt(args[0]); ++i) {
            children.add(execProcess(Worker.class, args));
        }

        // waits for the Master process to complete using the waitFor method. It then iterates over the child processes,
        // waits for each process to complete for up to 500 milliseconds using the waitFor method
        // total number of spawned processes: n+1 (master)
        try {
            master.waitFor();
            for (var c : children) {
                if (c.waitFor(500, TimeUnit.MILLISECONDS)) {
                    System.err.println("shut down cleanly pid: " + c.pid() + "\texit val: " + c.exitValue());
                }
            }
        } finally {
            for (var c : children) {
                c.destroy();    //cleans up by destroying any child processes that are still running using the destroy method
            }
        }
    }

    // https://stackoverflow.com/a/723914
    //takes a Class object and an array of strings as arguments, and returns a Process object.
    // It is used to execute a new process for the specified class, passing the given arguments
    private static Process execProcess(Class<?> cls, String[] args) throws IOException {
        List<String> command = new LinkedList<>();

        command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        // class path java flag
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(cls.getName());
        Collections.addAll(command, args);

        return new ProcessBuilder(command).inheritIO().start();   //new ProcessBuilder object with the command list, sets its IO streams to inherit from the parent process, and starts the new process.
    }
}
//The path to the Java executable (java.home system property + /bin/java)
//The class path flag (-cp) followed by the current class path (java.class.path system property)
//The name of the class to execute (cls.getName())
//The arguments passed to the method (args)