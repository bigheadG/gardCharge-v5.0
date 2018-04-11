package com.joybien.gardCharge;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.AnimationUtils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.List;
import java.lang.Object;
import java.util.Random;
import static android.content.Context.BATTERY_SERVICE;
import static com.joybien.gardCharge.MainActivity.mBluetoothLeService;
import static com.joybien.gardCharge.MainActivity.mConnected;
import static com.joybien.gardCharge.MainActivity.mConnectionName;
import static com.joybien.gardCharge.MainActivity.mDeviceName;
import static com.joybien.gardCharge.MainActivity.nav_Menu;


/**
 * Created by chenbighead on 2017/7/1.
 */
//
// GraphicView : based on http://www.android-graphview.org//
//
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@TargetApi(25)
 public class GardChargeFragment extends Fragment {
    SharedPreferences sp;
    private static Context context;
    static BluetoothAdapter mBluetoothAdapter = null;
    static BluetoothLeScanner mLEScanner = null;
    static BluetoothDevice btDevice = null; //new added
    static ScanSettings settings;
    static List<ScanFilter> filters;
    private Handler mHandler;
    static String activeDeviceName;
    static int sendCnt = 0;

    private static final long SCAN_PERIOD = 10000; //5000   15000
    static Button b0, b1, lowButton;
    static ImageView chainImgV, timerImgV, onImgV, speedImgV, chargeImgV;
    private LinearLayout layout_btn;
    static float globCurrent, globVolt, globCapacity, globR, globTc, globTci, globIb, globTs;
    final CharSequence myList[] = {};

    private String[] deviceNameArray = {};
    private String[] nickNameArray = {};
    private String[] tmpArray = {};
    static String scanAirString;
    private static TextView meterCurrent, meterVolt, meterEnergy, meterCapacity, meterTimer, meterDynamic;
    private  RadioGroup radioGroup;
    private RadioButton btnMeter, btnTimer, btnGraph;
    private static ImageButton bP, bTM, bR;
    private Button bON, bOFF;
    private static boolean clickBP = false;
    private static boolean click1 = false;
    private static boolean click2 = false;
    private static boolean flag0x07 = false;
    private ProgressBar progBar;
    private float mProgressStatus;
    private static TextView progTc, progTci;
    private float conVolt, conCurrent, conCapacity, conR, conTc, conTci, conIb, conTs, conLi;
    private String selectedDisplaDevice;
    //
    // for graph
    //
    private CircularBuffer<DataPoint> sQ1;
    private CircularBuffer<DataPoint> sQ2;


    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private LineGraphSeries<DataPoint> mSeries4;
    private LineGraphSeries<DataPoint> mSeries5;

    private Button graphBtn1, graphBtn2, graphBtn3, graphBtn4;

    private double graphLastXValue = 1d;
    private Runnable mTimer;

    private Handler mHandlerTime = new Handler();
    private static int myTick = 0;
    private static int timerStatus;
    private Display screen;
    static boolean check;
    private static Toast toast0x4D;
    private static TextView toastText0x4D;
    private static Toast toastBtn;
    private static TextView toastTextBtn;
    public static boolean scheduler = false;


    public GardChargeFragment() {
        // Required empty public constructor

    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = getActivity();
        mHandler = new Handler();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        Log.v("GC","ScreenSize(height)"+ dm.heightPixels + " width:"+dm.widthPixels);

        deviceNameArray = new String[64];
        nickNameArray = new String[64];
        tmpArray = new String[64];
        scanAirString = "";
     //   sQ1 = new CircularBuffer<DataPoint>(40);
     //   sQ2 = new CircularBuffer<DataPoint>(40);



        //sp = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT > 22) {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setReportDelay(1)
                    .build();
        } else {

        }

        sp = context.getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        activeDeviceName = sp.getString("activeDeviceName",null);
        String nickString = searchNickName(activeDeviceName);
        ((MainActivity)getActivity()).printDeviceName(activeDeviceName);
        String tmpString = sp.getString("devInfo", null);

        int count = countMatches(tmpString, "]");
        if (count != 0) {
            tmpArray = tmpString.split("]");
            for (int i = 0; i < count; i++) {
                String[] xA = {"", ""};
                xA = tmpArray[i].split(",");
                deviceNameArray[i] = xA[0];
                nickNameArray[i] = xA[1];
            }
        }
        Log.d("GC","deviceName:"+ deviceNameArray[0] + " nickName:" + nickNameArray[0]);
    }

    public static int getWidthScreen(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    public static int getHeightScreen(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }
    private static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);


        return displayMetrics;
    }
    public int countMatches(String xString, String c) {
         int count = 0;
         for( int i = 0; i<xString.length();i++)  {
           if (xString.charAt(i) == c.charAt(0)) {
             count++;
           }
         }
         return count;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getActivity();
        View v = inflater.inflate(R.layout.fragment_gardcharge, container, false);

        chainImgV = (ImageView) v.findViewById(R.id.imageChain);
        timerImgV = (ImageView) v.findViewById(R.id.imageTimer);
        onImgV = (ImageView) v.findViewById(R.id.imageOn);
        speedImgV = (ImageView) v.findViewById(R.id.imageSpeed);
        b0 = (Button) v.findViewById(R.id.imageButton);
        b1 = (Button) v.findViewById(R.id.button1);
        lowButton = (Button) v.findViewById(R.id.lowButton);
        layout_btn = (LinearLayout) v.findViewById(R.id.layout_btn);
        final View fragment_meter = (View)v.findViewById(R.id.includeMeter);
        final View timer_gardcharge = (View)v.findViewById(R.id.includeTimer);
        final View fragment_graph = (View)v.findViewById(R.id.includeGraph);
        btnMeter = (RadioButton) v.findViewById(R.id.btnMeterMode);
        btnTimer = (RadioButton)v.findViewById(R.id.btnTimerMode);
        btnGraph = (RadioButton)v.findViewById(R.id.btnGraphMode);

        bON = (Button) v.findViewById(R.id.btnON);
        bOFF = (Button) v.findViewById(R.id.btnOFF);
        bTM = (ImageButton)v.findViewById(R.id.btnTM);
        bR = (ImageButton)v.findViewById(R.id.btnRESET);
        bP = (ImageButton)v.findViewById(R.id.btnPopup);

        meterCurrent = (TextView) v.findViewById(R.id.meterCurrent);
        meterVolt = (TextView) v.findViewById(R.id.meterVolt);
        meterEnergy = (TextView) v.findViewById(R.id.meterEnergy);
        meterCapacity = (TextView) v.findViewById(R.id.meterCapacity);
        meterTimer = (TextView) v.findViewById(R.id.meterTimer);
        meterDynamic = (TextView) v.findViewById(R.id.meterDynamic);
        progBar= (ProgressBar) v.findViewById(R.id.progressBar);
        progBar.setProgress(100);
        progBar.setSecondaryProgress(100);
        progTc = (TextView) v.findViewById(R.id.progTc);
        progTci = (TextView) v.findViewById(R.id.progTci);
        final GraphView graph = (GraphView) v.findViewById(R.id.graph);
        graphBtn1 = (Button) v.findViewById(R.id.graphBtn1);
        graphBtn2 = (Button) v.findViewById(R.id.graphBtn2);
        graphBtn3 = (Button) v.findViewById(R.id.graphBtn3);
        graphBtn4 = (Button) v.findViewById(R.id.graphBtn4);
        radioGroup = (RadioGroup) v.findViewById(R.id.radioGroup);
        scheduler = false;
        info0x47FlagSetByBLEService(true);
        mConnectionName.setText(mDeviceName);
        btnTimer.setTextColor(Color.WHITE);

        /*-------------------------------------------------------------------plot graph-------------------------------------------------------------------*/
        if(mSeries1 == null) {
            Log.i("GRAPH","--mSeries1--");
            mSeries1 = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, 0)
            });
        }
        if(mSeries2 == null) {
            Log.i("GRAPH","--mSeries2--");
            mSeries2 = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, 0)
            });
        }
        if(mSeries3 == null) {
            Log.i("GRAPH","--mSeries3--");
            mSeries3 = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, 0)
            });
        }
        if(mSeries4 == null) {
            Log.i("GRAPH","--mSeries4--");
            mSeries4 = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, 0)
            });
        }
        /*
        if(mSeries5 == null) {
            Log.i("GRAPH","--mSeries5--");
            mSeries5 = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(0, 0)
            });
        }
        */

        graph.setBackgroundColor(Color.parseColor("#F5F5F5"));
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(60);
        graph.getGridLabelRenderer().setTextSize(25);

        //graph.getViewport().setYAxisBoundsManual(true);
        //graph.getViewport().setMinY(0);
        //graph.getViewport().setMaxY(6);

        mSeries1.setColor(Color.parseColor("#FF8800"));
        mSeries2.setColor(Color.parseColor("#0066FF"));
        mSeries3.setColor(Color.parseColor("#FF3333"));
        mSeries4.setColor(Color.parseColor("#00DD00"));
        //mSeries5.setColor(Color.TRANSPARENT);

        graph.addSeries(mSeries1);
        graph.addSeries(mSeries2);
        graph.addSeries(mSeries3);
        graph.addSeries(mSeries4);
        //graph.addSeries(mSeries5);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*------------------------------------------------------------Scheduler State------------------------------------------------------------*/
        Runnable drawTimerRun = new Runnable() {
            @Override
            public void run() {
                if (scheduler == false){
                    if (myTick % 2 == 0) {
                        if (timer_gardcharge.getVisibility() == View.VISIBLE) {
                            progressBarTest();
                        }
                        if (mConnected == true) {
                            chainImgV.setImageResource(R.drawable.gc_chain_lk57);
                            layout_btn.setAlpha(1.0f);
                            drawGraph();
                        } else if (mConnected == false) {
                            chainImgV.setImageResource(R.drawable.gc_chain_bkx57);
                            layout_btn.setAlpha(0.3f);
                        }
                    }
                    if (myTick % 2 == 0 && scheduler == false && mConnected == true) {
                        nav_Menu.findItem(R.id.nav_disconnect).setEnabled(true);
                        nav_Menu.findItem(R.id.nav_setup).setEnabled(true);
                        nav_Menu.findItem(R.id.nav_setting).setEnabled(true);
                    }
                    if(myTick % 3 == 0 && mConnected == false) {
                        progTc.setText(R.string.proTc);
                        progTci.setText(R.string.proTci);
                    }
                    if(myTick % 4 == 0 && mConnected == false){
                        meterCurrent.setText(R.string.Amp);
                        meterVolt.setText(R.string.Volt);
                        meterEnergy.setText(R.string.Energy);
                        meterCapacity.setText(R.string.Capacity);
                        meterTimer.setText(R.string.Timer);
                        meterDynamic.setText(R.string.Dynamic);
                    }
                    if(myTick % 5 == 0 && mConnected == false){
                        graphBtn1.setText(R.string.graphCurrent);
                        graphBtn2.setText(R.string.graphVoltage);
                        graphBtn3.setText(R.string.graphEnergy);
                        graphBtn4.setText(R.string.graphCap);
                    }

                Log.v("GC", "GardChargeFragment-tick:" + myTick);
                myTick++;
                mHandlerTime.postDelayed(this, 500);
                }
            }
        };
        mHandlerTime.post(drawTimerRun);



        btnMeter.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                btnMeter.setTextColor(Color.WHITE);
                btnTimer.setTextColor(Color.parseColor("#FF8800"));
                btnGraph.setTextColor(Color.parseColor("#FF8800"));
                if(timer_gardcharge.isShown()){
                    includeAnimation(fragment_meter, 800, true);
                    includeAnimation(timer_gardcharge, 500, false);
                }
                if(fragment_graph.isShown()){
                    includeAnimation(fragment_meter, 800, true);
                    includeAnimation(fragment_graph, 500, false);
                }
                fragment_meter.setVisibility(View.VISIBLE);
                timer_gardcharge.setVisibility(View.INVISIBLE);
                fragment_graph.setVisibility(View.INVISIBLE);
            }});

        btnTimer.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                btnMeter.setTextColor(Color.parseColor("#FF8800"));
                btnTimer.setTextColor(Color.WHITE);
                btnGraph.setTextColor(Color.parseColor("#FF8800"));
                if(fragment_meter.isShown()){
                    includeAnimation(timer_gardcharge, 800, true);
                    includeAnimation(fragment_meter, 500, false);
                }
                if(fragment_graph.isShown()){
                    includeAnimation(timer_gardcharge, 800, true);
                    includeAnimation(fragment_graph, 500, false);
                }
                timer_gardcharge.setVisibility(View.VISIBLE);
                fragment_meter.setVisibility(View.INVISIBLE);
                fragment_graph.setVisibility(View.INVISIBLE);
            }});

        btnGraph.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                btnMeter.setTextColor(Color.parseColor("#FF8800"));
                btnTimer.setTextColor(Color.parseColor("#FF8800"));
                btnGraph.setTextColor(Color.WHITE);
                if(fragment_meter.isShown()){
                    includeAnimation(fragment_graph, 800, true);
                    includeAnimation(fragment_meter, 500, false);
                }
                if(timer_gardcharge.isShown()){
                    includeAnimation(fragment_graph, 800, true);
                    includeAnimation(timer_gardcharge, 500, false);
                }
                fragment_meter.setVisibility(View.INVISIBLE);
                timer_gardcharge.setVisibility(View.INVISIBLE);
                fragment_graph.setVisibility(View.VISIBLE);
            }});

        b0.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){ //test

             //   mBluetoothAdapter.getBluetoothLeScanner();
             /* if(mLEScanner != null) {
              mLEScanner.startScan(filters, settings, mScanCallback);
              } */

              //startScanBT();

            }
        });

        b1.setAlpha(((MainActivity.mConnected == true) ? 0.3f : 1.0f));
        b1.setOnClickListener(new Button.OnClickListener(){

            public void onClick(View v){ //test
               //Connection mode only
                if(b1.getAlpha() != 1.0f) return;
               if(scanAirString.length() == 0) {
                   Toast.makeText(context, "No device On Air, Please Pressed Again.",Toast.LENGTH_SHORT).show();
               }else {
                   radioDialog();
               }
            }
        });


        activeDeviceName = sp.getString("activeDeviceName",null);

        bP.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){

                int width = getWidthScreen(context);
                int height = getHeightScreen(context);

                if (mConnected == true) {
                    if(clickBP == false){
                        bP.setImageResource(R.drawable.menu_on);
                        rgAnimation(radioGroup, 0.0f, 0.0f, 200.0f, 0.0f,200,true);
                        btnAnimation(bON,     -170.0f, 0.0f,   0.0f, 0.0f,200,true);
                        btnAnimation(bOFF,     150.0f, 0.0f,   0.0f, 0.0f,200,true);
                        btnAnimation(bTM,       110.0f, 0.0f,  110.0f, 0.0f,200,true);
                        btnAnimation(bR,       -100.0f, 0.0f,  110.0f, 0.0f,200,true);
                        click2 = true;
                        clickBP = true;
                    }else if(clickBP == true){
                        bP.setImageResource(R.drawable.menu_off);
                        if(click1 == true){
                            rgAnimation(radioGroup,0.0f,  0.0f, 0.0f, 200.0f,200,false);
                            click1 = false;
                        }else {
                            rgAnimation(radioGroup, 0.0f, 0.0f, 0.0f, 200.0f, 200, false);
                            btnAnimation(bON, 0.0f, -170.0f, 0.0f, 0.0f, 200, false);
                            btnAnimation(bOFF, 0.0f, 150.0f, 0.0f, 0.0f, 200, false);
                            btnAnimation(bTM, 0.0f, 110.0f, 0.0f, 110.0f, 200, false);
                            btnAnimation(bR, 0.0f, -100.0f, 0.0f, 110.0f, 200, false);
                            click2 = false;
                        }
                        clickBP = false;
                    }
                }

                if(mConnected == false){
                    if(clickBP == false) {
                        bP.setImageResource(R.drawable.menu_on);
                        rgAnimation(radioGroup,0.0f, 0.0f, 200.0f, 0.0f,200,true);
                        click1 = true;
                        clickBP = true;
                    }else if(clickBP == true){
                        bP.setImageResource(R.drawable.menu_off);
                        if(click2 == true){
                            rgAnimation(radioGroup, 0.0f, 0.0f, 0.0f, 200.0f, 200, false);
                            btnAnimation(bON, 0.0f, -170.0f, 0.0f, 0.0f, 200, false);
                            btnAnimation(bOFF, 0.0f, 150.0f, 0.0f, 0.0f, 200, false);
                            btnAnimation(bTM, 0.0f, 110.0f, 0.0f, 110.0f, 200, false);
                            btnAnimation(bR, 0.0f, -100.0f, 0.0f, 110.0f, 200, false);
                            click2 = false;
                        }else {
                            rgAnimation(radioGroup, 0.0f, 0.0f, 0.0f, 200.0f, 200, false);
                            click1 = false;
                        }
                        clickBP = false;
                    }
                }
            }
        });
        bON.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                ((MainActivity) getActivity()).packBTSend(1, MainActivity.DRIVE_ON_OFF);
            }
        });
        bOFF.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                ((MainActivity) getActivity()).packBTSend(0, MainActivity.DRIVE_ON_OFF);
            }
        });
        bR.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                if(mConnected == true)
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle(R.string.factory_setting_title)
                        .setMessage(R.string.factory_setting_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ((MainActivity) getActivity()).packBTSend(0, MainActivity.FACTORY_RESET);

                                //LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                //View view = inflater.inflate(R.layout.progressbar_center, null);
                                //view.bringToFront();

                                    new AlertDialog.Builder(context)
                                            .setCancelable(false)
                                            .setTitle(R.string.setting_title)
                                            .setMessage(R.string.setting_msg)
                                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    mBluetoothLeService.disconnect();
                                                }
                                            })
                                            .show();
                            }
                        })
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
        bTM.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                String tagString = String.valueOf(timerImgV.getTag());
                ((MainActivity) getActivity()).packBTSend((tagString.equals("off")) ? 1 : 0 , MainActivity.TIMER_ENABLE);
                //bTM.setImageResource((timerStatus == 0)? R.mipmap.timer:R.mipmap.timerx);
            }
        });
        return v;
    }

    private void drawGraph(){
        if (mConnected == true) {
            graphLastXValue += 1d;
            float displayCapacity;
            float scale = (int)conCapacity/7+1;
            float scale1 = (float)(1.0/scale);
            float u = (float)(Math.round(scale1*100000))/100000;
            displayCapacity = conCapacity * u;

            graphBtn1.setText(String.format("%.3fA", conCurrent));
            graphBtn2.setText(String.format("%.3fV", conVolt));
            graphBtn3.setText(String.format("%.3fW", conVolt * conCurrent));
            graphBtn4.setText(String.format("%.1fmAH\nu:%.5f", conCapacity,u));

            mSeries1.appendData(new DataPoint(graphLastXValue, conCurrent), false, 200);
            mSeries2.appendData(new DataPoint(graphLastXValue, conVolt), false, 200);
            mSeries3.appendData(new DataPoint(graphLastXValue, conCurrent * conVolt), false, 200);
            mSeries4.appendData(new DataPoint(graphLastXValue, displayCapacity), false, 200);

            if (graphLastXValue >= 60d) {
                mSeries1.appendData(new DataPoint(graphLastXValue, conCurrent), true, 200);
                mSeries2.appendData(new DataPoint(graphLastXValue, conVolt), true, 200);
                mSeries3.appendData(new DataPoint(graphLastXValue, conCurrent * conVolt), true, 200);
                mSeries4.appendData(new DataPoint(graphLastXValue, displayCapacity), false, 200);
            }
        }
    }

/*-------------------Button Animation--------------------*/
    public void rgAnimation(RadioGroup btn, float x1, float x2, float y1, float y2, int duration ,boolean dir) {
        Animation me1 = new TranslateAnimation(x1,x2,y1,y2);   //TranslateAnimation(float x1, float x2, float y1, float y2)  ,  (x1,y1)移動到(x2,y2)
        me1.setDuration(duration);                //setDuration (long durationMillis) 設定動畫開始到結束的執行時間
        me1.setRepeatCount(0);               //setRepeatCount (int repeatCount) 設定重複次數 -1為無限次數 0
        Animation me2 = new AlphaAnimation((dir)?0:1 ,(dir)?1:0);    // 透明度
        me2.setDuration(duration);
        me2.setRepeatCount(0);
        // 動畫集合
        AnimationSet me = new AnimationSet(false);
        me.addAnimation(me1);
        me.addAnimation(me2);
        // 圖片配置動畫
        btn.setAnimation(me);
        // 動畫開始
        me.startNow();
        btn.setVisibility((dir)?btn.VISIBLE : btn.INVISIBLE);
    }

    public void btnAnimation(ImageButton btn, float x1, float x2, float y1, float y2, int duration ,boolean dir) {
        Animation me1 = new TranslateAnimation(x1,x2,y1,y2);   //TranslateAnimation(float x1, float x2, float y1, float y2)  ,  (x1,y1)移動到(x2,y2)
        me1.setDuration(duration);                //setDuration (long durationMillis) 設定動畫開始到結束的執行時間
        me1.setRepeatCount(0);                   //setRepeatCount (int repeatCount) 設定重複次數 -1為無限次數 0
        Animation me2 = new AlphaAnimation((dir)?0:1 ,(dir)?1:0);
        me2.setDuration(duration);
        me2.setRepeatCount(0);
        // Animation Set
        AnimationSet me = new AnimationSet(false);
        me.addAnimation(me1);
        me.addAnimation(me2);
        // Set Animation
        btn.setAnimation(me);
        // start Animation
        me.startNow();
        btn.setVisibility((dir)?btn.VISIBLE : btn.INVISIBLE);
    }

    public void btnAnimation(Button btn,float x1,float x2, float y1,float y2, int duration , boolean dir) {
        Animation me1 = new TranslateAnimation(x1,x2,y1,y2);   //TranslateAnimation(float x1, float x2, float y1, float y2)  ,  (x1,y1)移動到(x2,y2)
        me1.setDuration(duration);                //setDuration (long durationMillis) 設定動畫開始到結束的執行時間
        me1.setRepeatCount(0);               //setRepeatCount (int repeatCount) 設定重複次數 -1為無限次數 0
        Animation me2 = new AlphaAnimation((dir)?0:1 ,(dir)?1:0);    // 透明度
        me2.setDuration(duration);
        me2.setRepeatCount(0);
        // 動畫集合
        AnimationSet me = new AnimationSet(false);
        me.addAnimation(me1);
        me.addAnimation(me2);
        // 圖片配置動畫
        btn.setAnimation(me);
        // 動畫開始
        me.startNow();
        btn.setVisibility((dir)?btn.VISIBLE : btn.INVISIBLE);
    }

    public void includeAnimation(View view, int duration ,boolean dir) {
        Animation am = new AlphaAnimation((dir)?0:1 ,(dir)?1:0);
        am.setDuration(duration);
        am.setRepeatCount(0);
        view.setAnimation(am);
        am.startNow();
    }

    public void progressBarTest() {
        mProgressStatus = 0;

        if(mConnected == true){mProgressStatus = conTc / conTci;}
        else{mProgressStatus = globTc / globTci;}

        final int nProgressStatus = (int) (mProgressStatus * 100);
        progBar.setProgress(nProgressStatus);

    }

    public void progressBarTest1() {

        new Thread(new Runnable() {
            public void run() {
                mProgressStatus = 0;
                while (mProgressStatus < 100) {

                    if(mConnected == true){mProgressStatus = conTc / conTci;}
                    else{mProgressStatus = globTc / globTci;}

                    final int nProgressStatus = (int) (mProgressStatus * 100);
                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                                progBar.setProgress(nProgressStatus);
                            Log.v("GC","proBar-Tick:"+nProgressStatus);
                        }
                    });
                    try {

                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
           }
        }).start();
    }

    private static void makeTextAndShow0x4D(final Context context, final String text, final int duration) {
        if (toast0x4D == null) {
            //如果還沒有建立過Toast，才建立
            final ViewGroup toastView = new FrameLayout(context);
            final FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            final GradientDrawable background = new GradientDrawable();
            toastText0x4D = new TextView(context);
            toastText0x4D.setLayoutParams(flp);
            toastText0x4D.setSingleLine(false);
            toastText0x4D.setTextSize(18);
            toastText0x4D.setTextColor(Color.argb(0xFF, 0xFF, 0x00, 0x00));
            background.setColor(Color.argb(0xE1, 0xFF, 0xFF, 0xFF));
            background.setCornerRadius(20);
            toastView.setPadding(30, 30, 30, 30);
            toastView.addView(toastText0x4D);
            toastView.setBackgroundDrawable(background);

            toast0x4D = new Toast(context);
            toast0x4D.setView(toastView);
        }
        toastText0x4D.setText(text);
        toast0x4D.setDuration(duration);
        toast0x4D.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast0x4D.show();
    }

    private static void makeTextAndShowBtn(final Context context, final String text, final int duration) {
        if (toastBtn == null) {

            final ViewGroup toastView = new FrameLayout(context);
            final FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            final GradientDrawable background = new GradientDrawable();
            toastTextBtn = new TextView(context);
            toastTextBtn.setLayoutParams(flp);
            toastTextBtn.setSingleLine(false);
            toastTextBtn.setTextSize(18);
            toastTextBtn.setTextColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
            background.setColor(Color.argb(0xD2, 0xFF, 0x92, 0x24));
            background.setCornerRadius(20);
            toastView.setPadding(30, 30, 30, 30);
            toastView.addView(toastTextBtn);
            toastView.setBackgroundDrawable(background);

            toastBtn = new Toast(context);
            toastBtn.setView(toastView);
        }
        toastTextBtn.setText(text);
        toastBtn.setDuration(duration);
        toastBtn.show();
    }

    static void startScanBT() {
        Log.v("GC","----------startScanBT-------------");
        if (Build.VERSION.SDK_INT > 22) {
            mLEScanner.startScan(filters, settings, mScanCallback);
        } else {

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        scanAirString  = "";
    }


    static void stopScanBT() {
        if (Build.VERSION.SDK_INT > 22) {
            mLEScanner.stopScan(mScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    //for BluetoothLeService.java
    static void info0x47FlagSetByBLEService(boolean status){
        flag0x07 = status;
    }
    private static ScanCallback mScanCallback = new ScanCallback() {
        @Override
        //onScanResult: Callback when a BLE advertisement has been found.
        public void onScanResult(int callbackType, ScanResult result) {
            Log.v("MFG", String.valueOf(callbackType));
            Log.v("MFG", result.toString());
        }


        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                 Log.v("MFG", sr.toString());
                parserMFGMeterData(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.v("MFG", "------------Error Code:--------------- " + errorCode);
        }
    };
    static public String int2TimeString(int time) {
        int hh = time / 3600;
        int ss = time % 60;
        int  mm = (time % 3600 - ss)/60;
        String str = String.format("%02d:%02d:%02d",hh, mm, ss);
        return str;
    };

    //V/Zach:11: ScanResult{mDevice=00:1A:C0:55:55:55, mScanRecord=ScanRecord
    // [mAdvertiseFlags=26,
    // mServiceUuids=null,
    // mManufacturerSpecificData={16970=[48, 50, 40, 55, 18, 92, 86, 78, 89, 88, 2, -114, 81, 84, 75, -62, -39, 81, -80, -79, 103, 41, -57]}
    // , mServiceData={}, mTxPowerLevel=0, mDeviceName=USBC.    555555E630]
    // , mRssi=-73, mTimestampNanos=190854729186332}
    //
    //V/Zach:11: ----:USBC.    555555E630
    //V/Zach:11: 02 01 1A
    //           1A FF 4A 42 30 32 28 37 12 5C 56 4E 59 58 02 8E 51 54 4B C2 D9 51 B0 B1 67 29 C7
    //          len  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
    //                  J  B  0  2  [  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  ]
    //                              0                                                       19
    //           14 09 55 53 42 43 2E 20 20 20 20 35 35 35 35 35 35 45 36 33 30 05 12 50 00 20 03 02 0A 00
    // 02011A1AFF4A42303228370A55E3E578505F1B6E5C6998F3F2EDEC6F29C71409555342432E2020202037373737373745363330051250002003020A00
    //


    static public void parserMFGMeterData(ScanResult sr){
        byte[] mfg = new byte[20];
        btDevice = sr.getDevice();
        Log.v("MFG","(MFG)sr:"+sr.toString());
        //(1)Parser Name and filter out
        //String deviceName = btDevice.getName();
        String deviceName = parserDeviceNameFromBT(sr.toString());
        if(deviceName.length() < 3) return;

       Log.v("MFG","(MFG)-DN(p):"+deviceName + " btD:"+ btDevice);

        //(2) filter out non "USB" device information
        if (((MainActivity)context).deviceNamePermissionCheck(deviceName) && btDevice.toString().contains("00:1A:C0")) {
            //(2.1) append DeviceName to scanAirString
            //scaAirString is only append USBxxxx name
           /* if(!scanAirString.contains(deviceName)) {
                if(scanAirString.length() == 0 ) scanAirString = deviceName+ ","+ btDevice+ "]";
                else scanAirString = scanAirString  + deviceName +","+btDevice  +"]";
            } */

            if(!scanAirString.contains(deviceName)) {
                if(scanAirString.length() == 0 ) scanAirString = deviceName + "]";
                else scanAirString = scanAirString  + deviceName  +"]";
            }

            Log.v("MFG","(GC)AirString:"+ scanAirString);


            //(2.2) check deviceName Length > 3 if not return
            if (deviceName.length() > 3) {
                //(2.2.1) check activeDeviceName is not return
                if(!deviceName.equals(activeDeviceName)) return;

                Log.v("MFG", "(GC) ScanType:MFG:" + deviceName);
                byte[] x = sr.getScanRecord().getBytes();
                //(2.2.1.1) Alignment buffer for easy read
                for(int i = 9; i < 24+5 ;i++){
                    mfg[i-9] = x[i];
                    //Log.v("Zach:11","data["+ (i-9) + "]="+ mfg[i-9] );
                }

                //(2.2.1.2) decrypt
                for(int i = 2 ; i <18 ; i++ ){
                    mfg[i] = (byte) (mfg[i] ^ (((i & 0xFF) ^ mfg[18]) ^ 0x38));
                }

                if((x[5] == 0x4A) && (x[6] == 0x42) && (x[9] == 0x28) && (x[28] == 0x29)) {
                    //28 39 0F 40 70 54 44 45 CD 21 4A 49 4E 4F 4C 4D AD AC 7A 29
                    // [                                                        ]
                    // 0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  9
                     parsernDisplayScanData(mfg);

                }
            }
        }
    }


    public static void parsernDisplayScanData(byte[] mfg) {
        // Data from Air
     /*   byte[] mfg = new byte[20];
        for(int i = 9; i < 24+5 ;i++){
                    mfg[i-9] = sr[i];
                    //Log.v("Zach:11","data["+ (i-9) + "]="+ mfg[i-9] );
        }

                //(2.2.1.2) decrypt
        for(int i = 2 ; i <18 ; i++ ){
                    mfg[i] = (byte) (mfg[i] ^ (((i & 0xFF) ^ mfg[18]) ^ 0x38));
        } */

        if(mfg[2] == 0x4f) {
            Log.v("Zach:11", "0x4F:"+ bytesToHex(mfg));
            int status  = (mfg[3] & 0xff);
            int volt    = (mfg[4] & 0xff)    | (mfg[5] & 0xff) << 8 ;
            int current = (mfg[6] & 0xff)    | (mfg[7] & 0xff) << 8 ;
            int capacity = (mfg[8] & 0xff)   | (mfg[9] & 0xff)  << 8 | (mfg[10] & 0xff) << 16 | (mfg[11] & 0xff) << 24;
            int tc       = (mfg[12] & 0xff)  | (mfg[13] & 0xff) << 8 | (mfg[14] & 0xff) << 16 | (mfg[15] & 0xff) << 24;
            int r        = (mfg[16] & 0xff)  | (mfg[17] & 0xff) << 8 ;

            //Global Data
            globVolt = (float)volt / 1000;
            globCurrent = (float)current / 1000;
            globCapacity = (float)capacity /1000;
            globR = (float)r /1000;
            globTc = (float) tc /1000;


            if(globVolt <= 0.0) { globVolt = 0.0f;};

            if(globCurrent <= 0.0) { globCurrent = 0.0f;};



            ((MainActivity)context).chargeIndicator((globCurrent > 0.03)? true:false);
            if(status == 1 || status == 0) {
                onImgV.setImageResource((status == 1) ? R.drawable.gc_on_64 : R.drawable.gc_off_64);
            }
            //顯示空中的資料(電流，電壓，功率，容量，電阻)
            if(current != 43690 ){ //0xaaaa
                meterCurrent.setText(String.valueOf(globCurrent));
                meterEnergy.setText(String.valueOf(String.format("%.3f",globVolt * globCurrent)));
            }
            meterVolt.setText(String.valueOf(globVolt));

            meterCapacity.setText(String.valueOf(String.format("%.1f",globCapacity)));
            if(r != 65535) meterDynamic.setText(String.valueOf(globR+"Ω"));

            //顯示空中的資料(倒數時間，prog倒數時間，prog設定的時間)

            meterTimer.setText("Timer:" + int2TimeString((int)globTc));

        }

        //空中的設定值
        if(mfg[2] == 0x5f) {
            // Log.v("Zach:11", "decrypt:"+ bytesToHex(mfg));
            Log.v("Zach:11", "0x5F:"+ bytesToHex(mfg));
            int e       = (mfg[3] & 0xff);
            int tc      = (mfg[4] & 0xff)   | (mfg[5] & 0xff)  << 8 | (mfg[6] & 0xff) << 16 | (mfg[7] & 0xff) << 24;
            int tci     = (mfg[8] & 0xff)   | (mfg[9] & 0xff)  << 8 | (mfg[10] & 0xff) << 16 | (mfg[11] & 0xff) << 24;
            int ib      = (mfg[12] & 0xff) ;
            int ts      = (mfg[13] & 0xff) ;

            //Global Data
            globTc = (float)tc / 1000;
            globTci = (float)tci / 1000;
            globIb = (float)ib /10;
            globTs = ts;

            if(e == 1 || e == 0) {
                timerImgV.setImageResource((e == 1) ? R.drawable.gc_timer_64 : R.drawable.gc_timer_64x);
                bTM.setImageResource((e == 1)? R.drawable.timer:R.drawable.timerx);
                timerImgV.setTag((e == 1)? "on":"off");
                timerStatus = e;
            }
            String ibString = globIb + "A";
            b0.setText(ibString);

            meterTimer.setText("Timer:" + int2TimeString((int)globTc));
            progTc.setText(int2TimeString((int)globTc));
            progTci.setText(int2TimeString((int)globTci));

        }
    }

    static public String parserDeviceNameFromBT(String btString){
        String nameString = "";
        String[] x = btString.split("mDeviceName=");
        if(x[1].indexOf("]") > 15 ) {
            nameString = x[1].substring(0,x[1].indexOf("]"));
        }
        return nameString;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String searchNickName(String nameString){

        String[] tmpA  = new String[64];
        sp = context.getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        String tmpString = sp.getString("devInfo", null);

        int count = countMatches(tmpString, "]");
        if (count != 0) {
            tmpA = tmpString.split("]");
            for (int i = 0; i < count; i++) {
                String[] xA = {"", ""}; //xA[0,1]:[deviceName,nickName]
                xA = tmpA[i].split(",");
                if(xA[0].equals(nameString)){
                   return (xA[1].length() == 0) ? nameString : xA[1];
                }
            }
        }
        return nameString;
    }
    //For APIs less than 21, Returns Device UUID
    public String getUUID(AdvertiseData data){
        List<ParcelUuid> UUIDs = data.getServiceUuids();
        //ToastMakers.message(scannerActivity.getApplicationContext(), UUIDs.toString());
        String UUIDx = UUIDs.get(0).getUuid().toString();
        Log.e("UUID", " as list ->" + UUIDx);
        return UUIDx;
    }


    // form ScanRecord
    // String advString=bytesToHex(scanRecord).toString();
    //:02 01 1A    1A FF 4A 42 30 32 28 36 07 58 D2 0D 6B 09 06 07 04 05 7E 0E FE FF E0 E1 62 29 C7
    //            len  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
    //                    J  B  0  2  [  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  ]
    //  0  1  2    3   4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8
    //            1409555342432E2020202033333333333345363330051250002003020A000000
    //
    // Device scan callback. under API 23
    private static BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                  /*  runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addName(device.getName());
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    }); */
                    byte[] mfg = new byte[20];
                    String deviceName = device.getName();
                    String macString  = device.getAddress();


                    if( deviceName != null &&  macString.contains("00:1A:C0")) {
                       if (((MainActivity)context).deviceNamePermissionCheck(deviceName)) {

                           if(!scanAirString.contains(deviceName)) {
                               if(scanAirString.length() == 0 ) scanAirString = deviceName + "]";
                               else scanAirString = scanAirString  + deviceName  +"]";
                           }

                           Log.v("GC","(GC)API21 AirString:"+ scanAirString);

                           if(!deviceName.equals(activeDeviceName)) return;

                        //if(deviceName.contains("USB") || deviceName.contains("CLICK") || deviceName.contains("PMD") || deviceName.contains("INFOS")) {
                            Log.v("GC", "(GCx)use:" + deviceName + " Mac:" + macString );

                            if((scanRecord[5] == 0x4A) && (scanRecord[6] == 0x42) && (scanRecord[9] == 0x28) && (scanRecord[28] == 0x29))
                            {
                                String advString = bytesToHex(scanRecord).toString();
                                Log.v("GC", "(GC)Not:v7.0 -ADv--LeScanCallBack:" + advString);

                                for(int i = 9; i < 24+5 ;i++){
                                    mfg[i-9] = scanRecord[i];
                                    //Log.v("Zach:11","data["+ (i-9) + "]="+ mfg[i-9] );
                                }

                                //(2.2.1.2) decrypt
                                for(int i = 2 ; i <18 ; i++ ){
                                    mfg[i] = (byte) (mfg[i] ^ (((i & 0xFF) ^ mfg[18]) ^ 0x38));
                                }
                                Log.v("GC", "(GC)Not:v7.0 decrypt:" + bytesToHex(mfg).toString());

                                parsernDisplayScanData(mfg);


                               //  if(!deviceScanList.contains(macString)) {
                                //   mLeDeviceListAdapter.addInfo(deviceName,device);
                                //   mLeDeviceListAdapter.notifyDataSetChanged();
                                //  }

                                //Log.v("SA","(SAx) scanList:"+deviceScanList);
                            }
                        }
                    }
                }
            };

    private static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    public void radioDialog(){
        final android.support.v7.app.AlertDialog.Builder ad = new android.support.v7.app.AlertDialog.Builder(context);
        ad.setIcon(R.drawable.gardsave162);
        //tmpMaString = sp.getString("auto",null);
        //if(tmpMaString == null) tmpMaString = "manual";

        String[] sA = {};

        int cnt = countMatches(scanAirString,"]");
        if(cnt > 0)  sA = scanAirString.split("]");

        ad.setTitle(R.string.selectName);

        final String[] finalSA =  sA;
        final String[] selectedName = {""};

        Log.v("USB:1", "------RadioDialog------------");
        ad.setSingleChoiceItems(finalSA, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                selectedName[0] = finalSA[index];
                Log.v("USB:1", "selected:" + selectedName[0]);
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                activeDeviceName = selectedName[0];
                saveActiveDeviceName(activeDeviceName);
                Log.v("USB:1.","activeName:"+ activeDeviceName);
                dialog.dismiss();
            }
        });
       /*
        ad.setNeutralButton("Reflash",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] xxA = {};

                Toast.makeText(context, "Reflash",Toast.LENGTH_SHORT).show();
                //ad.setMessage("Test Ok");
                dialog.dismiss();
                ad.create();
                ad.show();

            }
        });
        */
        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }


    public void saveActiveDeviceName(String dName){
        SharedPreferences sp = context.getSharedPreferences("address", MainActivity.MODE_PRIVATE);
        activeDeviceName = dName;
        sp.edit().putString("activeDeviceName", activeDeviceName).commit();
        String nickString = searchNickName(activeDeviceName);
        ((MainActivity)getActivity()).printDeviceName(nickString);
    };

    public final BroadcastReceiver gcGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte etd[] = new byte[40];
            final String action = intent.getAction();
            String macString;
         /*  if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.v("Zach","-GC--Connected-----");
                ((MainActivity)getActivity()).connectionShow(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.v("Zach","-GC--DisConnected-----");
                ((MainActivity)getActivity()).connectionShow(false);
            } else */
           // Log.v("Zach", "-GC--rxv:"+ action);
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //Log.v("Zach", "-GC--ACTION_DATA_AVAILABLE:" );
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                etd = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                macString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_NAME);
                Log.v("Zach", "(BLEIN)-ACTION_DATA_AVAILABLE:"+macString );
                if(etd!= null)displayConnectedData(etd,macString);
            }
        }

    };

    private void displayConnectedData(byte data[], String macString ) {
        byte[] iBuf = new byte[20];
        if (data.length != 20) return;          //接收字串長度不到 20Bytes就 return
        if (data[0] != 40 && data[19] != 41) return;
        // Log.v("RXV", "(TX):--(ORI):" +  Arrays.toString(data));
        //decrypt
        for (int i = 0; i < 20; i++) {
            if (i == 0 || i == 1 || i == 19 || i == 18) {
                iBuf[i] = data[i];
            } else {
                iBuf[i] = (byte) (data[i] ^ (((i & 0xFF) ^ data[18]) ^ 0x38));
            }
        }
        Log.v("GC", "(CONN)inData:" + Arrays.toString(iBuf));

        // 連結後的資料
        if (iBuf[2] == MainActivity.ECHO_DEVICE_STATUS) { // 0x4A = 74
            int status   = (iBuf[3] & 0xff);
            int volt     = (iBuf[4] & 0xff)  | ( iBuf[5] & 0xff) << 8;
            int current  = (iBuf[6] & 0xff)  | ( iBuf[7] & 0xff) << 8;
            int capacity = (iBuf[8] & 0xff)  | ( iBuf[9] & 0xff) << 8 | (iBuf[10] & 0xff) << 16 | (iBuf[11] & 0xff) << 24;
            int tc       = (iBuf[12] & 0xff) | (iBuf[13] & 0xff) << 8 | (iBuf[14] & 0xff) << 16 | (iBuf[15] & 0xff) << 24;
            int r        = (iBuf[16] & 0xff) | (iBuf[17] & 0xff) << 8;

            volt    = (iBuf[4] == 0xaa && iBuf[5] == 0xaa) ? 0 : volt;
            current = (iBuf[6] == 0xaa && iBuf[7] == 0xaa) ? 0 : current;

            conVolt = (float) volt / 1000;
            conCurrent = (float) current / 1000;
            conCapacity = (float) capacity / 1000;
            conR = (float) r / 1000;
            conTc = (float) tc/1000 ;
            if(status == 1 || status == 0) {
                Log.v("GC","(ECHO)ECHO_DEVICE_STATUS 0x4A:"+ status); //got it effected by TimerButton
                onImgV.setImageResource((status == 1) ? R.drawable.gc_on_64 : R.drawable.gc_off_64);
            }

            //Connect Data
            //顯示連結後的資料(電流，電壓，功率，容量，電阻，時間，prog倒數時間)
            if(!(iBuf[4] == 0xaa && iBuf[5] == 0xaa)) {
                meterCurrent.setText(String.format("%.3f", conCurrent));
                meterEnergy.setText(String.format("%.3f", conVolt * conCurrent));
            }
            meterVolt.setText(String.format("%.3f", conVolt));
            meterCapacity.setText(String.format("%.1f", conCapacity));

             if(((iBuf[12] & 0xff) != 0xAA) && ((iBuf[13] & 0xff) != 0xAA) && ((iBuf[14] & 0xff) != 0xAA) && ((iBuf[15] & 0xff) != 0xAA) ) {
                String sString = getResources().getString(R.string.Timer);
                meterTimer.setText(sString + int2TimeString((int)conTc));
                progTc.setText(int2TimeString((int)conTc));
                Log.v("GC","meterTimer:="+int2TimeString((int)conTc));

                 if(conTci >= conTc) {
                     String xString = getResources().getString(R.string.elapsed) + int2TimeString((int) conTci - (int) conTc);
                     ((MainActivity) getActivity()).maTV.setText(xString);
                 }
             }
            if(r != 65535)  meterDynamic.setText(String.valueOf(conR + "Ω"));
            if(r == 65535) meterDynamic.setText("");

            if (conCurrent > 1.0) {
                speedImgV.setImageResource(R.drawable.gc_fast57);
            } else if (conCurrent > 0.45) {
                speedImgV.setImageResource(R.drawable.gc_mid57);
            } else {
                speedImgV.setImageResource(R.drawable.gc_slow57);

            }

            if( flag0x07) {
                ((MainActivity)getActivity()).packBTSend(0,MainActivity.READ_CONF_1);
                info0x47FlagSetByBLEService(false);
            }
            sendCnt++;
        }
 
        // 連結後的設定值資料(ib)
        if (iBuf[2] == MainActivity.ECHO_TRIP_CURRENT) { //0x4B  75
            int ib = (iBuf[3] & 0xff);
            float conSettingIb = (float) ib / 10;
            String ibString = conSettingIb + "A";
            b0.setText(ibString);
            String xString = R.string.echo_trip_current + ibString ;
            Toast.makeText(context, xString ,Toast.LENGTH_SHORT).show();
        }

        if(iBuf[2] == MainActivity.ECHO_DRIVE_ON_OFF) { //0x41  65
            Log.v("GC","(ECHO) Drive ON OFF");
            int status  = (iBuf[3] & 0xff);
            if(status == 0 || status == 1) {
                onImgV.setImageResource((status == 1) ? R.drawable.gc_on_64 : R.drawable.gc_off_64);
            }
            String sString = getResources().getString(R.string.echo_led_on);
            String tString = getResources().getString(R.string.echo_led_off);
            if(status == 1 && mConnected == true){makeTextAndShowBtn(context, sString, Toast.LENGTH_SHORT);}
            if(status == 0 && mConnected == true){makeTextAndShowBtn(context, tString, Toast.LENGTH_SHORT);}
        }

        if(iBuf[2] == MainActivity.ECHO_SET_ENABLE_TIMER) { //0x43  67
            Log.v("GC","(ECHO) SET ENABLE TIMER");
             int e = (iBuf[3] & 0xff);
            if(e == 1 || e == 0)  {
                timerStatus = e;
                timerImgV.setImageResource((timerStatus == 1)? R.drawable.gc_timer_64:R.drawable.gc_timer_64x);
                timerImgV.setTag((timerStatus == 1)? "on":"off");
                bTM.setImageResource((timerStatus == 1)? R.drawable.timer:R.drawable.timerx);

                String sString = getResources().getString(R.string.echo_timer_on);
                String tString = getResources().getString(R.string.echo_timer_off);
                if(timerStatus == 1 && mConnected == true){makeTextAndShowBtn(context, sString, Toast.LENGTH_SHORT);}
                if(timerStatus == 0 && mConnected == true){makeTextAndShowBtn(context, tString, Toast.LENGTH_SHORT);}
            }
        }

        if(iBuf[2] == MainActivity.ECHO_FACTORY_RESET) { //0x45   69
            //Toast.makeText(context, R.string.echo_reset_ok,Toast.LENGTH_SHORT).show();
        }

        if(iBuf[2] == MainActivity.ECHO_READ_CONF_1) { //0x47    71

            int ts = (iBuf[4] & 0xff);
            int ib = (iBuf[12] & 0xff) ;

            conTs = (float)ts;
            conIb = (float)ib /10;

            b0.setText(String.valueOf(conIb));
            //if (!(MainActivity.activeMode == 1)) {
                SharedPreferences sp = context.getSharedPreferences("address", MainActivity.MODE_PRIVATE);
                // PUT Data
                Log.v("Kevin","---"+conIb);
                sp.edit().putFloat("para_ibf", conIb).commit();      // ibf
                //sp.edit().putFloat("para_tsf", conTs).commit();      // tsf
            //}
            try{
                ((MainActivity)getActivity()).packBTSend(0,MainActivity.READ_CONF_2);
                Thread.sleep(500);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        if(iBuf[2] == MainActivity.ECHO_SET_TIMER_VAL) { //0x42
            Log.d("GC","(ECHO) ECHO_SET_TIMER_VAL");
            ((MainActivity)getActivity()).packBTSend(0,MainActivity.READ_CONF_1); //kevin ask don't send
        }


        if (iBuf[2] == MainActivity.ECHO_READ_CONF_2) { //0x4E   78

            int status = (iBuf[4] & 0x000000ff);
            int tci = (iBuf[5] & 0x000000ff) | (iBuf[6] & 0x000000ff) << 8 | (iBuf[7] & 0x000000ff) << 16 | (iBuf[8] & 0x000000ff) << 24;
            int Li = (iBuf[9] & 0x000000ff);
            int Lt = (iBuf[10] & 0x000000ff);
            int Le = (iBuf[11] & 0x000000ff);
            int Ltc = (iBuf[12] & 0x000000ff);
            int OE = (iBuf[13] & 0x000000ff);

            Log.v("Kevin","---Li:---"+Li+"---Lt:---"+Lt+"---Le:---"+Le+"---Ltc:---"+Ltc+"---OE:---"+OE);

            timerStatus = status;
            conTci = (float) tci / 1000;
            progTci.setText(int2TimeString((int)conTci));
            Log.v("Kevin","---"+timerStatus);
            if(status == 0 || status == 1) {
                timerImgV.setImageResource((timerStatus == 1) ? R.drawable.gc_timer_64 : R.drawable.gc_timer_64x);
                bTM.setImageResource((timerStatus == 1) ? R.drawable.timer : R.drawable.timerx);
                timerImgV.setTag((timerStatus == 1)? "on":"off");
            }
            conLi = (float)Li*2/1000;
            lowButton.setText(String.format("%.2f",conLi));
            if(Le == 1){
                check = true;
                lowButton.setAlpha(1.0f);
            }
            if(Le == 0){
                check = false;
                lowButton.setAlpha(0.3f);
            }
            //if (!(MainActivity.activeMode == 1)) {
                SharedPreferences sp = context.getSharedPreferences("address", MainActivity.MODE_PRIVATE);
                // PUT Data
                sp.edit().putFloat("para_tcif", conTci).commit();      // tcif
                sp.edit().putFloat("para_Li", Li).commit();      // Li
                sp.edit().putFloat("para_Lt", Lt).commit();      // Lt
            //}
        }

        if (iBuf[2] == MainActivity.ECHO_WARNING) { //0x4D
            int status = (iBuf[3] & 0x000000ff);
            int v = (iBuf[4] & 0x000000ff) | (iBuf[5] & 0x000000ff) << 8;
            float i = (iBuf[6] & 0x000000ff) | (iBuf[7] & 0x000000ff) << 8;
            int Hw = (iBuf[8] & 0x000000ff);
            int Lw = (iBuf[9] & 0x000000ff);
            int Ltc = (iBuf[10] & 0x000000ff);

            Log.v("Kevin1","---status:"+status+"---v:"+v+"---i:"+i+"---Hw:"+Hw+"---Lw:"+Lw+"---Ltc:"+Ltc);
            String h1String = getResources().getString(R.string.hw1);
            String l1_1String = getResources().getString(R.string.lw1_1);
            String l1_2String = getResources().getString(R.string.lw1_2);
            String l1_3String = getResources().getString(R.string.lw1_3);
            String l2String = getResources().getString(R.string.lw2);
            String l3String = getResources().getString(R.string.lw3);
            String l4String = getResources().getString(R.string.lw4);
            if(Hw == 1){
                makeTextAndShow0x4D(context,h1String,Toast.LENGTH_LONG);
            }
            if(Lw == 1){
                makeTextAndShow0x4D(context,String.format(l1_1String+" %.3fA "+l1_2String+" %d "+l1_3String,i/1000,Ltc),Toast.LENGTH_LONG);
            }
            if(Lw == 2){
                makeTextAndShow0x4D(context,l2String,Toast.LENGTH_LONG);
            }
            if(Lw == 3){
                makeTextAndShow0x4D(context,l3String,Toast.LENGTH_LONG);
            }
            if(Lw == 4){
                makeTextAndShow0x4D(context,l4String,Toast.LENGTH_LONG);
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        // intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        // intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }



    //onResume
    @Override
    public void onResume() {
        super.onResume();
      // if(gcGattUpdateReceiver == null) {
            context.registerReceiver(gcGattUpdateReceiver, makeGattUpdateIntentFilter());
            Log.v("GC"," onREsume()--mGattUpdateReceiver register");
      // }

        if(Build.VERSION.SDK_INT >= 23){

        }
    }
    private DataPoint[] generateData() {
        int count = 40;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = mRand.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }

    //onPause
    @Override
    public void onPause() {
        super.onPause();

        try {
           // if (gcGattUpdateReceiver != null) {
                context.unregisterReceiver(gcGattUpdateReceiver);
                Log.v("GC","GC----onPause():gcGattUpdateReceiver unregister");
           // }

        } catch (Exception ex) {

        }

    }
}

