package edu.vu.isis.ammo.TwoWayBTCommunication;

import edu.vu.isis.ammo.TwoWayBTCommunication.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


// This class is not currently used. This is in case a different settings activity
// is wanted for the zephyr main activity but currently it is not
public class ZephyrSettings extends Activity implements OnClickListener {

	CheckBox cb;
	EditText et;
	Button b;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zephyr_settings_layout);
		
		cb = (CheckBox) findViewById(R.id.checkBox1);
		et = (EditText) findViewById(R.id.writingEditText);
		b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(this);
		loadPrefs();
	}
	
	private void loadPrefs() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean cbValue = sp.getBoolean("CHECKBOX", false); //if no CHECKBOX then returns false, else returns corresponding value
		String name = sp.getString("NAME", "YourName.");
		if(cbValue) {
			cb.setChecked(true);
		} else {
			cb.setChecked(false);
		}
		et.setText(name);
	}
	
	private void savePrefs(String key, boolean value) { //boolean to turn on widget communication?
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = sp.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}
	
	private void savePrefs(String key, String value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = sp.edit();
		edit.putString(key, value);
		edit.commit();
	}
	
	@Override
	public void onClick(View v) { //Saves preferences
		savePrefs("CHECKBOX", cb.isChecked());
		if(cb.isChecked()) {
			savePrefs("NAME", et.getText().toString());
		}
		finish();
		
	}
	
	public void changeHeartRate(String heartRate) {
		et.setText(heartRate);
		savePrefs("NAME", et.getText().toString());
	}
	
	public ZephyrSettings returnThis() {
		return this;
	}
	
}
