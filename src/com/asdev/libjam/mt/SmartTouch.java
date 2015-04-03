package com.asdev.libjam.mt;

public class SmartTouch implements Runnable {

	public static final long LOOP_DELAY = (long)(1000.0 / 60.0);
	
	@Override
	public void run() {
		while(true){
			
			for(int i = 0; i < 10; i ++){
				
			}
			
			try {
				Thread.sleep(LOOP_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
