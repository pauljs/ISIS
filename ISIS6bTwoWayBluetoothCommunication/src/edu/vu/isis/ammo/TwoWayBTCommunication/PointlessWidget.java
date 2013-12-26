package edu.vu.isis.ammo.TwoWayBTCommunication;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import edu.vu.isis.ammo.TwoWayBTCommunication.R;
import edu.vu.isis.ammo.TwoWayBTCommunication.provider.SensordemoSchema.ZephyrTableSchema;


// This is the activity in charge of updating the widget and where the main changes in the widget occur
@SuppressLint("NewApi")
public class PointlessWidget extends AppWidgetProvider implements LoaderCallbacks<Cursor>{

	
	
	
	// The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int LOADER_ID = 0;

    // The callbacks through which we will interact with the LoaderManager.
    @SuppressLint("NewApi")
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    // The adapter that binds our data to the ListView
    private SimpleCursorAdapter mAdapter;
    
    
    
    
  private static final String LOG = "de.vogella.android.widget.example";
  public BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
  Button connectDisconnectBT;
  
  //Button screenWakeToggleBtn;
  Context widgetContext;
  
  @Override
public void onDeleted(Context context, int[] appWidgetIds) {
	// TODO Auto-generated method stub
//	  ScreenWake screen = new ScreenWake(context);
	  
	super.onDeleted(context, appWidgetIds);
}
  
  
  // getting all the ids is necessary in case multiple versions of the same widget are being used
  // for instance one widget on lock screen and another widget on your main screen
  // Also remoteviews is the main way I use in changing what the widget looks like
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
	  super.onUpdate(context, appWidgetManager, appWidgetIds);
	  queryDB(0, 0, context);
	  
	// Get all ids
	    ComponentName thisWidget = new ComponentName(context,
	        PointlessWidget.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
	    for (int widgetId : allWidgetIds) {
			// Register an onClickListener
	    	
			  RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
			          R.layout.widget);
//		      Intent intent = new Intent(context, ScreenWakeToggleActivity.class);
//				
//		      PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(),
//		          0, intent, 0);
//		      remoteViews.setOnClickPendingIntent(R.id.screenWakeToggleBtn, pendingIntent);
		      
		      Intent intent = new Intent(context, UpdateWidgetHelper.class);
		      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		      intent.putExtra("isFromWidget", true);
		      PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		      remoteViews.setOnClickPendingIntent(R.id.bwidgetOpen, pendingIntent);
		      
		      appWidgetManager.updateAppWidget(widgetId, remoteViews);
		      
		      
		      
	    } 

	  }
	  
  
  // This was going to be used to access the database to get the most recent data but now i just use
  // remoteviews in the zephyr main activity
  private Number queryDB(long start, long end, Context context) {
		Log.d("queryDB", "queryDB");
		
		long endTime = System.currentTimeMillis() - ((2L * 60L) * 1000L); //CHANGED END TIME TO - ....
		
		String[] projection = {ZephyrTableSchema.INSTANT_PULSE, ZephyrTableSchema.MEASUREMENT_TIME};
		//String selection = ZephyrTableSchema.MEASUREMENT_TYPE + "=?"  
		String selection = ZephyrTableSchema.MEASUREMENT_TYPE + "=? and " + ZephyrTableSchema.MEASUREMENT_TIME + ">?"; 

		//String[] selectionArgs = {String.valueOf(ZephyrTableSchema.MEASUREMENT_TYPE_PULSE)} ; 
		String[] selectionArgs = {String.valueOf(ZephyrTableSchema.MEASUREMENT_TYPE_PULSE), String.valueOf(endTime)};  //CHANGED START TO ENDTIME

		String sortOrder = null;
		sortOrder = ZephyrTableSchema.MEASUREMENT_TIME + " DESC";

		// TODO: catch exceptions or PASS ALONG CONTEXT
		Log.d("before cursor", "before cursor");
		Cursor c = context.getContentResolver().query(ZephyrTableSchema.CONTENT_URI, 
				projection, selection, selectionArgs, sortOrder);
		Log.d("after cursor", "after cursor");
		
		//Log.d(TAG, "queryDB - after query");
		//Log.d(TAG, "queryDB: cursor rows = " + String.valueOf(c.getCount()));

		// TODO: catch exceptions
		//Long[] numbers = new Long[2 * c.getCount()];
//		ArrayList<Number> num2 = new ArrayList<Number>();
		
		// TODO: catch exceptions
		int n = 0;
//		while (c.moveToFirst()) {
//			n++;
//			long timestamp = c.getLong(c.getColumnIndex(ZephyrTableSchema.MEASUREMENT_TIME)) ;
//			int pulseVal = Math.abs(c.getInt(c.getColumnIndex(ZephyrTableSchema.INSTANT_PULSE)));
//			//Log.d(TAG, "     row " + String.valueOf(n) + "  " + String.valueOf(pulseVal) + "  " + String.valueOf(timestamp));
//			
//			num2.add(timestamp);
//			num2.add(pulseVal);
		Log.d("before moveToFirst", "before moveToFirst");
		c.moveToFirst();
		Log.d("after moveToFirst", "after moveToFirst");
//		if (c.getCount() > 0) {
//			Log.d("after c!=null", "after c!=null");
//			int pulseVal = Math.abs(c.getInt(c.getColumnIndex(ZephyrTableSchema.INSTANT_PULSE)));
//			Log.d("after pulseVal def", "after pulseVal def");
//			c.close();
//			return pulseVal;
//		} else {
//			Toast.makeText(context, "You must connect Zephyr via Bluetooth", Toast.LENGTH_SHORT).show();
			c.close();
			return null; //Arbitrary Value to return since nothing in cursor
//		}
		
		
  }
  
  
  
  
  
  
  
  
  
// This is not used
@Override
public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
	// TODO Auto-generated method stub
	long endTime = System.currentTimeMillis() - ((2L * 60L) * 1000L); // last 2 min
	String[] projection = {ZephyrTableSchema.INSTANT_PULSE, ZephyrTableSchema.MEASUREMENT_TIME};
	//String selection = ZephyrTableSchema.MEASUREMENT_TYPE + "=?"  
	String selection = ZephyrTableSchema.MEASUREMENT_TYPE + "=? and " + ZephyrTableSchema.MEASUREMENT_TIME + ">?"; 

	//String[] selectionArgs = {String.valueOf(ZephyrTableSchema.MEASUREMENT_TYPE_PULSE)} ; 
	String[] selectionArgs = {String.valueOf(ZephyrTableSchema.MEASUREMENT_TYPE_PULSE), String.valueOf(endTime)};  //CHANGED START TO ENDTIME

	String sortOrder = null;
	sortOrder = ZephyrTableSchema.MEASUREMENT_TIME + " DESC";
	
	return new CursorLoader(widgetContext, ZephyrTableSchema.CONTENT_URI,
            projection, selection, selectionArgs, sortOrder);
	
	
	
	
}


// This is not used
@Override
public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
	// TODO Auto-generated method stub
	// A switch-case is useful when dealing with multiple Loaders/IDs
    switch (loader.getId()) {
    case LOADER_ID:
        // The asynchronous load is complete and the data is now
        // available for use. Only now can we associate the
        // queried Cursor with the SimpleCursorAdapter.
        mAdapter.swapCursor(cursor);
        break;
    }
    // The listview now displays the queried data.
}



// This is not used
@Override
public void onLoaderReset(Loader<Cursor> arg0) {
	// TODO Auto-generated method stub
	
}
		
//		}
		
		//Log.d(TAG, "queryDB: arraylist size = " + String.valueOf( num2.size() ));
		
		// Close the cursor
//		c.close();
//		
//		return num2;
	}
  
  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  //STUFF WITH SERVICE STARTS HERE
	  
////	  if(btAdapter.isEnabled()) {
//		  Log.w(LOG, "onUpdate method called");
//		    // Get all ids
//		    ComponentName thisWidget = new ComponentName(context,
//		        PointlessWidget.class);
//		    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
//		
//		    // Build the intent to call the service
//		    Intent intent = new Intent(context.getApplicationContext(),
//		        UpdateWidgetService.class);
//		    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
//		
//		    // Update the widgets via the service
//		    context.startService(intent);
////	  } else {
////		  Intent turnOnBluetoothIntent = new Intent(this, bluetoothScanner.class);
////		  startActivity(turnOnBluetoothIntent);
////	  }
//	    
//  }
