package edu.vu.isis.ammo.TwoWayBTCommunication;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import edu.vu.isis.ammo.TwoWayBTCommunication.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
//import edu.vu.isis.ammo.biosensor.demo.R;



// IMPORTANT: MANY OF THE METHODS HERE ARE THE SAME METHODS CALLED FROM THE MAIN ACTIVITY
// IF YOU SEE A DUPLICATE METHOD, THERE MAY BE SOME MINOR DIFFERENCES.
// CHECK MAIN ACTIVITY FOR EXPLANATIONS OF WHAT METHODS DO IN GENERAL
public class listViewBTDevices extends Activity implements OnItemClickListener{
	
	ArrayAdapter<String> listAdapter;
	public ListView listView;
	public BluetoothAdapter btAdapter;
	public static ArrayList<BluetoothDevice> devices;
	private BluetoothDevice remoteDevice;
	public ProgressBar progressBar;
	public Button refreshListBtn;
	
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Calling Activity is " + this.getCallingActivity(), "Calling Activity is " + this.getCallingActivity());
        Log.d("listViewBTDevices activity created", "listViewBTDevices activity created");
        setContentView(R.layout.bluetooth_list_view);
        Log.d("contentview created", "contentview created");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("btadapter created", "btadapter created");
        refreshListBtn = (Button) findViewById(R.id.refreshListBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setVisibility(View.INVISIBLE);
        
        listView = (ListView)findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		
		// listAdapter is needed to actually add things to the listView
		// adding to the listAdapter will then add things to the listView
		listAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
		listView.setAdapter(listAdapter);
        
		devices = this.getIntent().getExtras().getParcelableArrayList("devices");


        
		Log.d("before for loop devices", "before for loop devices");
		
		// This should always be called because the screen will not show if bluetooth is disabled
		if(btAdapter.isEnabled()) {
	        for (int i = 0; i < devices.size() && btAdapter.isEnabled(); i++) {
	        	Log.d("Check to see devices in BTAdapter" + devices, "Check to see devices in BTAdapter" + devices);
	        	if (devices.equals(null)) {
	        		Log.d("devices is null", "devices is null");
	        	} else if(btAdapter.isEnabled() && devices.get(i).getName().startsWith("HXM")) {
	        		listAdapter.add("Zephyr Device " + devices.get(i).getName() + " " + " " + "\n" + devices.get(i).getAddress() + "\n" + "Pairing ID: 1234");
				} else {
					listAdapter.add(devices.get(i).getName() + " " + " " + "\n" + devices.get(i).getAddress());
				}
	        }
		}
    }
	
	
	// Restarts Main Activity
	public void backToBTConnectScrnBtnClicked(View v) { 
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("devices", devices);
		startActivity(i);
		finish();
	}
	
	// Refreshes the bluetooth devices list nearby
	public void refreshListBtnClicked(View v) {
		Log.i("yay", "yay");
		btAdapter = null;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter != null) {
			Log.i("yay2", "yay2");
			if(btAdapter.isEnabled()) {
				Log.i("yay3", "yay3");
				devices.clear();
				Log.i("yay4", "yay4");
				for(int i = 0; i < devices.size(); i++) {
					Log.d("CHEESE", devices.get(i).getName());
				}
				listAdapter.clear();
				Toast.makeText(listViewBTDevices.this, "Discovery in progress", Toast.LENGTH_SHORT).show();
				Log.i("yay5", "yay5");
				this.findDevices();
				Log.i("yay6", "yay6");
				for(int i = 0; i < devices.size(); i++) {
					Log.d("PIE", devices.get(i).getName());
				}
				Log.i("yay7", "yay7");
				progressBar.setVisibility(View.VISIBLE);
				Log.i("yay8", "yay8");
				refreshListBtn.setVisibility(View.INVISIBLE);
				
				stopBluetoothProgressBar(10);
				
			}
		}
	}
	
	private void stopBluetoothProgressBar(int seconds) {
		Handler handler = new Handler();
		Runnable r = new Runnable() {
			public void run() {
				progressBar.setVisibility(View.INVISIBLE);
				refreshListBtn.setVisibility(View.VISIBLE);
			}
		};
		handler.postDelayed(r, seconds * 1000);
	}

	String toastText;
	private void findDevices() {
		String lastUsedRemoteDevice = getLastUsedRemoteBTDevice();
		Log.d("in findDevices", "in findDevices");
		if (lastUsedRemoteDevice != null && btAdapter.isEnabled()) { 
			toastText = "Checking for known paired devices, namely: " + lastUsedRemoteDevice;
			Toast.makeText(listViewBTDevices.this, toastText, Toast.LENGTH_SHORT).show();
			Set <BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
			for (BluetoothDevice pairedDevice : pairedDevices) {
				if (pairedDevice.getAddress().equals(lastUsedRemoteDevice)) {
					toastText = "Found device: " + pairedDevice.getName() + "@" + lastUsedRemoteDevice;
					Toast.makeText(listViewBTDevices.this, toastText, Toast.LENGTH_SHORT).show();
					remoteDevice = pairedDevice;
				}
			}
		} 
		if(!btAdapter.isEnabled()) {
			Log.d("wamp", "wamp");
		}
		if (remoteDevice == null && btAdapter.isEnabled()) { 
			Log.d("remoteDevice == null && btAdapter enabled", "remoteDevice == null && btAdapter enabled");

			// start discovery
			if (btAdapter.startDiscovery()) {
				Log.d("btAdapter.startDiscovery()", "btAdapter.startDiscovery()");

				for(int i = 0; i < devices.size(); i++) {
					Log.d("APPLE", devices.get(i).getName());
				}
				registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
			}
		}
	}
		
	 //Create a BroadcastReceiver to receive device discovery
	BroadcastReceiver discoveryResult = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
			Log.d("in onReceive", "in onReceive");
			String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
			BluetoothDevice remoteDevice;
			remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(remoteDeviceName, remoteDeviceName);
			
			
			for(int i = 0; i < devices.size(); i++) {
				Log.d("blah", devices.get(i).getName());
			}
			
			if(!devices.contains(remoteDevice)) {
				toastText = "Discovered: " + remoteDeviceName;
				Toast.makeText(listViewBTDevices.this, toastText, Toast.LENGTH_SHORT).show();
				devices.add(remoteDevice);
				if(remoteDevice.getName().startsWith("HXM")) {
					listAdapter.add("Zephyr Device " + devices.get(devices.indexOf(remoteDevice)).getName() + " " + " " + "\n" + 
							devices.get(devices.indexOf(remoteDevice)).getAddress() + "\n" + "Pairing ID: 1234");
					stopBluetoothProgressBar(0);
				} else {
					listAdapter.add(devices.get(devices.indexOf(remoteDevice)).getName() + " " + " " + "\n" + 
							devices.get(devices.indexOf(remoteDevice)).getAddress());
				}
				
				Log.d(devices.get(devices.indexOf(remoteDevice)).getName(), devices.get(devices.indexOf(remoteDevice)).getName());
				Log.d("apple", devices.get(devices.indexOf(remoteDevice)).getName());
			}
			} catch (Exception ex) {
				Log.e("sdcard-err2:",ex.getMessage());
			}
		}
				
	};
	
	private String getLastUsedRemoteBTDevice() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String result = prefs.getString("LAST_REMOTE_DEVICE_ADDRESS", null);
		return result;
	}
	
	// similar to Spinners onItemSelected
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		BluetoothDevice device = devices.get(index);
		Log.d("" + devices.get(index), "" + devices.get(index));
		String name = device.getName();
		
		// Zephyr's Name always starts with HXM so if Zephyr Device is clicked
		// it will go to the heart rate monitor screen
		if (device.getName().startsWith("HXM"))
		{
			// If the Zephyr device is not paired yet, make pairing request
			if(!btAdapter.getBondedDevices().contains(device)) {
				try {
					
					Log.d("creating bond", "creating bond");
					
					PairingBluetoothAsyncTask task = new PairingBluetoothAsyncTask(device);
					task.execute();
					
				} catch (Exception e) {

					e.printStackTrace();
				}
				if (btAdapter.getBondedDevices().contains(device)) {
					Toast.makeText(this, "Bond Created! Click Again to Connect!", Toast.LENGTH_SHORT);
				}
				
			// if device is already paired, then start heart rate monitor activity
			} else {
				Intent i = new Intent(this, ZephyrMainActivity.class);
				i.putExtra("devices", devices);
				i.putExtra("isFromWidget", false);
				startActivity(i);
				Log.d("before MyTask", "before MyTask");
			}
			
			
		}
	}	
	
	
	// If bluetooth is turned off end activity and go to Main Activity
	@Override
    protected void onResume() {
      super.onResume();
      btAdapter = BluetoothAdapter.getDefaultAdapter();
      
      if(!btAdapter.isEnabled()) {
    	  Log.d("btAdapter not enabled on resume", "btAdapter not enabled on resume");
    	  Intent i = new Intent(this, MainActivity.class);
    	  i.putExtra("devices", new ArrayList<BluetoothDevice>());
    	  startActivity(i);
    	  btAdapter = null;
    	  finish();
      }
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK ) {
	    	Intent i = new Intent(this, MainActivity.class);
			i.putExtra("devices", devices);
			startActivity(i);
			finish();
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	

	
	
	private class PairingBluetoothAsyncTask extends AsyncTask<Void, String, Void> {


		
		BluetoothDevice device;
		
		public PairingBluetoothAsyncTask(BluetoothDevice device) {
			this.device = device;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				createBond(device);
				Log.d("Pairing " + device, "Pairing " + device);
			} catch (Exception e) {

				e.printStackTrace();
			}

			return null;
		}
		
		public boolean createBond(BluetoothDevice btDevice)  
			    throws Exception  
			    { 
			        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
			        Method createBondMethod = class1.getMethod("createBond");  
			        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
			        return returnValue.booleanValue();  
			    } 
		
		
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
//			
		}
		
	}
	
	
}
