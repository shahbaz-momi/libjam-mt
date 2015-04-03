package com.asdev.libjam.mt;

public interface OnTouchListener {
	
	public void onUpdate(double tx, double ty, int tid);
	public void onTouch(double tx, double ty, int tid);
	public void onRelease(int tid);
	
}