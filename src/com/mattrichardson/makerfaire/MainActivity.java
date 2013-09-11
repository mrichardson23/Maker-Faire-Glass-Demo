package com.mattrichardson.makerfaire;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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
			// alert to no network here.
		}
	}
	

	// Unused classes:
	
	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
		
		// usage: new DownloadWebpageTask().execute("http://mattrichardson.com");
		
		@Override
		protected String doInBackground(String... urls) {
			// params comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {

		}
	}

	private String downloadUrl(String myurl) throws IOException {
		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.
		
		
		int len = 500;

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d(DEBUG_TAG, "The response is: " + response);
			is = conn.getInputStream();

			// Convert the InputStream into a string
			String contentAsString = readIt(is, len);
			return contentAsString;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Reads an InputStream and converts it to a String.
	public String readIt(InputStream stream, int len) throws IOException,
		UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}

	
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		EditText udpIp = new EditText(this);
		udpIp = (EditText) findViewById(R.id.udpIp);

		EditText udpPort = new EditText(this);
		udpPort = (EditText) findViewById(R.id.udpPort);
		
	    switch (keyCode) {
	        case KeyEvent.KEYCODE_DPAD_DOWN:
	        {
	        	new Thread(new UDPClient(udpIp.getText().toString(), udpPort.getText()
						.toString(), "0,-1"));
	            return true;
	        }
	        case KeyEvent.KEYCODE_DPAD_UP:
	        {
	        	new Thread(new UDPClient(udpIp.getText().toString(), udpPort.getText()
						.toString(), "0,1"));
	            return true;
	        }
	        case KeyEvent.KEYCODE_DPAD_LEFT:
	        {
	        	new Thread(new UDPClient(udpIp.getText().toString(), udpPort.getText()
						.toString(), "-1,0"));
	            return true;
	        }
	        case KeyEvent.KEYCODE_DPAD_RIGHT:
	        {
	        	new Thread(new UDPClient(udpIp.getText().toString(), udpPort.getText()
						.toString(), "1,0"));
	            return true;
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}
	

}
