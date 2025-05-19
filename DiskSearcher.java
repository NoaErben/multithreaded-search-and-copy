import java.io.File;

/**
 * Name: Liraz Gabbay
 * ID: 323958561
 * Main application class.
 * This application searches for all files under some given path that contain a
 * given textual pattern. All files found are copied to some specific directory.
 * This is the main class of the application. This class contains a main method
 * that starts the search process according to the given command lines.
 */
public class DiskSearcher extends java.lang.Object {

    static int DIRECTORY_QUEUE_CAPACITY = 50;
    static int RESULTS_QUEUE_CAPACITY = 50;

    public DiskSearcher() {

    }

    /**
     * Main method. Reads arguments from command line and starts the search.
     */

    public static void main(String[] args) {
        int numOfSearchers;
        int numOfCopiers;
        String pattern = args[0];
        String extension = args[1];
        String directoryToSearch = args[2];
        String destination = args[3];
        String numSearchers = args[4];
        String numCopiers = args[5];
        if (args.length == 6) {
            File root = new File(directoryToSearch);
            File destFile = new File(destination);
            // check validation of arguments
            if (!root.isDirectory()) {
                System.out.println("wrong source file format");
                return;
            }
            if (!destFile.isDirectory()) {
                System.out.println("wrong destination file format");
                return;
            }
            try {
                numOfSearchers = Integer.parseInt(numSearchers);
            } catch (Exception e) {
                System.out.println("Number of searcher is not a integer");
                return;
            }
            try {
                numOfCopiers = Integer.parseInt(numCopiers);
            } catch (Exception e) {
                System.out.println("Number of copiers is not a integer");
                return;
            }

            // create directoryQueue,resultsQueue
            SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
            SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);

            // Start a single scouter thread
            Thread scouter = new Thread(new Scouter(directoryQueue, root));
            scouter.start();

            // Start a group of searcher threads (number of searchers as specified in
            // arguments)
            Thread[] searchers = new Thread[numOfSearchers];
            for (int i = 0; i < numOfSearchers; i++) {
                searchers[i] = new Thread(new Searcher(pattern, extension, directoryQueue, resultsQueue));
                searchers[i].start();
            }

            // Start a group of copier threads (number of copiers as specified in arguments)
            Thread[] copiers = new Thread[numOfCopiers];
            for (int j = 0; j < numOfCopiers; j++) {
                copiers[j] = new Thread(new Copier(destFile, resultsQueue));
                copiers[j].start();
            }

            // Wait for scouter to finish
            try {
                scouter.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Wait for searcher and copier thread to finish
            try {
                for (Thread search : searchers) {
                    search.join();
                }
                for (Thread copier : copiers) {
                    copier.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
