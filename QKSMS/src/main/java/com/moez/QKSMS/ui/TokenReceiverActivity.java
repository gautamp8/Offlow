package com.moez.QKSMS.ui;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.moez.QKSMS.R;

import java.util.Arrays;
import java.util.Random;

import pl.edu.agh.mobilne.ultrasound.android.lib.receive.ReceiverService;
import pl.edu.agh.mobilne.ultrasound.core.TokenGenerator;


public class TokenReceiverActivity extends ActionBarActivity {

    String totp;
    int code;
    int INTERVAL;
    Handler mHandler;
    private boolean isStarted = false;
    private byte[] token;
    private Button startReceivingButton;
    private Button stopReceivingButton;
    private TextView tokenValueTextView;
    private BroadcastReceiver serviceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                byte[] tokenByteArray = bundle.getByteArray(ReceiverService.BYTE_BUFFER_KEY);
                updateToken(tokenByteArray);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_receiver);

        startReceivingButton = (Button) findViewById(R.id.startReceiveButton);
        stopReceivingButton = (Button) findViewById(R.id.stopReceiveButton);
        tokenValueTextView = (TextView) findViewById(R.id.tokenValueTextView);


        updateButtons();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                code = new Random().nextInt(9999);
                tokenValueTextView.setText("TOTP Code = " + String.valueOf(code));

                handler.postDelayed(this, 60000); //now is every 1 minute
            }
        }, 60000); //Every 60000 ms (1 minute)


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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(serviceBroadcastReceiver, new IntentFilter(ReceiverService.NOTIFICATION_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(serviceBroadcastReceiver);
    }

    public void startReceivingToken(View view) {
        isStarted = true;
        updateButtons();
        startReceiverService();
    }

    public void stopReceivingToken(View view) {
        isStarted = false;

        updateButtons();
        stopReceiverService();
    }

    private void updateButtons() {
        startReceivingButton.setEnabled(!isStarted);
        stopReceivingButton.setEnabled(isStarted);
    }

    private void startReceiverService() {
        Intent intent = new Intent(this, ReceiverService.class);
        startService(intent);
    }

    private void stopReceiverService() {
        Intent intent = new Intent(this, ReceiverService.class);
        stopService(intent);
    }

    private void updateToken(byte[] receivedToken) {
        if (token == null || !Arrays.equals(token, receivedToken)) {
            //tokenValueTextView.setText(TokenGenerator.convertFromByteArray(receivedToken));
            //token = receivedToken;

            String data = TokenGenerator.convertFromByteArray(receivedToken);

            int n = data.indexOf('-');

            int l = data.length();

            String t = data.substring(0, n);
            String a = data.substring(n + 1, l);

            int as = Integer.valueOf(t);

            Log.d("yo", "t = " + t);
            Log.d("yo", "a = " + a);
            Log.d("yo", "n = " + n);
            Log.d("yo", "l = " + l);
            Log.d("yo", "totp = " + totp);

            if (as == code) {
                tokenValueTextView.setText(a);
                startReceivingButton.setText("Copy to Clipboard");
                Log.d("yo", "in if");


                Button bt = new Button(this);
                bt.setText("Copy to Clipboard");
                bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.FILL_PARENT,
                        ActionBar.LayoutParams.WRAP_CONTENT));
                ViewGroup linearLayout = (ViewGroup) findViewById(R.id.linearLayout);

                linearLayout.addView(bt);

                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String text = a;
                        String label = "Token";
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(label, text);
                        clipboard.setPrimaryClip(clip);

                        startReceivingButton.setEnabled(true);

                        stopReceivingButton.setVisibility(View.INVISIBLE);

                        Toast.makeText(getApplicationContext(), "Text has been copied to clipboard.", Toast.LENGTH_LONG).show();


                    }
                });


            } else {
                Toast.makeText(getApplicationContext(), "Incorrect TOTP was entered. Please try again.", Toast.LENGTH_LONG).show();
                Log.d("yo", "in else");
            }

        }
    }
}
