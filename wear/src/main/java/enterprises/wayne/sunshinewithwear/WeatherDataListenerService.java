package enterprises.wayne.sunshinewithwear;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ahmed on 1/16/2017.
 */

public class WeatherDataListenerService extends WearableListenerService {

    static Listener mListener;
    GoogleApiClient mGoogleClient;


    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleClient == null)
            mGoogleClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
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
            return null;
        }

        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleClient, asset).await().getInputStream();
        mGoogleClient.disconnect();

        if (assetInputStream == null) {
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }



    interface Listener {
        void onDataUpdate(WeatherData weatherData);
    }
}
