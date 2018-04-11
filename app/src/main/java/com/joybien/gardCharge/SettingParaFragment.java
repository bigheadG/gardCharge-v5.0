package com.joybien.gardCharge;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Button;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.google.android.gms.plus.PlusOneButton;
import com.joybien.gardCharge.R;

import static android.R.attr.checkable;
import static android.R.attr.gravity;
import static android.R.attr.progress;
import static android.content.Context.MODE_PRIVATE;
import static com.joybien.gardCharge.GardChargeFragment.check;
import static com.joybien.gardCharge.GardChargeFragment.scheduler;
import static com.joybien.gardCharge.MainActivity.mConnected;
import static com.joybien.gardCharge.MainActivity.nav_Menu;


/**
 * A fragment with a Google +1 button.
 */
public class SettingParaFragment extends Fragment {

    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;
    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";
    //private PlusOneButton mPlusOneButton;
    private TextView txtTime;
    private TimePicker timePicker;
    private TextView txtCurrent, txtLi, txtLt, lcSubject, lcoffSubject, txt0_05A, txt0_5A, txt0min, txt60mins;
    private SeekBar sbIbf, sbLi, sbLt;
    //private SeekBar sbTsf;
    //private TextView txtSample;
    private Button btnSaveTimer, btnSaveLC;
    private CheckBox checkLe;
    private int minSample;
    private int hourSample;
    private double currentValue;
    private static Context context;
    float floatTcifValue;
    float floatIbfValue;
    float floatTsfValue;
    static int mHour;
    static int mMinute;
    private float liValue;
    private int ltValue;
    private int leValue;
    float floatLiValue;
    float floatLtValue;
    private static Toast toast;
    private static TextView toastText;
    private static Animation amTimer;
    private static Animation amLC;
    private Handler mHandlerTime = new Handler();
    private static int myTick = 0;

    public SettingParaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settingpara, container, false);
        txtTime = (TextView) view.findViewById(R.id.txtTime);
        timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        txtCurrent = (TextView) view.findViewById(R.id.txtCurrent);
        sbIbf = (SeekBar) view.findViewById(R.id.sbIbf);
        //txtSample = (TextView) view.findViewById(R.id.txtSample);
        //sbTsf = (SeekBar) view.findViewById(R.id.sbTsf);
        btnSaveTimer = (Button) view.findViewById(R.id.btnSaveTimer);
        btnSaveLC = (Button) view.findViewById(R.id.btnSaveLC);
        txtLi = (TextView) view.findViewById(R.id.txtLi);
        txtLt = (TextView) view.findViewById(R.id.txtLt);
        sbLi = (SeekBar) view.findViewById(R.id.sbLi);
        sbLt = (SeekBar) view.findViewById(R.id.sbLt);
        checkLe = (CheckBox) view.findViewById(R.id.checkLe);
        lcSubject = (TextView) view.findViewById(R.id.lcSubject);
        lcoffSubject = (TextView) view.findViewById(R.id.lcoffSubject);
        txt0_05A = (TextView) view.findViewById(R.id.txt0_05A);
        txt0_5A = (TextView) view.findViewById(R.id.txt0_5A);
        txt0min = (TextView) view.findViewById(R.id.txt0min);
        txt60mins = (TextView) view.findViewById(R.id.txt60mins);
        sbLi.setEnabled(false);
        sbLt.setEnabled(false);
        scheduler = true;
        if(check == true){
            checkLe.setChecked(true);
        }
        if(checkLe.isChecked() == true) {
            lcSubject.setEnabled(true);lcoffSubject.setEnabled(true);sbLi.setEnabled(true);
            sbLt.setEnabled(true);txtLt.setEnabled(true);txtLi.setEnabled(true);
            txt0_05A.setEnabled(true);txt0_5A.setEnabled(true);txt0min.setEnabled(true);
            txt60mins.setEnabled(true);btnSaveLC.setEnabled(true);
            check = true;
        }

        Runnable drawTimerRun = new Runnable() {
            @Override
            public void run() {
                if (scheduler == true){

                    if (myTick % 2 == 0 && scheduler == true && mConnected == true) {
                        nav_Menu.findItem(R.id.nav_disconnect).setEnabled(false);
                        nav_Menu.findItem(R.id.nav_setup).setEnabled(false);
                        nav_Menu.findItem(R.id.nav_setting).setEnabled(false);
                    }


                    Log.v("GC", "SettingParaFragment-tick:" + myTick);
                    myTick++;
                    mHandlerTime.postDelayed(this, 500);
                }
            }
        };
        mHandlerTime.post(drawTimerRun);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//抓取時間 TimePicker
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            public void onTimeChanged(TimePicker view, int hour, int minute) {
                String tString = String.format("%02d:%02d", hour, minute);
                txtTime.setText(tString);
                mHour = hour;
                mMinute = minute;
                if(timePicker.hasFocus()) {
                    btnTimerAnimation(btnSaveTimer);
                    btnSaveTimer.getBackground().setColorFilter(Color.parseColor("#FF8800"), PorterDuff.Mode.DARKEN);
                }
            }
        });

//拖動條 SeekBaribf
        sbIbf.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            //開始拉動SeekBar時做的動作
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            //拉動SeekBar停止時做的動作
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.v("SET", "-------ibf-stop----------");
                ((MainActivity) getActivity()).packBTSend((int) (currentValue * 10), MainActivity.SET_TRIP_AMP);
                GardChargeFragment.info0x47FlagSetByBLEService(true);
                String tString = getResources().getString(R.string.successfully);
                makeTextAndShow(getContext(), tString, 500);
            }

            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentValue = (1 + progress) * 0.1;
                String sString = String.format("%.1f" + "A", currentValue);
                txtCurrent.setText(sString);

                if (checkLe.isChecked() == true){
                    if (liValue/1000 > currentValue) {
                        currentValue = (float) (Math.ceil(liValue / 1000 * 10)) / 10;
                        sbIbf.setProgress((int) ((currentValue-0.1) * 10));

                        if(liValue/1000 == currentValue){
                            currentValue = (float) (Math.ceil(liValue / 1000 * 10)) / 10;
                            currentValue += 0.1;
                            sbIbf.setProgress((int) ((currentValue-0.1) * 10));

                        }
                        if (liValue / 1000 == 0.5) {
                            currentValue = (float) (Math.ceil(liValue / 1000 * 10)) / 10;
                            currentValue += 0.1;
                            sbIbf.setProgress((int) ((currentValue-0.1) * 10));

                        }
                    }
                }

            }
        });

        btnSaveTimer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity) getActivity()).packBTSend((mHour * 3600 + mMinute * 60)*1000, MainActivity.SET_TIME_VALUE);
                GardChargeFragment.info0x47FlagSetByBLEService(true);
                String tString = getResources().getString(R.string.successfully);
                makeTextAndShow(getContext(), tString, 500);
                btnSaveTimer.getBackground().clearColorFilter();
                if(amTimer != null){
                    amTimer.cancel();
                }
            }
        });

        btnSaveLC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GardChargeFragment.info0x47FlagSetByBLEService(true);
                byte[] x = new byte[4];
                x[0] = (byte)((int) liValue/2);
                x[1] = (byte)(ltValue);
                x[2] = (byte) 1;
                x[3] = 0;
                int data = convertirOctetEnEntier(x);
                ((MainActivity) getActivity()).packBTSend(data, MainActivity.SET_LOW_CURRENT);
                ((MainActivity) getActivity()).packBTSend((int) (currentValue * 10), MainActivity.SET_TRIP_AMP);
                String tString = getResources().getString(R.string.successfully);
                makeTextAndShow(getContext(), tString, 500);
                btnSaveLC.getBackground().clearColorFilter();
                if(amLC != null){
                    amLC.cancel();
                }
            }
        });


        sbLi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override

            public void onStopTrackingTouch(SeekBar seekBar) {
                btnLCAnimation(btnSaveLC);
                btnSaveLC.getBackground().setColorFilter(Color.parseColor("#FF8800"), PorterDuff.Mode.DARKEN);
            }

            @Override
            //SeekBar改變時做的動作
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                liValue = progress * 2;
                String sString = String.format("%.2f" + "A", liValue/1000);
                txtLi.setText(sString);

                if(currentValue <= 0.5){
                    currentValue = (float)(Math.ceil(liValue/1000*10))/10;
                    sbIbf.setProgress((int) ((currentValue-0.1) * 10));

                    if(liValue/1000 == currentValue){
                        currentValue = (float) (Math.ceil(liValue / 1000 * 10)) / 10;
                        currentValue += 0.1;
                        sbIbf.setProgress((int) ((currentValue-0.1) * 10));

                    }
                    if(liValue/1000 == 0.5){
                        currentValue = (float)(Math.ceil(liValue/1000*10))/10;
                        currentValue += 0.1;
                        sbIbf.setProgress((int) ((currentValue-0.1) * 10));
                    }
                }

            }
        });


        sbLt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                btnLCAnimation(btnSaveLC);
                btnSaveLC.getBackground().setColorFilter(Color.parseColor("#FF8800"), PorterDuff.Mode.DARKEN);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ltValue = progress;
                String tString = getResources().getString(R.string.mins);
                String sString = String.format("%d" + tString, ltValue);
                txtLt.setText(sString);

            }
        });

        checkLe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    lcSubject.setEnabled(true);lcoffSubject.setEnabled(true);sbLi.setEnabled(true);
                    sbLt.setEnabled(true);txtLt.setEnabled(true);txtLi.setEnabled(true);
                    txt0_05A.setEnabled(true);txt0_5A.setEnabled(true);txt0min.setEnabled(true);
                    txt60mins.setEnabled(true);btnSaveLC.setEnabled(true);
                    check = true;

                    if(liValue/1000 > currentValue){
                        currentValue = (float) (Math.ceil(liValue / 1000 * 10)) / 10;
                        sbIbf.setProgress((int) ((currentValue-0.1) * 10));

                        if(liValue/1000 == currentValue){
                            currentValue = (float) (Math.ceil(liValue / 1000 * 10)) / 10;
                            currentValue += 0.1;
                            sbIbf.setProgress((int) ((currentValue-0.1) * 10));
                        }
                    }

                }
                else
                {
                    lcSubject.setEnabled(false);lcoffSubject.setEnabled(false);sbLi.setEnabled(false);
                    sbLt.setEnabled(false);txtLt.setEnabled(false);txtLi.setEnabled(false);
                    txt0_05A.setEnabled(false);txt0_5A.setEnabled(false);txt0min.setEnabled(false);
                    txt60mins.setEnabled(false);btnSaveLC.setEnabled(false);
                    check = false;
                    btnSaveLC.getBackground().clearColorFilter();
                    if(amLC != null){
                        amLC.cancel();
                    }
                    GardChargeFragment.info0x47FlagSetByBLEService(true);
                    byte[] x = new byte[4];
                    x[0] = 0;
                    x[1] = 0;
                    x[2] = 0;
                    x[3] = 0;
                    int data = convertirOctetEnEntier(x);
                    ((MainActivity) getActivity()).packBTSend(data, MainActivity.SET_LOW_CURRENT);
                }
            }
        });

//GET Last Data
        // tcif
        SharedPreferences sp = getContext().getSharedPreferences("address",MainActivity.MODE_PRIVATE);
        floatTcifValue = sp.getFloat("para_tcif", 0);
        //Log.v("Kevin","---TCIF:"+String.valueOf(floatTcifValue));
        int tcifSecond = (int) (floatTcifValue % 60);
        int tcifHour = (int) (floatTcifValue / 3600);
        int tcifMins = (int) ((floatTcifValue % 3600 - tcifSecond) / 60);
        timePicker.setCurrentHour(tcifHour);                             // Last setting hours
        timePicker.setCurrentMinute(tcifMins);                           // Last setting mins

        // ibf
        floatIbfValue = sp.getFloat("para_ibf", 0);
        sbIbf.setProgress((int) ((floatIbfValue - 0.1) * 10));                     // Last setting seekbar progress

        // Li
        floatLiValue = sp.getFloat("para_Li", 0);
        sbLi.setProgress((int) (floatLiValue));

        // Lt
        floatLtValue = sp.getFloat("para_Lt", 0);
        sbLt.setProgress((int) (floatLtValue));


        return view;
    }

    private static void makeTextAndShow(final Context context, final String text, final int duration) {
        if (toast == null) {

            final ViewGroup toastView = new FrameLayout(context);
            final FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            final GradientDrawable background = new GradientDrawable();
            toastText = new TextView(context);
            toastText.setLayoutParams(flp);
            toastText.setSingleLine(false);
            toastText.setTextSize(18);
            toastText.setTextColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
            background.setColor(Color.argb(0xD2, 0xFF, 0x92, 0x24));
            background.setCornerRadius(20);

            toastView.setPadding(30, 30, 30, 30);
            toastView.addView(toastText);
            toastView.setBackgroundDrawable(background);

            toast = new Toast(context);
            toast.setView(toastView);
        }
        toastText.setText(text);
        toast.setDuration(duration);
        toast.show();
    }

    public void btnTimerAnimation(View v) {
        amTimer = new ScaleAnimation( 1.0f, 0.7f, 1.0f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );    //(startXScale, endXScale, startYScale, endYScale)
        amTimer.setRepeatMode(Animation.REVERSE);
        amTimer.setDuration(700);                //setDuration (long durationMillis)
        amTimer.setRepeatCount(-1);               //setRepeatCount (int repeatCount)
        amTimer.setFillEnabled(true);
        amTimer.setFillBefore(false);
        amTimer.setFillAfter(true);
        // 圖片配置動畫
        v.setAnimation(amTimer);
        // 動畫開始
        v.startAnimation(amTimer);
    }

    public void btnLCAnimation(View v) {
        amLC = new ScaleAnimation( 1.0f, 0.7f, 1.0f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );    //(startXScale, endXScale, startYScale, endYScale)
        amLC.setRepeatMode(Animation.REVERSE);
        amLC.setDuration(700);                //setDuration (long durationMillis)
        amLC.setRepeatCount(-1);               //setRepeatCount (int repeatCount)
        amLC.setFillEnabled(true);
        amLC.setFillBefore(false);
        amLC.setFillAfter(true);
        // Set Animation
        v.setAnimation(amLC);
        // Start Animation
        v.startAnimation(amLC);
    }

    public int convertirOctetEnEntier(byte[] b){
        int MASK = 0xFF;
        int result = 0;
        result = b[0] & MASK;
        result = result + ((b[1] & MASK) << 8);
        result = result + ((b[2] & MASK) << 16);
        result = result + ((b[3] & MASK) << 24);
        return result;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.setting_name);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the state of the +1 button each time the activity receives focus.
        //mPlusOneButton.initialize(PLUS_ONE_URL, PLUS_ONE_REQUEST_CODE);
    }


    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v("Zach", "---BCast----");

        }
    };
}