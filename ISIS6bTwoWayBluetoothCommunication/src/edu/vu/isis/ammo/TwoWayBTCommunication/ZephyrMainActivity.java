package edu.vu.isis.ammo.TwoWayBTCommunication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import edu.vu.isis.ammo.TwoWayBTCommunication.R;
import edu.vu.isis.ammo.TwoWayBTCommunication.UpdateWidgetHelper.UpdateTextType;
import edu.vu.isis.ammo.api.AmmoRequest;

import edu.vu.isis.ammo.TwoWayBTCommunication.provider.SensordemoSchema.ZephyrTableSchema;
//import edu.vu.isis.ammo.biosensor.demo.R;
//import android.R.*;
//import android.app.Activity;
//import android.os.Bundle;
//import android.view.View.OnClickListener;

// TODO: catch exceptions
// e.g. IOException can be thrown by Bluetooth

public class ZephyrMainActivity extends Activity implements Parcelable{
    /** Called when the activity is first created. */
	BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	static BTClient _bt = null;
	ZephyrProtocol _protocol;
	NewConnectedListener _NConnListener;
	private final int HEART_RATE = 0x100;
	private final int INSTANT_SPEED = 0x101;
	
	private static final String TAG = "ZephyrMain";
	
	public AmmoRequest.Builder ab;
	
	Button backToListViewBtn;
	Button keepScreenOnOffBtn;
	Button refreshConnectionBtn;
	Button bwidgetOpenBtn;
	SharedPreferences preferences;
	TextView labelHeartRate;
	TextView labelStatusMsg;
	TextView labelInstantSpeed;
//	BluetoothAdapter btAdapter = null;
	ProgressBar connectingProgressBar;
	public enum UpdateTextType {
		  HEARTRATE, SPEED, CONNECTIVITY
	}
	boolean isReceiving = false;
	boolean isOkToReturn = false;
	
	//SharedPreferences preferences = getPreferences(MODE_PRIVATE);
	
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		
        
        
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2a = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
		IntentFilter filter4 = new IntentFilter(scanModeChanged);
		
		this.registerReceiver(bluetoothStateReceiver, filter4);

		this.registerReceiver(connectivityReceiver, filter4);
        this.registerReceiver(connectivityReceiver, filter1);
        this.registerReceiver(connectivityReceiver, filter2a);
        this.registerReceiver(connectivityReceiver, filter3);
        
        bwidgetOpenBtn = (Button) findViewById(R.id.bwidgetOpen);
        backToListViewBtn = (Button) findViewById(R.id.backToListViewBtn);
        keepScreenOnOffBtn = (Button) findViewById(R.id.keepScreenOnBtn);
        refreshConnectionBtn = (Button) findViewById(R.id.refreshConnectionBtn);
        refreshConnectionBtn.setVisibility(View.INVISIBLE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        labelHeartRate = (TextView)findViewById(R.id.labelHeartRate);
        labelInstantSpeed = (TextView)findViewById(R.id.labelInstantSpeed);
        labelStatusMsg = (TextView) findViewById(R.id.labelStatusMsg);
        connectingProgressBar = (ProgressBar) findViewById(R.id.connectingProgressBar);
        
        
        
        boolean isFromWidget = this.getIntent().getExtras().getBoolean("isFromWidget");
        if(isFromWidget && this.getIntent().getExtras().getBoolean("isDisconnecting")) {
//        	if(!savedInstanceState.getParcelableArrayList("_bt").get(0).equals(null)) {
//            	Log.d("_bt not equal to null!", "_bt not equal to null!");
//            	_bt = (BTClient) savedInstanceState.getParcelableArrayList("_bt").get(0);
//            } else {
//            	Log.d("_bt equal to null!", "_bt equal to null!");
//            }
//    		stopBluetoothConnection();
    		Log.d("from widget isDisconnecting disabling adapter", "from widget isDisconnecting disabling adapter");
    		labelStatusMsg.setText("Disconnected from Zephyr");
    		backToListViewBtn.setText("Back");
    		connectingProgressBar.setVisibility(View.INVISIBLE);
    		refreshConnectionBtn.setText("Connect");
    		refreshConnectionBtn.setVisibility(View.VISIBLE);
    		isOkToReturn = true;
    		isFromWidget = false;
//    		adapter.disable();
//    		Handler handler = new Handler();
//			Runnable r = new Runnable() {
//				public void run() {
//					finish();
//				}
//			};
//			handler.postDelayed(r, 1000 * 0);        		
        } else {
        
	        /*Sending a message to android that we are going to initiate a pairing request*/
	        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
	        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
	       this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
	        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
	        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
	       this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);
	        
	       // Create Ammo builder
	       
	       try {
				this.ab = AmmoRequest.newBuilder(this);
			} catch (Throwable e) {
				Log.e(TAG, "no ammo today: " + e.toString());
				Toast.makeText(this, "Ammo initialization failed", Toast.LENGTH_SHORT).show();
			}
	       
	       // Ammo subscription
	    		try {
	    			Log.d(TAG, "SUBSCRIBING");
	    		    this.ab.provider(ZephyrTableSchema.CONTENT_URI)
	    			.topic(ZephyrTableSchema.CONTENT_TOPIC)
	    			.subscribe();
	    		} catch (RemoteException e) {
	    		    Log.e(TAG, "error creating subscription: " + e);
	    		}
	       
	       
	       
	      //Obtaining the handle to act on the CONNECT button
	        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
			//String ErrorText  = "Not Connected to HxM ! !";
	//        String ErrorText  = "Connected to HxM ! !";
	//		 tv.setText(ErrorText);
	        tv.setText("Connecting...");
	
	        Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
	        btnConnect.setVisibility(View.INVISIBLE);
	
	        String BhMacID = "00:07:80:9D:8A:E8";
	        //String BhMacID = "00:07:80:88:F6:BF";
	        
	        BluetoothAsyncTask task = new BluetoothAsyncTask();
	        task.execute();
	//        adapter = BluetoothAdapter.getDefaultAdapter();
	//        
	//        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
	//        
	//        if (pairedDevices.size() > 0) 
	//        {
	//        	for (BluetoothDevice device : pairedDevices) 
	//        	{
	//        		if (device.getName().startsWith("HXM")) 
	//        		{
	//        			Log.d("device received from bonded devices", "device receved from bonded devices");
	//        			BluetoothDevice btDevice = device;
	//        			BhMacID = btDevice.getAddress();
	//        			break;
	//        			
	//        		}
	//        	}
	//                        
	//                        
	//        }
	//        			
	//        //BhMacID = btDevice.getAddress();
	//        BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
	//        String DeviceName = Device.getName();
	//        _bt = new BTClient(adapter, BhMacID);
	//        _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
	//        _bt.addConnectedEventListener(_NConnListener);
	//        			
	//        TextView labelHeartRate = (EditText)findViewById(R.id.labelHeartRate);
	//        labelHeartRate.setText("000");
	//        			
	//        labelHeartRate = (EditText)findViewById(R.id.labelInstantSpeed);
	//        labelHeartRate.setText("0.0");
	//        
	//        //labelHeartRate = 	(EditText)findViewById(R.id.labelSkinTemp);
	//        //labelHeartRate.setText("0.0");
	//        			 
	//        //labelHeartRate = 	(EditText)findViewById(R.id.labelPosture);
	//        //labelHeartRate.setText("000");
	//        			 
	//        //labelHeartRate = 	(EditText)findViewById(R.id.labelPeakAcc);
	//        //labelHeartRate.setText("0.0");
	//        if(_bt.IsConnected())
	//        {
	//        	_bt.start();
	//        	TextView labelStatusMsg = (TextView) findViewById(R.id.labelStatusMsg);
	//        	String ErrorText2  = "Connected to HxM "+DeviceName;
	//        	labelStatusMsg.setText(ErrorText2);
	//        	
	//        	//Reset all the values to 0s
	//        	
	//        }
	//        else
	//        {
	//        	TextView labelStatusMsg = (TextView) findViewById(R.id.labelStatusMsg);
	//        	String ErrorText3  = "Unable to Connect !";
	//        	labelStatusMsg.setText(ErrorText3);
	//        }

	        /*Obtaining the handle to act on the DISCONNECT button*/
	        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        }

    }
    
    BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() { 
		
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("BluetoothStateReceiver" + action, "BluetoothStateReceiver " + action);
			if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				Log.d("ABluetoothState is " + state, "ABluetoothState is " + state);
				switch(state) {
					case(BluetoothAdapter.STATE_TURNING_ON) :
					{
						Log.d("ABluetoothState is BluetoothAdapter.STATE_TURNING_ON", "ABluetoothState is BluetoothAdapter.STATE_TURNING_ON");
	//					toastText = "Bluetooth turning on";
	//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
						break;
					}
					case(BluetoothAdapter.STATE_ON) :
					{
						Log.d("BluetoothState is BluetoothAdapter.STATE_ON", "BluetoothState is BluetoothAdapter.STATE_ON");
	//					toastText = "Bluetooth on";
	//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
	//					setupUI();
						break;
					}
					case(BluetoothAdapter.STATE_TURNING_OFF) :
					{
						Log.d("BluetoothState is BluetoothAdapter.STATE_TURNING_OFF", "BluetoothState is BluetoothAdapter.STATE_TURNING_OFF");
	//					toastText = "Bluetooth turning off";
	//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
						break;
					}
					case(BluetoothAdapter.STATE_OFF) :
					{
						Log.d("BluetoothState is BluetoothAdapter.STATE_OFF", "BluetoothState is BluetoothAdapter.STATE_OFF");
	//					toastText = "Bluetooth off";
	//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
	//					setupUI();
						break;
					}
				} 
			} else if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
						final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
						Log.d("BBluetoothStateACTIONSCAN is " + state, "BBluetoothStateACTIONSCAN is " + state);
						switch(state) {
							case(BluetoothAdapter.ERROR) :
							{
//								showBTErrorToast();
								Log.d("getVisibility int = " + refreshConnectionBtn.getVisibility(), "VIEW.INVISIBLE int =" + View.INVISIBLE);
								if(refreshConnectionBtn.getVisibility() == View.INVISIBLE) {
									Log.d("ref. btn. is invisible", "yep");
//									stopBluetoothConnection();
								}
								
							}
							case(BluetoothAdapter.STATE_TURNING_ON) :
							{
								Log.d("BBluetoothState is BluetoothAdapter.STATE_TURNING_ON", "BBluetoothState is BluetoothAdapter.STATE_TURNING_ON");
//								showBTErrorToast();
								if(refreshConnectionBtn.getVisibility() == View.INVISIBLE) {
									Log.d("ref. btn. is invisible", "yep");
//									stopBluetoothConnection();
									adapter = BluetoothAdapter.getDefaultAdapter();
//									adapter.disable();
								}
								
			//					toastText = "Bluetooth turning on";
			//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
								break;
							}
							case(BluetoothAdapter.STATE_ON) :
							{
								Log.d("BluetoothState is BluetoothAdapter.STATE_ON", "BluetoothState is BluetoothAdapter.STATE_ON");
			//					toastText = "Bluetooth on";
			//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
			//					setupUI();
								break;
							}
							case(BluetoothAdapter.STATE_TURNING_OFF) :
							{
								Log.d("BluetoothState is BluetoothAdapter.STATE_TURNING_OFF", "BluetoothState is BluetoothAdapter.STATE_TURNING_OFF");
			//					toastText = "Bluetooth turning off";
			//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
								break;
							}
							case(BluetoothAdapter.STATE_OFF) :
							{
								Log.d("BluetoothState is BluetoothAdapter.STATE_OFF", "BluetoothState is BluetoothAdapter.STATE_OFF");
			//					toastText = "Bluetooth off";
			//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
			//					setupUI();
								break;
							}
						}
				}
//			}
//			String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE; // get info when state change
//			int prevState = intent.getIntExtra(prevStateExtra, -1);
//			Log.d("BluetoothState was " + prevState, "BluetoothState was " + prevState);
//			String stateExtra = BluetoothAdapter.EXTRA_STATE;
//			int state = intent.getIntExtra(stateExtra, -1);
//			Log.d("BluetoothState is " + state, "BluetoothState is " + state);
			
			
		}		
	}; 
	
	private void showBTErrorToast() {
		Toast.makeText(this, "Manual Bluetooth Disconnect Occurred. If Bluetooth no longer works restart your phone to enable it.", Toast.LENGTH_LONG).show();
	}
    
    
    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("Broadcast action = " + action, "Broadcast action = " + action);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            	Log.d("The action is " + BluetoothAdapter.ACTION_SCAN_MODE_CHANGED, "The action is " + BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            	if(isReceiving) {
            		Log.d("stopBluetoothConnection after Scan mode change", "stopBluetoothConnection after Scan mode change");
//            		stopBluetoothConnection();
//            		adapter.enable();
            		adapter = null;
            		isReceiving = false;
            	}
            } else if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
            	Log.d("it worked huzzahzah", "it worked huzzahzah");
            	updateViaBroadcastReceiver("Disconnect");
            } else if(action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
            	Log.d("it worked huzzahzah2", "it worked huzzahzah2");
            	labelStatusMsg.setText("Disconnected from Zephyr");
            	backToListViewBtn.setText("Back");
            	refreshConnectionBtn.setText("Connect");
            	refreshConnectionBtn.setVisibility(View.VISIBLE);
            	isOkToReturn = true;
            	updateViaBroadcastReceiver("Connect");
//            	returnToListView();
//            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//               //Device found
//            	Log.d("Broadcast action = " + action, "Broadcast action = " + action);
//            }
            } else if ((BluetoothAdapter.STATE_CONNECTED + "").equals(action)) {
               //Device is now connected
            	Log.d("Broadcast action = " + action, "Broadcast action = " + action);
            } else if(action.equals("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED")) {
            	Log.d("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED", "android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
            }
//            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//               //Done searching
//            	Log.d("Broadcast action = " + action, "Broadcast action = " + action);
//            }
////            else if (BluetoothAdapter.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
////               //Device is about to disconnect
////            	Log.d("Broadcast action = " + action, "Broadcast action = " + action);
////            }
//            else if ((BluetoothAdapter.STATE_DISCONNECTED + "").equals(action)) {
//               //Device has disconnected
//            	Log.d("Broadcast action = " + action, "Broadcast action = " + action);
//            }           
        }		
    };
    
    private void updateViaBroadcastReceiver(String connectivity) {
//		RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget);
//		updateViews.setTextViewText(R.id.bwidgetOpen, connectivity);
		updateWidget(connectivity, UpdateTextType.CONNECTIVITY);
	}
    
    public void btnDisconnectOnClick(View v) {
    	
    	/*
    	TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
		String ErrorText  = "Disconnected from HxM!";
		tv.setText(ErrorText);
		Intent i = new Intent(this, listViewBTDevices.class);
		startActivity(i);
		_bt.removeConnectedEventListener(_NConnListener);
		
		// Close the communication with the device & throw an exception if failure
		_bt.Close();
		*/
    }
    
    public void backToListViewBtnOnClick(View v) {
    	returnToListView();
    }
    
    public void keepScreenOnOffBtnOnClick(View v) {
    	keepScreenOnOffPartitioning(true);
    }

	private void keepScreenOnOffPartitioning(Boolean isFromBtnClick) {
		keepScreenOnOffBtn = (Button) findViewById(R.id.keepScreenOnBtn);
		SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(this);
		if(isFromBtnClick) {
			Log.d("Toggle Screen Wake isFromBtnClick", "Toggle Screen Wake isFromBtnClick");
			Editor edit = manager.edit();
			if(manager.getBoolean("screen_wake_toggle_checkbox", false)) {				
				edit.putBoolean("screen_wake_toggle_checkbox", false);				
			} else {
				edit.putBoolean("screen_wake_toggle_checkbox", true);
			}
			edit.commit();
		}
		
		if(manager.getBoolean("screen_wake_toggle_checkbox", false)) {
	    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, RESULT_OK);
	    	keepScreenOnOffBtn.setText("Disable Screen\nWake");
    		Log.d("Screen Wake is On", "Screen wake is On");
	  		Toast.makeText(this, "Screen will remain on until screen wake toggle disabled or screen view changed", Toast.LENGTH_LONG).show();
	      } else{
	    	  Toast.makeText(this, "Screen wake toggle turned off", Toast.LENGTH_LONG).show();
	    	  this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    	  keepScreenOnOffBtn.setText("Enable Screen\nWake");
	    	  Log.d("Screen Wake is Off", "Screen wake is Off");
	      }
		
		
//		if (keepScreenOnOffBtn.getText().toString().startsWith("Enable")){
//    		//TODO NEW ANSWER DIALOG FRAGMENT
//    		keepScreenOnOffBtn.setText("Disable Screen\nOn");
//    		Log.d("set to Disable", "set to Disable");
//    	} else {
//    		keepScreenOnOffBtn.setText("Enable Screen\nOn");
//    	}
	}
    
    public void refreshConnectionBtnOnClick(View v) {
//    	stopBluetoothConnection();
    	Intent i = new Intent(this, ZephyrMainActivity.class);
    	i.putExtra("devices", this.getIntent().getParcelableArrayListExtra("devices"));
    	startActivity(i);
    	finish();
    }
    


    public void showGraph(View view) {
    	Log.d("zephyrmain", "show graphs");
    	Intent intent = new Intent(this, GraphingActivity.class);
        startActivity(intent);
    }


    private class BTBondReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("Bond state", "BOND_STATED = " + device.getBondState());
		}
    }
    private class BTBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BTIntent", intent.getAction());
			Bundle b = intent.getExtras();
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
			try {
				BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
				Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
				byte[] pin = (byte[])m.invoke(device, "1234");
				m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
				Object result = m.invoke(device, pin);
				Log.d("BTTest", result.toString());
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    //SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
    //SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    public static String heartRatetextGlobal = "test";
    final Handler Newhandler = new Handler() {
    	public void handleMessage(Message msg)
    	{
    		Log.d("handler", "handleMessage");
    		TextView tv;
    		ContentValues cv = new ContentValues();
    		switch (msg.what)
    		{
    			case HEART_RATE:
    				adapter = BluetoothAdapter.getDefaultAdapter();
    				if(adapter.isEnabled()) {
    					connectingProgressBar.setVisibility(View.INVISIBLE);
        				String ErrorText2  = "Connected to HxM";
        				backToListViewBtn.setText("Disconnect");
        				labelStatusMsg.setText(ErrorText2);
        				String HeartRatetext = msg.getData().getString("HeartRate");
        				tv = (TextView)findViewById(R.id.labelHeartRate);
        				System.out.println("Heart Rate Info is "+ HeartRatetext);
        				if (tv != null) {
        					tv.setText(HeartRatetext);
        					updatePreferencesHR(HeartRatetext);
        					updateWidget(HeartRatetext, UpdateTextType.HEARTRATE);
        					updateWidget("Disconnect", UpdateTextType.CONNECTIVITY);
//        					updateWidgetHR(HeartRatetext);
        					isReceiving = true;
        					
        					
        					
//        					SharedPreferences preferences = getPreferences(MODE_PRIVATE);
//        					Log.d(preferences.getString("NAME", "Cant retrieve"), preferences.getString("NAME", "Cant retrieve"));
//        					SharedPreferences.Editor editor = app_preferences.edit();
//        					editor.putString("NAME", HeartRatetext);
//        					editor.commit();
        					
        					//Bundle bundle = new Bundle();
        					Log.d("b4 making a service", "same");
//        					ZephyrInfo zephyrInfo = new ZephyrInfo(msg.getData().getString("HeartRate"), msg.getData().getString("InstantSpeed"));
//        					UpdateWidgetService service = new UpdateWidgetService(zephyrInfo);
//        					service.onStart(this, startId);
        					Log.d("after making a service", "same");
        					//new WidgetAsyncTask(service, this);
//        					heartRatetextGlobal = HeartRatetext;
//        					TextView hr = (EditText) findViewById(R.id.heartRateTextView);
//        					hr.setText(HeartRatetext);
        					
        				}
        				
        				// store in content provider
        				cv.put(ZephyrTableSchema.MEASUREMENT_TYPE, ZephyrTableSchema.MEASUREMENT_TYPE_PULSE);
        	    		cv.put(ZephyrTableSchema.INSTANT_PULSE, Short.parseShort(HeartRatetext));
        	    		cv.put(ZephyrTableSchema.MEASUREMENT_TIME, System.currentTimeMillis());
        	    		Uri pulseUri = getContentResolver().insert(ZephyrTableSchema.CONTENT_URI, cv);
        	    		
        	    		try {
        	            	ab.provider(pulseUri).topicFromProvider().post();
        	            	Log.d(TAG, "posted to Ammo with Uri: " + pulseUri.toString());
        	            	//Toast.makeText(this, "Quick 1 message sent", Toast.LENGTH_SHORT).show();
        	            } catch (RemoteException ex) {
        	            	Log.e(TAG, "post incident failed: " + ex.toString());
        	            }
        	    		
        	    		//Log.d("handler", "handleMessage: cv = " + cv.toString());
        	    		//Log.d("handler", "handleMessage: uri = " + pulseUri.toString());
    				} else {
    					stopBluetoothConnection();
    				}
    				
    	    		
    				break;
    		
    			case INSTANT_SPEED:
    				adapter = BluetoothAdapter.getDefaultAdapter();
    				if(adapter.isEnabled()) {
    					connectingProgressBar.setVisibility(View.INVISIBLE);
        				String ErrorText3  = "Connected to HxM";
        				backToListViewBtn.setText("Disconnect");
        				labelStatusMsg.setText(ErrorText3);
        				String InstantSpeedtext = msg.getData().getString("InstantSpeed");
        				InstantSpeedtext = roundToHundrethsString(InstantSpeedtext);
        				tv = (TextView)findViewById(R.id.labelInstantSpeed);
        				if (tv != null) {
        					tv.setText(InstantSpeedtext);
        					updatePreferencesSpeed(InstantSpeedtext);
        					updateWidget(InstantSpeedtext, UpdateTextType.SPEED);
        					updateWidget("Disconnect", UpdateTextType.CONNECTIVITY);
        					isReceiving = true;
        					
        					
//        					ZephyrInfo zephyrInfo = new ZephyrInfo(msg.getData().getString("HeartRate"), msg.getData().getString("InstantSpeed"));
//        					UpdateWidgetService service = new UpdateWidgetService(zephyrInfo);
        				}
        				
        				// store in content provider
        				cv.put(ZephyrTableSchema.MEASUREMENT_TYPE, ZephyrTableSchema.MEASUREMENT_TYPE_SPEED);
        				cv.put(ZephyrTableSchema.INSTANT_SPEED, Float.parseFloat(InstantSpeedtext));
        				cv.put(ZephyrTableSchema.MEASUREMENT_TIME, System.currentTimeMillis());
        				Uri speedUri = getContentResolver().insert(ZephyrTableSchema.CONTENT_URI, cv);
        				
        				//Log.d("handler", "handleMessage: cv = " + cv.toString());
        	    		//Log.d("handler", "handleMessage: uri = " + speedUri.toString());
    				} else {
    					stopBluetoothConnection();
    				}
    				
    	    		
    				break;
    		}
    		
    	}

		private String roundToHundrethsString(String InstantSpeedtext) {
			double speed = Double.parseDouble(InstantSpeedtext);
			double speedRounded = Math.round(speed * 100.0) / 100.0;
			InstantSpeedtext = "" + speedRounded;
			return InstantSpeedtext;
		}

		

		

    };
    
    
    private void updateWidget(String textUpdate, UpdateTextType type) {
		// TODO Auto-generated method stub
    	Intent intent = new Intent(this, UpdateWidgetHelper.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	      intent.putExtra("isFromWidget", true);
	      PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	      	    
    	
		RemoteViews updateViews = buildUpdate(textUpdate, type); // Update the view using the new data.
		
		updateViews.setOnClickPendingIntent(R.id.bwidgetOpen, pendingIntent);
		
		ComponentName thisWidget = new ComponentName(this, PointlessWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		manager.updateAppWidget(thisWidget, updateViews);
	}
    
    private RemoteViews buildUpdate(String textUpdate, UpdateTextType type) {
		// TODO Auto-generated method stub
	  RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.widget);
	  Log.d("in buildUpdate", "in buildUpdate");
	  	switch (type) {
	  		case HEARTRATE:
	  			Log.d("updateViews heart", "updateViews heart");
	  			updateViews.setTextViewText(R.id.heartRateTextView, textUpdate);
	  			break;
	  		case SPEED:
	  			Log.d("updateViews speed", "updateViews speed");
	  			updateViews.setTextViewText(R.id.speedTextView, textUpdate);
	  			break;
	  		case CONNECTIVITY:
	  			Log.d("updateViews connectivity", "updateViews connectivity");
	  			updateViews.setTextViewText(R.id.bwidgetOpen, textUpdate);
	  			updatePreferencesConnectivity(textUpdate);
	  			break;	  			
	  	}			
		
	  	Log.d("returning updateViews", "returning updateViews");
		return updateViews;
	}

    private void updatePreferencesConnectivity(String connectivity) {
		// TODO Auto-generated method stub
    	Editor edit = preferences.edit();
		String username = preferences.getString("connectivity", "n/a");
		// We will just revert the current user name and save again
//		StringBuffer buffer = new StringBuffer();
//		for (int i = username.length() - 1; i >= 0; i--) {
//			buffer.append(username.charAt(i));
//		}
		//edit.putString("username", buffer.toString());
		edit.putString("connectivity", connectivity);
		edit.commit();
		// A toast is a view containing a quick little message for the
		// user. We give a little feedback
//		Toast.makeText(this,
//				"Reverted string sequence of user name.", Toast.LENGTH_SHORT)
//				.show();
	}
    
	private void updatePreferencesHR(String heartRate) {
		// TODO Auto-generated method stub
    	Editor edit = preferences.edit();
		String username = preferences.getString("username", "n/a");
		// We will just revert the current user name and save again
//		StringBuffer buffer = new StringBuffer();
//		for (int i = username.length() - 1; i >= 0; i--) {
//			buffer.append(username.charAt(i));
//		}
		//edit.putString("username", buffer.toString());
		edit.putString("username", heartRate);
		edit.commit();
		// A toast is a view containing a quick little message for the
		// user. We give a little feedback
//		Toast.makeText(this,
//				"Reverted string sequence of user name.", Toast.LENGTH_SHORT)
//				.show();
	}
    
    private void updatePreferencesSpeed(String speed) {
		// TODO Auto-generated method stub
    	Editor edit = preferences.edit();
		String username = preferences.getString("password", "n/a");
		// We will just revert the current user name and save again
//		StringBuffer buffer = new StringBuffer();
//		for (int i = username.length() - 1; i >= 0; i--) {
//			buffer.append(username.charAt(i));
//		}
		//edit.putString("username", buffer.toString());
		edit.putString("password", speed);
		edit.commit();
		// A toast is a view containing a quick little message for the
		// user. We give a little feedback
//		Toast.makeText(this,
//				"Reverted string sequence of password.", Toast.LENGTH_SHORT)
//				.show();
	}
    
    private static final int MENU_ABOUT = 1;
    private static final int MENU_OPTION_SETTINGS = 2;
    private static final int MENU_GRAPHS = 3;
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.add(0, MENU_ABOUT, 0, "About");
        menu.add(0, MENU_OPTION_SETTINGS, Menu.NONE, "Settings");
        //menu.add(0, MENU_GRAPHS, Menu.NONE, "Graphs");
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		//Log.d(TAG, "menu item " + String.valueOf(item.getItemId()));
		switch (item.getItemId()) {
			case MENU_OPTION_SETTINGS:
				launchSettingsActivity();
				break;
//			case MENU_ABOUT:
//				launchAboutActivity();
//				break;
//			case MENU_GRAPHS:
//				showGraph();
//				break;
		}
		return true;
	}
    
    
	private void launchSettingsActivity() {
        Log.d("zephyrsettings", "showing settings");
//        Intent intent = new Intent(this, ZephyrSettings.class);
//        startActivity(intent);
        Log.d(TAG, "showing settings");
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("devices", this.getIntent().getParcelableArrayListExtra("devices"));
        startActivity(intent);
    }
    
    
    
    
    
    
    
    
    
    
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK ) {
	        // do something on back.
	    	return returnToListView();
	    	
	    	
	    } else {

	    	return super.onKeyDown(keyCode, event);
	    }
	}

	private boolean returnToListView() {
		if ((isReceiving || isOkToReturn) && _bt.getComms() != null && !backToListViewBtn.getText().equals("Back")) {
			stopBluetoothConnection();
			IntentFilter filter = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"); 
			registerReceiver(connectivityReceiver, filter);
			updateWidget("Connect", UpdateTextType.CONNECTIVITY);
			Handler handler = new Handler();
			Runnable r = new Runnable() {
				public void run() {
					editSharedPreferencesScreenWake(false);
					startListViewActivity();
				}
			};
			handler.postDelayed(r, 1000 * 2);
			return true;
		} else if(isReceiving || isOkToReturn) {
			editSharedPreferencesScreenWake(false);
			startListViewActivity();
		    return true;
		} else {
			Toast.makeText(this, "Wait for connection to load before ending connnection", Toast.LENGTH_SHORT).show();
			return true;
		}
	}
	
	private void editSharedPreferencesScreenWake(Boolean turnOn) {
		SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = manager.edit();
		if(turnOn) {				
			edit.putBoolean("screen_wake_toggle_checkbox", true);	
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, RESULT_OK);
			Toast.makeText(this, "Screen will remain on until screen wake toggle disabled or screen view changed", Toast.LENGTH_LONG).show();
		} else {
			edit.putBoolean("screen_wake_toggle_checkbox", false);
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			Toast.makeText(this, "Screen wake toggle turned off", Toast.LENGTH_LONG).show();
		}
		edit.commit();
	}

	private void startListViewActivity() {
		adapter = null;
		if(!backToListViewBtn.getText().equals("Back")) {
			Toast.makeText(this, "Zephyr disconnected", Toast.LENGTH_SHORT).show();
		}		
		Intent i = new Intent(this, listViewBTDevices.class);
		i.putExtra("devices", this.getIntent().getParcelableArrayListExtra("devices"));
		startActivity(i);
		Log.d("before finish", "before finish");
		isReceiving = false;
		isOkToReturn = false;
		finish();
	}

	public static void stopBluetoothConnection() {
		if(_bt != null) {			
			Log.d("_bt!= null", "_bt != null");
			Log.d("" + _bt, "" + _bt);
//			_bt.getComms().Close();
			_bt.Close();
		} else {
			Log.d("_bt == null", "_bt == null");
			Log.d("" + _bt, "" + _bt);
			_bt.getComms().Close();
			_bt.Close();
		}
		
	}
    
    
    
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
//    	Log.d("onSaveInstance", "onSaveInstance");
//    	if(!_bt.equals(null)) {
//    		ArrayList<BTClient> arrayList = new ArrayList<BTClient>();
//    		arrayList.add(_bt);
//    		outState.putParcelableArrayList("_bt", (ArrayList<? extends Parcelable>) arrayList);
//    	}
    	super.onSaveInstanceState(outState);
    	
    	
    }
    

	@Override
    protected void onResume() {
		super.onResume();
	    adapter = BluetoothAdapter.getDefaultAdapter();
		  
		if(!adapter.isEnabled()) {
			Log.d("btAdapter not enabled on resume", "btAdapter not enabled on resume");
			updateWidget("Connect", UpdateTextType.CONNECTIVITY);
			Intent i = new Intent(this, MainActivity.class);
			i.putExtra("devices", new ArrayList<BluetoothDevice>());
			startActivity(i);
			adapter = null;
			finish();
		  }
		
		keepScreenOnOffPartitioning(false);
	      
      
//      if(!adapter.isEnabled()) {
//    	  Log.d("btAdapter not enabled on resume", "btAdapter not enabled on resume");
//    	  Intent i = new Intent(this, MainActivity.class);
//    	  startActivity(i);
//    	  adapter = null;
//    	  finish();
//      }
//      Log.d("service bind attempt", "service bind attempt");
//      bindService(new Intent(this, UpdateWidgetService.class), mConnection,
//          Context.BIND_AUTO_CREATE);
//      Log.d("bind succes", "bind success");
    }
//    
//    private UpdateWidgetService s;
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//    	@Override
//        public void onServiceConnected(ComponentName className, IBinder binder) {
//          s = ((UpdateWidgetService.MyBinder) binder).getService();
//          Log.d("onServiceConnected Called", "onServiceConnected Called");
//          Toast.makeText(ZephyrMainActivity.this, "Connected", Toast.LENGTH_LONG)
//              .show();
//        }
//
//    	@Override
//        public void onServiceDisconnected(ComponentName className) {
//    	Toast.makeText(ZephyrMainActivity.this, "Disconnected", Toast.LENGTH_LONG)
//            .show();
//          s = null;
//        }
//    	
//    	
//      };
//    
	public interface stringFactory {
		public String doSomething(String param);
	}
	
	private class BluetoothAsyncTask extends AsyncTask<Void, String, Void> {

//		private HashMap<String, TextView> commandMap = new HashMap<String, TextView>();
		
		
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
//			adapter = BluetoothAdapter.getDefaultAdapter();
	        
	        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
	        String BhMacID = "00:07:80:9D:8A:E8";
	        
	        
	        if (pairedDevices.size() > 0) 
	        {
	        	for (BluetoothDevice device : pairedDevices) 
	        	{
	        		if (device.getName().startsWith("HXM")) 
	        		{
	        			Log.d("device received from bonded devices", "device received from bonded devices");
	        			BluetoothDevice btDevice = device;
	        			BhMacID = btDevice.getAddress();
	        			break;
	        			
	        		}
	        	}
	                        
	                        
	        }
	        			
	        //BhMacID = btDevice.getAddress();
	        Log.d("before Device", "before Device");
	        BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
	        String DeviceName = Device.getName();
	        Log.d("after addConnectedEvent", "after addConnectedEvent");
	        Log.d(BhMacID, BhMacID);
	        
	        Log.d("pray", "prey");
	        _bt = new BTClient(adapter, BhMacID);
	        
	        
	        
//	        commandMap.put("000", labelHeartRate);
	        
	        
//	        onProgressUpdate("000");
//	        labelHeartRate.setText("000");
	        			
	        
//	        commandMap.put("0.0", labelInstantSpeed);
	       
	        
//	        onProgressUpdate("0.0");
//	        labelHeartRate.setText("0.0");
	        
	        //labelHeartRate = 	(EditText)findViewById(R.id.labelSkinTemp);
	        //labelHeartRate.setText("0.0");
	        			 
	        //labelHeartRate = 	(EditText)findViewById(R.id.labelPosture);
	        //labelHeartRate.setText("000");
	        			 
	        //labelHeartRate = 	(EditText)findViewById(R.id.labelPeakAcc);
	        //labelHeartRate.setText("0.0");
	        if(_bt.IsConnected())
	        {
	        	
	        	Log.d("after addConnectedEvent", "after addConnectedEvent");
		        _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
		        Log.d("after addConnectedEvent", "after addConnectedEvent");
		        _bt.addConnectedEventListener(_NConnListener);
	        	Log.d("_bt.IsConnected", "_bt.IsConnected");
	        	_bt.start();
//	        	Button bwidgetOpen = (Button) findViewById(R.id.bwidgetOpen);
//	        	bwidgetOpen.setText("Disconnect");
	        	
	        	
//	        	String ErrorText2  = "Connected to HxM";
//	        	commandMap.put(ErrorText2, labelStatusMsg);
//	        	onProgressUpdate(ErrorText2);
//	        	labelStatusMsg.setText(ErrorText2);
	        	
	        	//Reset all the values to 0s
	        	
	        }
	        else
	        {
	        	Log.d("_bt.IsNotConnected", "_bt.IsNotConnected");
	        	String ErrorText3  = "Unable to Connect!";
	        	
	        	isOkToReturn = true;
	        	isReceiving = true;
	        	
//	        	commandMap.put(ErrorText3, labelStatusMsg);
	        	onProgressUpdate(ErrorText3);
//	        	labelStatusMsg.setText(ErrorText3);
	        }
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
//			commandMap.get(values).setText(values+"");
			if (values.equals("000")) {
				labelHeartRate.setText("000");
			} else if(values.equals("0.0")) {
				labelInstantSpeed.setText("0.0");
			} else if (values.equals("Connected to HxM")) {
				labelStatusMsg.setText("Connected to HxM");
			} else if (values.equals("Unable to Connect!")) {
				labelStatusMsg.setText("Unable to Connect!");
			}
			
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			if(isReceiving) {
				backToListViewBtn.setText("Back");
				refreshConnectionBtn.setText("Refresh Connection");
				Log.d("INVISIBLE int " + refreshConnectionBtn.getVisibility(), "INVISIBLE int " + refreshConnectionBtn.getVisibility());
				refreshConnectionBtn.setVisibility(View.VISIBLE);
				Log.d("VISIBLE int " + refreshConnectionBtn.getVisibility(), "VISIBLE int " + refreshConnectionBtn.getVisibility());
				connectingProgressBar.setVisibility(View.INVISIBLE);
				labelStatusMsg.setText("Unable to Connect!");
			}
			super.onPostExecute(result);
			
		}
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
	
}


