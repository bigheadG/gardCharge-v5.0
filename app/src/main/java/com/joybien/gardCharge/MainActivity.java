package com.joybien.gardCharge;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.DialogInterface.*;
import static com.joybien.gardCharge.GardChargeFragment.scheduler;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static SharedPreferences sharedPreferences;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothAdapter mBluetoothAdapter;
   // private BluetoothLeScanner mLEScanner; //API > 22
    public static boolean mConnected = false;
    public static BluetoothLeService mBluetoothLeService;


    public int actionScanMode; //1:Scan/Connect
    public static String mDeviceName;
    private String mDeviceAddress;
    private ImageView indicator;
    public static TextView mConnectionName;
    private TextView txtStatus;

    private String maString;
    private String tmpMaString;
    public TextView maTV;
    public static Menu nav_Menu;

    static Context context = null;
    private Button be,bc;
    public Boolean beflag = false;
    public Boolean bnflag = false;

    private Handler mHandlerTime = new Handler();
    public static int myTick = 0;
    public static int state_ss = 0;
    public static int testFunc = 0;
    public static int ga = 0;
    public static int gaddress=0;
    public static int testCnt = 0;
    public static int DELAY_PERIOD = 1000; //230
    public static int oFlow = 0;
    public static int activeMode = 0;
    private static FragmentTransaction ft;
    private static boolean ftFlag = false;

    public static final byte DRIVE_ON_OFF  = 0x01;
    public static final byte SET_TIME_VALUE = 0x02;
    public static final byte TIMER_ENABLE  = 0x03;
    public static final byte FACTORY_RESET = 0x05;
    public static final byte SAMPLE_TIME_INTERVAL = 0x06;
    public static final byte SET_TRIP_AMP = 0x0B;
    public static final byte SET_LOW_CURRENT = 0x0C;
    public static final byte READ_CONF_1 = 0x07;
    public static final byte READ_CONF_2 = 0x0E;
    public static final byte ECHO_DRIVE_ON_OFF = 0x41;
    public static final byte ECHO_SET_TIMER_VAL = 0x42;
    public static final byte ECHO_SET_ENABLE_TIMER = 0x43;
    public static final byte ECHO_FACTORY_RESET = 0x45;

    public static final byte ECHO_DEVICE_STATUS = 0x4A;
    public static final byte ECHO_TRIP_CURRENT = 0x4B;
    public static final byte ECHO_LOW_CURRENT = 0x4C;
    public static final byte ECHO_WARNING = 0x4D;
    public static final byte ECHO_READ_CONF_1 = 0x47;
    public static final byte ECHO_READ_CONF_2 = 0x4E;


    //private TextView tvb0,tvb1;
    final CharSequence myList[] = { "Manual Connection", "Auto Connection"};
    ArrayList<Integer> selList=new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtStatus = (TextView) findViewById(R.id.textViewBar0);
        mConnectionName = (TextView) findViewById(R.id.textViewBar1);
        indicator = (ImageView) findViewById(R.id.imageViewBar0);
        be = (Button) findViewById(R.id.editBTN);
        bc = (Button) findViewById(R.id.chargeBTN);
        maTV = (TextView) findViewById(R.id.maTV);

        be.setVisibility(View.INVISIBLE);
        bc.setVisibility(View.INVISIBLE);

        SharedPreferences sp= getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        maString = sp.getString("auto",null);
        if(maString == null) maString = "manual";

        maTV.setText("");

        //maTV.setText((maString.equals("auto")?"a":"m"));

        //mHandlerTime.post(timerRun);


        //txtStatus = (TextView) findViewById(R.id.textView2);
        //mConnectionState = (TextView) findViewById(R.id.textView3);


      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        be.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Log.v("Zach","Be Pressed:"+beflag);
                be.setBackgroundResource((!beflag)?R.drawable.edit_v120:R.drawable.edit_x120);
                beflag = !beflag;
            }
        });

        bc.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Log.v("Zach","Bn Pressed:"+bnflag);
             //   bc.setBackgroundResource((!bnflag)?R.drawable.edit_nv150:R.drawable.edit_nx150);
             //   bnflag = !bnflag;

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displaySelectedScreen(R.id.nav_address);

        mConnected = false;

        //JB added for set Setting Visible(false/true)
        nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setEnabled(false);

        sharedPreferences = getSharedPreferences("firstRunPreference", 0);
        if (FirstRun.isFirstRun() == true) {
            FirstRun.appRunned();

            initPerfData();
            readPerData();
        } else {

            readPerData();
        }

        //check device supports BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bleDialog();
            finish();
        }


        // reference http://developer.radiusnetworks.com/2015/09/29/is-your-beacon-app-ready-for-android-6.html
        // LOLLIPOP_MR1: API22, 5.1
        // M: API23, 6.0 Marshmallow
        // Great then API
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){// Android M Permission check 
        //
        //if(Build.VERSION.SDK_INT >= 23){  //checkSelfPermission is API 23
            // if(Build.VERSION.SDK_INT >  Build.VERSION_CODES.LOLLIPOP_MR1){ //LOLLIPOP_MR1 is API 22
                if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[] {
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                }
        }





    }


    private void bleDialog() {
        final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
        MyAlertDialog.setTitle("Warning Message!!!");
        String Msg = getResources().getString(R.string.ble_not_supported);
        MyAlertDialog.setMessage(Msg);
        OnClickListener OkClickD = new OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        MyAlertDialog.setNeutralButton("OK",OkClickD);
        MyAlertDialog.show();
    }


    final public void printDeviceName(String nameString){
         mConnectionName.setText(nameString);
    }
    public void setTestFunc(int func,int gai,int address){
        testFunc = 1;
        ga = gai;
        gaddress = address;
    }

    final public void chargeIndicator(Boolean c){
        bc.setVisibility((c)?View.VISIBLE:View.INVISIBLE);
    }
    final public void connectedIcon(Boolean c){
        bc.setVisibility((c)?View.VISIBLE:View.INVISIBLE);
    }

    public void scanStateMachine() {
        Log.v("MA","----scanStateMachine:actionScanMode:"+actionScanMode + " state:"+state_ss);

        switch(state_ss) {
            case 0:
                if(mConnected != true) {
                    Log.v("MA","scanStateMachine: startScanBT()");
                    GardChargeFragment.startScanBT();
                    state_ss = 1;
                }
                break;
            case 1:

                if(mConnected == true){
                    Log.v("MA","scanStateMachine: stopScanBT()");
                    GardChargeFragment.stopScanBT();
                    state_ss = 0;
                }
                break;
        }
    }



    public void testStateMachine(){
        if( mConnected != true) return;
        switch(state_ss) {
            case 0:
               if(testFunc == 1) {
                  state_ss = 1;
                   testCnt = 0;
                   testCodeOnSend();
               }
               break;
            case 1:
                testCodeOffSend();
                state_ss = 2;
                if(testCnt >= 3) state_ss  = 3;
                break;
            case 2:
                testCodeOnSend();
                state_ss = 1;
                testCnt++;
                break;
            case 3:
                testFunc = 0;
                state_ss = 0;
                break;
            default: break;
        }
    }

    static final public boolean deviceNamePermissionCheck(String deviceName){
        Log.v("MA","(MA)input for DeviceNameCheck:"+deviceName);
        return ( deviceName.contains("USB")  || deviceName.contains("PMS") ||
                deviceName.contains("CLICK") || deviceName.contains("PMD") ) ? true : false;

    }


    public void testCodeOnSend(){
        byte[] c = new byte[2];
        c[0]= (ga == 0)?(byte)0xff:((byte) ((gaddress * 2 + 1) & 0x000000FF));
        c[1]=0x05;
        sendByteACommand(c);
    }

    public void testCodeOffSend(){
        byte[] c = new byte[2];
        c[0]= (ga == 0)?(byte)0xff:((byte) ((gaddress * 2 + 1) & 0x000000FF));
        c[1]=0x00;
        sendByteACommand(c);
    }
    public void scanWhenDisconnect(){
        if( mConnected == true) return;


    }
    public void reconnectProcess(){
        if( mConnected == true) return;
        if(maString.equals("manual")) return;
        Log.v("MA","----------reconnected---------");
        SharedPreferences sp= getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        mDeviceName = sp.getString("deviceName",null);
        mDeviceAddress = sp.getString("deviceAddress",null);

       

        try {
            Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            if (mBluetoothLeService != null) {
                Log.v("MA.b","(BT)----bt.connect--------");
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public void radioDialog(){
        final AlertDialog.Builder ad = new AlertDialog.Builder(this);
        SharedPreferences sp= getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        tmpMaString = sp.getString("auto",null);
        if(tmpMaString == null) tmpMaString = "manual";

        ad.setTitle("BLE Connection : "+ tmpMaString);
        ad.setSingleChoiceItems(myList,-1,new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                tmpMaString = (index == 0) ? "manual" :"auto";
            }
        });

        ad.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sp= getSharedPreferences("address", MainActivity.MODE_PRIVATE);
                sp.edit().putString("auto",tmpMaString).commit();
                //maTV.setText((tmpMaString.equals("auto")?"a":"m"));
                maString = tmpMaString;
                dialog.dismiss();
            }
        });
        ad.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }


    private void initPerfData() {
        // TODO Auto-generated method stub
        String str;
        SharedPreferences sp= getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        sp.edit().putString("auto","manual");
        sp.edit().putString("devInfo","test,x]").commit();
        sp.edit().putString("activeDeviceName","").commit();
        for(int i=0;i<=63;i++){
            str="A"+i;

            sp.edit().putString(str,str+",0, ").commit();
            if(i<16) {
                sp.edit().putString("G" + i, "G" + i+",1, ").commit();
                sp.edit().putString("S" + i, "S" + i+",1, ").commit();
            }
        }

    }

    private void readPerData() {
        // TODO Auto-generated method stub
        String str;
        SharedPreferences sp= getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        for(int i=0;i<=63;i++){
            str="A"+i;
            Log.d("MA",str+"="+sp.getString(str,null));
            if(i<16){
                Log.d("MA",sp.getString("S"+i,null));
                Log.d("MA",sp.getString("G"+i,null));
            }
        }
        Log.d("MA",sp.getString("devInfo",null));
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        if (ftFlag == true) {
            getFragmentManager().popBackStack();
            GardChargeFragment.info0x47FlagSetByBLEService(true);
        } else if (ftFlag == false) {
            super.onBackPressed();
        }

    }



/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
      /*  if (id == R.id.action_settings) {
            return true;
        } */


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displaySelectedScreen(item.getItemId());
        return true;
    }

    public void displaySelectedScreen(int id){
        Fragment fragment = null;

        be.setVisibility(View.INVISIBLE);
        //bn.setVisibility(View.INVISIBLE);
        actionScanMode = 0;
        activeMode = 0;

        switch(id){
            case R.id.nav_address:
                fragment = new GardChargeFragment();
               // fragment = new AddressFragment();
                ftFlag = false;
                break;

            case R.id.nav_setup: //BT connection mode
                //Intent serverIntent = new Intent(this,DeviceScanActivity.class);
                //startActivityForResult(serverIntent, 1);
                actionScanMode = 1;
                GardChargeFragment.stopScanBT();
                Intent intent = new Intent(this,ScanActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_disconnect:
                if (mConnected == true) {
                    mBluetoothLeService.disconnect();
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_setting:
                //setupDialog();
                //radioDialog();
                fragment = new SettingParaFragment();
                activeMode = 1;
                ftFlag = true;
                //fragment = new SettingFragment();
                break;
            case R.id.nav_about:
                //Url
                String url = "http://www.joybien.com/";
                Uri uri = Uri.parse(url);
                Intent intentX =new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intentX);
                //fragment = new AboutFragment();
                break;
            default: break;
        }
        if(fragment != null){
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main,fragment);
            ft.commit();
            if(ftFlag == true) {
                ft.addToBackStack(null);
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void setupDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Manual/AutoConnection")
                .setTitle("Bluethooth Connection Setting");

        builder.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.v("MA","--------OK-------------");
            }
        });
        builder.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.v("MA","--------Cancel-------------");
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();

    }
   /* public static void txCommandString(String cmd){
        mBluetoothLeService.writeCharacteristic(cmd.getBytes());
    } */

    public void sendByteACommand(byte[] SendByte){
        if(mConnected == true){
            try {
                Log.v("ECHO","----sendCommand:"+SendByte.toString());
                mBluetoothLeService.writeCharacteristic(SendByte);
            } catch (Exception e5) {
                e5.printStackTrace();
            }
        } else{
//   	     Toast.makeText(this, "Check Connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendCommand(String SendString){
        if(mConnected == true){
            try {
                mBluetoothLeService.writeCharacteristic(SendString.getBytes());
            } catch (Exception e5) {
                e5.printStackTrace();
            }
        } else{
//   	     Toast.makeText(this, "Check Connection", Toast.LENGTH_SHORT).show();
        }
    }

    //example: packBTSend(12,SAMPLE_TIME_INTERVAL)
    public void packBTSend(int sData , byte mode){
        Log.v("MA","btSend:"+sData + " mode:"+mode);
        byte[] inBuf  = new byte[20];
        byte[] outBuf = new byte[20];
        for(int i=0;i<20;i++) { inBuf[i] = (byte)0xaa; outBuf[i]= (byte)0xaa;}

        outBuf[0]  = inBuf[0]  = (byte) 40;
        outBuf[19] = inBuf[19] = (byte) 41;
        outBuf[1]  = inBuf[1]  = (byte) oFlow;

        switch(mode) {
            case DRIVE_ON_OFF: //0x01 data: 0:ON 1:OFF
                inBuf[2] = DRIVE_ON_OFF;
                inBuf[3] = (byte) sData;
                break;
            case TIMER_ENABLE: //0x03 data: 0:disable 1:enable
                inBuf[2] = TIMER_ENABLE;
                inBuf[3] = (byte) sData;
                break;
            case FACTORY_RESET: //0x05
                inBuf[2] = FACTORY_RESET;
                break;
            case SAMPLE_TIME_INTERVAL: //0x06 data range:1..12     setting ts
                inBuf[2] = 0x06;
                inBuf[3] = (byte) (sData & 0x000000FF);
                break;
            case SET_TRIP_AMP: //0x0B 0..5 A => 0..50       setting ib
                inBuf[2] = 0x0B;
                inBuf[3] = (byte) (sData & 0x000000FF);
                break;
            case SET_TIME_VALUE:
                inBuf[2] = 0x02;
                inBuf[4] = (byte)(sData & 0x000000FF);
                inBuf[5] = (byte)((sData & 0x0000FF00)>>8);
                inBuf[6] = (byte)((sData & 0x00FF0000)>>16);
                inBuf[7] = (byte)((sData & 0xFF000000)>>24);
                Log.v("Send","---"+inBuf[4]+"   "+inBuf[5]+"   "+inBuf[6]+"   "+inBuf[7]+"   ");
                break;
            case READ_CONF_1:
                inBuf[2] = 0x07;
                break;
            case READ_CONF_2:
                inBuf[2] = 0x0E;
                break;
            case SET_LOW_CURRENT:
                inBuf[2] = 0x0C;
                inBuf[3] = (byte)(sData & 0x000000FF); // Li
                inBuf[4] = (byte)((sData & 0x0000FF00)>>8); // Lt
                inBuf[5] = (byte)((sData & 0x00FF0000)>>16); // Le
                break;
            default: break;

        }

        inBuf[18] = jb_computeCRC(inBuf,(byte)17);
        for(int i=2; i <18; i++) {
            outBuf[i] = (byte) (inBuf[i] ^ (( (byte)i ^ inBuf[18]) ^ (byte)0x38));
        }

        outBuf[18] = inBuf[18];

        sendByteACommand(outBuf);
        oFlow = oFlow+1;
        oFlow =  oFlow % 10;
    }

    //private static byte INITILIZATION 0xFF // init 0xFF; refer page 11 of datasheet
    //private static byte  POLYNOMIAL 0x0131  // init 0xFF; refer page 11 of datasheet
    public static final byte POLYNOMIAL  = 0x31;
    public byte jb_computeCRC(byte[] ibuf, byte len)
    {
        int i = 0;
        int j = 0;
        boolean flag = false;
        byte reg = (byte) 0xff; // ALERT: refer page 11 of data sheet

        //* algorithm: flag + shift + xor
        for (i = 0; i < len; i++){
            reg ^= ibuf[i];
            for (j = 0; j < 8; j++){ // processs (flag, shift, xor)  for 8 bits
                flag = ((reg & 0x80) > 0 ) ? true : false; // flag
                reg <<= 1; // shift
                // on {flag} xor(reg, POLYNOMIAL)
                reg ^= (flag ? POLYNOMIAL : 0); // xor
            }
        }
        return reg ;
    }

    //----------Bluetooth portion--------------

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        SharedPreferences sp= getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        switch (requestCode) {
            case 1:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // connectDevice(data, true);
                    mDeviceName = data.getStringExtra("device_name");
                    mDeviceAddress = data.getStringExtra("device_address");
                    //Back from ScanActivity


                    if(mConnected == true) {

                        String dString = sp.getString("deviceAddress",null);
                        if(mDeviceAddress != dString) {
                            mBluetoothLeService.disconnect();
                            Log.v("MA","Device(disconnect)=" + mDeviceName + "/" + mDeviceAddress);
                        }
                    }

                    if(mDeviceName != null){ //Save DeviceName & Device Address
                        mConnectionName.setText(mDeviceName);
                        sp.edit().putString("deviceName",mDeviceName).commit();
                        sp.edit().putString("deviceAddress",mDeviceAddress).commit();
                    }
                    Log.v("MA","Device=" + mDeviceName + "/" + mDeviceAddress);
                    // Connection

                    Intent gattServiceIntent = new Intent(this,
                            BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection,BIND_AUTO_CREATE);
                    Log.v("MA", "Preparing to connect");
                    if (mBluetoothLeService != null) {
                        final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                        Log.v("MA", "Connect request result=" + result);
                    } else {
                        Log.v("MA", "mBluetoothLeService is null");
                    }
                }
                break;

        }
    }
    private void updateConnectionState(final int resourceId) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText(resourceId);
            }
        });
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.v("MA", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public final void connectionShow(boolean conn){

        if(conn) {
            mConnectionName.setTextColor(Color.BLUE);
            mConnected = true;
            //indicator.setVisibility(View.VISIBLE);
            (GardChargeFragment.chainImgV).setImageResource(R.drawable.gc_chain_lk57);
        }else{
            mConnected = false;
            //txtStatus.setText("Disconnect");
            //txtStatus.setTextColor(Color.WHITE);
            mConnectionName.setTextColor(Color.WHITE);
            //indicator.setVisibility(View.INVISIBLE);
            (GardChargeFragment.chainImgV).setImageResource(R.drawable.gc_chain_bkx57);
        }
    };


    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte etd[] = new byte[40];
            final String action = intent.getAction();
             if (mBluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.v("MA","--MA-Connected-----");
                //txtStatus.setTextColor(Color.BLUE);
                //txtStatus.setText("Connected");
                 (GardChargeFragment.b1).setAlpha(0.3f);
                mConnectionName.setTextColor(Color.parseColor("#800000"));
                mConnected = true;
                //indicator.setVisibility(View.VISIBLE);
                (GardChargeFragment.chainImgV).setImageResource(R.drawable.gc_chain_lk57);
                //onImgV.setImageResource((status == 1)? R.drawable.gc_on_64:R.drawable.gc_off_64);
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
                 nav_Menu.findItem(R.id.nav_setting).setEnabled(true);
                 String addressString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_NAME);
                 Log.v("MA","--MA-Connected:"+addressString);


            } else if (mBluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //txtStatus.setText("Disconnect");
                //txtStatus.setTextColor(Color.WHITE);
                mConnectionName.setTextColor(Color.TRANSPARENT);
                 //Toast.makeText(MainActivity.this, R.string.reconnect, Toast.LENGTH_SHORT).show();
                //indicator.setVisibility(View.INVISIBLE);
                (GardChargeFragment.chainImgV).setImageResource(R.drawable.gc_chain_bkx57);
                Log.v("MA","---MA--Disconnected");
                 (GardChargeFragment.b1).setAlpha(1.0f);
                //updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                // clearUI();

                 nav_Menu.findItem(R.id.nav_setting).setEnabled(false);
            } else  if (mBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.v("MA", "ACTION_GATT_SERVICES_DISCOVERED");
                procGattServices(mBluetoothLeService.getSupportedGattServices());
            } else

                if (mBluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.v("MA", "ACTION_DATA_AVAILABLE");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                etd = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                 if(etd!= null)displayConnectedData(etd);
            }
        }
    };

    private void displayConnectedData(byte data[]) {
        if (data.length!=20)return;			//接收字串長度不到 20Bytes就 return
        if (data[0] != 40 && data[19] != 41 )return;

        Log.v("MA", "(TX):" + data + "--(RXS):" +  Arrays.toString(data));


    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void procGattServices(List<BluetoothGattService> gattServices) {

        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            Log.v("MA", "MA Service UUID=" + uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                String char_uuid = gattCharacteristic.getUuid().toString();
                Log.v("MA", "MA Characteristic UUID=" + char_uuid);

                //0000ffe2-0000-1000-8000-00805f9b34fb
                if (char_uuid.contains("0000ffe2")) {

                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(
                                mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(gattCharacteristic);
                    mNotifyCharacteristic = gattCharacteristic;
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        //intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    //onResume
    @Override
    public void onResume() {
        super.onResume();
      //  Log.v("Zach","MA-------onREsume()--------mGattUpdateReceiver register");
      //   if(mGattUpdateReceiver== null ) {
            Log.v("MA","MA-------onREsume()--------mGattUpdateReceiver register");
         registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
      // }
    }

    //onPause
    @Override
    public void onPause() {
        super.onPause();

        try {

                Log.v("MA","MA-------onPause()--------");
                unregisterReceiver(mGattUpdateReceiver);

        } catch (Exception ex) {

        }
    }

    //onDestroy
   @Override
    public void onDestroy() {
        super.onDestroy();
       Log.v("MA","-------onDestroy()--------");
        try {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        } catch (Exception ex) {

        }
    }



}

class FirstRun {
    protected static boolean isFirstRun() {
        //default value to return if this preference does not exist is <em>true</em>
        return MainActivity.sharedPreferences.getBoolean("isFirstRun", true);
    }

    protected static void appRunned() {
        SharedPreferences.Editor edit = MainActivity.sharedPreferences.edit();
        edit.putBoolean("isFirstRun", false);
        edit.commit();
    }
}