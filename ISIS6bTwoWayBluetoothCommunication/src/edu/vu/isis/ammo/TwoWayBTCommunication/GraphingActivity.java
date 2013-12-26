package edu.vu.isis.ammo.TwoWayBTCommunication;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.androidplot.xy.*;
import com.androidplot.series.XYSeries;

import edu.vu.isis.ammo.TwoWayBTCommunication.R;

import edu.vu.isis.ammo.TwoWayBTCommunication.provider.SensordemoSchema.ZephyrTableSchema;

import java.util.ArrayList;
import java.util.Arrays;

public class GraphingActivity extends Activity implements OnItemSelectedListener {

	private static final String TAG = "GraphingActivity";

	private static final int LAST_2_MIN = 0;
	private static final int LAST_30_MIN = 1;
	private static final int LAST_HOUR = 2;
	private static final int LAST_6_HOURS = 3;
	private static final int LAST_48_HOURS = 4;
	private static final int LAST_WEEK = 5;

	private int mCurrentInterval;

	private Spinner spinner;
	
    private ZephyrDataObserver mObserver;

	private XYPlot xyLineGraph; // declare line graph
	//private XYPlot xyBarGraph; // declare bar graph
	
	private XYSeries series5 ; 
	
	// declare foramtter classes that will be used to build the graphs
	LineAndPointFormatter lineFormat;
	BarFormatter barFormat;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graphing);
		
		Log.d(TAG, "onCreate");

		xyLineGraph = (XYPlot) findViewById(R.id.xy_line_graph);
		//xyBarGraph = (XYPlot) findViewById(R.id.xy_bar_graph);
		
		// spinner setup
		spinner = (Spinner) findViewById(R.id.timerange_spinner);
//		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//		        R.array.timerange_array, android.R.layout.simple_spinner_item);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spinner.setAdapter(adapter);
//		spinner.setOnItemSelectedListener(this);
		
		// content observer to update graph
		this.mObserver = new ZephyrDataObserver(null, this);
		getContentResolver().registerContentObserver(ZephyrTableSchema.CONTENT_URI, true, this.mObserver);
		
		mCurrentInterval = LAST_2_MIN;
		
		lineFormat = new LineAndPointFormatter(
				Color.rgb(200,0,0),
				Color.rgb(100, 0, 0),
				Color.rgb(0, 0, 150));   // or null for no fill
		
		
		setGraphSettings();
		updateGraphs(LAST_30_MIN);
		
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, LAST_2_MIN, 0, "2 minutes");
		menu.add(0, LAST_30_MIN, 0, "30 minutes");
		menu.add(0, LAST_HOUR, 0, "1 hour");
		menu.add(0, LAST_6_HOURS, 0, "6 hours");
		menu.add(0, LAST_48_HOURS, 0, "48 hours");
		menu.add(0, LAST_WEEK, 0, "1 week");
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "menu item " + String.valueOf(item.getItemId()));
		
		switch (item.getItemId()) {
			case LAST_2_MIN:
				updateGraphs(LAST_2_MIN);
				break;
			case LAST_30_MIN:
				updateGraphs(LAST_30_MIN);
				break;
			case LAST_HOUR:
				updateGraphs(LAST_HOUR);
				break;
			case LAST_6_HOURS:
				updateGraphs(LAST_6_HOURS);
				break;
			case LAST_48_HOURS:
				updateGraphs(LAST_48_HOURS);
				break;
			case LAST_WEEK:
				updateGraphs(LAST_WEEK);
				break;
		}
		return true;
	}
	*/
	
	private void updateGraphs(int interval)	{
		
		xyLineGraph.clear();

		long graphStartTime = 0L;
		double horizAxisStep = 30000.0;  //30 sec
		switch (interval) {
			case LAST_2_MIN:
				graphStartTime = System.currentTimeMillis() - ((2L * 60L) * 1000L);
				break;
			case LAST_30_MIN:
				graphStartTime = System.currentTimeMillis() - ((30L * 60L) * 1000L);
				horizAxisStep = 180000.0; //3 min
				break;
			case LAST_HOUR:
				graphStartTime = System.currentTimeMillis() - ((60L * 60L) * 1000L);
				horizAxisStep = 360000.0; //6 min
				break;
			case LAST_6_HOURS:
				graphStartTime = System.currentTimeMillis() - ((6L * 60L * 60L) * 1000L);
				horizAxisStep = 5.0 * 360000.0; //30 min
				break;
			case LAST_48_HOURS:
				graphStartTime = System.currentTimeMillis() - ((8L * 6L * 60L * 60L) * 1000L);
				horizAxisStep = 8.0 * 5.0 * 360000.0; //4 hrs
				break;
			case LAST_WEEK:
				graphStartTime = System.currentTimeMillis() - ((28L * 6L * 60L * 60L) * 1000L);
				horizAxisStep = 6.0 * 8.0 * 5.0 * 360000.0; //24 hrs
				break;
			default:
				// default display: last hour's worth of data
				graphStartTime = System.currentTimeMillis() - ((60L * 60L) * 1000L);
				break;
		}
		//Log.d(TAG, "updateGraphs: graphStartTime=" + String.valueOf(graphStartTime));
		
		series5 = new SimpleXYSeries(queryDB(graphStartTime, -1), 
				SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED,
				"Series5");
		//Log.d(TAG, "updateGraphs: series5=" + String.valueOf(series5.size()));
		
		// update horizontal-axis boundaries
		xyLineGraph.setDomainBoundaries(graphStartTime, System.currentTimeMillis(),	BoundaryMode.FIXED);
		xyLineGraph.setDomainStep(XYStepMode.INCREMENT_BY_VAL, horizAxisStep); 
		xyLineGraph.setTicksPerDomainLabel(5);
		xyLineGraph.addSeries(series5, lineFormat);

		// redraw
		xyLineGraph.redraw();
	}
	
	// set some default graph settings
	private void setGraphSettings()	{
		xyLineGraph.setBackgroundColor(Color.rgb(255,255,255));
		
		xyLineGraph.setRangeBoundaries(0, 200, BoundaryMode.FIXED) ;
		xyLineGraph.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 25);
		xyLineGraph.setTicksPerRangeLabel(1);
		xyLineGraph.setRangeLabel("instant pulse");
		
		xyLineGraph.setDomainBoundaries(System.currentTimeMillis() - 500000L, 
					System.currentTimeMillis(), 
					BoundaryMode.FIXED);
		xyLineGraph.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 50000.0);
		
		xyLineGraph.setDomainLabel("time");
		
		// disable default AndroidPlot onscreen guides
		xyLineGraph.disableAllMarkup();
		
	}
	
	private ArrayList<Number> queryDB(long start, long end) {
		Log.d(TAG, "queryDB");
		
		long endTime = System.currentTimeMillis() ;
		
		String[] projection = {ZephyrTableSchema.INSTANT_PULSE, ZephyrTableSchema.MEASUREMENT_TIME};
		//String selection = ZephyrTableSchema.MEASUREMENT_TYPE + "=?"  
		String selection = ZephyrTableSchema.MEASUREMENT_TYPE + "=? and " + ZephyrTableSchema.MEASUREMENT_TIME + ">?"; 

		//String[] selectionArgs = {String.valueOf(ZephyrTableSchema.MEASUREMENT_TYPE_PULSE)} ; 
		String[] selectionArgs = {String.valueOf(ZephyrTableSchema.MEASUREMENT_TYPE_PULSE), String.valueOf(start)};  

		String sortOrder = null; 

		// TODO: catch exceptions
		Cursor c = getContentResolver().query(ZephyrTableSchema.CONTENT_URI, 
				projection, selection, selectionArgs, sortOrder);
		
		//Log.d(TAG, "queryDB - after query");
		//Log.d(TAG, "queryDB: cursor rows = " + String.valueOf(c.getCount()));

		// TODO: catch exceptions
		//Long[] numbers = new Long[2 * c.getCount()];
		ArrayList<Number> num2 = new ArrayList<Number>();
		
		// TODO: catch exceptions
		int n = 0;
		while (c.moveToNext()) {
			n++;
			long timestamp = c.getLong(c.getColumnIndex(ZephyrTableSchema.MEASUREMENT_TIME)) ;
			int pulseVal = Math.abs(c.getInt(c.getColumnIndex(ZephyrTableSchema.INSTANT_PULSE)));
			//Log.d(TAG, "     row " + String.valueOf(n) + "  " + String.valueOf(pulseVal) + "  " + String.valueOf(timestamp));
			
			num2.add(timestamp);
			num2.add(pulseVal);
		}
		
		//Log.d(TAG, "queryDB: arraylist size = " + String.valueOf( num2.size() ));
		
		// Close the cursor
		c.close();
		
		return num2;
	}

	public void updateGraphNewData() {
		updateGraphs(mCurrentInterval);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {

		//Log.d(TAG, "item selected: " + String.valueOf(id));
		Long idl = new Long(id);
		final int elementId = idl.intValue();
		
		mCurrentInterval = elementId;
		
		switch (elementId) {
		case LAST_2_MIN:
			updateGraphs(LAST_2_MIN);
			break;
		case LAST_30_MIN:
			updateGraphs(LAST_30_MIN);
			break;
		case LAST_HOUR:
			updateGraphs(LAST_HOUR);
			break;
		case LAST_6_HOURS:
			updateGraphs(LAST_6_HOURS);
			break;
		case LAST_48_HOURS:
			updateGraphs(LAST_48_HOURS);
			break;
		case LAST_WEEK:
			updateGraphs(LAST_WEEK);
			break;
		}
    }

	@Override
    public void onNothingSelected(AdapterView<?> parent) {
        // spinner interface callback
    }
	
	/*
     * Content observer for BLOX table; enables to keep a running count of 
     * contacts sync'ed, etc.
     */
    private static class ZephyrDataObserver extends ContentObserver 
    {
	private static final String TAG = "Observer";

	private GraphingActivity callback;
	
	public ZephyrDataObserver(Handler handler, GraphingActivity aCallback) {
	    super(handler);
	    if (Log.isLoggable(TAG, Log.VERBOSE)) {
	    	Log.d(TAG, "constructor");
	    }
	    this.callback = aCallback;
	}

	@Override
	public void onChange(boolean selfChange) {
	    if (Log.isLoggable(TAG, Log.VERBOSE)) {
	    	Log.d(TAG, "onChange");
	    }
	    //this.callback.updateLastSyncInfo();
	    this.callback.updateGraphNewData();
	}
    }

}
