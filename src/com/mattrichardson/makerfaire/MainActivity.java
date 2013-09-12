package com.mattrichardson.makerfaire;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

	public static final String DEBUG_TAG = "MyLoggingTag";
	public static final String PREFS_NAME = "MyPrefsFile";
	
	boolean sendAccel = false;
	String serverIP = "";
	String serverPort = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	       String ipPref = settings.getString("udpIpAddress", "10.0.1.5");
	       String portPref = settings.getString("udpPort", "4444");
	       	EditText udpIp = new EditText(this);
			udpIp = (EditText) findViewById(R.id.udpIp);
			EditText udpPort = new EditText(this);
			udpIp.setText(ipPref);
		
			udpPort = (EditText) findViewById(R.id.udpPort);
			udpPort.setText(portPref);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		EditText udpIp = new EditText(this);
		udpIp = (EditText) findViewById(R.id.udpIp);

		EditText udpPort = new EditText(this);
		udpPort = (EditText) findViewById(R.id.udpPort);
		
		
		// Save the IP and Port to the preferences file:
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("udpIpAddress", udpIp.getText().toString());
		editor.putString("udpPort", udpPort.getText().toString());
		editor.commit();
		
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			
			Intent intent = new Intent(this, SensorSender.class);
			intent.putExtra("udpIp", udpIp.getText().toString());
			intent.putExtra("udpPort", udpPort.getText().toString());
			startActivity(intent);
		} else {
			// TODO: alert to no network here.
		}
	}	
}
