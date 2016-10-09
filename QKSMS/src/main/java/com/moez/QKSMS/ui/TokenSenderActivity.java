package com.moez.QKSMS.ui;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moez.QKSMS.R;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pl.edu.agh.mobilne.ultrasound.android.lib.send.SenderService;
import pl.edu.agh.mobilne.ultrasound.core.TokenGenerator;


public class TokenSenderActivity extends ActionBarActivity {

    private static final String TAG = "TokenSenderActivity";
    private boolean isStarted = false;

//    private Button startSendingButton;
//    private Button stopSendingButton;
    private Button generateTokenButton;
    private TextView tokenEditText;
    EditText editLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_sender);

//      startSendingButton = (Button) findViewById(R.id.startSendButton);
//      stopSendingButton = (Button) findViewById(R.id.stopSendButton);
        generateTokenButton = (Button) findViewById(R.id.generateTokenButton);
        tokenEditText = (TextView) findViewById(R.id.tokenEditText);
        editLocation = (EditText) findViewById(R.id.locationEditText);
        tokenEditText.setText(getIntent().getStringExtra("TEXT"));
        RippleBackground ripple = (RippleBackground) findViewById(R.id.ripple);
        ripple.startRippleAnimation();

//      updateButtons();
        startSendingToken();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
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

    public void startSendingToken() {
        isStarted = true;

//        updateButtons();
        startSenderService(tokenEditText.getText().toString());
    }

    public void stopSendingToken(View view) {
        isStarted = false;

//        updateButtons();
        stopSenderService();
    }

    public void generateToken(View view) {
        String generatedToken = TokenGenerator.getStringToken();
        tokenEditText.setText(generatedToken);
    }

    private void startSenderService(String tokenString) {
        byte[] tokenData = TokenGenerator.convertFromString(tokenString);

        Intent intent = new Intent(this, SenderService.class);
        intent.putExtra(SenderService.BYTE_BUFFER_KEY, tokenData);
        startService(intent);
    }

    private void stopSenderService() {
        Intent intent = new Intent(this, SenderService.class);
        stopService(intent);
    }

    public void startReceiverActivity(View view) {
        Intent intent = new Intent(this, TokenReceiverActivity.class);
        startActivity(intent);
    }
//    private void updateButtons() {
////        startSendingButton.setEnabled(!isStarted);
////        stopSendingButton.setEnabled(isStarted);
//        generateTokenButton.setEnabled(!isStarted);
//    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            editLocation.setText("");
//            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Latitude: " + loc.getLatitude() + " Longitude: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);

        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String s = longitude + "\n" + latitude;
            editLocation.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
