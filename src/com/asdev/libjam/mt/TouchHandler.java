package com.asdev.libjam.mt;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
*	TouchHandler class by Asdev. Call start(devNum) to start listening.
*	You may make any modifications but you must but credit to Asdev in your application.
*/
public class TouchHandler {

	private static ArrayList<OnTouchListener> listeners = new ArrayList<>();
	private static Object listLock = new Object();

	/**
	*	Gets the attached listeners
	*	@return the ArrayList of listeners
	*/
	public static synchronized ArrayList<OnTouchListener> getListeners(){
		synchronized (listLock) {
			return listeners;
		}
	}
	
	/**
	*	Removes the specified OnTouchListener
	*	@param the listener to remove
	*/
	public static synchronized void removeOnTouchListener(OnTouchListener ot){
		synchronized (listLock) {
			getListeners().remove(ot);
		}
	}
	
	/**
	*	Adds the specified OnTouchListener
	* 	@param the OnTouchListener to add
	*/
	public static synchronized void addOnTouchListener(OnTouchListener ot){
		synchronized (listLock) {
			getListeners().add(ot);
		}
	}
	
	/**
	*	Make sure no one else can create an instance
	*/
	private TouchHandler(){
	}

	private static volatile boolean inited = false, enabled = false;

	/**
	*	Use this as the entry point. This loads in the JNI library and calls init on that library. After calling this method, updates will start happening.
	* 	@param the event device number. For example, if the event device is /dev/input/event4 then the event device number is 4.
	*/
	public static boolean start(int devNum){
		if(!inited){
			System.loadLibrary("jam-mt");
			new TouchHandler().init(devNum);
			inited = true;
			
			//TODO: thread for updating
			Thread thrd = new Thread(new SmartTouch());
			thrd.setDaemon(true);
			thrd.start();
			enable();
			return true;
		}else{
			enable();
			return false;
		}
	}

	/**
	*	Allows updates to happen.
	*/
	public static void enable(){
		enabled = true;
	}
	
	/**
	*	Disallows updates to happen.
	*/
	public static void disable(){
		enabled = false;
	}
	
	/**
	*	Entry point for test application
	*/
	public static void main(String[] args) throws Exception {
		
		addOnTouchListener(new OnTouchListener() {
			@Override
			public void onUpdate(double tx, double ty, int tid) {
				System.out.println("From Java: X: " + tx + " Y: " + ty + " ID: " + tid);
			}
			
			@Override
			public void onTouch(double tx, double ty, int tid){
				System.out.println("Touch ID: " + tid);
			}
			
			@Override
			public void onRelease(int tid){
				System.out.println("On release id: " + tid);
			}
		});
		
		start(Integer.parseInt(args[0]) /* replace with your event device number */ );
		
		while(true)
			Thread.sleep(50); //keeps thread alive. Use CTRL-C to kill the program
	}

	public native void init(int devNum);

	protected static ConcurrentHashMap<Integer, Long> touchTimes = new ConcurrentHashMap<>(10);
	private static Object ttLock = new Object();
	
	/**
	*	Called by the C library whenever an update happens. Updates will only happen if enabled.
	*/
	public static void onUpdate(double x, double y, int id){
		//make sure it is enabled
		if(!enabled)
			return;
		
		int corrId = id % 10;
		//check if touch was active before, if wasn't call onTouch
		if(syncTTGet(corrId) == -1)
			for(OnTouchListener o: getListeners())
				o.onTouch(x, y, corrId);
		
		//store touch time to hashmap
		syncTTPut(corrId, System.nanoTime());
		
		//call update of every listener
		for(OnTouchListener o : getListeners())
			o.onUpdate(x, y, corrId);
	}
	
	protected static void syncTTPut(int k, long v) {
		touchTimes.put(k, v);
	}
	
	protected static long syncTTGet(int k) {
		return touchTimes.get(k);
	}

}
