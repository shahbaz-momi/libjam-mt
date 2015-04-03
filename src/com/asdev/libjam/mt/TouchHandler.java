package com.asdev.libjam.mt;

import java.util.ArrayList;
import java.util.HashMap;

public class TouchHandler {

	private static ArrayList<OnTouchListener> listeners = new ArrayList<>();
	private static Object listLock = new Object();
	
	public static synchronized ArrayList<OnTouchListener> getListeners(){
		synchronized (listLock) {
			return listeners;
		}
	}
	
	public static synchronized void removeOnTouchListener(OnTouchListener ot){
		synchronized (listLock) {
			getListeners().remove(ot);
		}
	}
	
	public static synchronized void addOnTouchListener(OnTouchListener ot){
		synchronized (listLock) {
			getListeners().add(ot);
		}
	}
	
	private TouchHandler(){
	}

	private static volatile boolean inited = false, enabled = false;

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

	public static void enable(){
		enabled = true;
	}
	
	public static void disable(){
		enabled = false;
	}
	
	public static void main(String[] args) throws Exception {
		
		addOnTouchListener(new OnTouchListener() {
			@Override
			public void onUpdate(double tx, double ty, int tid) {
				System.out.println("From Java: X: " + tx + " Y: " + ty + " ID: " + tid);
			}
			
			@Override
			public void onTouch(double tx, double ty, int tid){
				System.out.println("Touch ID: " + tid)
			}
			
			@Override
			public void onRelease(int tid){
				System.out.println("On release id: " + tid);
			}
		});
		
		start(4 /* replace with your event device number */ );
		
		while(true)
			Thread.sleep(50); //keeps thread alive. Use CTRL-C to kill the program
	}

	public native void init(int devNum);

	protected static HashMap<Integer, Long> touchTimes = new HashMap<>(10);
	private static Object ttLock = new Object();
	
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
	
	protected static synchronized void syncTTPut(int k, long v) {
		synchronized (ttLock) {
			touchTimes.put(k, v);
		}
	}
	
	protected static synchronized long syncTTGet(int k) {
		synchronized (ttLock) {
			return touchTimes.get(k);
		}
	}

}
