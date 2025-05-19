import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class reads a file from the results queue and copies it into the
 * specified destination directory.
 */
public class Copier implements Runnable {
    static final int COPY_BUFFER_SIZE = 4096;
    private final File destination;
    private final SynchronizedQueue<File> resultsQueue;

    /**
     * Constructor. Initializes the worker with a destination directory and a queue
     * of files to copy.
     *
     * @param destination  Destination directory
     * @param resultsQueue Queue of files found, to be copied
     */
    public Copier(File destination, SynchronizedQueue<File> resultsQueue) {
        this.destination = destination;
        this.resultsQueue = resultsQueue;
    }

    /**
     * Runs the copier thread.
     *
     * Thread will fetch files from queue and copy them, one after each other,
     * to the destination directory. When the queue has no more files, the thread
     * finishes.
     */
    @Override
    public void run() {
        if (!destination.exists() && !destination.mkdir()) {
            return;
        }
        File file;
        while ((file = resultsQueue.dequeue()) != null) {
            try {
                File destFile = getFileDestination(file);
                copyFile(file, destFile);
            } catch (IOException e) {
                // Log the entire stack trace for better debugging
                e.printStackTrace();
            }
        }
    }

    private void copyFile(File sourceFile, File destinationFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(sourceFile);
                FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[COPY_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private File getFileDestination(File file) throws IOException {
        String originalFileName = file.getName();
        String baseName = originalFileName;
        String extension = "";

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            baseName = originalFileName.substring(0, dotIndex);
            extension = originalFileName.substring(dotIndex);
        }

        File destFile = new File(destination, originalFileName);
        int attemptCounter = 2;
        while (!destFile.createNewFile()) {
            String newFileName = baseName + " (" + attemptCounter + ")" + extension;
            destFile = new File(destination, newFileName);

            if (++attemptCounter > 1000) {
                throw new IOException("Unable to create a new file after 1000+ attempts");
            }
        }
        return destFile;
    }
}
