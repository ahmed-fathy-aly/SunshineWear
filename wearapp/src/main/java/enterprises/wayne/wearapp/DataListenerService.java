package enterprises.wayne.wearapp;

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

    public static void setListener(Listener listener) {
        mListener = listener;
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

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
                if (mListener != null)
                    mListener.onDataUpdate(weatherData);
                Log.e("Game", "invoked listener " + (mListener != null));
            }
        }
    }


    interface Listener {
        void onDataUpdate(WeatherData weatherData);
    }
}
