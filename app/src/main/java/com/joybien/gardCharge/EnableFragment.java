package com.joybien.gardCharge;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.joybien.gardCharge.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnableFragment extends Fragment  {

    SharedPreferences sp;
    private Context context;
    private Toolbar tb;
    private String[] addrArray={};
    private String[] addrState={};
    private String[] addrNick={};
    private static String[] dataToken;

    private ListView lvDevice;
    boolean edit = false;
    boolean nick = false;
    public EnableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        addrArray = new String[64];
        addrState = new String[64];
        addrNick = new String[64];

        sp = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        for(int i=0 ;i<64;i++){
            String str="A"+i;
            String savedString = sp.getString(str,null);
            dataToken = savedString.split(",");
            addrArray[i]= dataToken[0];
            addrState[i]= dataToken[1];
            addrNick[i] = dataToken[2];
        }

       // for(int i=0;i<64;i++) addrArray[i]= "A"+i;
        Log.v("Zach","(1)----onCreate----Enable");
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Enable Mode");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_enable, container, false);
        Log.v("Zach","(1.1)----onCreateView----Enable");

        /*
        //(1)set toolBar
        tb = (Toolbar) v.findViewById(R.id.toolbarS);
       // tb.setTitle("Enable Device");
       // tb.setSubtitle("x Not Use");
       // tb.setLogo(R.mipmap.dali);
        ((AppCompatActivity)getActivity()).setSupportActionBar(tb);
        tb.setNavigationIcon(R.drawable.ic_menu_manage);
        tb.setOnMenuItemClickListener(onMenuItemClick);

        setHasOptionsMenu(true);
        */
        //(2)set List View
        lvDevice = (ListView) v.findViewById(R.id.lvDevice);
        CustomAdapter customAdapter = new CustomAdapter();
        lvDevice.setAdapter(customAdapter);
        return v;
    }

/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.enable, menu);
    }
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "MenuItem ";
            switch (menuItem.getItemId()) {
                case R.id.action_edit:
                    nick = false;
                    edit =!edit;
                    tb.setNavigationIcon((edit)?R.drawable.ic_menu_send:R.drawable.ic_menu_gallery );
                    tb.getMenu().getItem(1).setIcon((edit)?R.drawable.ic_menu_send:R.drawable.ic_menu_gallery );
                    msg += "Click edit";
                    break;
                case R.id.action_nick:
                    msg += "Click nick";
                    edit = false;
                    nick = !nick;
                    tb.getMenu().getItem(0).setIcon((nick)?R.drawable.ic_menu_send:R.drawable.ic_menu_gallery );
                    break;
                case R.id.action_settings:
                    msg += "Click setting";
                    break;
            }

            if(!msg.equals("")) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };

    */
   class CustomAdapter extends BaseAdapter{
       @Override
       public int getCount() {
           return addrArray.length;
       }
       @Override
       public Object getItem(int position) {
           return null;
       }
       @Override
       public long getItemId(int position) {   return 0; }
       @Override
       public View getView(final int position, View cv, ViewGroup parent) {
           cv = getActivity().getLayoutInflater().inflate(R.layout.list_fragment_enable,null);
           final EditText txtUrl = new EditText(getContext());

           final ImageView images = (ImageView) cv.findViewById(R.id.ive0);
           final TextView tv0 = (TextView) cv.findViewById(R.id.tv0);
          // final TextView tv1 = (TextView) cv.findViewById(R.id.tv1);
           images.setImageResource(addrState[position].equals("1")?R.drawable.check:R.drawable.error);
           tv0.setText(addrArray[position]+":"+addrNick[position]);
          // tv1.setText(addrNick[position]);

           cv.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View arg0) {
                   edit = ((MainActivity)getActivity()).beflag;
                   if(edit) { //change address status
                       String dataString = sp.getString("A" + position, null);
                       dataToken = dataString.split(",");
                       dataToken[1] = (dataToken[1].equals("1")) ? "0" : "1";
                       addrArray[position] = dataToken[0];
                       addrState[position] = dataToken[1];
                       addrNick[position] = dataToken[2];
                       sp.edit().putString(dataToken[0], dataToken[0] + "," + dataToken[1] + "," + dataToken[2]).commit();
                       Log.v("Zach", "Edit Saved:" + sp.getString(dataToken[0], null));
                       images.setImageResource(addrState[position].equals("1") ? R.drawable.check : R.drawable.error);
                   }
                   nick = ((MainActivity)getActivity()).bnflag;
                   if(nick){  //change nick name
                       AlertDialog.Builder bd= new AlertDialog.Builder(context);
                       bd.setTitle("Nick Name Editor");
                       bd.setMessage("Change Nick Name");
                       final EditText input = new EditText(context);
                       input.setRawInputType(InputType.TYPE_CLASS_TEXT);
                       bd.setView(input);
                       bd.setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               String str = input.getEditableText().toString();
                               String dataString = sp.getString("A" + position, null);
                               dataToken = dataString.split(",");
                               addrArray[position] = dataToken[0];
                               addrState[position] = dataToken[1];
                               addrNick[position]  = dataToken[2] =(str.equals("")?" ":str);
                               sp.edit().putString(dataToken[0], dataToken[0] + "," + dataToken[1] + "," + dataToken[2]).commit();
                              // tv1.setText(str);
                               tv0.setText(addrArray[position]+":"+addrNick[position]);

                           }
                       });
                       bd.setNegativeButton(R.string.string_cancel, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               Context context=getActivity();
                               Toast.makeText(context,R.string.string_cancel, Toast.LENGTH_LONG).show();
                           }
                       });
                       bd.show();
                   }
               }
           });
           return cv;
       }
   }
}


//********** reference ***************
/*
(1) toolBar in Fragments
http://stackoverflow.com/questions/18714322/how-to-add-action-bar-options-menu-in-android-fragments

(2) list View
(2.1)keywords: display a listView in a fragment
https://www.youtube.com/watch?v=edZwD54xfbk

(2.2)custom View: youtube
Android Studio Tutorials - 44 : customer ListView in Android

*/