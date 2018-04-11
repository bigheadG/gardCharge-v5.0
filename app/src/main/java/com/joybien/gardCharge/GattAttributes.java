package com.joybien.gardCharge;

/**
 * Created by chenbighead on 2017/3/7.
 */

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
/*
    public static String BLE_UART = "180a0000-0000-1000-8000-886282273098";
    public static String BLE_RX   = "0000ffe0-0000-1000-8000-886282273098";
    public static String BLE_RX_CHARACTERISTIC = "ffe0ffe2-0000-1000-8000-886282273098";
     static {
        attributes.put("fff0fff5-0000-1000-8000-886282273098", "TX Service");
        attributes.put("ffe0ffe2-0000-1000-8000-886282273098", "RX Service");
     }
*/


    public static String BLE_UART = "180a0000-0000-1000-8000-00805f9b34fb";
    public static String BLE_RX   = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String BLE_RX_CHARACTERISTIC = "ffe0ffe2-0000-1000-8000-00805f9b34fb";
    static {
        attributes.put("fff0fff5-0000-1000-8000-00805f9b34fb", "TX Service");
        attributes.put("ffe0ffe2-0000-1000-8000-00805f9b34fb", "RX Service");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
