package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	     this.conditionLock = conditionLock;
	     
	     //waitQueue = new LinkedList<ThreadKernel>(); 
    }
    

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
    	boolean intStatus = Machine.interrupt().disable();
    
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	//initialize wait value
    //waitQueue.add(wait);
	
	conditionLock.release();

	if (value == 0) {
	    waitQueue.waitForAccess(KThread.currentThread());
	    KThread.sleep();
	}
	else {
	    value--;
	}

	Machine.interrupt().restore(intStatus);	

	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	
    	boolean intStatus = Machine.interrupt().disable();

    	KThread thread = waitQueue.nextThread();
    	if (thread != null) {
    	    thread.ready();
    	}
    	else {
    	    value++;
    	}
    	
    	Machine.interrupt().restore(intStatus);
	
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	 	wake(); 
    }
    
    public static void selfTest(){
    	int[] item = new int[1];
    	item[0] = 0;
    	Lock lock = new Lock();
    	Condition2 con = new Condition2(lock);
    	KThread producer = new KThread(new Producer(item, lock, con));
    	KThread consumer1 = new KThread(new Consumer(item, lock, con));
    	KThread consumer2 = new KThread(new Consumer(item, lock, con));
    	System.out.println("\n----------------------------------------------\nTEST CONDITION2\n");
    	consumer1.fork();
    	consumer2.fork();
    	producer.fork();
    	//ThreadedKernel.alarm.waitUntil(100000);
    }
    
    private static class Consumer implements Runnable{
    	private int[] item;
    	private Lock lock;
    	private Condition2 sleepingConsumers;
    	public Consumer(int[] iteM, Lock lock, Condition2 con_var){
    		item = iteM;
    		this.lock = lock;
    		sleepingConsumers = con_var;
    	}
    	public void run(){
    		lock.acquire();
    		while(item[0] < 1){
    			System.out.println("Consumer: no item, so I sleep.");
    			sleepingConsumers.sleep();
    		}
    		item[0] -= 1;
    		System.out.println("Consumer: I use 1 item.");
    		lock.release();
    	}
    }
    
    private static class Producer implements Runnable{
    	private int[] item;
    	private Lock lock;
    	private Condition2 sleepingConsumers;
    	public Producer(int[] iteM, Lock lock, Condition2 con_var){
    		item = iteM;
    		this.lock = lock;
    		sleepingConsumers = con_var;
    	}
    	public void run(){
    		lock.acquire();
    		item[0] += 1;
    		System.out.println("Producer: I make one item.");
    		sleepingConsumers.wakeAll();
    		lock.release();
    	}
    }
    
    private int value; 
    private Lock conditionLock;
    private ThreadQueue waitQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
	public void call() {
		// TODO Auto-generated method stub
	}
}
