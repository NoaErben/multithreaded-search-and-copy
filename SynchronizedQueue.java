/**
 * Name: Liraz Gabbay
 * ID: 323958561
 * A synchronized bounded-size queue for multithreaded producer-consumer
 * applications.
 * This class should allow multithreaded enqueue/dequeue operations
 * 
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

	private T[] buffer;
	private int currentSize;
	private int producerCount;
	private int firstItemIndex;
	private int rearItemIndex;

	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * 
	 * @param capacity Buffer capacity
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedQueue(int capacity) {
		this.buffer = (T[]) (new Object[capacity]);
		this.currentSize = 0;
		this.producerCount = 0;
		this.firstItemIndex = 0;
		this.rearItemIndex = 0;
	}

	/**
	 * Dequeues the first item from the queue and returns it.
	 * If the queue is empty but producers are still registered to this queue,
	 * this method blocks until some item is available.
	 * If the queue is empty and no more items are planned to be added to this
	 * queue (because no producers are registered), this method returns null.
	 * 
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public T dequeue() {
		synchronized (this) {
			T dequeuedItem = null;
			// If the queue is empty and no more items are planned to be added to this
			// queue (because no producers are registered), this method returns null.
			if (this.currentSize <= 0 && producerCount == 0) {
				return null;
			}
			// If the queue is empty but producers are still registered to this queue,
			// this method blocks until some item is available.
			while (this.currentSize <= 0 && producerCount != 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Dequeue the item from the front of the queue
			dequeuedItem = buffer[this.firstItemIndex];
			buffer[this.firstItemIndex] = null;
			this.firstItemIndex = (this.firstItemIndex + 1) % buffer.length;
			this.currentSize--;

			notifyAll();

			// Return the dequeued item
			return dequeuedItem;
		}
	}

	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this
	 * method blocks until some space becomes available.
	 * 
	 * @param item Item to enqueue
	 */
	public void enqueue(T item) {
		synchronized (this) {
			// If the queue is full, this method blocks until some space becomes available.
			while (this.currentSize == buffer.length && this.producerCount > 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Enqueue the item from the rear
			buffer[this.rearItemIndex] = item;
			this.rearItemIndex = (this.rearItemIndex + 1) % buffer.length;
			this.currentSize++;
			notifyAll();
		}
	}

	/**
	 * Returns the capacity of this queue
	 * 
	 * @return queue capacity
	 */
	public int getCapacity() {
		return this.buffer.length;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * 
	 * @return queue size
	 */
	public int getSize() {
		return this.currentSize;
	}

	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items.
	 * Every producer of this queue must call this method before starting to
	 * enqueue items, and must also call <see>{@link #unregisterProducer()}</see>
	 * when
	 * finishes to enqueue all items.
	 * 
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public void registerProducer() {
		synchronized (this) {
			this.producerCount++;
		}
	}

	/**
	 * Unregisters a producer from this queue. See
	 * <see>{@link #registerProducer()}</see>.
	 * 
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public void unregisterProducer() {
		synchronized (this) {
			this.producerCount--;
			if (this.producerCount == 0) {
				notifyAll();
			}
		}
	}

}
