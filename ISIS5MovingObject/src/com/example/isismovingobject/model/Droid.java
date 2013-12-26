/**
 * 
 */
package com.example.isismovingobject.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.example.isismovingobject.MainThread2;
import com.example.isismovingobject.model.components.Speed;


/**
 * This is a test droid that is dragged, dropped, moved, smashed against
 * the wall and done other terrible things with.
 * Wait till it gets a weapon!
 * 
 * @author impaler
 *
 */
public class Droid {

	private Bitmap bitmap;	// the actual bitmap
	private float x;			// the X coordinate
	private float y;			// the Y coordinate
	private boolean touched;	// if droid is touched/picked up
	private Speed speed;	// the speed with its directions
	private MainThread2 thread; // thread to make sure it stays on screen
	private int widthOfScreen;
	private int heightOfScreen;
	private Matrix translate;
	private float prevDx;
	private float prevDy;
	
	public Droid(Bitmap bitmap, int x, int y, int widthOfScreen, int heightOfScreen) {
		this.bitmap = bitmap;
		this.x = x;
		this.y = y;
		this.translate = new Matrix();
		getMatrix().postTranslate(-50, -50);
        getMatrix().postTranslate(x, y);
        this.touched = false;
        
        this.prevDx = 0;
        this.prevDy = 0;
		
		this.widthOfScreen = widthOfScreen;
		this.heightOfScreen = heightOfScreen;
		
		this.speed = new Speed();
//		thread = new MainThread2(this, widthOfScreen, heightOfScreen);
//        thread.setRunning(true);
//        thread.start();
//        Log.d("starting thread", "starting thread");
	}
	
	
	public float getPrevDx() {
		return this.prevDx;
	}
	
	public float getPrevDy() {
		return this.prevDy;
	}
	
	public void setPrevDx(float num) {
		this.prevDx = num;
	}
	
	public void setPrevDy(float num) {
		this.prevDy = num;
	}
	
	public Matrix getMatrix() {
		return this.translate;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public float getX() {
		return x;
	}
	public void setX(float f) {
		this.x = f;
	}
	public float getY() {
		return y;
	}
	public void setY(float f) {
		this.y = f;
	}

	public boolean isTouched() {
		return touched;
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}
	
	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}

	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
	}

	/**
	 * Method which updates the droid's internal state every tick
	 */
	public void update() {
		if (!touched) {
			Log.d("updating", "updating");
			x += (speed.getXv() * speed.getxDirection()); 
			y += (speed.getYv() * speed.getyDirection());
		}
	}
	
	
	/**
	 * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the 
	 * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
	 * @param eventX - the event's X coordinate
	 * @param eventY - the event's Y coordinate
	 */
	public void handleActionDown(int eventX, int eventY) {
		if (eventX >= (x - bitmap.getWidth() / 2) && (eventX <= (x + bitmap.getWidth()/2))) {
			if (eventY >= (y - bitmap.getHeight() / 2) && (y <= (y + bitmap.getHeight() / 2))) {
				// droid touched
				setTouched(true);
			} else {
				setTouched(false);
			}
		} else {
			setTouched(false);
		}

	}
}
