package robor.forestfireboundaries;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Mathijs de Groot on 18/01/2018.
 */

public class DotMarker {

    private Context context;

    private GoogleMap googleMap;

    private Marker marker;

    private MarkerOptions markerOptions;

    private int color;

    private Drawable icon;

    public DotMarker(LatLng position, int color, GoogleMap googleMap, Context context) {
        this.context = context;
        this.googleMap = googleMap;

        icon = ContextCompat.getDrawable(context, R.drawable.dot);
        setColor(color);

        markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(icon)))
                .title("" + position.latitude + " / " + position.longitude);

        marker = googleMap.addMarker(markerOptions);
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setColor(int color) {
        this.color = color;
        icon.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public LatLng getPosition() {
        return marker.getPosition();
    }

    public void setPosition(LatLng position) {
        marker.setPosition(position);
    }

    public void setVisible(boolean visible) {
        marker.setVisible(visible);
    }

    public void setTitle(String title) {
        marker.setTitle(title);
    }

    public void setSnippet(String snippet) {
        marker.setSnippet(snippet);
    }
}
