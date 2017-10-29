package robor.forestfireboundaries.bluetooth;

import java.util.UUID;

/**
 * Created by xborre on 26/10/2017.
 */

public final class Constants {

    // The MLDP UUID will be included in the RN4020 Advertising packet unless a private service and characteristic exists. In that case use the private service UUID here instead.
    public static final byte[] SCAN_RECORD_MLDP_PRIVATE_SERVICE
            = {0x00, 0x03, 0x00, 0x3a, 0x12, 0x08, 0x1a, 0x02, (byte) 0xdd, 0x07, (byte) 0xe6, 0x58, 0x03, 0x5b, 0x03, 0x00};

    // Private service for Microchip MLDP
    public static final UUID UUID_MLDP_PRIVATE_SERVICE
            = UUID.fromString("00035b03-58e6-07dd-021a-08123a000300");

    // Characteristic for MLDP Data, properties - notify, write
    public static final UUID UUID_MLDP_DATA_PRIVATE_CHAR
            = UUID.fromString("00035b03-58e6-07dd-021a-08123a000301");

    // Characteristic for MLDP Control, properties - read, write
    public static final UUID UUID_MLDP_CONTROL_PRIVATE_CHAR
            = UUID.fromString("00035b03-58e6-07dd-021a-08123a0003ff");

    // Private service for Microchip Transparent
    public static final UUID UUID_TRANSPARENT_PRIVATE_SERVICE
            = UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455");

    // Characteristic for Transparent Data from BM module, properties - notify, write, write no response
    public static final UUID UUID_TRANSPARENT_TX_PRIVATE_CHAR
            = UUID.fromString("49535343-1e4d-4bd9-ba61-23c647249616");

    // Characteristic for Transparent Data to BM module, properties - write, write no response
    public static final UUID UUID_TRANSPARENT_RX_PRIVATE_CHAR
            = UUID.fromString("49535343-8841-43f4-a8d4-ecbe34729bb3");

    // Special descriptor needed to enable notifications
    public static final UUID UUID_CHAR_NOTIFICATION_DESCRIPTOR
            = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

}
