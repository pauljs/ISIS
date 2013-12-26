package edu.vu.isis.ammo.TwoWayBTCommunication;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.vu.isis.ammo.TwoWayBTCommunication.Testing.dataTypeEnum;



public class MainActivity extends Activity implements OnItemSelectedListener{
	protected static final int DISCOVERY_REQUEST = 1; // set to true
	
	private static final String TAG = "BiosensorDemoMain";

	//These are for the menu options select
    private static final int MENU_ABOUT = 1;
    private static final int MENU_OPTION_SETTINGS = 2;
    private static final int MENU_GRAPHS = 3;
    private static final int SELECT_PICTURE = 1;
    
    // manages the Bluetooth hardware on device
    // Allows for enabling and disabling of Bluetooth
	public BluetoothAdapter btAdapter = null; 
	
	// Tells if the Main Activity is created from the listViewBTDevices Activity
	// if so it will get the devices ArrayList from that activity (seen in beginning of setUpUI)
	// if not it will make a new devices arraylist that is empty
	public static boolean isNewActivity = false;
	
	// Tells if it is from a menu activity. Makes sure we dont try and
	// get the devices arraylist from that activity since it wasn't passed to it
	public static boolean isFromMenuOption = false;
	
	// widgets used in the layouts of mMinactivity
	public TextView statusUpdate;
	public Button connect;
	public Button disconnect;
	public Button listViewFirstUseBtn;
	public ProgressBar progressBar;
	public ImageView logo;
	public String toastText = "";
	
	// This will be the remote device 
	private BluetoothDevice remoteDevice;
	
	// More widgets used in the layouts of Mainactivity
	public Spinner spinnerDevices;
	public ArrayAdapter<String> spinnerAdapter;
	public Button sendBtn;
	public Button hostBtn;
	public Button clientBtn;
	public TextView readingTextView;
	public EditText writingEditText;
	
	// used to keep track of which item was selected in the spinner
	// -1
	public int selectedDeviceIndex = -1;
	
	// used to keep track of changes made in the main activity that
	// should be changed in the settings menu
	SharedPreferences manager;
	
	// Threads needed for Bluetooth Communication for sending text
	AcceptThread hostAcceptThread = null;
	ConnectThread connectThread = null;
	ConnectedThread connectedThread = null;
	
	// Used to Stop threads when bluetooth disabled or decide to disconnect
	volatile AcceptThread stopHostAcceptThread = null;
	volatile ConnectedThread stopConnectedThread = null;
	
	// More widgets used in the layouts of Mainactivity
	public Button importBtn;
	public Button sendImgBtn;
	
	// a string for the image path when selected to send over bluetooth
	public String selectedImagePath;
	
	// needed to tell onResume when an image is selected not to reset setupUI
	public boolean	isFromImgFinder = false;
	
	// This was originially used when I was using bitmap but is not longer needed
	public boolean isString = true;
	
	// Lists the bluetooth pairedDevices
	ArrayList<BluetoothDevice> pairedDevices;
	
	// this list will be for the listview
	public ArrayList<BluetoothDevice> devices;
	
	
	// Broadcastreceiver to receive state changes
	// This is not used at the moment but if used it would keep track of bluetooth states
	BroadcastReceiver bluetoothState = new BroadcastReceiver() { 
		
		public void onReceive(Context context, Intent intent) {

			String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE; // get info when state change
			String stateExtra = BluetoothAdapter.EXTRA_STATE;
			int state = intent.getIntExtra(stateExtra, -1);

			switch(state) {
			case(BluetoothAdapter.STATE_TURNING_ON) :
			{
				toastText = "Bluetooth turning on";
				Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
				break;
			}
			case(BluetoothAdapter.STATE_ON) :
			{
				toastText = "Bluetooth on";
				Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
				setupUI();
				break;
			}
			case(BluetoothAdapter.STATE_TURNING_OFF) :
			{
				toastText = "Bluetooth turning off";
				Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
				break;
			}
			case(BluetoothAdapter.STATE_OFF) :
			{
				toastText = "Bluetooth off";
				Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
				setupUI();
				break;
			}
			}
		}
	}; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		//get references
		statusUpdate = (TextView) findViewById(R.id.result);
		connect = (Button) findViewById(R.id.connectBtn);
		disconnect = (Button) findViewById(R.id.disconnectBtn);
		listViewFirstUseBtn = (Button) findViewById(R.id.listViewFirstUseBtn);
		logo = (ImageView) findViewById(R.id.logo);
		
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		spinnerDevices = (Spinner) findViewById(R.id.spinnerDevices);
		spinnerDevices.setOnItemSelectedListener(this);
		
		spinnerAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerDevices.setAdapter(spinnerAdapter);
		spinnerDevices.setPrompt("Select Bluetooth Device to Connect");
		
		sendBtn = (Button) findViewById(R.id.sendBtn);
		hostBtn = (Button) findViewById(R.id.hostBtn);
		clientBtn = (Button) findViewById(R.id.clientBtn);
		writingEditText = (EditText) findViewById(R.id.writingEditText);
		manager = PreferenceManager.getDefaultSharedPreferences(this);
		
		importBtn = (Button) findViewById(R.id.importBtn);
		sendImgBtn = (Button) findViewById(R.id.sendImgBtn);
		
		// This is a method to automatically send text over bluetooth in real time
		// Only will run when a bluetooth connection and threads are established
		// and someone is typing
		writingEditText.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	isString = true;
	        	Testing s2 = new Testing();
	            s2.test = writingEditText.getText() + "";
	            s2.type = dataTypeEnum.STRING;
	            Log.i("BLAH", "s implements Serializable: " + (s2 instanceof Serializable));
	            try {
	                ByteArrayOutputStream bos = new ByteArrayOutputStream();
	                ObjectOutput out = null;
	                try {
	                	
	                	if(writingEditText.getVisibility() != View.INVISIBLE && manager.getBoolean("real_time_bluetooth_checkbox", true)) {
		                    out = new ObjectOutputStream(bos);
		                    out.writeObject(s2);
		                    connectedThread.write(bos.toByteArray());
	                	}
	                } finally {
	                    out.close();
	                    bos.close();
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }

	        }
	        
	        //these methods are not used at the moment
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
		
		
		readingTextView = (TextView) findViewById(R.id.ReadingTextView);
		
		setupUI(); 

	}

	
	// Creates the options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, "About");
        menu.add(0, MENU_OPTION_SETTINGS, Menu.NONE, "Settings");
        menu.add(0, MENU_GRAPHS, Menu.NONE, "Graphs");
		return true;
	}
	
	
	// if the options menu item is selected...
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		isFromMenuOption = true;
		switch (item.getItemId()) {
			case MENU_OPTION_SETTINGS:
				launchSettingsActivity();
				break;
			case MENU_ABOUT:
				launchAboutActivity();
				break;
			case MENU_GRAPHS:
				showGraph();
				break;
		}
		return true;
	}
	
	
	// This method is the main method for setting up the UI depending on what bluetooth state it is in
	private void setupUI() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.d("btAdapter", "btAdapter");
		
		// this if statement is to get the devices from the listViewBTDevices activity
		// if its from that activity
		if (btAdapter.isEnabled() && !this.getIntent().equals(null) && isNewActivity && !isFromMenuOption) {
			Log.d("got devices from listView", "look left");
			Log.d("" + this.getIntent(), "" + this.getIntent());
			devices = this.getIntent().getExtras().getParcelableArrayList("devices");
			spinnerAdapter.clear();
			spinnerAdapter.add("None");
			for(int i = 0; i < devices.size(); i++) {
				spinnerAdapter.add(devices.get(i).getName() + " " + " " + "\n" + devices.get(i).getAddress());
			}
		// if the activity is not created from the listViewBTDevices activity
		// make a new ArrayList of BT devices and clear the spinner
		} else {
			Log.d("new arraylist of devices", "new arraylist of devices");
			devices = new ArrayList<BluetoothDevice>();
			spinnerAdapter.clear();
			spinnerAdapter.add("None");
		}
		
		refreshPairedDevicesArrayList();
		
				

		disconnect.setVisibility(View.INVISIBLE);
		spinnerDevices.setVisibility(View.INVISIBLE);
		hideRealTimeBluetooth();
		
		

		logo.setVisibility(View.INVISIBLE);
		
		listViewFirstUseBtn.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.INVISIBLE);
		
		
		// sees if bluetooth is ready, if btAdapter.getDefaultAdapter is null then device doesnt support bluetooth
		//This is done here so as soon as it opens it tells if bluetooth is enabled or not
		// If bluetooth is enabled then we want to show the widgets that correspond to if its on
		if (btAdapter.isEnabled()) { 

			// gets hardware address of the device
			String address = btAdapter.getAddress(); 
			
			//get name of the device
			String name = btAdapter.getName(); 
			
			String statusText = name + " : " + address;
			statusUpdate.setText(statusText);
			
			logo.setVisibility(View.VISIBLE);
			connect.setVisibility(View.INVISIBLE);
			listViewFirstUseBtn.setVisibility(View.VISIBLE);
			disconnect.setVisibility(View.VISIBLE);
			spinnerDevices.setVisibility(View.VISIBLE);
			
		// if bluetooth is not on only show certain things
		} else {
			connect.setVisibility(View.VISIBLE);
			statusUpdate.setText("Bluetooth is disabled");
		}
		
		// OnClick callback for "Connect" button
		// If connect btn clicked, then ask to turn on bluetooth and make discoverable
		connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				btAdapter = BluetoothAdapter.getDefaultAdapter();
				Log.d("connect clicked", "connect clicked");
				//register for discovery events
				String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
				String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
				IntentFilter filter = new IntentFilter(scanModeChanged); 
				registerReceiver(bluetoothState, filter);
				// request that device that become discoverable
				//this above method returns an intent thats passed for onActivityResult(see below)
				startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST); 
				
			}
		}); 
		
		// OnClick callback for "Disconnect" button
		// if disconnect btn click, turn bluetooth off and only show certain widgets
		disconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopBTConnectingThreads();
				isFromImgFinder = false;
				toastText = "Bluetooth disabled";
				Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
				spinnerAdapter.clear();
				devices.clear();
				btAdapter.disable();
				btAdapter = null;
				disconnect.setVisibility(View.INVISIBLE);
				spinnerDevices.setVisibility(View.INVISIBLE);
				hideRealTimeBluetooth();
				logo.setVisibility(View.INVISIBLE);
				listViewFirstUseBtn.setVisibility(View.INVISIBLE);
				connect.setVisibility(View.VISIBLE);
				statusUpdate.setText("Bluetooth is disabled");
				isNewActivity = false;
			}			
		}); 
		

	} 

	// Since btAdapter.getBondedDevices gives a set, I put them into an ArrayList
	// so it can be accessed later
	// Also it refreshes the ArrayList for pairedDevices
	private void refreshPairedDevicesArrayList() {
		Set<BluetoothDevice> btset = btAdapter.getBondedDevices();
		Iterator<BluetoothDevice> it = btset.iterator();
		pairedDevices = new ArrayList<BluetoothDevice>();
		while(it.hasNext()) {
			BluetoothDevice tmp = it.next();
			Log.d("Added " + tmp + " to pairedDevices ArrayList", "Added " + tmp + " to pairedDevices ArrayList");
			pairedDevices.add(tmp);
			
		}
	}

	// Makes all the widgets used in sending over bluetooth in Main Activity INVISIBLE
	private void hideRealTimeBluetooth() {
		importBtn.setVisibility(View.INVISIBLE);
		sendImgBtn.setVisibility(View.INVISIBLE);
		sendBtn.setVisibility(View.INVISIBLE);
		hostBtn.setVisibility(View.INVISIBLE);
		clientBtn.setVisibility(View.INVISIBLE);
		readingTextView.setVisibility(View.INVISIBLE);
		writingEditText.setVisibility(View.INVISIBLE);
	}
	

	// Starts the listViewBTDevices Activity
	public void listViewFirstUseBtnClicked(View v) {
		isNewActivity = true;
		isFromImgFinder = false;
		btAdapter = null;
		Intent i = new Intent(this, listViewBTDevices.class);
		i.putExtra("devices", devices);
		startActivity(i);
		finish();
	}
	
	// This method will start from either clicking the Connect Btn or
	// after selecting an image to send over bluetooth
	// The DISCOVERY REQUEST is for clicking the Connect Btn while
	// the RESULT_OK is for the image select
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		// Handle result of "permission to enable Bluetooth" dialogue; 
		// isEnabled() is true if user clicked ok for device to be discoverable.
		if (requestCode == DISCOVERY_REQUEST && btAdapter.isEnabled() && !isFromImgFinder) { 
			Log.d("Discovery Request onActivityResult", "Discovery Request onActivityResult");
			Toast.makeText(MainActivity.this, "Discovery in progress", Toast.LENGTH_SHORT).show();
			findDevices();
			startBluetoothProgressBar(0);
			stopBluetoothProgressBar(10);
		} else if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
            	Log.d("OnActivityResult got image to send", "OnActivityResult got image to send");
                final Uri selectedImageUri = data.getData();
                final String selectedImagePath = getRealPathFromURI(selectedImageUri);
                Log.d("SelectedImagePath is " + selectedImagePath, "SelectedImagePath is " + selectedImagePath);
                sendImgBtn.setVisibility(View.VISIBLE);
                final Bitmap picBitmap = BitmapFactory.decodeFile(selectedImagePath);
                logo.setImageBitmap(picBitmap);
                sendImgBtn.setOnClickListener(new OnClickListener() {
                	// once the sendImgBtn is clicked it will prompt how the user
                	// wishes to send the image in this method
        			@Override
        			public void onClick(View v) { 
        				isString = false;

        				Intent intent = new Intent();
        				intent.setAction(Intent.ACTION_SEND);
        				intent.setType("image/jpeg"); 
        				File sourceFile = new File(selectedImagePath);
        				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sourceFile));
        				startActivity(intent);
        			}
                });

            }
        }
	}
	
	// Used when getting the image location once selecting the image for sending
	// over bluetooth
	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

	// called when searching for devices in the main activity
	// shows a progress bar and making certain widgets invisible
	private void startBluetoothProgressBar(int seconds) {		
		Handler handler = new Handler();
		Runnable r = new Runnable() {
			public void run() {
				String address = btAdapter.getAddress(); 
				
				//get name of the device
				String name = btAdapter.getName(); 
				
				String statusText = name + " : " + address;
				statusUpdate.setText(statusText);
				connect.setVisibility(View.INVISIBLE);
				logo.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				listViewFirstUseBtn.setVisibility(View.INVISIBLE);
				disconnect.setVisibility(View.INVISIBLE);
				spinnerDevices.setVisibility(View.INVISIBLE);
			}
		};
		handler.postDelayed(r, seconds * 1000);		
	}
	
	
	// This force stops the startBluetoothProgress bar when the device
	// finds a certain device (currently when it finds a pairedDevice)
	private void stopBluetoothProgressBar(int seconds) {
		Handler handler = new Handler();
		Runnable r = new Runnable() {
			public void run() {
				progressBar.setVisibility(View.INVISIBLE);
				listViewFirstUseBtn.setVisibility(View.VISIBLE);
				disconnect.setVisibility(View.VISIBLE);
				spinnerDevices.setVisibility(View.VISIBLE);
			}
		};
		handler.postDelayed(r, seconds * 1000);
	}
	
	// Method for finding devices via bluetooth
	private void findDevices() {
		String lastUsedRemoteDevice = getLastUsedRemoteBTDevice();
		spinnerAdapter.clear();
		spinnerAdapter.add("None");
		
		//This if statement can be ignored, it doesnt get called
		if (lastUsedRemoteDevice != null && btAdapter.isEnabled()) { // && btAdapter.isEnabled()
			toastText = "Checking for known paired devices, namely: " + lastUsedRemoteDevice;
			Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
			Set <BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
			for (BluetoothDevice pairedDevice : pairedDevices) {
				if (pairedDevice.getAddress().equals(lastUsedRemoteDevice)) {
					toastText = "Found device: " + pairedDevice.getName() + "@" + lastUsedRemoteDevice;
					Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
					remoteDevice = pairedDevice;
				}
			}
		} 
		
		// This is the if statement that will get called when findDevices method is called
		// itll call to discoveryResult receiver when it receives a remoteDevice
		if (remoteDevice == null && btAdapter.isEnabled()) { // && btAdapter.isEnabled()

			if (btAdapter.startDiscovery()) {
				registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
			}
		}
	}
		
	//Create a BroadcastReceiver to receive device discovery
	// Called by the findDevices method
	BroadcastReceiver discoveryResult = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
			BluetoothDevice remoteDevice;
			remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			
			
			// Adding to both the devices Arraylist and the spinner
			if(remoteDeviceName != null) {
				Log.d("test1a", "test1a");
				if(!devices.contains(remoteDevice)) {
					toastText = "Discovered: " + remoteDeviceName;
					Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
					devices.add(remoteDevice);
					Log.d("cake", devices.get(devices.indexOf(remoteDevice)).getName());
					spinnerAdapter.add(remoteDevice.getName() + " " + " " + "\n" + remoteDevice.getAddress());
				}
				
				// Telling the BluetoothProgressBar to stop showing
				if(pairedDevices.contains(remoteDevice)) {
					Log.d("In pairedDevices, stopingBluetoothProgressBar(0)", devices.get(devices.indexOf(remoteDevice)).getName());
					stopBluetoothProgressBar(0);
				} else if(remoteDevice.getName().startsWith("HXM") || remoteDevice.getName().startsWith("Justin") || remoteDevice.getName().startsWith("Sam")) {
					Log.d("stopingBluetoothProgressBar(0)", devices.get(devices.indexOf(remoteDevice)).getName());
					stopBluetoothProgressBar(0);
				}
			}
			
		}
	};
	
	//Ignore this method for it has not worked / is not used
	private String getLastUsedRemoteBTDevice() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String result = prefs.getString("LAST_REMOTE_DEVICE_ADDRESS", null);
		return result;
	}
	
	// Method for starting graph activity in Menu Item
	public void showGraph() {
    	Log.d(TAG, "show graphs");
    	Intent intent = new Intent(this, GraphingActivity.class);
    	intent.putExtra("devices", devices);
        startActivity(intent);

   }
	
	// Method for starting settings activity in Menu Item
	private void launchSettingsActivity() {
        Log.d(TAG, "showing settings");
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("devices", devices);
        startActivity(intent);

    }

	// Method for starting about activity in Menu Item
    @SuppressLint("NewApi")
	private void launchAboutActivity() {
        Log.d(TAG, "showing about");
        isFromMenuOption = false;

    }

    // When activity is resumed, it must make sure which bluetooth state it is in
    // if the bluetooth state has changed since the previous use ofthe activity
    // it will change the UI
    @Override
    protected void onResume() {
      super.onResume();
      btAdapter = BluetoothAdapter.getDefaultAdapter();
      Log.d("onResume", "onResume");
      Log.d("" + getCallingActivity(), "" + getCallingActivity());
      if(isFromImgFinder) {
    	  Log.d("isFromImgFinder", "isFromImgFinder");
    	  isFromImgFinder = false;
      } else if(!btAdapter.isEnabled()) {
    	  Log.d("!", "!");
    	  this.getIntent().putExtra("devices", devices);
    	  setupUI();
      }
      Log.d("!!", "!!");
      

    }
    
    // This method makes sure that while in the Main Activity that the
    // back button will not do anything. This is because it will create
    // errors when moving to different activities.
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK ) {

	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}

    // These methods are used instaed of canceling a thread because
    // the cancel method was suppressed for it created errors in older 
    // versions of android. This is now the suggested way of stopping a thread
    // by having a different thread = to null when the user wants to stop the thread
    private void stopBTConnectingThreads() {
		if(stopConnectedThread != null) {
			connectedThread.stopThread();
			Log.d("stopped connected thread", "stopped connected thread");
		}
		
		if(stopHostAcceptThread != null) {
			hostAcceptThread.stopThread();
			Log.d("stopped hostAccept thread", "stopped hostAccept thread");
		}
	}
    
    
    // This method is regarding when an item in the spinner is selected
    @Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int spinnerIndex,
			long arg3) {

    	//The index 0 will always be None meaning it will end all bluetooth connections
    	// and nothing else will occur
    	// it also hides certain widgets
    	if(spinnerIndex == 0) {
    		selectedDeviceIndex = -1;
    		stopBTConnectingThreads();
    		hideRealTimeBluetooth();
    		
    	// if the index is not 0 then something was selected
    	} else {
    		int devicesIndex = spinnerIndex - 1;
			BluetoothDevice device = devices.get(devicesIndex);
			Log.d("" + devices.get(devicesIndex), "" + devices.get(devicesIndex));
			String name = device.getName();
			Log.d("item clicked" + device.getName(), "item clicked" + device.getName());

				// in case that an item selected is not currently paired with the device
				// then a request to pair the devices will occur
				// SINCE THERE IS A DELAY IN PAIRING, the user must reconnect to the 
				// device after pairing. THIS MEANS, selecting none and then reselecting the device
				// to connect to.
				if(!btAdapter.getBondedDevices().contains(device)) {
					try {
						hideRealTimeBluetooth();
						Toast.makeText(this, "Attempting to bond devices", Toast.LENGTH_SHORT).show();
						Log.d("creating bond", "creating bond");
						
						PairingBluetoothAsyncTask task = new PairingBluetoothAsyncTask(device);
						task.execute();

						
					} catch (Exception e) {

						e.printStackTrace();
					}

				// This means the devices are already paired and connnection can start
				} else {
					hostBtn.setVisibility(View.VISIBLE);
					clientBtn.setVisibility(View.VISIBLE);
				}

				
			selectedDeviceIndex = devicesIndex;
    	}
	}

    // This method is not used at the moment
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}	
	
	
	// This AsyncTask is in charge of pairing bluetooth devices that are not
	// currently paired
	private class PairingBluetoothAsyncTask extends AsyncTask<Void, String, Void> {

		
		BluetoothDevice device;
		boolean createdBond = false;
		
		
		public PairingBluetoothAsyncTask(BluetoothDevice device) {
			this.device = device;
		}
		
		@Override
		protected Void doInBackground(Void... params) {

			
			try {
				createdBond = createBond(device);
				
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

			super.onPostExecute(result);
			
			if(createdBond) {
				Log.d("bond created", "bond created");

			} else {
				Log.d("bond not created", "bond not created");
			}
		
		}
			
	}	 
	
	
	
	
	
	// This method changes the readingTextView to whatever string was sent over
	// via Bluetooth
	private void changeReadingTextView(Testing obj) {
		readingTextView.setText(obj.test);
	}
	
	// This starts a thread for making sockets and starting sendinga and receiving
	// bluetooth methods. BOTH THE HOST AND CLIENT BUTTON MUST BE CLICKED FOR IT TO WORK
	public void clientBtnOnClick(View v) {
		if(selectedDeviceIndex != -1) {
			Toast.makeText(getApplicationContext(), "Client", Toast.LENGTH_SHORT).show();

			readingTextView.setVisibility(View.VISIBLE);
			Log.d("" + devices.get(selectedDeviceIndex), "" + devices.get(selectedDeviceIndex));
			connectThread = new ConnectThread(devices.get(selectedDeviceIndex));
		    connectThread.start();
		} else {
			Log.d("uhoh" + devices.get(selectedDeviceIndex), "uhoh" + devices.get(selectedDeviceIndex));
		}
		
	}
	
	
	// This starts a thread for making sockets and starting sendinga and receiving
		// bluetooth methods. BOTH THE HOST AND CLIENT BUTTON MUST BE CLICKED FOR IT TO WORK
	public void hostBtnOnClick(View v) {
        Toast.makeText(getApplicationContext(), "Host", Toast.LENGTH_SHORT).show();
        sendBtn.setVisibility(View.VISIBLE);

		writingEditText.setVisibility(View.VISIBLE);
		importBtn.setVisibility(View.VISIBLE);
        hostAcceptThread = new AcceptThread();
        hostAcceptThread.start();
    }
	
	// Starts an Activity for result allowing user to choose which image to send
	public void importBtnOnClick(View v) {
		Intent intent = new Intent();
		Log.d("importBtnOnClick", "importBtnOnClick");
		isFromImgFinder = true;
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
	}
	

	// one of the three threads in charge of bluetooth connection
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public void stopThread() {
        	stopHostAcceptThread = null;
        }
        

        public AcceptThread() {

            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            //Log.i("THIS IS THE UUID", uuid.toString());
            try {
                // MY_UUID is the app's UUID string, also used by the client code

                tmp = btAdapter.listenUsingRfcommWithServiceRecord("Host", constants.uuid); // see constants class
                Log.i("It worked", "Temp is" + tmp == null ? "null" : "not null");
            } catch (IOException e) {
                Log.i("EXCEPTION", "exception", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
        	stopHostAcceptThread = (AcceptThread) AcceptThread.currentThread();
        	AcceptThread tmp = (AcceptThread) AcceptThread.currentThread();
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true && stopHostAcceptThread == tmp) {
                try {
                    Log.i("before accept", "GOT HERE");
                    socket = mmServerSocket.accept();
                    Log.i("after accept", "Socket is" + socket == null ? "null" : "not null");
                } catch (IOException e) {
                    Log.i("darn", "it didnt work");
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    connectedThread = manageConnectedSocket(socket); // called to do stuff!
                    /*
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                    */
                }
            }
        }


        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }


    public ConnectedThread manageConnectedSocket(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        return connectedThread;
    }

    // one of the three threads in charge of bluetooth connection
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }
        

        public void stopThread() {
        	stopConnectedThread = null;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

           	stopConnectedThread = (ConnectedThread) ConnectedThread.currentThread();
           	ConnectedThread tmp = (ConnectedThread) ConnectedThread.currentThread();
            // Keep listening to the InputStream until an exception occurs
            while (true && stopConnectedThread == tmp) {
                Log.i("BLAH", "Reading");
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    ByteArrayInputStream bin = new ByteArrayInputStream(buffer, 0, bytes);
                    ObjectInputStream oin = new ObjectInputStream(bin);

                    final Testing obj = (Testing) oin.readObject();
                    
                    runOnUiThread(new Runnable() {
                        public void run() {
                        	switch (obj.type) {
                        		case STRING :
                        		{
                        			Log.d("changing text view", "changing text view");
                        			changeReadingTextView(obj);
                        		}
                        			
                        	}

                       }						
                   });                 
                   
                    Log.i("BLAH", "Read " + bytes);

                } catch (Exception e) {
                	Log.d("waaah", "waaah");
                    e.printStackTrace();
                    break;
                }
            }
        }

		

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            Log.i("BLAH", "Writing " + bytes.length);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }


    }

    // This is used if the bluetooth text is not in real time. THis sends the string via bluetooth
    public void sendBtnOnClick(View view) {
        setUpSendingBluetooth(dataTypeEnum.STRING, writingEditText.getText() + "");
    }
    
    // Sets up the ability to send object over bluetooth
	private void setUpSendingBluetooth(dataTypeEnum dataType, Object obj) {
		Testing s = new Testing();
		switch (dataType) {
			case STRING :
			{
				s.test = (String) obj;
				s.type = dataType;
		        Log.i("BLAH", "s implements Serializable: " + (s instanceof Serializable));
		        try {
		            ByteArrayOutputStream bos = new ByteArrayOutputStream();
		            ObjectOutput out = null;
		            try {
		                out = new ObjectOutputStream(bos);
		                out.writeObject(s);
		                connectedThread.write(bos.toByteArray());
		            } finally {
		                out.close();
		                bos.close();
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
			}
			
			//THIS is currently not used
			case IMAGE :
			{
				
			}
				
		}
        
	}

	// one of the three threads in charge of bluetooth connection
    class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(constants.uuid);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            connectedThread = manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }	
}
