package enterprises.wayne.wearapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by ahmed on 1/14/2017.
 */

public class DataListenerService extends WearableListenerService {

    static Listener mListener;
    GoogleApiClient mGoogleClient;


    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleClient == null)
            mGoogleClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(@Nullable Bundle bundle) {
                                    Log.e("Game", "connected");
                                }

                                @Override
                                public void onConnectionSuspended(int i) {

                                }
                            }
                    )
                    .build();
        mGoogleClient.connect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleClient != null && mGoogleClient.isConnected())
            mGoogleClient.disconnect();
    }

    public static void setListener(Listener listener) {
        mListener = listener;
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

       Log.e("Game", "onDataChanged");

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;

            if ("/Weather".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                // read your values from map:
                int low = map.getInt("low");
                int high = map.getInt("high");
                WeatherData weatherData = new WeatherData(low, high);

                // read the bitmap
                if (map.containsKey("icon")) {
                    Bitmap bitmapIcon = loadBitmapFromAsset(map.getAsset("icon"));
                    weatherData.setIcon(bitmapIcon);
                    Log.e("Game", "got bitmap " + bitmapIcon.getWidth() + "*" + bitmapIcon.getHeight());
                }

                // invoke the observer
                if (mListener != null)
                    mListener.onDataUpdate(weatherData);
                Log.e("Game", "invoked listener " + (mListener != null));
            }
        }
    }


    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleClient.blockingConnect(3000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            Log.e("Game", "result not sucess but " + result.toString());
            return null;
        }
        Log.e("Game", "result success");

        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleClient, asset).await().getInputStream();
        mGoogleClient.disconnect();

        if (assetInputStream == null) {
            Log.e("Game", "asset input stream null");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    interface Listener {
        void onDataUpdate(WeatherData weatherData);
    }
}
