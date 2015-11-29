package com.mandar.movementalert;


import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Set;


public class ScrollingActivity extends AppCompatActivity implements SensorEventListener {

    InterstitialAd mInterstitialAd;
    AdView adView;

    RippleBackgroundV2 rippleBackground;
    ImageView radarimage;

    private SensorManager sensorManager;
    Vibrator v;
    long[] pattern = { 0,10,500};


    private boolean update;
    private boolean isStart;
    private float sxLow, syLow, szLow, sxHi, syHi, szHi, watermark;

    Uri notification;

    Ringtone r;

    TextView tv;
    TextView tv2;

    // UI
    AlertDialog.Builder alert;
    WebView wv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        //
        // Ringer
        //
        v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);

        //
        // AdMob
        //
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-2991750498895413/1445363761");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();

        AdView adView = (AdView)findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
        //
        // UI
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rippleBackground = (RippleBackgroundV2) findViewById(R.id.content);
        radarimage = (ImageView) findViewById(R.id.centerImage);
       // rippleBackground.startRippleAnimation();

        rippleBackground.setNewRipple(5000);
        radarimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStart == true) {
                    // next stop
                    isStart=false;
                    update=false;
                    radarimage.setImageResource(R.drawable.on);
                    UnSetAlarm();
                    Snackbar.make(view, "Scanning Stopped", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    // next start
                    isStart=true;
                    update=true;
                    rippleBackground.stopRippleAnimation();
                    radarimage.setImageResource(R.drawable.off);
                    Snackbar.make(view, "Scanning Started", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        // Alert box
        //

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://play.google.com/store/apps/developer?id=Mandar+Shinde")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Mandar+Shinde")));
                }

            }
        });

//
//        FloatingActionButton fabringtone = (FloatingActionButton) findViewById(R.id.fabring);
//        fabringtone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                currentTone = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_ALARM);
//                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
//                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
//                startActivityForResult(intent, 999);
//                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
//                }
//
//            }
//        });


        //
        // Sensor
        //
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 10000);

        //
        // Default Setting
        //
        watermark = 0.95f;
        update=false;
        isStart=false;
        radarimage.setImageResource(R.drawable.on);
    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // Check which request we're responding to
//        if (requestCode == 999) {
//            // Make sure the request was successful
//            if (resultCode == RESULT_OK) {
//            }
//        }
//    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object

            alert = new AlertDialog.Builder(this);
            wv = new WebView(this);

            wv.loadUrl("file:///android_asset/index.html");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton("done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///
    /// Sensor Code
    ///
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isStart == true) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;
                movement(values[0], values[1], values[2], (float) watermark);
            }
        }
    }

    public boolean movement(float a, float b, float c, double d) {
        if (update == true) {
            sxLow = (float) (a - d);
            syLow = (float) (b - d);
            szLow = (float) (c - d);
            sxHi = (float) (a + d);
            syHi = (float) (b + d);
            szHi = (float) (c + d);
            update = false;
        }

        if(((sxLow > a) || (a > sxHi) || (syLow > b) || (b > syHi) || (szLow > c) || (c > szHi)) && isStart) {
            SetAlarm();
        } else {
            UnSetAlarm();
        }
        return true;
    }

    void SetAlarm()
    {
        try {
            if (!r.isPlaying())
                r.play();
              v.vibrate( pattern,-1);
           if(!rippleBackground.isRippleAnimationRunning())
                rippleBackground.startRippleAnimation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void UnSetAlarm()
    {
        try {
            if (r.isPlaying())
                r.stop();

            v.cancel();
            if(rippleBackground.isRippleAnimationRunning())
                rippleBackground.stopRippleAnimation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
