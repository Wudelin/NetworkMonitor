package com.wdl.networkmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.NetworkStatsManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.wdl.monitor.NetMonitor;
import com.wdl.monitor.NetState;
import com.wdl.monitor.NetStateMonitor;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @NetMonitor(stateValue = {NetState.GPRS})
    public void showNetGPRS(NetState netState)
    {
        Log.e(TAG, "showNetGPRS: ");
    }

    @NetMonitor(stateValue = {NetState.WIFI})
    public void showNetWIFI(NetState netState)
    {
        Log.e(TAG, "showNetWIFI: ");
    }

    @NetMonitor(stateValue = {NetState.NONE})
    public void showNetNONE(NetState netState)
    {
        Log.e(TAG, "showNetNONE: ");
    }

    @NetMonitor
    public void showNet(NetState netState)
    {
        Log.e(TAG, "showNet: ");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        NetStateMonitor.getInstance().register(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        NetStateMonitor.getInstance().unregister(this);
    }
}
