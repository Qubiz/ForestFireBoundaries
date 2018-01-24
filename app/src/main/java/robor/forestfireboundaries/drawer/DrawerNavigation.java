package robor.forestfireboundaries.drawer;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.Toolbar;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import robor.forestfireboundaries.GoogleMapsActivity;
import robor.forestfireboundaries.R;
import robor.forestfireboundaries.bluetooth.DeviceScanActivity;

/**
 * Created by Mathijs de Groot on 24/01/2018.
 */

public class DrawerNavigation {

    public static final int GOOGLE_MAPS_ACTIVITY_ID = 1;
    public static final int DEVICE_SCAN_ACTIVITIY_ID = 2;

    public static Drawer getDrawer(final Activity activity, Toolbar toolbar, Drawer.OnDrawerItemClickListener onDrawerItemClickListener) {
        Drawer drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .addDrawerItems(
                        getGoogleMapDrawerItem(activity, GOOGLE_MAPS_ACTIVITY_ID),
                        new DividerDrawerItem(),
                        getDeviceScanDrawerItem(activity, DEVICE_SCAN_ACTIVITIY_ID)
                )
                .withOnDrawerItemClickListener(onDrawerItemClickListener)
                .withSelectedItem(-1)
                .build();

        return drawer;
    }

    private static PrimaryDrawerItem getGoogleMapDrawerItem(Context context, long identifier) {
        return new PrimaryDrawerItem()
                .withName("Map")
                .withIcon(new IconicsDrawable(context)
                        .icon(MaterialDesignIconic.Icon.gmi_map))
                .withSelectedIcon(new IconicsDrawable(context)
                        .icon(MaterialDesignIconic.Icon.gmi_map)
                        .colorRes(R.color.colorPrimaryDark))
                .withIdentifier(identifier)
                .withSetSelected(true);
    }

    private static PrimaryDrawerItem getDeviceScanDrawerItem(Context context, long identifier) {
        return new PrimaryDrawerItem()
                .withName("Device Scan")
                .withIcon(new IconicsDrawable(context)
                        .icon(MaterialDesignIconic.Icon.gmi_bluetooth))
                .withSelectedIcon(new IconicsDrawable(context)
                        .icon(MaterialDesignIconic.Icon.gmi_bluetooth)
                        .colorRes(R.color.colorPrimaryDark))
                .withIdentifier(identifier);
    }

}
