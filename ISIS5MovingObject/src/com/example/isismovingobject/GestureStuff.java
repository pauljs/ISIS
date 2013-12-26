/*
 * Copyright (c) 2010, Lauren Darcey and Shane Conder
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of 
 *   conditions and the following disclaimer.
 *   
 * * Redistributions in binary form must reproduce the above copyright notice, this list 
 *   of conditions and the following disclaimer in the documentation and/or other 
 *   materials provided with the distribution.
 *   
 * * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific prior 
 *   written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * <ORGANIZATION> = Mamlambo
 */
package com.example.isismovingobject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.isismovingobject.model.Droid;
import com.example.isismovingobject.model.movingBitmap;

public class GestureStuff extends Activity {
    private static final String DEBUG_TAG = "GestureFunActivity";
    PlayAreaView image;
    Intent restartIntent;
    FrameLayout frame;
    int widthOfScreen = 50;
    int heightOfScreen = 50;
    ArrayList<Droid> droidsOnScreen;
    boolean firstCreated = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_gesturestuff);

        
        droidsOnScreen = new ArrayList<Droid>();
        frame = (FrameLayout) findViewById(R.id.graphics_holder);
        image = new PlayAreaView(this, 100, 100);
        frame.addView(image);
        restartIntent = new Intent(this, GestureStuff.class);
        
     // create the game loop thread
//		thread = new MainThread(getHolder(), this);
    }

    public class PlayAreaView extends View {

        private GestureDetector gestures;
        private Matrix translate;
        private Bitmap droid;
        private Droid bitmap;
        private int bitmapsIndex;
        private Droid droid2;
        private ArrayList<Droid> droids;
        private Bitmap blue1;
        private Bitmap blue2;

        private Matrix animateStart;
        private Interpolator animateInterpolator;
        private long startTime;
        private long endTime;
        private float totalAnimDx;
        private float totalAnimDy;
        
        private MainThread2 thread;
        int startingPosition = 50;
        float currentX;
        float currentY;
        boolean newDroid = false;
        boolean tapped = false;
        boolean tappedConfirmed = false;

        float finalX;
        float finalY;
        public void onAnimateMove(float dx, float dy, long duration, MotionEvent e2) {
            animateStart = new Matrix(bitmap.getMatrix());
            animateInterpolator = new OvershootInterpolator();
            startTime = System.currentTimeMillis();
            endTime = startTime + duration;
            totalAnimDx = dx;
            totalAnimDy = dy;
            finalX = (int)e2.getX() + totalAnimDx;
            finalY = (int)e2.getY() + totalAnimDy;
            
            if(finalX < bitmap.getBitmap().getWidth()) {
            	finalX = bitmap.getBitmap().getWidth();
            	totalAnimDx = finalX - (int)e2.getX();
            }
            
            if(finalX > getWidth() - bitmap.getBitmap().getWidth()) {
            	finalX = getWidth() - bitmap.getBitmap().getWidth();
            	totalAnimDx = finalX - (int)e2.getX();
            }
            
            if(finalY < bitmap.getBitmap().getHeight()) {
            	finalY = bitmap.getBitmap().getHeight();
            	totalAnimDy = finalY - (int)e2.getY();
            }
            
            if(finalY > getHeight() - bitmap.getBitmap().getHeight()) {
            	finalY = getHeight() - bitmap.getBitmap().getHeight();
            	totalAnimDy = finalY - (int)e2.getY();
            }
            
            currentX = bitmap.getX();
            currentY = bitmap.getY();
//            bitmap.setX((int)e2.getX() + totalAnimDx);//THESE WILL GO ONCE I FIGURE OUT EACH STEP
//			bitmap.setY((int)e2.getY() + totalAnimDy);//THESE WILL GO ONCE I FIGURE OUT EACH STEP
            post(new Runnable() {
                @Override
                public void run() {
                    onAnimateStep();
                }
            });
        }

        
        boolean goingDown = false;
        private void onAnimateStep() {
            long curTime = System.currentTimeMillis();
            float percentTime = (float) (curTime - startTime)
                    / (float) (endTime - startTime);
            float percentDistance = animateInterpolator
                    .getInterpolation(percentTime);
            
//            if(bitmap.getX() <= bitmap.getBitmap().getWidth())
            
            float curDx = percentDistance * totalAnimDx;
            float curDy = percentDistance * totalAnimDy;
            bitmap.getMatrix().set(animateStart);
            onMove(curDx, curDy);
            
//            bitmap.setX(bitmap.getX() + curDx);
//          bitmap.setY(bitmap.getY() + curDy);
//          translate.set(animateStart);
//          onMove(curDx, curDy);
          
//          onAnimateStepCheckSides();

            
          if(percentDistance > 1.01f) {
          	goingDown = true;
          } 
          if (goingDown && percentDistance < 1.01f) {
          	goingDown = false;
          	currentX = finalX;
          	currentY = finalY;
          	bitmap.setX(currentX);
  			bitmap.setY(currentY);
          	Log.d("goingDown x/y", (currentX) + "/" + (currentY));
          } else {
              
//            Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
            if (percentTime < 1.0f) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        onAnimateStep();
                    }
                });
            } 
          }
          

//            if(percentDistance > 1.01f) {
//            	goingDown = true;
//            } 
//            if (goingDown && percentDistance < 1.01f) {
//            	goingDown = false;
//            	currentX = finalX;
//            	currentY = finalY;
//            	bitmap.setX(currentX);
//    			bitmap.setY(currentY);
//            	Log.d("goingDown x/y", (currentX) + "/" + (currentY));
//            }
        }

		private void onAnimateStepCheckSides() {
			if(currentX + bitmap.getBitmap().getWidth() / 2 >= getWidth()) {
			   	//bitmap.setX(getWidth());
			  	Log.d("bitmap.getX() + bitmap.getBitmap().getWidth() / 2 >= getWidth()", bitmap.getX() + bitmap.getBitmap().getWidth() / 2 + ">=" + getWidth());
			   	onResetLocation(1);
//          	bitmap = new Droid(blue1, 400, 400, getWidth(), getHeight());
			  	
//           	return;
			    
			   } else if(currentX - bitmap.getBitmap().getWidth() / 2 <= 0) {
			  	 Log.d("bitmap.getX() - bitmap.getBitmap().getWidth() / 2 <= 0", bitmap.getX() - bitmap.getBitmap().getWidth() / 2 + "<=" + 0);
			  	 onResetLocation(2);
//           	bitmap.setX(0);
//           	bitmap = new Droid(droid, 50, (int) bitmap.getY(), getWidth(), getHeight());
//           	return;
			   } else {
//           	bitmap.setX(newX);
//          	 translate.set(animateStart);
//               onMove(curDx, curDy);
//             //Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
//               if (percentTime < 1.0f) {
//                   post(new Runnable() {
//                       @Override
//                       public void run() {
//                           onAnimateStep();
//                       }
//                   });
//               }
			   }
			   
			   if(currentY + bitmap.getBitmap().getHeight() / 2 >= getHeight()) {
			  	 Log.d("bitmap.getY() + bitmap.getBitmap().getHeight() / 2 >= getHeight()", bitmap.getY() + bitmap.getBitmap().getHeight() / 2 + ">=" + getHeight());
			  	onResetLocation(3);
			  	 //bitmap.setX(getWidth());
//           	bitmap = new Droid(droid, (int) bitmap.getX(), getHeight() - 50, getWidth(), getHeight());
////           	onMove()
//           	return;
			   } else if(currentY - bitmap.getBitmap().getHeight() / 2 <= 0) {
			  	 Log.d("bitmap.getY() - bitmap.getBitmap().getHeight() / 2 <= 0", bitmap.getY() - bitmap.getBitmap().getHeight() / 2 + "<=" + getHeight());
			  	onResetLocation(4);
			  	 //           	bitmap.setX(0);
//           	bitmap = new Droid(droid, (int) bitmap.getX(), 50, getWidth(), getHeight());
//           	return;
			   } else {
//           	bitmap.setY(newY);
//          	 translate.set(animateStart);
//               onMove(curDx, curDy);
//             //Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
//               if (percentTime < 1.0f) {
//                   post(new Runnable() {
//                       @Override
//                       public void run() {
//                           onAnimateStep();
//                       }
//                   });
//               }
			   }
		}

//        float prevDx = 0;
//        float prevDy = 0;
        public void onMove(float dx, float dy) {
        	float prevDx = bitmap.getPrevDx();
            float prevDy = bitmap.getPrevDy();
            bitmap.getMatrix().postTranslate(dx, dy);
            currentX += dx - prevDx;
            currentY += dy - prevDy;
            Log.d("On moves current x = ", currentX + "");
            Log.d("On moves current y = ", currentY + "");
            bitmap.setX(currentX);
			bitmap.setY(currentY);
            bitmap.setPrevDx(dx);
           	bitmap.setPrevDy(dy);
//            Log.d("onMove x/y", dx + "/" + dy);
//            Log.d("onMove x/y", (currentX) + "/" + (currentY));
//            Log.d("Bitmap's x = " + bitmap.getX(), "Bitmap's y = " + bitmap.getY());
            
            invalidate();
        }

        public void onResetLocation(int loc) {
//        	bitmap.setX(startingPosition);
//        	bitmap.setY(startingPosition);
        	
//            translate.reset();
//            invalidate();
            int RIGHT = 1;
            int LEFT = 2;
            int BOTTOM = 3;
            int TOP = 4;
            Handler handler = new Handler();
            Runnable r = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
				}
			};
			postDelayed(r, 1000 * 1);
            
			if(loc == 0){ // loc == 0
            	firstCreated = true;
            	droidsOnScreen.clear();
            	droids.clear();
            	startActivity(restartIntent);
            	finish();
            } else {
            	if(loc == RIGHT) {
                	Log.d("Fell off right", "Fell off right");
                	
//                	createdOnFling((this.getWidth() - (2 * bitmap.getBitmap().getWidth())) - bitmap.getX(), totalAnimDy);
                	
//                	onMove(-(bitmap.getX() - this.getWidth() - bitmap.getBitmap().getWidth()), bitmap.getPrevDy());
                	
//                	image = new PlayAreaView(getContext(), getWidth() - bitmap.getBitmap().getWidth(), (int) bitmap.getY());
//                	image = new PlayAreaView(getContext(), 50, 50);
                	
//                    bitmap.getMatrix().reset();
//                    invalidate();
                	
                } else if(loc == LEFT) {
                	Log.d("Fell off LEFT", "Fell off LEFT");
                	
//                	createdOnFling(((2 * bitmap.getBitmap().getWidth())) - bitmap.getX(), totalAnimDy);
//                	onMove((bitmap.getBitmap().getWidth()), bitmap.getPrevDy());
                	
//                	image = new PlayAreaView(getContext(), bitmap.getBitmap().getWidth(), (int) bitmap.getY());
//                	image = new PlayAreaView(getContext(), 50, 50);
                	
                	
                } else if(loc == TOP) {
                	Log.d("Fell off TOP", "Fell off TOP");
//                	createdOnFling(totalAnimDx, ((2 * bitmap.getBitmap().getHeight())) - bitmap.getY());
                	
//                	onMove(bitmap.getPrevDx(), bitmap.getBitmap().getHeight());
                	
//                	image = new PlayAreaView(getContext(), (int) bitmap.getX(), bitmap.getBitmap().getHeight()); 
//                	image = new PlayAreaView(getContext(), 50, 50);
                	
                	
                } else if(loc == BOTTOM) {
                	Log.d("Fell off DOWN", "Fell off DOWN");
//                	createdOnFling(totalAnimDx, (this.getHeight() - (2 * bitmap.getBitmap().getHeight())) - bitmap.getY());
                	
//                	onMove(bitmap.getPrevDx(), this.getHeight() - bitmap.getBitmap().getHeight());
                	
//                	image = new PlayAreaView(getContext(), (int) bitmap.getX(), getHeight() - bitmap.getBitmap().getHeight());
//                	image = new PlayAreaView(getContext(), 50, 50);
                	
                	
                } 
                
//                frame.removeAllViews();
//                droidsOnScreen.clear();
//                for(int i = 0; i < droids.size(); i++) {
//                	droidsOnScreen.add(droids.get(i));
//                }
////            	droidsOnScreen = droids;
//            	droids.clear();
//                frame.addView(image);

            }
			
            
            
        }
        
        
        public boolean createdOnFling(final float velocityX, final float velocityY) {
            Log.v(DEBUG_TAG, "onFling");
//            for(int i = 0; i < droids.size(); i++)
//            	bitmap = droids.get(i);
//	            if(bitmap.isTouched()) {
		            final float distanceTimeFactor = 0.4f;
		            final float totalDx = (distanceTimeFactor * velocityX / 2);
		            final float totalDy = (distanceTimeFactor * velocityY / 2);
		
//		            createdOnAnimateMove(totalDx, totalDy,
//		                    (long) (1000 * distanceTimeFactor), bitmap.getX(), bitmap.getY());
//	            }
            return true;
        }
        
        public void createdOnAnimateMove(float dx, float dy, long duration, float lastX, float lastY) {
        	animateStart = new Matrix(bitmap.getMatrix());
            animateInterpolator = new OvershootInterpolator();
            startTime = System.currentTimeMillis();
            endTime = startTime + duration;
            totalAnimDx = dx;
            totalAnimDy = dy;
            finalX = lastX + totalAnimDx;
            finalY = lastY + totalAnimDy;
            currentX = bitmap.getX();
            currentY = bitmap.getY();
//            bitmap.setX((int)e2.getX() + totalAnimDx);//THESE WILL GO ONCE I FIGURE OUT EACH STEP
//			bitmap.setY((int)e2.getY() + totalAnimDy);//THESE WILL GO ONCE I FIGURE OUT EACH STEP
            post(new Runnable() {
                @Override
                public void run() {
                    createdOnAnimateStep();
                }
            });
        }
        
        
        boolean createdGoingDown = false;
        private void createdOnAnimateStep() {
            long curTime = System.currentTimeMillis();
            float percentTime = (float) (curTime - startTime)
                    / (float) (endTime - startTime);
            float percentDistance = animateInterpolator
                    .getInterpolation(percentTime);
            float curDx = percentDistance * totalAnimDx;
            float curDy = percentDistance * totalAnimDy;
            bitmap.getMatrix().set(animateStart);
            onMove(curDx, curDy);
            
//            bitmap.setX(bitmap.getX() + curDx);
//          bitmap.setY(bitmap.getY() + curDy);
//          translate.set(animateStart);
//          onMove(curDx, curDy);
          
//          onAnimateStepCheckSides();

            

//            Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
//            if(!inBounds()) {
            
	            if (percentTime < 1.0f) {
	                post(new Runnable() {
	                    @Override
	                    public void run() {
	                        onAnimateStep();
	                    }
	                });
	            } 
//            } 
        else {
            	createdGoingDown = true;
            }
            
            if(percentDistance > 1.01f) {
            	createdGoingDown = true;
            } 
            if (createdGoingDown && percentDistance < 1.01f) {
            	createdGoingDown = false;
            	currentX = finalX;
            	currentY = finalY;
            	bitmap.setX(currentX);
    			bitmap.setY(currentY);
            	Log.d("goingDown x/y", (currentX) + "/" + (currentY));
            }
        }
        
        public boolean inBounds(MotionEvent event) {        	
        	return event.getX() < getWidth() - bitmap.getBitmap().getWidth() && event.getX() > bitmap.getBitmap().getWidth() && event.getY() < getHeight() - bitmap.getBitmap().getHeight() && event.getY() > 2* bitmap.getBitmap().getHeight();
        }
        

        public void onSetLocation(float dx, float dy) {
            bitmap.getMatrix().postTranslate(dx, dy);
        }

        public PlayAreaView(Context context, int startingPositionX, int startingPositionY) {
            super(context);
//            translate = new Matrix(); // for some reason this starts at 50 50!
//            translate.postTranslate(-50, -50);
//            translate.postTranslate(startingPositionX, startingPositionY);
            gestures = new GestureDetector(GestureStuff.this,
                    new GestureListener(this));
            
            droid = BitmapFactory.decodeResource(getResources(),
                    R.drawable.blueball);
            droids = new ArrayList<Droid>();
            for(int i = 0; i < droidsOnScreen.size(); i++) {
            	droids.add(droidsOnScreen.get(i));
            }
//            droids = droidsOnScreen;
//            if(firstCreated) {
            Log.d("first android created", "added");
	            bitmap = new Droid(droid, startingPositionX, startingPositionY, getWidth(), getHeight());
	            droids.add(bitmap);
	            bitmapsIndex = 0;
//            } 
//            else {
//            	bitmap = droids.get(bitmapsIndex);
//            }
//            bitmap.getMatrix().postTranslate(-50, -50);
//            bitmap.getMatrix().postTranslate(startingPositionX, startingPositionY);
            currentX = droids.get(0).getX();
            currentY = droids.get(0).getY();
//            Log.d("onCreate x/y", (currentX) + "/" + (currentY));
            blue1 = BitmapFactory.decodeResource(getResources(),
                    R.drawable.bluetoothpic);
            blue2 = BitmapFactory.decodeResource(getResources(),
                    R.drawable.blueball);
            
            widthOfScreen = getWidth();
            heightOfScreen = getHeight();
            invalidate();
    
        }

        @Override
        protected void onDraw(Canvas canvas) {
//             Log.v(DEBUG_TAG, "onDraw");
//             if(newDroid) {
////            	 newDroid = false;
////            	 Matrix translate2 = new Matrix();
//            	 canvas.drawBitmap(droid2.getBitmap(), droid2.getMatrix(), null);
//             }
//        	canvas.drawBitmap(bitmap.getBitmap(), bitmap.getMatrix(), null);
             for(int i = 0; i < droids.size(); i++) {
//     			bitmap = droids.get(i);
     			canvas.drawBitmap(droids.get(i).getBitmap(), droids.get(i).getMatrix(), null);
             }
//             canvas.drawBitmap(droids.get(0).getBitmap(), droids.get(0).getMatrix(), null);
            
//            Log.d("onCreate x/y", (currentX) + "/" + (currentY));
            Matrix m = canvas.getMatrix();
            
//            onAnimateStepCheckSides();
            
//            if(currentX >= getWidth()) {
//            	Log.d("Fell off right", "Fell off right");
//            	onMove(this.getWidth() - bitmap.getBitmap().getWidth(), bitmap.getPrevDy());
//            	
////            	image = new PlayAreaView(getContext(), getWidth() - bitmap.getBitmap().getWidth(), (int) bitmap.getY());
//////            	image = new PlayAreaView(getContext(), 50, 50);
////            	droidsOnScreen.clear();
////                for(int i = 0; i < droids.size(); i++) {
////                	droidsOnScreen.add(droids.get(i));
////                }
////                droids.clear();
////            	frame.removeAllViews();
//                
////            	droidsOnScreen = droids;
//            	
////                frame.addView(image);
////                bitmap.getMatrix().reset();
////                invalidate();
//            	
//            } else if(currentX <= 0) {
//            	Log.d("Fell off LEFT", "Fell off LEFT");
//            	image = new PlayAreaView(getContext(), bitmap.getBitmap().getWidth(), (int) bitmap.getY());
////            	image = new PlayAreaView(getContext(), 50, 50);
//            	frame.removeAllViews();
//                frame.addView(image);
//            	
//            } else if(currentY <= 0) {
//            	Log.d("Fell off TOP", "Fell off TOP");
//            	image = new PlayAreaView(getContext(), (int) bitmap.getX(), bitmap.getBitmap().getHeight()); 
////            	image = new PlayAreaView(getContext(), 50, 50);
//            	frame.removeAllViews();
//                frame.addView(image);
//            	
//            } else if(currentY >= getHeight()) {
//            	Log.d("Fell off DOWN", "Fell off DOWN");
//            	image = new PlayAreaView(getContext(), (int) bitmap.getX(), getHeight() - bitmap.getBitmap().getHeight());
////            	image = new PlayAreaView(getContext(), 50, 50);
//            	frame.removeAllViews();
//                frame.addView(image);
//            }
            
            //Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
            //Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());
        }

        @Override
        public synchronized boolean onTouchEvent(MotionEvent event) {
        	if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// delegating event handling to the droid
        		Log.d("action down", "action down");
        		return gestures.onTouchEvent(event);
//        		gestures.onTouchEvent(event); // looks for a tap
//        		Droid tmp = bitmap;
//        		bitmap.handleActionDown((int)event.getX(), (int)event.getY());
//        		if(tapped && tappedConfirmed) {
//        			//do nothing
//        			tapped = tappedConfirmed = false;
//        		} else if (tapped) {
//        			Toast.makeText(GestureStuff.this, "Tap Not Confirmed", Toast.LENGTH_SHORT);
//        			tapped = false;
//        		} else {
//        			if(!bitmap.isTouched()); {
//	        			Log.d("leaving bitmap's final x / y = ", this.currentX + " / " + this.currentY);
//	        			Log.d("leaving bitmap's final x / y = ", bitmap.getX() + " / " + bitmap.getY());
//		        		for(int i = droids.size() - 1; i >= 0; i--) {
//			        			droids.get(i).handleActionDown((int)event.getX(), (int)event.getY());
//			        			if(droids.get(i).isTouched()) {
//			        				bitmap = droids.get(i);
//			        				currentX = bitmap.getX();
//			        				currentY = bitmap.getY();
//			        				Log.d("different droid touched", "different droid touched");
//			        				break;
//			        			}
//		        		}
//	        		}
//        		
//        		
//        		}
	        		
//        	} else {
//    			tapped = false;
//    		}
				
        	}
        	if (event.getAction() == MotionEvent.ACTION_MOVE) {
				// the gestures
//        		for(int i = 0; i < droids.size(); i++) {
//        			bitmap = droids.get(i);
//					if (bitmap.isTouched()) {
//						// the droid was picked up and is being dragged
//						bitmap.setX((int)event.getX());
//						bitmap.setY((int)event.getY());
//						currentX = bitmap.getX();
//						currentY = bitmap.getY();
						return gestures.onTouchEvent(event);
//					}
//        		}
			} 
        	if (event.getAction() == MotionEvent.ACTION_UP) {
				// touch was released
//        		for(int i = 0; i < droids.size(); i++) {
//        			bitmap = droids.get(i);
        			Log.d("actionup", "actionup");
					if (bitmap.isTouched()) {
						currentX = event.getX();
						currentY = event.getY();
						bitmap.setX(currentX);
						bitmap.setY(currentY);
						gestures.onTouchEvent(event);
						bitmap.setTouched(false);
					}
					
//	        		for(int i = 0; i < droids.size(); i++) {
//	        			droids.get(i).setTouched(false);
//	        		}
//        		}
        	}
			
//        	if(bitmap.isTouched()) {
        		 return gestures.onTouchEvent(event);
//        	}
//        	return true;
           
        }
        
        /**
		 * This is the game update method. It iterates through all the objects
		 * and calls their update method if they have one or calls specific
		 * engine's update method.
		 */
//		public void update() {
//			// check collision with right wall if heading rights
//			if (bitmap.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
//					&& bitmap.getX() + bitmap.getBitmap().getWidth() / 2 >= getWidth()) {
//				bitmap.getSpeed().toggleXDirection();
//			}
//			// check collision with left wall if heading left
//			if (bitmap.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
//					&& bitmap.getX() - bitmap.getBitmap().getWidth() / 2 <= 0) {
//				bitmap.getSpeed().toggleXDirection();
//			}
//			// check collision with bottom wall if heading down
//			if (bitmap.getSpeed().getyDirection() == Speed.DIRECTION_DOWN
//					&& bitmap.getY() + bitmap.getBitmap().getHeight() / 2 >= getHeight()) {
//				bitmap.getSpeed().toggleYDirection();
//			}
//			// check collision with top wall if heading up
//			if (bitmap.getSpeed().getyDirection() == Speed.DIRECTION_UP
//					&& bitmap.getY() - bitmap.getBitmap().getHeight() / 2 <= 0) {
//				bitmap.getSpeed().toggleYDirection();
//			}
//			// Update the lone droid
////			droid.update();
//		}
        	
//    	public void handleActionDown(int eventX, int eventY) {
//    		if (eventX >= (x - bitmap.getWidth() / 2) && (eventX <= (x + bitmap.getWidth()/2))) {
//    			if (eventY >= (y - bitmap.getHeight() / 2) && (y <= (y + bitmap.getHeight() / 2))) {
//    				// droid touched
//    				setTouched(true);
//    			} else {
//    				setTouched(false);
//    			}
//    		} else {
//    			setTouched(false);
//    		}
//
//    	}

    

	    private class GestureListener implements GestureDetector.OnGestureListener,
	            GestureDetector.OnDoubleTapListener {
	
	        PlayAreaView view;
	
	        public GestureListener(PlayAreaView view) {
	            this.view = view;
	        }
	
	        @Override
	        public synchronized boolean onDown(MotionEvent event) {
	            Log.v(DEBUG_TAG, "onDown");
//	            gestures.onTouchEvent(event); // looks for a tap
//        		Droid tmp = bitmap;
        		bitmap.handleActionDown((int)event.getX(), (int)event.getY());
//        		if(tapped && tappedConfirmed) {
//        			//do nothing
//        			Log.d("tapped and tappedConfirmed", "tapped and tappedConfirmed");
//        			tapped = tappedConfirmed = false;
//        		} else if (tapped) {
//        			Log.d("tapped not confirmed", "tapped not confirmed");
//        			Toast.makeText(GestureStuff.this, "Tap Not Confirmed", Toast.LENGTH_SHORT);
//        			tapped = false;
//        		} else {
        			if(!bitmap.isTouched()); {
	        			Log.d("leaving bitmap's final x / y = ", currentX + " / " + currentY);
	        			Log.d("leaving bitmap's final x / y = ", bitmap.getX() + " / " + bitmap.getY());
		        		for(int i = droids.size() - 1; i >= 0; i--) {
			        			droids.get(i).handleActionDown((int)event.getX(), (int)event.getY());
			        			if(droids.get(i).isTouched()) {
			        				bitmap = droids.get(i);
			        				currentX = bitmap.getX();
			        				currentY = bitmap.getY();
			        				Log.d("different droid touched", "different droid touched");
			        				bitmapsIndex = i;
			        				break;
			        			}
		        		}
	        		}
        		
        		
//        		}
	            return true;
	        }
	        
	        
	
	        @Override
	        public synchronized boolean onFling(MotionEvent e1, MotionEvent e2,
	                final float velocityX, final float velocityY) {
	            Log.v(DEBUG_TAG, "onFling");
//	            for(int i = 0; i < droids.size(); i++)
//	            	bitmap = droids.get(i);
	            
	            for(int i = 0; i < droids.size(); i++) {
	     			Log.d(i + "'s x and y", droids.get(i).getX() + " / " + droids.get(i).getY());
	             }
	            
	            
		            if(bitmap.isTouched()) {
//		            	Log.d("velocityX ", "" + velocityX)
			            final float distanceTimeFactor = 0.4f;
			            final float totalDx = (distanceTimeFactor * velocityX / 2);
			            final float totalDy = (distanceTimeFactor * velocityY / 2);
			
			            view.onAnimateMove(totalDx, totalDy,
			                    (long) (1000 * distanceTimeFactor), e2);
		            }
	            return true;
	        }
	
	        @Override
	        public boolean onDoubleTap(MotionEvent e) {
	            Log.v(DEBUG_TAG, "onDoubleTap");
	            view.onResetLocation(0); // 0 means restarting whole thing
	            return true;
	        }
	
	        @Override
	        public void onLongPress(MotionEvent e) {
	        	Bitmap bitmapBlah = BitmapFactory.decodeResource(getResources(), R.drawable.bluetoothpic);
	        	Thread t1 = new Thread(new movingBitmap(bitmapBlah, (int) e.getX(), (int) e.getY(), widthOfScreen, heightOfScreen));
	        	t1.start();
	            Log.v(DEBUG_TAG, "onLongPress");
	            
	        	
	        }
	
	        @Override
	        public synchronized boolean onScroll(MotionEvent e1, MotionEvent e2,
	                float distanceX, float distanceY) {
//	            Log.v(DEBUG_TAG, "onScroll");
	        	
	        	if (bitmap.isTouched() ) {//&& inBounds(e2)
					// the droid was picked up and is being dragged
					bitmap.setX((int)e2.getX());
					bitmap.setY((int)e2.getY());
					currentX = bitmap.getX();
					currentY = bitmap.getY();
//					return gestures.onTouchEvent(e2);
				}
	        	
	        	if(bitmap.isTouched() ) { //&& inBounds(e2)
	            	view.onMove(-distanceX, -distanceY);
	        	}
	            return true;
	        }
	
	        @Override
	        public void onShowPress(MotionEvent e) {
	            Log.v(DEBUG_TAG, "onShowPress");
	        }
	
	        @Override
	        public boolean onSingleTapUp(MotionEvent e) {
	            Log.v(DEBUG_TAG, "onSingleTapUp");
	            tapped = true;
	            
	            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
	                    R.drawable.blueball);
//	            newDroid = true;
	            droid2 = new Droid(bitmap2, (int) e.getX(), (int) e.getY(), getWidth(), getHeight());
	            Droid droid3 = new Droid(bitmap2, (int) e.getX(), (int) e.getY(), getWidth(), getHeight());
	            droids.add(droid3);
	            tappedConfirmed = true;
	            bitmap.setTouched(false);
//	            bitmap = droids.get(droids.size() - 1);
//	            bitmap.setTouched(true);
	            invalidate();
	            
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
//	            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
//	                    R.drawable.blueball);
////	            newDroid = true;
//	            droid2 = new Droid(bitmap2, (int) e.getX(), (int) e.getY(), getWidth(), getHeight());
//	            Droid droid3 = new Droid(bitmap2, (int) e.getX(), (int) e.getY(), getWidth(), getHeight());
//	            droids.add(droid3);
//	            tappedConfirmed = true;
//	            bitmap.setTouched(false);
//	            bitmap = droids.get(droids.size() - 1);
//	            invalidate();
	            return false;
	        }
	
	    }
    }
}