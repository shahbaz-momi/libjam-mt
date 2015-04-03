package com.asdev.libjam.mt;

public class SmartTouch implements Runnable {

	public static final long LOOP_DELAY = (long)(1000.0 / 60.0), TOUCH_TIMEOUT_NS = 10 /* 1 ms in ns */ * 1000000;
	
	/**
	*	Makes sure the release function when touch expires.
	*/
	@Override
	public void run() {
		while(true){
			
			int execS = System.nanoTime();
			for(int i = 0; i < 10; i ++){
				long val = TouchHandler.syncTTGet(i);
				if(Math.abs(execS - val) >= TOUCH_TIMEOUT_NS){
					for(OnTouchListener o : TouchHandler.getListeners())
						o.onRelease(i);
					TouchHandler.syncTTPut(i, -1);
				}
			}
			
			try {
				Thread.sleep(LOOP_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
