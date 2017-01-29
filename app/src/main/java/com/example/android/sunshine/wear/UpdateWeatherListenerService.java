package com.example.android.sunshine.wear;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * listens for update requests from wear
 * Created by ahmed on 1/16/2017.
 */
public class UpdateWeatherListenerService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        startService(new Intent(getApplicationContext(), SendDataToWearService.class));
    }
}
