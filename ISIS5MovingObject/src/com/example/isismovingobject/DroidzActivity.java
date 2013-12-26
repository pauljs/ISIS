package com.example.isismovingobject;

//import com.example.isismovingobject.GestureStuff.GestureListener;
//import com.example.isismovingobject.GestureStuff.PlayAreaView;

import com.example.isismovingobject.model.Droid;
import com.example.isismovingobject.model.components.Speed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

public class DroidzActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static final String TAG = DroidzActivity.class.getSimpleName();
	private static final String DEBUG_TAG = "GestureFunActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set our MainGamePanel as the View
        MainGamePanel mainG = new MainGamePanel(this);
        setContentView(mainG);
        Log.d(TAG, "View added");
        
        
//        FrameLayout frame = (FrameLayout) findViewById(R.id.graphics_holder);
//        PlayAreaView image = new PlayAreaView(this);
//        frame.addView(mainG);
    }

	@Override
	protected void onDestroy() {
		Log.d(TAG, "Destroying...");
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stopping...");
		super.onStop();
	}
	
	/**
	 * @author impaler
	 * This is the main surface that handles the ontouch events and draws
	 * the image to the screen.
	 */
	public class MainGamePanel extends SurfaceView implements
			SurfaceHolder.Callback {

		public final String TAG = MainGamePanel.class.getSimpleName();
		private static final String DEBUG_TAG = "GestureFunActivity";
		
		private MainThread thread;
		private Droid droid;
		
		private GestureDetector gestures;
	    private Matrix translate;

	    private Bitmap blue1;
	    private Bitmap blue2;

	    private Matrix animateStart;
	    private Interpolator animateInterpolator;
	    private long startTime;
	    private long endTime;
	    private float totalAnimDx;
	    private float totalAnimDy;

		public MainGamePanel(Context context) {
			super(context);
			// adding the callback (this) to the surface holder to intercept events
			getHolder().addCallback(this);

			// create droid and load bitmap
			droid = new Droid(BitmapFactory.decodeResource(getResources(), R.drawable.blueball), 50, 50, getWidth(), getHeight());
			
			// create the game loop thread
			thread = new MainThread(getHolder(), MainGamePanel.this);
			
			// make the GamePanel focusable so it can handle events
			setFocusable(true);
			
			translate = new Matrix();
			gestures = new GestureDetector(DroidzActivity.this,
	                new GestureListener(this));
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// at this point the surface is created and
			// we can safely start the game loop
			thread.setRunning(true);
			thread.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "Surface is being destroyed");
			// tell the thread to shut down and wait for it to finish
			// this is a clean shutdown
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {
					// try again shutting down the thread
				}
			}
			Log.d(TAG, "Thread was shut down cleanly");
		}
		
		
		@Override
        protected void onDraw(Canvas canvas) {
             Log.v(DEBUG_TAG, "onDraw");
             if(canvas != null) {
            	 Log.v(DEBUG_TAG, "onDrawIf");
            	 canvas.drawBitmap(droid.getBitmap(), translate, null);
                 Matrix m = canvas.getMatrix();
             } else {
            	 Log.v(DEBUG_TAG, "onDrawElse");
             }
            
            //Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
            //Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());
        }
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// delegating event handling to the droid
				droid.handleActionDown((int)event.getX(), (int)event.getY());
				
				// check if in the lower part of the screen we exit
//				if (event.getY() > getHeight() - 50) {
//					thread.setRunning(false);
//					((Activity)getContext()).finish();
//				} else {
					Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
//				}
			} if (event.getAction() == MotionEvent.ACTION_MOVE) {
				// the gestures
				if (droid.isTouched()) {
					// the droid was picked up and is being dragged
					droid.setX((int)event.getX());
					droid.setY((int)event.getY());
					return gestures.onTouchEvent(event);
				}
			} 
        	if (event.getAction() == MotionEvent.ACTION_UP) {
				// touch was released
				if (droid.isTouched()) {
					droid.setTouched(false);
					return gestures.onTouchEvent(event);
				}
        	}
			
        	if(droid.isTouched()) {
        		 return gestures.onTouchEvent(event);
        	}
        	return true;
           
		}

		public void render(Canvas canvas) {
			canvas.drawColor(Color.BLACK);
			droid.draw(canvas);
		}

		/**
		 * This is the game update method. It iterates through all the objects
		 * and calls their update method if they have one or calls specific
		 * engine's update method.
		 */
		public void update() {
			// check collision with right wall if heading right
			if (droid.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
					&& droid.getX() + droid.getBitmap().getWidth() / 2 >= getWidth()) {
				droid.getSpeed().toggleXDirection();
			}
			// check collision with left wall if heading left
			if (droid.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
					&& droid.getX() - droid.getBitmap().getWidth() / 2 <= 0) {
				droid.getSpeed().toggleXDirection();
			}
			// check collision with bottom wall if heading down
			if (droid.getSpeed().getyDirection() == Speed.DIRECTION_DOWN
					&& droid.getY() + droid.getBitmap().getHeight() / 2 >= getHeight()) {
				droid.getSpeed().toggleYDirection();
			}
			// check collision with top wall if heading up
			if (droid.getSpeed().getyDirection() == Speed.DIRECTION_UP
					&& droid.getY() - droid.getBitmap().getHeight() / 2 <= 0) {
				droid.getSpeed().toggleYDirection();
			}
			// Update the lone droid
//			droid.update();
		}
		
		
		
		///////////////////////NEW STUFF/////////////////////
		
		

	    public void onAnimateMove(float dx, float dy, long duration) {
	        animateStart = new Matrix(translate);
	        animateInterpolator = new OvershootInterpolator();
	        startTime = System.currentTimeMillis();
	        endTime = startTime + duration;
	        totalAnimDx = dx;
	        totalAnimDy = dy;
	        droid.setSpeed(new Speed(totalAnimDx, totalAnimDy));
	        if(totalAnimDx >= 0) {
	        	droid.getSpeed().setxDirection(-1);
	        } else {
	        	droid.getSpeed().setxDirection(1);
	        }
	        
	        if(totalAnimDy >= 0) {
	        	droid.getSpeed().setyDirection(-1);
	        } else {
	        	droid.getSpeed().setyDirection(1);
	        }
	        
	        post(new Runnable() {
	            @Override
	            public void run() {
	                onAnimateStep();
	            }
	        });
	    }

	    private void onAnimateStep() {
	        long curTime = System.currentTimeMillis();
	        float percentTime = (float) (curTime - startTime)
	                / (float) (endTime - startTime);
	        float percentDistance = animateInterpolator
	                .getInterpolation(percentTime);
	        float curDx = percentDistance * totalAnimDx;
	        float curDy = percentDistance * totalAnimDy;
	        
	        if(droid.getSpeed().getXv() > 1) {
	        	droid.setSpeed(new Speed((float) (droid.getSpeed().getXv() * 0.5), droid.getSpeed().getYv()));
	        } else {
	        	droid.setSpeed(new Speed(1, droid.getSpeed().getYv()));
	        }
	        
	        if(droid.getSpeed().getYv() > 1) {
	        	droid.setSpeed(new Speed(droid.getSpeed().getXv(), (float) (droid.getSpeed().getYv() * 0.5)));
	        } else {
	        	droid.setSpeed(new Speed(droid.getSpeed().getXv(), 1));
	        }
	     
	      
	        
	        translate.set(animateStart);
	        onMove(curDx, curDy);

	        Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
	        if (percentTime < 1.0f) {
	            post(new Runnable() {
	                @Override
	                public void run() {
	                    onAnimateStep();
	                }
	            });
	        }
	    }

	 
		public void onMove(float dx, float dy) {
	    	Log.v(DEBUG_TAG, "onMove");
	        translate.postTranslate(dx, dy);
	        
	        invalidate();
//	        this.onDraw(MainThread.canvas);
	    }
	    
	    public void onSetLocation(float dx, float dy) {
	        translate.postTranslate(dx, dy);
	    }
		
		private class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

			MainGamePanel view;
			
			public GestureListener(MainGamePanel view) {
			    this.view = view;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
			    Log.v(DEBUG_TAG, "onDown");
			    return true;
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
			        final float velocityX, final float velocityY) {
			    Log.v(DEBUG_TAG, "onFling");
			    final float distanceTimeFactor = 0.4f;
			    final float totalDx = (distanceTimeFactor * velocityX / 2);
			    final float totalDy = (distanceTimeFactor * velocityY / 2);
			
			    view.onAnimateMove(totalDx, totalDy,
			            (long) (1000 * distanceTimeFactor));
			    return true;
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e) {
			    Log.v(DEBUG_TAG, "onDoubleTap");
//			    view.onResetLocation();
			    return true;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
			    Log.v(DEBUG_TAG, "onLongPress");
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
			        float distanceX, float distanceY) {
			    //Log.v(DEBUG_TAG, "onScroll");
			
			    view.onMove(-distanceX, -distanceY);
			    return true;
			}
			
			@Override
			public void onShowPress(MotionEvent e) {
			    Log.v(DEBUG_TAG, "onShowPress");
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
			    Log.v(DEBUG_TAG, "onSingleTapUp");
			    return false;
			}
			
			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
			    Log.v(DEBUG_TAG, "onDoubleTapEvent");
			    return false;
			}
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
			    Log.v(DEBUG_TAG, "onSingleTapConfirmed");
			    return false;
			}
			
		}

	}
}