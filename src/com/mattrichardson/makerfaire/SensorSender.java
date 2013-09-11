package com.mattrichardson.makerfaire;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
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

  
/** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.sensor_sender);
    view = findViewById(R.id.textView);
    view.setBackgroundColor(Color.GREEN);

    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    
    // MainActivity will pass the IP and port from the input screen. Get that:
    Intent iin= getIntent();
    Bundle extras = iin.getExtras();
    udpIp = extras.getString("udpIp");
    udpPort = extras.getString("udpPort");
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
      getRotation(event);
    }

  }

  private void getRotation(SensorEvent event) {
    float[] values = event.values;
    // Movement
    float x = values[0];
    float y = values[1];
    float z = values[2];
    
    String message = String.format("%.2g", x) + "," + String.format("%.2g", y) + "," + String.format("%.2g", z);
    
    long currMillis= System.currentTimeMillis();
    
    if (currMillis > prevMillis + interval) {
    	new Thread(new UDPClient(udpIp, udpPort, message)).start();
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
  }
} 