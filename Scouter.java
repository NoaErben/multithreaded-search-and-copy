import java.io.File;

/*
 * Name: Liraz Gabbay
 * ID: 323958561
 * This class is responsible for listing all directories that exist under the given root directory. 
 * It enqueues all directories into the directory queue.
 * There is always only one scouter thread in the system.
 */
public class Scouter extends java.lang.Object implements java.lang.Runnable {
    private SynchronizedQueue<File> directoryQueue;
    private File root;

    /**
     * Construnctor. Initializes the scouter with a queue for the directories to be
     * searched and a root directory to start from.
     * 
     * @param directoryQueue - A queue for directories to be searched
     * @param root           - Root directory to start from
     */
    public Scouter(SynchronizedQueue<java.io.File> directoryQueue, java.io.File root) {
        this.directoryQueue = directoryQueue;
        this.root = root;
    }

    /**
     * Recursively traverses directories starting from the given directory.
     * Enqueues each encountered directory into a directory queue.
     *
     * @param directory The directory to start traversal from.
     */
    public void enqueueDirectoriesRecursively(File directory) {
        if (directory.isDirectory()) {
            directoryQueue.enqueue(directory);
            File[] files = directory.listFiles(File::isDirectory);
            if (files != null) {
                for (File file : files) {
                    enqueueDirectoriesRecursively(file);
                }
            }
        }
    }

    /**
     * Starts the scouter thread.
     * Lists directories under root directory and adds them to queue,
     * then lists directories in the next level and enqueues them and so on.
     * This method begins by registering to the directory queue as a producer and
     * when finishes, it unregisters from it.
     */
    @Override
    public void run() {
        // begins by registering to the directory queue as a producer
        this.directoryQueue.registerProducer();
        try {
            enqueueDirectoriesRecursively(root);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // when finishes, it unregisters from it
        this.directoryQueue.unregisterProducer();
    }
}