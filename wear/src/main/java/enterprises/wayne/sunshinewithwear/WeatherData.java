package enterprises.wayne.sunshinewithwear;

import android.graphics.Bitmap;

/**
 * Created by ahmed on 1/14/2017.
 */

public class WeatherData {
    private int low;
    private int high;
    private Bitmap icon;

    public WeatherData(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }
}
