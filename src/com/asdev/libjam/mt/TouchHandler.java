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

	private static boolean inited = false;

	public static boolean start(int devNum){
		if(!inited){
			System.loadLibrary("jam-mt");
			new TouchHandler().init(devNum);
			inited = true;
			
			//TODO: thread for updating
			Thread thrd = new Thread(new SmartTouch());
			thrd.setDaemon(true);
			thrd.start();
			
			return true;
		}else{
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		addOnTouchListener(new OnTouchListener() {
			@Override
			public void onUpdate(double tx, double ty, int tid) {
				System.out.println("From Java: X: " + tx + " Y: " + ty + " ID: " + tid);
			}
		});
		
		start(4);
		
		while(true)
			Thread.sleep(50);
	}

	public native void init(int devNum);

	protected static HashMap<Integer, Long> touchTimes = new HashMap<>(10);
	private static Object ttLock = new Object();
	
	public static void onUpdate(double x, double y, int id){
		//update hashmap
		syncTTPut(id % 10, System.nanoTime());
		
		for(OnTouchListener o : getListeners())
			o.onUpdate(x, y, id);
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
