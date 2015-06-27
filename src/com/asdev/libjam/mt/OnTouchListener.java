package com.asdev.libjam.mt;

public interface OnTouchListener {
	
	/**
	*	Called whenever an update occurs.
	* 	@param tx the x coordinate of the touch
	*	@param ty the y coordinate of the touch
	*	@param tid the id of the touch
	*/
	public void onUpdate(double tx, double ty, int tid);
	/**
	*	Called whenever the user touches the surface.
	* 	@param tx the x coordinate of the touch
	*	@param ty the y coordinate of the touch
	*	@param tid the id of the touch
	*/
	public void onTouch(double tx, double ty, int tid);
	/**
	*	Called whenever the user releases a touch.
	*	@param tid the id of the touch
	*/
	public void onRelease(double tx, double ty, int tid);
	
}
