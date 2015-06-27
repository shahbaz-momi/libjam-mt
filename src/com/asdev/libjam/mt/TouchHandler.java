package com.asdev.libjam.mt;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
*	TouchHandler class by Asdev. Call start(devNum) to start listening.
*	You may make any modifications but you must but credit to Asdev in your application.
*/
public class TouchHandler {

	private static Queue<OnTouchListener> listeners = new ConcurrentLinkedQueue<>();
 
	/**
	*	Gets the attached listeners
	*	@return the ArrayList of listeners
	*/
	public static Queue<OnTouchListener> getListeners(){
		return listeners;
	}
	
	/**
	*	Removes the specified OnTouchListener
	*	@param the listener to remove
	*/
	public static void removeOnTouchListener(OnTouchListener ot){
		listeners.remove(ot);
	}
	
	/**
	*	Adds the specified OnTouchListener
	* 	@param the OnTouchListener to add
	*/
	public static void addOnTouchListener(OnTouchListener ot){
		listeners.add(ot);
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
	public static boolean start(final int devNum){
		enabled = true;
		if(!inited){
			System.loadLibrary("jam-mt");
			Thread initThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					new TouchHandler().init(devNum); 
				}
			});
			initThread.setName("TouchHandlerThread");
			initThread.setDaemon(true);
			initThread.start();
			
			inited = true;
 
			return true;
		}else{
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
 
			}
			
			@Override
			public void onTouch(double tx, double ty, int tid){
				System.out.println("Touch ID: " + tid);
			}
			
			@Override
			public void onRelease(double tx, double ty, int tid){
				System.out.println("On release id: " + tid);
			}
		});
		
		start(Integer.parseInt(args[0]) /* replace with your event device number */ );
		
		while(true)
			Thread.sleep(50); //keeps thread alive. Use CTRL-C to kill the program
	}

	public native void init(int devNum);

	protected static volatile boolean[] touches = new boolean[10], lastTouches = new boolean[10];
	
	private static int lastFrame = -1;

	/**
	*	Called by the C library whenever an update happens. Updates will only happen if enabled.
	*/
	public static void onUpdate(double x, double y, int id, int isTouch, int frame){
		//make sure it is enabled
		if(!enabled)
			return;

		if(id == -1){
			// blank frame reported
			for(int i = 0; i < touches.length; i ++) {
				if(touches[i])
					// call on release of speced touch
					for(OnTouchListener o : listeners)
						o.onRelease(x, y, i);

				// blank the touch
				touches[i] = false;
				lastTouches[i] = false;
			}
			// end early
			return;

		}

		// check current frame
		if(frame != lastFrame){
			lastFrame = frame;
			// read the last frames and report back
			for(int i = 0; i < touches.length; i ++){
				if(touches[i] == false && lastTouches[i] == true){
					// we got a release
					for(OnTouchListener o : listeners)
						o.onRelease(x, y, i);
				}
			}

			// copy over the touches
			System.arraycopy( touches, 0, lastTouches, 0, touches.length );
			// clear the current touches
			for(int i = 0; i < touches.length; i ++)
				touches[i] = false;
		}
		touches[id] = true;

		//check if touch was active before, if wasn't call onTouch
		if(isTouch == 1)
			for(OnTouchListener o: listeners)
				o.onTouch(x, y, id);
		
		//call update of every listener
 		for(OnTouchListener o : listeners)
			o.onUpdate(x, y, id);
	}

}
