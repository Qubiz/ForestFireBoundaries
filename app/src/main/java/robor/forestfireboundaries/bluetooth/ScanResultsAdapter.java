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
import android.widget.TextView;

import com.polidea.rxandroidble.scan.ScanResult;

import java.util.ArrayList;
import java.util.List;

import robor.forestfireboundaries.R;

public class ScanResultsAdapter extends ArrayAdapter<ScanResult> {

    private static final String TAG = ScanResultsAdapter.class.getSimpleName();

    private Context context;
    private List<ScanResult> scanResults;
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

        ScanResult scanResult = scanResults.get(position);

        TextView textViewName = (TextView) convertView.findViewById(R.id.device_name);
        TextView textViewAddress = (TextView) convertView.findViewById(R.id.device_address);

        String name = (scanResult.getBleDevice().getName() != null) ? scanResult.getBleDevice().getName() : "Unknown";

        textViewName.setText(name);
        textViewAddress.setText(scanResult.getBleDevice().getMacAddress());

        return convertView;
    }

    @Override
    public int getCount() {
        return scanResults.size();
    }

    @Nullable
    @Override
    public ScanResult getItem(int position) {
        return scanResults.get(position);
    }

    public void addScanResult(ScanResult scanResult) {
        for (int i = 0; i < scanResults.size(); i++) {
            if (scanResults.get(i).getBleDevice().getMacAddress().equals(scanResult.getBleDevice().getMacAddress())) {
                scanResults.set(i, scanResult);
                notifyDataSetChanged();
                return;
            }
        }

        scanResults.add(scanResult);

        notifyDataSetChanged();

        if (onScanResultAddedListener != null) {
            onScanResultAddedListener.onScanResultAdded(scanResult);
        }
    }

    public void clearScanResults() {
        scanResults.clear();
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public interface OnScanResultAddedListener {
        void onScanResultAdded(ScanResult scanResult);
    }
}
