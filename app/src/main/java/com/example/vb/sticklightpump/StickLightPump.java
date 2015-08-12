package com.example.vb.sticklightpump;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.vb.sticklightpump.util.SystemUiHider;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class StickLightPump extends Activity implements SensorEventListener {

    private static final int SHAKE_THRESHOLD = 2000;

    private SensorManager mSensorManager;
    private long lastUpdate;
    private int stickId = 2;
    private final Stick stick = new Stick(String.valueOf(stickId));
    private final Balloon balloon = new Balloon();
    private float x, y, z;
    private float last_x, last_y, last_z;

    protected void nextStick() {
        stickId++;
        if (stickId > 50) stickId = 2;
        updateStickId();
    }

    protected void previousStick() {
        stickId--;
        if (stickId < 2) stickId = 50;
        updateStickId();
    }


    private void updateStickId() {
        balloon.reset();
        stick.setId(String.valueOf(stickId));
    }

    public static final String PREFS_NAME = "LightStickPump";

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("stickId", stickId);
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        stickId = settings.getInt("stickId", 2);

        setContentView(R.layout.activity_stick_light_pump);

        mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        final Button leftButton = (Button) findViewById(R.id.leftButton);
        leftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousStick();
            }
        });
        final Button rightButton = (Button) findViewById(R.id.rightButton);
        rightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextStick();
            }
        });
        balloon.setFillStepSize(2);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        final long currentTime = System.currentTimeMillis();
        if (lastUpdate == 0 || currentTime - lastUpdate > 100) {

            if (balloon.isFull()) balloon.reset();

            long diffTime = (currentTime - lastUpdate);
            lastUpdate = currentTime;
            float speed = calcSpeed(event, diffTime);
            if (speed > SHAKE_THRESHOLD) {
                balloon.pump();
            }
            lastUpdate = currentTime;
            final int centerIndex = updateStick();

            updateDisplay(event, speed, centerIndex);
            sendUdpPacket();
        }
    }

    private float calcSpeed(SensorEvent event, long diffTime) {
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];
        float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
        last_x = x;
        last_y = y;
        last_z = z;
        return speed;
    }

    private int updateStick() {
        byte intensityLight = (byte)40;
        byte intensityDark = (byte)25;
        byte zero = (byte)0;

        int centerIndex = (int) (balloon.getFillLevel() / 100.0 * 59.0);


        stick.setLedRgbRange(zero, intensityDark, zero, centerIndex, 59);
        stick.setLedRgbRange(intensityLight, zero, zero, 0, centerIndex);
        return centerIndex;
    }

    private void sendUdpPacket() {
        new Thread(new Runnable() {
            public void run() {
                InetAddress serverAddress = null;
                try {
                    serverAddress = InetAddress.getByName(stick.getAddress());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                byte buf[] = stick.bytes();
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress,
                            2342);
                    socket.send(packet);
                    socket.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateDisplay(SensorEvent event, float speed, int centerIndex) {
        final TextView textViewToChange = (TextView) findViewById(R.id.textView);
        StringBuilder builder = new StringBuilder();
        final String[] axisLabels = new String[] {new String("x: "), new String(" \ny: "), new String(" \nz: ")};
        for (int i = 0; i < event.values.length; ++i) {
            builder.append(axisLabels[i]);
            if (i > 0) builder.append(" ");
            builder.append(event.values[i]);
        }
        builder.append(" \nspeed: ");
        builder.append(speed);
        builder.append(" \ncenter: ");
        builder.append(centerIndex);
        builder.append(" \nstick: ");
        builder.append(stickId);
        textViewToChange.setText(builder.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
