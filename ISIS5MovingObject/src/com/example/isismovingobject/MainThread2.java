/**
 * 
 */
package com.example.isismovingobject;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.isismovingobject.DroidzActivity.MainGamePanel;
import com.example.isismovingobject.GestureStuff.PlayAreaView;
import com.example.isismovingobject.model.Droid;
import com.example.isismovingobject.model.components.Speed;


/**
 * @author impaler
 *
 * The Main thread which contains the game loop. The thread must have access to 
 * the surface view and holder to trigger events every game tick.
 */
public class MainThread2 extends Thread {
	
	private static final String TAG = MainThread2.class.getSimpleName();

	// Surface holder that can access the physical surface
	private SurfaceHolder surfaceHolder;
	// The actual view that handles inputs
	// and draws to the surface
	private MainGamePanel gamePanel;
	private PlayAreaView playAreaView;
	private Droid bitmap;
	
	private int widthOfScreen;
	private int heightOfScreen;

	// flag to hold game state 
	private boolean running;
	public void setRunning(boolean running) {
		this.running = running;
	}

	public MainThread2(Droid bitmap, int widthOfScreen, int heightOfScreen) { //SurfaceHolder surfaceHolder, MainGamePanel gamePanel
		super();
		this.bitmap = bitmap;
		this.widthOfScreen = widthOfScreen;
		this.heightOfScreen = heightOfScreen;
//		this.playAreaView = playAreaView;
//		this.surfaceHolder = surfaceHolder;
//		this.gamePanel = gamePanel;
	}

	@Override
	public void run() {
		Canvas canvas;
		Log.d(TAG, "Starting game loop");
		while (running) {
			canvas = null;
			// try locking the canvas for exclusive pixel editing
			// in the surface
			try {
//				canvas = this.surfaceHolder.lockCanvas();
//				synchronized (surfaceHolder) {
					// update game state 
					this.update();
					// render state to the screen
					// draws the canvas on the panel
//					this.gamePanel.render(canvas);				
//				}
			} finally {
				// in case of an exception the surface is not left in 
				// an inconsistent state
//				if (canvas != null) {
//					surfaceHolder.unlockCanvasAndPost(canvas);
//				}
			}	// end finally
		}
	}
	
	public void update() {
		// check collision with right wall if heading rights
		if (bitmap.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
				&& bitmap.getX() + bitmap.getBitmap().getWidth() / 2 >= widthOfScreen) {
			bitmap.getSpeed().toggleXDirection();
		}
		// check collision with left wall if heading left
		if (bitmap.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
				&& bitmap.getX() - bitmap.getBitmap().getWidth() / 2 <= 0) {
			bitmap.getSpeed().toggleXDirection();
		}
		// check collision with bottom wall if heading down
		if (bitmap.getSpeed().getyDirection() == Speed.DIRECTION_DOWN
				&& bitmap.getY() + bitmap.getBitmap().getHeight() / 2 >= heightOfScreen) {
			bitmap.getSpeed().toggleYDirection();
		}
		// check collision with top wall if heading up
		if (bitmap.getSpeed().getyDirection() == Speed.DIRECTION_UP
				&& bitmap.getY() - bitmap.getBitmap().getHeight() / 2 <= 0) {
			bitmap.getSpeed().toggleYDirection();
		}
		// Update the lone droid
//		droid.update();
	}
	
}
