package com.mandar.movementalert;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.skyfishjy.library.RippleBackground;

public class ScrollingActivity extends AppCompatActivity implements SensorEventListener {

    RippleBackground rippleBackground;
    ImageView imageView;
    MediaPlayer mediaPlayer;
    private SensorManager sensorManager;
    Vibrator v;
    long[] pattern = {0, 100, 100};

    Uri notification;
    Ringtone r;
    private boolean update = true;
    private boolean isStart = true;
    boolean firstTimeReset = true;
    private float sxLow, syLow, szLow, sxHi, syHi, szHi, watermark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rippleBackground=(RippleBackground)findViewById(R.id.content);
        imageView=(ImageView)findViewById(R.id.centerImage);
        rippleBackground.startRippleAnimation();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Uri currentTone = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_ALARM);
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                startActivityForResult(intent, 999);

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "tune", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });




        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

       // mediaPlayer = MediaPlayer.create(this, R.raw.sound1);

        v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        watermark=1.52f;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///
    /// Sensor Code
    ///

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isStart==true)
        {	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float[] values = event.values;
            movement(values[0], values[1], values[2], (float) watermark);
        }
        }
    }

    public boolean movement(float a, float b, float c, double d)
    {

        if (update == true)
        {
            sxLow = (float) (a - d);
            syLow = (float) (b - d);
            szLow = (float) (c - d);
            sxHi = (float) (a + d);
            syHi = (float) (b + d);
            szHi = (float) (c + d);

            update = false;
        }

        if ((sxLow > a) || (a > sxHi) || (syLow > b) || (b > syHi) || (szLow > c)  || (c > szHi))
        {
            try {
            if(!r.isPlaying())
            {
                r.play();
                v.vibrate(pattern, 0);
                rippleBackground.startRippleAnimation();
                imageView.setImageResource(R.drawable.nuke);
            }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                if(r.isPlaying())
                {
                    r.stop();
                    v.cancel();
                    rippleBackground.stopRippleAnimation();
                    imageView.setImageResource(R.drawable.peace);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
