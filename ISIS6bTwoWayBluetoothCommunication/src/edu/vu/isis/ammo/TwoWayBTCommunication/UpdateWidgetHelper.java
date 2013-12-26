package edu.vu.isis.ammo.TwoWayBTCommunication;

import java.util.ArrayList;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;
import android.app.Activity;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import edu.vu.isis.ammo.TwoWayBTCommunication.R;
import edu.vu.isis.ammo.TwoWayBTCommunication.provider.SensordemoSchema.ZephyrTableSchema;


// This is called to connect to zephyr through widget
// it is called by the PointlessWidget Class
public class UpdateWidgetHelper extends Activity {

	protected static final int DISCOVERY_REQUEST = 1; // set to true
	private final int HEART_RATE = 0x100;
	  private final int INSTANT_SPEED = 0x101;
	  BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	  boolean isDisconnecting = false;
	  SharedPreferences preferences;
	  public enum UpdateTextType {
		  HEARTRATE, SPEED
	}
	  
	 // THIS METHOD IS NOT USED CURRENTLY
	// Broadcastreceiver to receive state changes
		BroadcastReceiver bluetoothState = new BroadcastReceiver() { 
			
			public void onReceive(Context context, Intent intent) {

				String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE; // get info when state change
				String stateExtra = BluetoothAdapter.EXTRA_STATE;
				int state = intent.getIntExtra(stateExtra, -1);

				switch(state) {
				case(BluetoothAdapter.STATE_TURNING_ON) :
				{
//					toastText = "Bluetooth turning on";
//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
					break;
				}
				case(BluetoothAdapter.STATE_ON) :
				{
//					toastText = "Bluetooth on";
//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
//					setupUI();
					break;
				}
				case(BluetoothAdapter.STATE_TURNING_OFF) :
				{
//					toastText = "Bluetooth turning off";
//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
					break;
				}
				case(BluetoothAdapter.STATE_OFF) :
				{
//					toastText = "Bluetooth off";
//					Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
//					setupUI();
					break;
				}
				}
			}
		}; 
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.widget_helper_layout);
		  Log.d("" + this.getCallingActivity(), "" + this.getCallingActivity());
		  Log.d("in UpdateWidgetHelper", "in UpdateWidgetHelper");
		  preferences = PreferenceManager.getDefaultSharedPreferences(this);
		  Log.d(getPreferencesConnectivity(), getPreferencesConnectivity());

		  
		  // If disconnecting from the widget do these things
		  if(getPreferencesConnectivity().equals("Disconnect") && btAdapter.isEnabled()) {
			  isDisconnecting = true;
			  Log.d("trying static stopBluetoothConnection()", "trying static stopBluetoothConnection()");
			  ZephyrMainActivity.stopBluetoothConnection();
			  RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.main);
			  updateViews.setTextViewText(R.id.StatusTextBox, "Disconnected from Zephyr");
			  Handler handler = new Handler();
				Runnable r = new Runnable() {
					public void run() {
						startZephyrMainActivity(isDisconnecting);
						finish();
					}
				};
				handler.postDelayed(r, 1000 * 2);

				
		// If connecting from the widget and bluetooth is already on do this
		  } else if(btAdapter.isEnabled()) {
			  isDisconnecting = false;
			  startZephyrMainActivity(isDisconnecting);
			  finish();

			  
		//If connecting from the widget and bluetooth is not on do this
		  } else {
			  String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
				String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
				IntentFilter filter = new IntentFilter(scanModeChanged); 
				registerReceiver(bluetoothState, filter);
				// request that device that become discoverable
				//this above method returns an intent thats passed for onActivityResult(see below)
				startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST); 
		  }
	  }
	  
	  // Gets th state of connectivity from Shared Preferences
	  private String getPreferencesConnectivity() {			
		  return preferences.getString("connectivity", "n/a");		  
		}
	  
	  
	  @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
			// Handle result of "permission to enable Bluetooth" dialogue; 
			// isEnabled() is true if user clicked ok for device to be discoverable.
			if (requestCode == DISCOVERY_REQUEST && btAdapter.isEnabled()) { 
				Toast.makeText(this, "Discovery in progress", Toast.LENGTH_SHORT).show();
				Handler handler = new Handler();
				Runnable r = new Runnable() {
					public void run() {
						startZephyrMainActivity(isDisconnecting);
						finish();
					}
				};
				handler.postDelayed(r, 1000 * 2);
				btAdapter = null;

			// If refused to turn on bluetooth
			} else {
				Toast.makeText(this, "Connection cancelled", Toast.LENGTH_SHORT).show();
				finish();
			}
		}

	private void startZephyrMainActivity(boolean isDisconnecting2) {
		Intent intent = new Intent(this, ZephyrMainActivity.class);
		intent.putExtra("isFromWidget", true);
		intent.putExtra("isDisconnecting", isDisconnecting2);
		intent.putExtra("devices", new ArrayList<BluetoothDevice>());
		startActivity(intent);
	}
}