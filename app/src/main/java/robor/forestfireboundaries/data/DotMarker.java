package robor.forestfireboundaries.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import robor.forestfireboundaries.R;

/**
 * Created by Mathijs de Groot on 18/01/2018.
 */

public class DotMarker {

    private static final String TAG = DotMarker.class.getSimpleName();

    private Context context;
    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private Marker marker;
    private Drawable icon;
    private int color;

    private boolean visible = false;

    public DotMarker(LatLng position, Context context) {
        this.context = context;

        icon = ContextCompat.getDrawable(context, R.drawable.dot);

        markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(icon)))
                .title("" + position.latitude + " / " + position.longitude);
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        icon.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(icon)));
        updateOnMap();
    }

    public LatLng getPosition() {
       return markerOptions.getPosition();
    }

    public void setPosition(LatLng position) {
        markerOptions.position(position);
        updateOnMap();
    }

    public void setVisible(boolean visible) {
        markerOptions.visible(visible);
        updateOnMap();
    }

    public void setTitle(String title) {
        markerOptions.title(title);
        updateOnMap();
    }

    public void setSnippet(String snippet) {
        markerOptions.snippet(snippet);
        updateOnMap();
    }

    public void addToMap(GoogleMap googleMap) {
        if (isAddedToMap()) {
            marker.remove();
        }

        marker = googleMap.addMarker(markerOptions);
        this.googleMap = googleMap;
    }

    public void removeFromMap() {
        if (isAddedToMap()) {
            marker.remove();
        }

        googleMap = null;
    }

    private void updateOnMap() {
        if (isAddedToMap()) {
            marker.remove();
            marker = googleMap.addMarker(markerOptions);
        }
    }

    private boolean isAddedToMap() {
        return googleMap != null;
    }


}
