package eu.alfred.alfredmonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.alfred.hmc.HMCLoader;
import eu.alfred.saf.SAFFacade;

import java.util.Calendar;

public class Main extends Activity {

    private String TAG = "AlfredMonitor";
    private SAFFacade saf;
    private HMCLoader hmc;
    private String url_temp="/tshirt/temp";
    private String url_acc="/tshirt/acc";
    private String url_hr="/tshirt/hr";
    private String url_rr="/tshirt/rr";
    private String url_steps="/tshirt/steps";
    private TextView tvTemp,tvAcc,tvElectro,tvECG,tvVersion;
    private LinearLayout lilaECG,lilaRR,lilaTemp,lilaSteps;
    private int steps=0;
    private Calendar calSteps;
    private boolean ready_to_step=false;
    private boolean selectedHR=true;
    private int prev_rr=0;
    private int prev_hr=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d("Main:", "onCreate");

        setContentView(R.layout.activity_main);

        //Views
        tvTemp = (TextView) findViewById(R.id.tvTempData);
        tvAcc = (TextView) findViewById(R.id.tvAccData);
        tvElectro = (TextView) findViewById(R.id.tvElectroData);
        tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvECG = (TextView) findViewById(R.id.tvECGData);

        lilaECG = (LinearLayout) findViewById(R.id.linearLayoutECG);
        lilaRR = (LinearLayout) findViewById(R.id.linearLayoutRR);
        lilaTemp = (LinearLayout) findViewById(R.id.linearLayoutTemp);
        lilaSteps = (LinearLayout) findViewById(R.id.linearLayoutAcc);

        try
        {
            tvVersion.setText(getResources().getString(R.string.app_name)+" "+this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            tvVersion.setText("0.0");
        }

        //New SAF Framework
        saf = new SAFFacade();

        //HMC
        hmc = new HMCLoader(this,saf);

        /*HMCLoader code:
        //listener = new DataReceiver(context,data_frequency);

        listenerTemp = new DataReceiver(context,5000);
        listenerAcc = new DataReceiver(context,500);
        listenerHR = new DataReceiver(context,10000);
         */

        hmc.start("C0:FF:EE:C0:FF:5E");
        /*hmc start method code
        shirt = new TShirtDriver(context,mac);

        try {
            saf.registerDriver(shirt);
            saf.registerListener("/tshirt/temp", listenerTemp);
            saf.registerListener("/tshirt/acc", listenerAcc);
            saf.registerListener("/tshirt/hr", listenerHR);
        }catch(SAFException e) {

         */

        lilaECG.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                selectedHR =true;
                hmc.setHR(selectedHR);
                lilaECG.setBackgroundColor(getResources().getColor(R.color.WARNING_ORANGE));
                lilaRR.setBackgroundColor(getResources().getColor(R.color.DISABLED));

            }
        });

        lilaRR.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                lilaRR.setBackgroundColor(getResources().getColor(R.color.WARNING_ORANGE));
                lilaECG.setBackgroundColor(getResources().getColor(R.color.DISABLED));
                selectedHR=false;
                hmc.setHR(selectedHR);

            }
        });

        calSteps = Calendar.getInstance();
        /*TEMPORARY TEST: BroadcastReceiver to get data from listeners*/
        setBroadcast();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Main:", "onStop");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Main:", "onStop");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Main:", "onDestroy");
        hmc.stop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        System.exit(0);

    }

    @Override
    public void onBackPressed() {
        Log.d("Main:", "onBackPressed");
        hmc.stop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        finish();
        System.exit(0);


    }

    private void setBroadcast()
    {
        IntentFilter filter = new IntentFilter(url_temp);
        filter.addAction(url_acc);
        filter.addAction(url_hr);
        filter.addAction(url_rr);
        filter.addAction(url_steps);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        //registerReceiver(receiver,Filter);

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Each listener is broadcasting data of its sensor at the configured frequency
            if (intent.getAction().contentEquals(url_temp)) {
                byte[] data = intent.getByteArrayExtra("dataHMC");
                long ts=intent.getLongExtra("ts",0);
                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(ts);

                if(data!=null) {

                    float x = (data[1] & 0xFF) << 8 |data[0] & 0xFF;
                    x /= 10;

                    tvTemp.setText(" "+x + " ºC");

                }
                lilaTemp.setBackgroundColor(getResources().getColor(R.color.NORMAL));

            }else if (intent.getAction().contentEquals(url_steps)) {

                byte[] data = intent.getByteArrayExtra("dataHMC");
                long ts=intent.getLongExtra("ts", 0);
                steps = (int) (data[1] & 0xFF) << 8 | data[0] & 0xFF;
                /*long now = Calendar.getInstance().getTimeInMillis();

                if(data!=null &&  (now > calSteps.getTimeInMillis() + 200)) {
                    int x = data[0];
                    int y = data[1];
                    int z = data[2];
                    //Log.d(TAG,"x:"+x +" y:"+y+" z:"+z);

                    if(y>45 && y<60) ready_to_step=true;


                    if(ready_to_step && y> 58 && x<25 && z < 15)
                    {
                        steps++;
                        ready_to_step=false;
                        calSteps = Calendar.getInstance();

                    }*/
                    tvAcc.setText(steps + "");

                    lilaSteps.setBackgroundColor(getResources().getColor(R.color.NORMAL));

                //}

            }else if (intent.getAction().contentEquals(url_hr))
            {
                if(selectedHR)
                {

                    byte[] data = intent.getByteArrayExtra("dataHMC");
                    long ts=intent.getLongExtra("ts",0);
                    Calendar now = Calendar.getInstance();
                    now.setTimeInMillis(ts);

                    if(data!=null) {
                        int x = data[0] & 0xFF;
                        int hr = data[1] & 0xFF;

                        if(hr == 0) hr = prev_hr;
                        else prev_hr=hr;

                        //tvElectro.setText(" "+x);
                        tvECG.setText(" " + hr + " bpm");

                        if (x != 0) {
                            lilaECG.setBackgroundColor(getResources().getColor(R.color.WARNING_ORANGE));
                        } else {
                            lilaECG.setBackgroundColor(getResources().getColor(R.color.NORMAL));
                        }
                    }
                }

            }else if (intent.getAction().contentEquals(url_rr))
            {
                if(!selectedHR) {
                    byte[] data = intent.getByteArrayExtra("dataHMC");
                    long ts = intent.getLongExtra("ts", 0);
                    Calendar now = Calendar.getInstance();
                    now.setTimeInMillis(ts);

                    if (data != null) {
                        int x = data[0] & 0xFF;
                        int rr = data[1] & 0xFF;

                        if(rr == 0) rr = prev_rr;
                        else prev_rr=rr;

                        tvElectro.setText(" " + rr + " rpm");

                        if (x != 0) {
                            lilaRR.setBackgroundColor(getResources().getColor(R.color.WARNING_ORANGE));
                        } else {
                            lilaRR.setBackgroundColor(getResources().getColor(R.color.NORMAL));
                        }
                    }
                }
            }
        }
    };


}
