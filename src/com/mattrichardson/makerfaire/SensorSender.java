package com.mattrichardson.makerfaire;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class SensorSender extends Activity implements SensorEventListener {
  private SensorManager sensorManager;
  private boolean color = false;
  private View view;
  private long lastUpdate;
  private String udpIp;
  private String udpPort;

  
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
    lastUpdate = System.currentTimeMillis();
    
    // MainActivity will pass the IP and port from the input screen. Get that:
    Intent iin= getIntent();
    Bundle extras = iin.getExtras();
    udpIp = extras.getString("udpIp");
    udpPort = extras.getString("udpPort");
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      getAccelerometer(event);
    }

  }

  private void getAccelerometer(SensorEvent event) {
    float[] values = event.values;
    // Movement
    float x = values[0];
    float y = values[1];
    float z = values[2];

    float accelationSquareRoot = (x * x + y * y + z * z)
        / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
    long actualTime = System.currentTimeMillis();
    if (accelationSquareRoot >= 2) //
    {
      if (actualTime - lastUpdate < 200) {
        return;
      }
      lastUpdate = actualTime;
      Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
          .show();
      if (color) {
        view.setBackgroundColor(Color.GREEN);
        new Thread(new UDPClient(udpIp, udpPort, "shaken!")).start();

      } else {
        view.setBackgroundColor(Color.RED);
        new Thread(new UDPClient(udpIp, udpPort, "shaken!")).start();
      }
      color = !color;
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
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause() {
    // unregister listener
    super.onPause();
    sensorManager.unregisterListener(this);
  }
} 