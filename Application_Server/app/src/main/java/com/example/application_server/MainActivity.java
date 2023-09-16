package com.example.application_server;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1001;
    private BluetoothAdapter mBluetoothAdapter;
    private Thread aThread;
    private final AtomicBoolean threadIsRunning = new AtomicBoolean(false);

    public void onClickStart(View view){
        if (threadIsRunning.get()==false) {
            this.aThread = new AcceptThread();
            aThread.start();
            Toast.makeText(this, "Waiting for the client to connect/reconnect", Toast.LENGTH_LONG).show();
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            String string = bundle.getString(String.valueOf(MessageConstants.MESSAGE_TOAST));
            final TextView myTextView = (TextView)findViewById(R.id.textView);
            myTextView.setText(string);

        }
    };

    private Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Bundle bundle = msg.getData();
            String string = bundle.getString(String.valueOf(MessageConstants.MESSAGE_TOAST2));
            Toast.makeText(getApplicationContext(), string ,Toast.LENGTH_LONG).show();

        }
    };


    // handler that gets info from Bluetooth service

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_TOAST = 0;
        public static final int MESSAGE_TOAST2 = 1;
        // ... (Add other message types here as needed.)
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private BluetoothSocket mmSocket;


        public AcceptThread() {

            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BLUETOOTH_ADAPTER", UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            threadIsRunning.set(true);
            while (threadIsRunning.get()==true) {
                try {
                    mmSocket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (mmSocket != null) {
                    BufferedReader in = null;
                    //TextView textView = findViewById(R.id.textView);
                    try {
                        in = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (in == null)
                        try {
                            throw new IOException();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    String inputLine="";
                    String s = new String("s");
                    while(true){
                        try {
                            if (!((inputLine = in.readLine()).equals(s)) )  {
                                System.out.println(inputLine);
                                Message receivedMsg =
                                        handler.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putString(String.valueOf(MessageConstants.MESSAGE_TOAST), inputLine);
                                receivedMsg.setData(bundle);
                                handler.sendMessage(receivedMsg);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                mmSocket.close();
                                threadIsRunning.set(false);
                                Message msg =
                                        handler2.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putString(String.valueOf(MessageConstants.MESSAGE_TOAST2),"Client has stopped the Thread. Press Start to launch a new Thread !");
                                msg.setData(bundle);
                                handler2.sendMessage(msg);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        }

                    }

                }

            }
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


     /*   public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || mBluetoothAdapter.isEnabled() == false) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }
        else{
            this.aThread = new AcceptThread();
            aThread.start();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            Thread thread2 = new AcceptThread();
            thread2.start();
        }

    }


}

