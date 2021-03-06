package com.example.filipe.socketcontroller;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Main Activity Watch";
    private TextView _x_acc;
    private TextView _y_acc;
    private TextView _z_acc;
    private TextView _tms;
    private CheckBox _leftHanded;
    private Button _startSensorBtn;
    private SensorManager _sensorManager;
    private Sensor _sensor;

    private GoogleApiClient _client;

    private boolean _sensor_running = false;

    private Node _phone; // the connected device to send the message to
    //private int _count=0;

    public static final String WEAR_ACC_SERVICE = "acc";

    private long _last_push;
    private long _sampling_diff = 40;        // alterei o sampling rate aqui
    private float _orientationVals[]={0,0,0};
    float[] _rotationMatrix = new float[16];

    float x;
    float z;
    private int _factor;

    PowerManager.WakeLock cpuWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                _x_acc          = (TextView) stub.findViewById(R.id.x_text_field);
                _y_acc          = (TextView) stub.findViewById(R.id.y_text_field);
                _z_acc          = (TextView) stub.findViewById(R.id.z_text_field);
                _tms            = (TextView) stub.findViewById(R.id.tms_text_field);
                _startSensorBtn = (Button) stub.findViewById(R.id.start_sensor_btn);
                _leftHanded     = (CheckBox) stub.findViewById(R.id.left_handed);

            }
        });

        _sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        _sensor = _sensorManager.getDefaultSensor( Sensor.TYPE_ORIENTATION);
        _last_push = System.currentTimeMillis();

    }

    @Override
    public void onResume() {
        super.onResume();

        _client = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        _client.connect();

        Log.i(TAG, "On resume called");

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        cpuWakeLock.acquire();

       // _sensorManager.registerListener(this, _sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //Log.wtf(TAG,event.toString());

/*
        SensorManager.getRotationMatrixFromVector(_rotationMatrix,
                event.values);
        SensorManager
                .remapCoordinateSystem(_rotationMatrix,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                        _rotationMatrix);
        SensorManager.getOrientation(_rotationMatrix, _orientationVals);

        // Optionally convert the result from radians to degrees
        _orientationVals[0] = (float) Math.toDegrees(_orientationVals[0]);
        _orientationVals[1] = (float) Math.toDegrees(_orientationVals[1]);
        _orientationVals[2] = (float) Math.toDegrees(_orientationVals[2]);

//        Yaw:  _orientationVals[0]
//        Pitch:  _orientationVals[1]
//        Roll:     _orientationVals[2]

        float x = _orientationVals[0];//event.values[0];
        float y = _orientationVals[1];//event.values[1];
        float z = _orientationVals[2];
        int val =4;*/

       // if(x>val||y>val||z>val) {
       // float y = event.values[1];





        //}
          //  Log.i(TAG,"Sending data");

          //  float[] data = {x,y};
            x = event.values[0];
           // _x_acc.setText(x+"");
            z = event.values[2];
            z = _factor*z;
           // _y_acc.setText(z+"");

//            Log.i("DEBUG",x+","+z);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void handleSensorClick(View v){

        if(v.getId() == R.id.start_sensor_btn && !_sensor_running) {
            _factor = _leftHanded.isChecked()?-1:1;
            _startSensorBtn.setText("Stop Sensor");
            _sensorManager.registerListener(this, _sensor, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i(TAG, "Aqui starting sensor");
            _sensor_running = true;
            new PushThread().start();
        }else{
            cpuWakeLock.release();
            _startSensorBtn.setText("Start Sensor");
            _sensorManager.unregisterListener(this);
            _sensor_running = false;

        }
    }

    public void handleQuitClick(View v){
        _sensorManager.unregisterListener(this);
        _sensor_running = false;
        this.finish();
    }

    public void handleSendReadingsClick(View v){
        Log.i(TAG, "Sending message");
        sendMessage("not a message");
    }
    //
    // WEAR COMMUNICATION STUFF
    //
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(_client)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            _phone = node;
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void sendMessage(String key){
        if (_phone != null && _client!= null && _client.isConnected()) {
            //   Log.d(TAG, "-- " + _client.isConnected());
            Wearable.MessageApi.sendMessage(
                    _client, _phone.getId(), WEAR_ACC_SERVICE + "" + key, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }else{
                              //  Log.i(TAG,"status "+sendMessageResult.getStatus().isSuccess());
                            }
                        }
                    }
            );
        }

    }

    private class PushThread extends Thread{

        public void run(){

            while(_sensor_running){

                sendMessage(x+"#"+z);
               // Log.i("DEBUG",x+"#"+z);

                try {
                    Thread.sleep(_sampling_diff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
