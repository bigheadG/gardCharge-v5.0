package com.joybien.gardCharge;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.joybien.gardCharge.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddressFragment extends Fragment {
    SharedPreferences sp;
    private Context context;
    private Button b0,b1;
    private boolean b1flag = false;
    private int filterCnt = 0;
    private Switch sw0;
    private TextView tva0;
    private GridView funcGView;
    private NumberPicker np0;
    private SeekBar sb0;
    private TextView tvac;
    private String[] addrArray={};
    private String[] addrState={};
    private String[] addrNick={};
    private String[] tmpArray={};
    private String[] filterArray={};
   // private String[] emptyArray = {};
    private static String[] dataToken;
    private int previousSelectedPosition = -1;

    private static Handler mHandlerTime = new Handler();
    private int myTick=0;
    public static int state_ss = 0; //for stateMachine
    public static int smFlag = 0;
    public static int smCnt = 0;
    public static int prevSlider=-1;
    public static int prevAddress=-1;
    public static int prevCnt=0;
    public static byte[] prevByte = new byte[2];
    private int gAddress;

    final String[] funcList = new String[]{"Off","Test","Up","Down","Step Up","Step Down",
            "Recall Max Level","Recall Min Level","On & Step Up","Step Down & Off"};
    final byte[] cmdByteList = new byte[] {0x00,0x50,0x01,0x02,0x03,0x04,0x05,0x06,0x08,0x07};

    public AddressFragment() {
        // Required empty public constructor
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        Log.v("Zach","(1)AddressFragment ----onCreate----Address B1 Pressed");
        addrArray = new String[64];
        addrState = new String[64];
        addrNick  = new String[64];
        tmpArray  = new String[64];
        filterArray  = new String[64];
      //  emptyArray = new String[] { "Empty"};
        sp = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        fillDataArray();
    }

    public void fillDataArray(){
        filterCnt = 0;
        for(int i=0 ;i<64;i++){
            String str="A"+i;
            String savedString = sp.getString(str,null);
            dataToken = savedString.split(",");
            addrArray[i]= dataToken[0];
            addrState[i]= dataToken[1];
            addrNick[i] = dataToken[2];
            Log.v("Zach", " Scense:"+addrArray[i]+":"+addrState[i]+":"+addrNick[i]);
            tmpArray[i]= ((dataToken[1].equals("1"))?"* ":"")+dataToken[0]+":"+dataToken[2];
            if(dataToken[1].equals("1")){
                filterArray[filterCnt] =  dataToken[0]+":"+dataToken[2];
                filterCnt++;
            }
        }

        for(int i=filterCnt;i<64;i++){
            filterArray[i]= "A"+i+":A"+i;
        }
    }

        @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Address Mode");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_address, container, false);
        context=getActivity();

        tvac = (TextView) v.findViewById(R.id.textViewCommand);
        tva0 = (TextView) v.findViewById(R.id.tva);
        sw0 = (Switch) v.findViewById(R.id.switch1);
        sb0 = (SeekBar) v.findViewById(R.id.seekBarA);
        sb0.setVisibility(View.INVISIBLE);

        //(1)NumberPicker
        np0 = (NumberPicker) v.findViewById(R.id.numberPicker0);
        np0.setMinValue(0);
        np0.setMaxValue(tmpArray.length - 1);
        np0.setBackground(getResources().getDrawable(R.drawable.shape_yellow));
        np0.setDisplayedValues(tmpArray);
        np0.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np0.setOnValueChangedListener(addressChangedListener);

        b0 = (Button) v.findViewById(R.id.bAction);
        b0.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Log.v("Zach","Address B0 Pressed");
               // b0.setBackgroundResource((np0.getVisibility()==View.INVISIBLE)?R.drawable.address_32
               // :R.drawable.home_32);
                b0.setBackgroundResource((np0.getVisibility()==View.INVISIBLE)?android.R.drawable.ic_dialog_map
                        :android.R.drawable.ic_dialog_dialer);

                np0.setVisibility((np0.getVisibility()==View.VISIBLE)?View.INVISIBLE:View.VISIBLE);
                b1.setVisibility((np0.getVisibility()==View.INVISIBLE)?View.INVISIBLE:View.VISIBLE);
                tva0.setVisibility(np0.getVisibility());
            }
        });
        b1 = (Button) v.findViewById(R.id.npButton);
        b1.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Log.v("Zach","filter B1 Pressed");
                b1.setBackgroundResource((b1flag)?R.drawable.filter150_v:R.drawable.filter150_x);

                fillDataArray();
                if(b1flag){
                    if(filterCnt == 0){
                        np0.setMaxValue(0);
                        np0.setDisplayedValues(tmpArray);
                    }else {
                        np0.setMaxValue(filterCnt - 1);
                        np0.setDisplayedValues(filterArray);
                    }

                }else{
                    np0.setMaxValue(tmpArray.length - 1);
                    np0.setDisplayedValues(tmpArray);
                }

                b1flag = !b1flag;

            }
        });

        //(2)set the switch to ON
        sw0.setChecked(true);
        sw0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    tvac.setText(R.string.command);
                    funcGView.setVisibility(View.VISIBLE);
                    sb0.setVisibility(View.INVISIBLE);
                    np0.setVisibility(View.VISIBLE);
                    b0.setVisibility(View.VISIBLE);
                    tva0.setVisibility(View.VISIBLE);
                    tva0.setText(Integer.toString(sb0.getProgress()));
                    b1.setVisibility(View.VISIBLE);
                }else{  //off
                    funcGView.setVisibility(View.INVISIBLE);
                   // tvac.setText("Direct Control");
                    tvac.setText(R.string.control);
                    sb0.setVisibility(View.VISIBLE);
                    b0.setVisibility(View.INVISIBLE);
                    np0.setVisibility(View.VISIBLE);
                    b1.setVisibility(View.VISIBLE);
                    //(2)textView Seeting
                    tva0.setVisibility(View.VISIBLE);
                    tva0.setText(Integer.toString(sb0.getProgress()));
                }

                b0.setBackgroundResource((np0.getVisibility()==View.VISIBLE)?android.R.drawable.ic_dialog_map
                        :android.R.drawable.ic_dialog_dialer);

            }
        });
        //(3)Address GridView Seeting

        funcGView = (GridView) v.findViewById(R.id.funcGV);
        funcGView.setNumColumns(2);
        funcAdapter customAdapterG = new funcAdapter();
        funcGView.setAdapter(customAdapterG);

        //
        //(1)[SeekBar Setting]
        //
        sb0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,  boolean fromUser) {
                tva0.setText(Integer.toString(progress));

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandlerTime.post(timerRun);
                prevAddress = -1;
                prevSlider = -1;
                prevCnt =0;
                sendSliderData();
                smFlag = 1;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                smFlag = 0;
                state_ss = 0;
                sendSliderData();
                mHandlerTime.removeCallbacks(timerRun);
            }
        });
        return v;
    }
  /*  @Override
    public void onPause() {
        super.onPause();
        mHandlerTime.removeCallbacks(timerRun);
    } */

    private final Runnable timerRun = new Runnable() {
        public void run() {
            //sendSliderData();
            sliderSendStateMachine(2);
            myTick++;
            Log.v("TICK","Tick="+myTick);
            mHandlerTime.postDelayed(this, 250);
        }
    };

    public void sendSliderData(){
        byte[] c = new byte[2];
        int addrVal = np0.getValue();
        int slideVal=(int) sb0.getProgress();
        c[0]= (byte) (addrVal*2 & 0x000000FF);
        c[1]= (byte) (slideVal & 0x000000FF);
        Log.v("TICK","------sendSLiderData()"+ sb0.getProgress()+"  np0:"+np0.getValue());
        txByteACommand(c);
    }

    public void sliderSendStateMachine(int icnt){
         Log.v("TICK","SM:state("+smFlag +") ="+state_ss);
          switch(state_ss){
              case 0: //idle
                  if(smFlag == 1){
                      state_ss = 1;
                      sendSliderData();
                      Log.v("TICK","---------------idle 0-------------------");
                  }
                  break;
              case 1: //looping
                  sendSliderData();
                  Log.v("TICK","---------------loop 1-------------------");
                  if(smFlag == 0) { state_ss = 2; smCnt =0;}
                  break;
          }
    }

    NumberPicker.OnValueChangeListener addressChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            tva0.setText(tmpArray[newVal]);
            gAddress = newVal;
            Log.v("AF","NP Select Value="+newVal);
        }
    };

    class funcAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return funcList.length;
        }
        @Override
        public Object getItem(int position) { return null; }
        @Override
        public long getItemId(int position) { return 0; }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //if(convertView == null )  return  convertView;

            final View  cv = (View) getActivity().getLayoutInflater().inflate(R.layout.list_grid_func,null);
            TextView tv0 = (TextView) cv.findViewById(R.id.ftv0);

            tv0.setText(funcList[position]);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //Selected Color
                    if(previousSelectedPosition != -1) {
                        View pv = funcGView.getChildAt(previousSelectedPosition);
                     //   pv.setBackgroundColor(Color.WHITE);
                        pv.setBackground(getResources().getDrawable(R.drawable.shape_yellow));
                    }
                    //cv.setBackgroundColor(Color.BLUE);
                    cv.setBackground(getResources().getDrawable(R.drawable.shape_golden));
                    previousSelectedPosition = position;

                    if(position == 1) {
                        int st = (np0.getVisibility() == View.VISIBLE)? 1:0; //group:0 address:1
                        ((MainActivity)getActivity()).setTestFunc(0,st,gAddress);
                    }else{
                       funcCodeSend(position);
                    //executeFunction(position);
                    }
                }
            });
            return cv;
        }
    }

    public void txCommand(String cmdString){
        ((MainActivity)getActivity()).sendCommand(cmdString);
    }

    public void txByteACommand(byte[] cmdByte){
       Log.v("AF","---txByteACommand------");
        ((MainActivity)getActivity()).sendByteACommand(cmdByte);
    }

    //--------------dali method--------------
    public byte gsCheck() {
        byte c =(np0.getVisibility()==View.INVISIBLE)?(byte)0xFF:(byte)((gAddress * 2 + 1) & 0x000000FF);
        return c;
    }

    public void funcCodeSend(int i){
        byte[] c = new byte[2];
        c[0]=gsCheck();
        c[1]=cmdByteList[i];
        txByteACommand(c);
    }

}



