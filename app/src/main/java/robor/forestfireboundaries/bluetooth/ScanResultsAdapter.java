package robor.forestfireboundaries.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.polidea.rxandroidble.scan.ScanResult;

import java.io.Serializable;
import java.util.ArrayList;

import robor.forestfireboundaries.BaseApplication;
import robor.forestfireboundaries.R;
import rx.Observable;

public class ScanResultsAdapter extends ArrayAdapter<BleDeviceSerializable> {

    private static final String TAG = ScanResultsAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<BleDeviceSerializable> scanResults;
    private AdapterView.OnItemClickListener onItemClickListener;
    private OnScanResultAddedListener onScanResultAddedListener;

    public ScanResultsAdapter(@NonNull Context context, AdapterView.OnItemClickListener onItemClickListener, @Nullable OnScanResultAddedListener onScanResultAddedListener) {
        super(context, 0);
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        this.onScanResultAddedListener = onScanResultAddedListener;
        scanResults = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.scan_list_item, parent, false);
        }

        BleDeviceSerializable scanResult = scanResults.get(position);

        TextView textViewName = (TextView) convertView.findViewById(R.id.device_name);
        TextView textViewAddress = (TextView) convertView.findViewById(R.id.device_address);

        String name = (scanResult.getName() != null) ? scanResult.getName() : "Unknown";

        ImageView connectedImageView = (ImageView) convertView.findViewById(R.id.icon_connected);

        textViewName.setText(name);
        textViewAddress.setText(scanResult.getMacAddress());

        if (BaseApplication.isMLDPConnectionServiceBound()) {
            if (BaseApplication.getMLDPConnectionService().isConnected()) {
                if (BaseApplication.getMLDPConnectionService().getConnectedDevice().getMacAddress().equals(scanResult.getMacAddress())) {
                    connectedImageView.setImageDrawable(new IconicsDrawable(context)
                            .icon(MaterialDesignIconic.Icon.gmi_bluetooth_connected)
                            .color(context.getResources().getColor(R.color.colorPrimaryDark))
                            .sizeDp(16));
                } else {
                    connectedImageView.setImageDrawable(new IconicsDrawable(context)
                            .icon(MaterialDesignIconic.Icon.gmi_bluetooth)
                            .color(context.getResources().getColor(R.color.colorPrimaryLight))
                            .sizeDp(16));
                }
            } else {

                connectedImageView.setImageDrawable(new IconicsDrawable(context)
                        .icon(MaterialDesignIconic.Icon.gmi_bluetooth)
                        .color(context.getResources().getColor(R.color.colorPrimaryLight))
                        .sizeDp(16));
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return scanResults.size();
    }

    @Nullable
    @Override
    public BleDeviceSerializable getItem(int position) {
        return scanResults.get(position);
    }

    public void addScanResult(ScanResult scanResult) {
        addDevice(new BleDeviceSerializable(scanResult.getBleDevice().getMacAddress(), scanResult.getBleDevice().getName()));
    }

    public void addDevice(BleDeviceSerializable bleDeviceSerializable) {
        for (int i = 0; i < scanResults.size(); i++) {
            if (scanResults.get(i).getMacAddress().equals(bleDeviceSerializable.getMacAddress())) {
                scanResults.set(i, bleDeviceSerializable);
                notifyDataSetChanged();
                return;
            }
        }

        scanResults.add(bleDeviceSerializable);

        notifyDataSetChanged();

        if (onScanResultAddedListener != null) {
            onScanResultAddedListener.onScanResultAdded(bleDeviceSerializable);
        }
    }

    public void clearScanResults() {
        scanResults.clear();
        notifyDataSetChanged();
    }

    public void clearScanResultsExcept(BleDeviceSerializable bleDeviceSerializable) {
        for (BleDeviceSerializable device : scanResults) {
            if (!device.getMacAddress().equals(bleDeviceSerializable.getMacAddress())) {
                scanResults.remove(device);
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<BleDeviceSerializable> getDevices() {
        return scanResults;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public interface OnScanResultAddedListener {
        void onScanResultAdded(BleDeviceSerializable bleDeviceSerializable);
    }
}
