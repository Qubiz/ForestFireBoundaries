package robor.forestfireboundaries.bluetooth;

import com.polidea.rxandroidble.RxBleDevice;

import java.io.Serializable;

import robor.forestfireboundaries.BaseApplication;

/**
 * Created by Mathijs de Groot on 25/01/2018.
 */

public class BleDeviceSerializable implements Serializable {
    private String macAddress;
    private String name;

    public BleDeviceSerializable(RxBleDevice rxBleDevice) {
        this.macAddress = rxBleDevice.getMacAddress();
        this.name = rxBleDevice.getName();
    }

    public BleDeviceSerializable(String macAddress, String name) {
        this.macAddress = macAddress;
        this.name = name;
    }

    public BleDeviceSerializable(BleDeviceSerializable bleDeviceSerializable) {
        this.macAddress = bleDeviceSerializable.getMacAddress();
        this.name = bleDeviceSerializable.getName();
    }

    public boolean isConnected() {
        if (BaseApplication.isMLDPConnectionServiceBound()) {
            if (BaseApplication.getMLDPConnectionService().isConnected()) {
                if (macAddress.equals(BaseApplication.getMLDPConnectionService().getConnectedDevice().getMacAddress())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }
}
