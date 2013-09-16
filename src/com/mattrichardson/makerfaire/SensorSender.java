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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class SensorSender extends Activity implements SensorEventListener {
	private SensorManager sensorManager;
	private String udpIp;
	private String udpPort;
	private long interval = 100;
	private long prevMillis = 0;
	private boolean sendPosition = false;

	// for the low-pass filter:
	static final float ALPHA = 0.1f;
	protected float[] accelVals;

	private MjpegView mv;
	private static final String TAG = "MjpegActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// We're going full screen:
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_sender);
		findViewById(R.id.textView);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// MainActivity will pass the IP and port from the input screen. Get
		// that:
		Intent iin = getIntent();
		Bundle extras = iin.getExtras();
		udpIp = extras.getString("udpIp");
		udpPort = extras.getString("udpPort");

		// Here we'll form the MJPG stream URL:
		String URL = "http://" + udpIp + ":8090/?action=stream";

		// Now show the MJPG stream:
		mv = new MjpegView(this);
		setContentView(mv);
		new DoRead().execute(URL);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			accelVals = lowPass(event.values, accelVals);
			getRotation(accelVals);
		}

	}

	private void getRotation(float[] values) {
		long currMillis = System.currentTimeMillis();
		// has the interval passed?
		if (currMillis > prevMillis + interval) {
			// Are we in sendPosition mode?
			if (sendPosition) {
				// Get the data
				// float[] values = event.values;
				float x = values[0];
				float y = values[1];
				float z = values[2];

				// Format the message for the UDPListener on the Pi
				String message = String.format("%.2f", x) + ","
						+ String.format("%.2f", y) + ","
						+ String.format("%.2f", z);

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
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
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
			// Toggle sendPosition flag on tap of touch pad:
			sendPosition = !sendPosition;
			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	// from:
	// http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter
	protected float[] lowPass(float[] input, float[] output) {
		if (output == null)
			return input;

		for (int i = 0; i < input.length; i++) {
			output[i] = output[i] + ALPHA * (input[i] - output[i]);
		}
		return output;
	}
}