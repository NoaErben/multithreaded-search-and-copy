# Multithreaded Search and Copy 

## Overview
This project implements a **multithreaded search and copy utility** in **Java**. 
The utility searches for files that contain a given pattern in their name and have a specific extension within a root directory and its subdirectories. 
Any matched files are copied to a specified destination directory. 
The system efficiently utilizes **multithreading** with synchronized queues to improve performance.

## Workflow
1. **Scouter Thread **:
   - Traverses the root directory recursively.
   - Enqueues each encountered directory into a queue.
2. **Searcher Threads **:
   - Dequeues directories from the queue.
   - Searches for files matching the pattern and extension.
   - Enqueues matching files into a results queue.
3. **Copier Threads **:
   - Dequeues files from the results queue.
   - Copies them to the destination directory.

## Features
- **Efficient multithreading using producer-consumer architecture**.
- **Thread-safe queues** for communication between threads.
- **Recursive directory traversal** to search all subdirectories.
- **Handles large-scale searches efficiently** using concurrent processing.
- **Supports command-line execution** for flexibility.
