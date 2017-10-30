package robor.forestfireboundaries.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.scan.ScanResult;

import java.util.ArrayList;
import java.util.List;

import robor.forestfireboundaries.BaseApplication;
import robor.forestfireboundaries.R;

public class ScanResultsAdapter extends ArrayAdapter<RxBleDevice> {

    private static final String TAG = ScanResultsAdapter.class.getSimpleName();

    private Context context;
    private List<RxBleDevice> scanResults;
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

        RxBleDevice scanResult = scanResults.get(position);

        TextView textViewName = (TextView) convertView.findViewById(R.id.device_name);
        TextView textViewAddress = (TextView) convertView.findViewById(R.id.device_address);

        String name = (scanResult.getName() != null) ? scanResult.getName() : "Unknown";

        ImageView selectedImageView = (ImageView)  convertView.findViewById(R.id.icon);
        ImageView connectedImageView = (ImageView) convertView.findViewById(R.id.icon_connected);

        textViewName.setText(name);
        textViewAddress.setText(scanResult.getMacAddress());

        if (BaseApplication.getMLDPConnectionService().isConnected()) {
            if (BaseApplication.getMLDPConnectionService().getConnectedDevice().getMacAddress().equals(scanResult.getMacAddress())) {
                connectedImageView.setImageResource(R.drawable.icons8_connected);
            } else {
                connectedImageView.setImageResource(R.drawable.icons8_disconnected);
            }
        } else {
            connectedImageView.setImageResource(R.drawable.icons8_disconnected);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return scanResults.size();
    }

    @Nullable
    @Override
    public RxBleDevice getItem(int position) {
        return scanResults.get(position);
    }

    public void addScanResult(ScanResult scanResult) {
        addDevice(scanResult.getBleDevice());
    }

    public void addDevice(RxBleDevice rxBleDevice) {
        for (int i = 0; i < scanResults.size(); i++) {
            if (scanResults.get(i).getMacAddress().equals(rxBleDevice.getMacAddress())) {
                scanResults.set(i, rxBleDevice);
                notifyDataSetChanged();
                return;
            }
        }

        scanResults.add(rxBleDevice);

        notifyDataSetChanged();

        if (onScanResultAddedListener != null) {
            onScanResultAddedListener.onScanResultAdded(rxBleDevice);
        }
    }

    public void clearScanResults() {
        scanResults.clear();
        notifyDataSetChanged();
    }

    public void clearScanResultsExcept(RxBleDevice rxBleDevice) {
        for (RxBleDevice device : scanResults) {
            if (!device.getMacAddress().equals(rxBleDevice.getMacAddress())) {
                scanResults.remove(device);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public interface OnScanResultAddedListener {
        void onScanResultAdded(RxBleDevice rxBleDevice);
    }
}
