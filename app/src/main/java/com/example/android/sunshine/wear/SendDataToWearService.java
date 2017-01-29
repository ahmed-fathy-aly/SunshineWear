package com.example.android.sunshine.wear;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.Log;

import com.example.android.sunshine.R;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import static android.R.attr.bitmap;
import static android.R.attr.icon;
import static com.example.android.sunshine.utilities.SunshineDateUtils.normalizeDate;


/**
 * reads the latest weather and sends it to the wear
 */
public class SendDataToWearService extends IntentService {


    private GoogleApiClient mGoogleApiClient;

    public SendDataToWearService() {
        super("SendDataToWearService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.e("Game", "connected google client");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // read the weather data from the database to get the latest weather
            long maxDate = 0;
            int low = 0;
            int high = 0;
            int weatherId = 0;
            long todayDate = SunshineDateUtils.normalizeDate(System.currentTimeMillis());
            Cursor cursor = getContentResolver()
                    .query(WeatherContract.WeatherEntry.buildWeatherUriWithDate(todayDate),
                            new String[]{WeatherContract.WeatherEntry.COLUMN_DATE,
                                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID},
                            null,
                            null,
                            null);
            if (cursor.moveToFirst())
                do {
                    long date = cursor.getLong(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
                    if (date > maxDate) {
                        maxDate = date;
                        low = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
                        high = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
                        weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
                    }
                } while (cursor.moveToNext());

            // send the data to the wear
            if (maxDate != 0) {
                final PutDataMapRequest putRequest = PutDataMapRequest.create("/Weather");
                final DataMap map = putRequest.getDataMap();

                // current time so the date is not duplicated and ignored
                map.putLong("date", System.currentTimeMillis());

                // low and high temps
                map.putInt("low", low);
                map.putInt("high", high);

                // weather icon resource->vector drawable->bitmap->asset
                int iconId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
                Bitmap bitmap = getBitmapFromVectorDrawable(this, iconId);
                map.putAsset("icon", createAssetFromBitmap(bitmap));
                mGoogleApiClient.blockingConnect();
                Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
            }
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }


}
