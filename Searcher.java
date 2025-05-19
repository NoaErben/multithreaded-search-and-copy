import java.io.File;
import java.io.FilenameFilter;

/*
 * Name: Liraz Gabbay
 * ID: 323958561
 * This class reads a directory from the directory queue and lists all files in this directory. 
 * Then, it checks each file name for containing the given pattern and if it has the correct extension. 
 * Files that contain the pattern and have the correct extension are enqueued to the results queue (to be copied).
 * 
 */
public class Searcher extends java.lang.Object implements java.lang.Runnable {
    private java.lang.String pattern;
    private java.lang.String extension;
    private SynchronizedQueue<java.io.File> directoryQueue;
    private SynchronizedQueue<java.io.File> resultsQueue;

    /**
     * Constructor. Initializes the searcher thread.
     * 
     * @param pattern        - Pattern to look for
     * @param extension      - wanted extension
     * @param directoryQueue - A queue with directories to search in (as listed by
     *                       the scouter)
     * @param resultsQueue   - A queue for files found (to be copied by a copier)
     */
    public Searcher(java.lang.String pattern, java.lang.String extension,
            SynchronizedQueue<java.io.File> directoryQueue, SynchronizedQueue<java.io.File> resultsQueue) {
        this.pattern = pattern;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
    }

    /*
     * Runs the searcher thread. Thread will fetch a directory to search in from the
     * directory queue, then search all files inside it (but will not recursively
     * search subdirectories!). Files that a contain the pattern and have the wanted
     * extension are enqueued to the results queue. This method begins by
     * registering to the results queue as a producer and when finishes, it
     * unregisters from it.
     */
    @Override
    public void run() {
        resultsQueue.registerProducer();
        try {
            File directory;
            while ((directory = directoryQueue.dequeue()) != null) {
                searchFiles(directory);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        resultsQueue.unregisterProducer();
    }

    /**
     * Searches for files within the specified directory that match the provided
     * criteria
     * based on a search pattern and optionally an extension. Matching files are
     * enqueued
     * into a queue for further processing.
     * 
     * @param directory The directory to search for files in.
     */
    private void searchFiles(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File directory, String name) {
                    // Splitting the filename into name of file without extension and fileExtension
                    int lastDotIndex = name.lastIndexOf(".");
                    String nameOfFile, fileExtension;

                    if (lastDotIndex != -1) {
                        nameOfFile = name.substring(0, lastDotIndex);
                        fileExtension = name.substring(lastDotIndex + 1);
                    } else {
                        nameOfFile = name;
                        fileExtension = "";
                    }

                    if (!pattern.isEmpty()) {
                        if (!extension.isEmpty()) {
                            return nameOfFile.contains(pattern) && fileExtension.equals(extension);
                        } else {
                            return nameOfFile.contains(pattern);
                        }
                    } else {
                        return true;
                    }
                }
            });

            // Enqueue the filtered files into the results queue
            if (files != null) {
                for (File file : files) {
                    resultsQueue.enqueue(file);
                }
            }
        }
    }

}