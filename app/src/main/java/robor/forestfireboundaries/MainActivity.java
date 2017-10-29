package robor.forestfireboundaries;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import robor.forestfireboundaries.bluetooth.DeviceScanActivity;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button button;
    @OnClick(R.id.button)
    public void onViewClicked() {
        open();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    private void open() {
        final Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);
    }


}
