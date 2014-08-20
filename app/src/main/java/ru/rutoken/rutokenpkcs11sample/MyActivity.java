package ru.rutoken.rutokenpkcs11sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import ru.rutoken.Pkcs11Caller.TokenManager;


public class MyActivity extends Activity {
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //finish();
            Log.d(getClass().getName(), "Pkcs11 event " + intent.getAction());
//            for(String serial: ru.rutoken.Pkcs11Caller.Pkcs11.getInstance().serialNumbers()) {
//                Log(serial);
//            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TokenManager.TOKEN_WAS_ADDED);
        intentFilter.addAction(TokenManager.TOKEN_WAS_REMOVED);

        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, MyActivity2.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
