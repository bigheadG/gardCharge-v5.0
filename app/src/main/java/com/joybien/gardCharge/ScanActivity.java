package com.joybien.gardCharge;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


import android.os.Handler;

import com.joybien.gardCharge.R;

//Android O   O            API 26
//Android 7.1 7.1.1 N_MR1  API 25
//Android 7.0 N            API 24
//Android 6.0 M            API 23
//-----------------------------------------
//Android 5.1 LOLLIPOP_MR1 API 22 is use StartLeScan not the Doc say use StartScan
//Android 5.0 LOLLIPOP     API


//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@TargetApi(25)
public class ScanActivity extends AppCompatActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeScanner mLEScanner; //API > 22
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private Context context;
    private ListView lvs;
    private boolean mScanning;

    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 15 seconds.
    private static final long SCAN_PERIOD = 10000; //15000

    private static int versionAPI = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        versionAPI = Build.VERSION.SDK_INT;
        Log.v("VER","Version:"+versionAPI);

        setContentView(R.layout.activity_scan);
        lvs = (ListView) findViewById(R.id.lvScan);
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT > 22) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setReportDelay(1)
                    .build();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                Log.v("Zach", "Scan Button Pressed");
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                Log.v("Zach", "Stop Button Pressed");
                break;
        }
        return true;
    }

    private void scanLeDevice(final boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;

                    if (Build.VERSION.SDK_INT > 22) {
                        try {
                            mLEScanner.stopScan(mScanCallback);
                        } catch (Exception e){
                          Log.v("Zach", " stopScan Fail");
                        }
                    } else {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            if (Build.VERSION.SDK_INT > 22) { //Original is 21 but Nexus can not work after change to 22 is scan ok
                mLEScanner.startScan(filters, settings, mScanCallback);
            } else {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {

            mScanning = false;
            if (Build.VERSION.SDK_INT > 22) {
                try {
                    mLEScanner.stopScan(mScanCallback);
                } catch (Exception e){
                    Log.v("Zach", " stopScan Fail");
                }
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
        invalidateOptionsMenu();
    }


    // Adapter for holding devices found through scanning.
    class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<String> mLeName;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mLeName = new ArrayList<String>();
            mInflator = ScanActivity.this.getLayoutInflater();
        }

        public void addInfo(String nameString , BluetoothDevice device){
             if (!mLeDevices.contains(device)) {
                  if(device.toString().contains("00:1A:C0")) {
                     mLeName.add(nameString);
                     mLeDevices.add(device);
                  }
             }
        }

/*
        public void addName(String nameString){
          //  if (!mLeName.contains(nameString)) {
                mLeName.add(nameString);
          //  }
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }
*/
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        public String getName(int position) {
            return mLeName.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            mLeName.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.

            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName    = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = mLeName.get(position);    //device.getName();

          // if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
           // else
           //     viewHolder.deviceName.setText(R.string.unknown_device);

            viewHolder.deviceAddress.setText(device.getAddress());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Log.v("SA", "(SA)-----view item pressed------:" + position);
                    final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                    final String nameString      = mLeDeviceListAdapter.getName(position);
                   // final String nameString = mLeName.get(position);
                    Log.v("SA","(SA)-:"+device + " nameString:"+nameString);

                   if (device == null) return;
                     if (mScanning) {
                        if (Build.VERSION.SDK_INT > 22) {
                            Log.v("SA","(SA)---- stopScan(>22)----");
                            try {
                                mLEScanner.stopScan(mScanCallback);
                            } catch (Exception e){
                                Log.v("SA","(SA)---- stopScan exception)----");
                            }
                         } else {
                            Log.v("SA","(SA)---- stopLeScan(<=22)----");
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                         }
                        mScanning = false;
                     }
                    Intent intent = new Intent();
                    Bundle b = new Bundle();
                    b.putString("device_name", nameString);
                    b.putString("device_address", device.getAddress());
                    Log.v("SA","(SA)To connect device: Name:"+nameString+" address:"+device.getAddress());

                    intent.putExtras(b);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
            return view;
        }
    }

    //add for > API22
    // Device scan callback.
    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        //onScanResult: Callback when a BLE advertisement has been found.
        public void onScanResult(int callbackType, ScanResult result) {

            Log.v("SA","(SA):Callback:"+ String.valueOf(callbackType));
            Log.v("SA","(SA):Callback sr:" + result.toString());
           // BluetoothDevice btDevice = result.getDevice();
           // connectToDevice(btDevice);
        }
        public String parserDeviceName(String btString){
            String nameString = "";
            String[] x = btString.split("mDeviceName=");
            if(x[1].indexOf("]") > 15 ) {
                nameString = x[1].substring(0,x[1].indexOf("]"));
            }
            return nameString;
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.v("SA", "(SA)sr: -API > 22 -----mScanCallBack------------ ");
            for (ScanResult sr : results) {
                Log.v("SA","(SA)sr:" + sr.toString());
                BluetoothDevice btDevice = sr.getDevice();
                String btDeviceString = btDevice.toString();
                String deviceName = sr.getScanRecord().getDeviceName();
              // String deviceName = parserDeviceName(sr.toString());
                Log.v("SA","(SA)btDevice:"+btDevice+ " dn:" + deviceName);

               if(deviceName != null && btDeviceString.contains("00:1A:C0")) {
                   if (((MainActivity)context).deviceNamePermissionCheck(deviceName)) {

                       Log.v("SA", "(SA)use DN:----:" + deviceName);
                       //mLeDeviceListAdapter.addName(deviceName);
                       //mLeDeviceListAdapter.addDevice(btDevice);

                       mLeDeviceListAdapter.addInfo(deviceName,btDevice);
                       mLeDeviceListAdapter.notifyDataSetChanged();
                   }
               }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.v("SA", "(SA)ScanFail-----Error Code:--------------- " + errorCode);
        }
    };

    public static int cnt =0;
    //
    // Which APIxx will call this Call Back function
    // Device scan callback. for API = 21
    // for API 21 Proved by ASUS_T00G Android 5.0
    // for API 22 is belongs to StartLeScan proved by Nexus 5 install Android 5.1.1 API22
    //
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                  //  runOnUiThread(new Runnable() {
                     //  @Override
                     //   public void run() {
                            Log.v("SA2", "(SA)Not:v7.0 ------------LeScanCallBack------------ ");
                            Log.v("SA","(SAx)den:"+device.getName() + "  addr:"+device.getAddress());
                            String deviceName = device.getName();
                            String macString  = device.getAddress();

                            if( deviceName != null &&  macString.contains("00:1A:C0")) {
                                if (((MainActivity)context).deviceNamePermissionCheck(deviceName)) {

                                    Log.v("SA", "(SAx)use:" + deviceName + " Mac:" + macString + " count:"+ cnt++);
                                         mLeDeviceListAdapter.addInfo(deviceName,device);
                                         mLeDeviceListAdapter.notifyDataSetChanged();

                                }
                            }

                      //  }
                   // });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            return; //new added
        }

        if (Build.VERSION.SDK_INT > 22) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
           settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(1)
                    .build();
            //filters = new ArrayList<ScanFilter>();
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        lvs.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }
/*
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        Intent intent = new Intent();
        Bundle b=new Bundle();
        b.putString("device_name", device.getName());
        b.putString("device_address", device.getAddress());
        intent.putExtras(b);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    */
}
