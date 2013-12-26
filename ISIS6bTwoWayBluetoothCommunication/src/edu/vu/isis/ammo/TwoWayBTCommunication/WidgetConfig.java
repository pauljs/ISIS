package edu.vu.isis.ammo.TwoWayBTCommunication;

import java.util.Random;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;

import edu.vu.isis.ammo.TwoWayBTCommunication.R;

// THIS CLASS IS NOT CURRENTLY USED.
// This class is used if a splash screen was being used for the widget but currently it is not!
public class WidgetConfig extends Activity implements OnClickListener { //implements OnClickListener
	
	String companyName = "Zephyr";
	EditText info;
	AppWidgetManager awm;
	Context c;
	Button b;
	int awID; //appwidgetid
	BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zephyr_bluetooth_widget_splash);
		b = (Button)findViewById(R.id.heartBtn);
		//b.setOnClickListener(this);
		c = WidgetConfig.this;
		info = (EditText)findViewById(R.id.etwidgetconfig); //all this set up all the references
		//Getting info about the widget that Launched this Activity
		Intent i = getIntent(); //an intent is opening this class
		Bundle extras = i.getExtras();
		if (extras != null){
			awID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID); 
			//getting id an make sure there is an id being passed, otherwise we 
			//cant do anything with this widget. We are here to check which widget 
			//covered this class
		} else
		{
			finish();
		}
		awm = AppWidgetManager.getInstance(c); //builds a bridge for AppWidgetManager to relate to this class
		// TODO Auto-generated method stub
//		String e = info.getText().toString();
		//Opening a new activity using the RemoteViews
//		RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget); //messing with layuout.widget
//		views.setTextViewText(R.id.tvConfigInput, "Robin's Gift");
////		
////		
//		//Set up intent we are going to open and setup PendingIntent because thats what RemoteViews and widgets like to use
//		Intent in = new Intent(c, Splash.class);
//		PendingIntent pi = PendingIntent.getActivity(c, 0, in, 0);
//		views.setOnClickPendingIntent(R.id.bwidgetOpen, pi); //can think this as setting up onClickListener but a little different
//		awm.updateAppWidget(awID, views);
//		
//		//setting a result
//		Intent result = new Intent();
//		result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID); //returning data back to the app or widget that called this class
//		setResult(RESULT_OK, result); //got to tell it everything is fine (like in Bundels). 
//		
//		finish(); //this is here because we want this activity to finish when hit the button
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//String e = info.getText().toString();
		//Opening a new activity using the RemoteViews
//		RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget); //messing with layuout.widget
//		views.setTextViewText(R.id.tvConfigInput, companyName + " Bluetooth Connection");
//		views.setTextViewText(R.id.tvwidgetUpdate, ""); //REMOVE THIS FOR OTHER!!!!!!!!!!!!!!!!
		Log.d("onClick for splash screen test", "onClick for splash screen test");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
		        .getApplicationContext());
		Log.d("onClick after appwidget manager",  "onClick for splash screen test");
		int[] allWidgetIds = this.getIntent()
		        .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		Log.d("onClick after allWidgetsIds",  "onClick after allWidgetsIds");
		    ComponentName thisWidget = new ComponentName(getApplicationContext(),
		        PointlessWidget.class);
		    Log.d("onClick after ComponentName",  "onClick after ComponentName");
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.d("onClick after allWidgetIds2",  "onClick after allWidgetIds2");
		//for (int widgetId : allWidgetIds) {
		      // Create some random data
			Log.d("onClick before Random",  "onClick before Random");
		      int number = (new Random().nextInt(100));
		      Log.d("onClick after Random",  "onClick after Random");
		      RemoteViews remoteViews = new RemoteViews(this
		          .getApplicationContext().getPackageName(),
		          R.layout.widget);
		      Log.w("WidgetExample", String.valueOf(number));
		      // Set the text
		      remoteViews.setTextViewText(R.id.heartRateTextView,
		          "Random: " + String.valueOf(number));

		      // Register an onClickListener
		      Intent clickIntent = new Intent(this.getApplicationContext(),
		          PointlessWidget.class);

		      clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		      clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
		          allWidgetIds);

		      PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
		          PendingIntent.FLAG_UPDATE_CURRENT);
		      Log.d("before setOnClickPendingIntent", "cmon");
		      remoteViews.setOnClickPendingIntent(R.id.bwidgetOpen, pendingIntent);
		      appWidgetManager.updateAppWidget(awID, remoteViews);
		      
		
		
		//Set up intent we are going to open and setup PendingIntent because thats what RemoteViews and widgets like to use
		Log.d("clicking", "clicking");
		awm.updateAppWidget(awID, remoteViews);
		
		//setting a result
		Intent result = new Intent();
		result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID); //returning data back to the app or widget that called this class
		setResult(RESULT_OK, result); //got to tell it everything is fine (like in Bundels). 
		
		finish(); //this is here because we want this activity to finish when hit the button
	//}
		//info.setText("Loading...");
//		if (btAdapter.isEnabled()) {
//			
//		} else {
//			Intent in = new Intent(c, bluetoothScanner.class); //Splash.class
//			PendingIntent pi = PendingIntent.getActivity(c, 0, in, 0);
//			Log.d("clicking2", "clicking2");
//			views.setOnClickPendingIntent(R.id.bwidgetOpen, pi); //can think this as setting up onClickListener but a little different
//			Log.d("clicking3", "clicking3");
//			awm.updateAppWidget(awID, views);
//			
//			//setting a result
//			Intent result = new Intent();
//			result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID); //returning data back to the app or widget that called this class
//			setResult(RESULT_OK, result); //got to tell it everything is fine (like in Bundels). 
//			
//			finish(); //this is here because we want this activity to finish when hit the button
//		}
//		
	}
	

}
