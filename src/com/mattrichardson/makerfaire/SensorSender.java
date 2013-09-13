package com.mattrichardson.makerfaire;

// Mostly based on: http://www.vogella.com/articles/AndroidSensor/article.html

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SensorSender extends Activity implements SensorEventListener {
	private SensorManager sensorManager;
	private View view;
	private String udpIp;
	private String udpPort;
	private long interval = 100;
	private long prevMillis = 0;
	private boolean sendPosition = false;

	private MjpegView mv;
	private static final String TAG = "MjpegActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_sender);
		view = findViewById(R.id.textView);
		view.setBackgroundColor(Color.GREEN);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// MainActivity will pass the IP and port from the input screen. Get
		// that:
		Intent iin = getIntent();
		Bundle extras = iin.getExtras();
		udpIp = extras.getString("udpIp");
		udpPort = extras.getString("udpPort");
		String URL = "http://" + udpIp + ":8090/?action=stream";
		mv = new MjpegView(this);
		setContentView(mv);

		new DoRead().execute(URL);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			getRotation(event);
		}

	}

	private void getRotation(SensorEvent event) {
		long currMillis = System.currentTimeMillis();
		// has the interval passed?
		if (currMillis > prevMillis + interval) {
			// Are we in sendPosition mode?
			if (sendPosition) {
				// Get the data
				float[] values = event.values;
				float x = values[0];
				float y = values[1];
				float z = values[2];

				// Format the message for the UDPListener on the Pi
				String message = String.format("%.2g", x) + ","
						+ String.format("%.2g", y) + ","
						+ String.format("%.2g", z);

				// Send the data in a new thread
				new Thread(new UDPClient(udpIp, udpPort, message)).start();
			}
			// Update time for last execution (keep on the interval)
			prevMillis = currMillis;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the orientation and
		// accelerometer sensors
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
		sensorManager.unregisterListener(this);
		mv.stopPlayback();
	}

	public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
		protected MjpegInputStream doInBackground(String... url) {
			// TODO: if camera has authentication deal with it and don't just
			// not work
			HttpResponse res = null;
			DefaultHttpClient httpclient = new DefaultHttpClient();
			Log.d(TAG, "1. Sending http request");
			try {
				res = httpclient.execute(new HttpGet(URI.create(url[0])));
				Log.d(TAG, "2. Request finished, status = "
						+ res.getStatusLine().getStatusCode());
				if (res.getStatusLine().getStatusCode() == 401) {
					// You must turn off camera User Access Control before this
					// will work
					return null;
				}
				return new MjpegInputStream(res.getEntity().getContent());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.d(TAG, "Request failed-ClientProtocolException", e);
				// Error connecting to camera
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAG, "Request failed-IOException", e);
				// Error connecting to camera
			}

			return null;
		}

		protected void onPostExecute(MjpegInputStream result) {
			mv.setSource(result);
			mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
			mv.showFps(false);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER: {
			// Toggle sendPosition flag on tap.
			sendPosition = !sendPosition;
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}
}