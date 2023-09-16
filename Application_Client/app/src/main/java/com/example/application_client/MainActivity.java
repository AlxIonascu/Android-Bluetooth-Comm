package com.example.application_client;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1001;
    private Spinner spinner;
    private String command = "u";
    private Thread thread1 ;
    private final AtomicBoolean threadIsRunning = new AtomicBoolean(false);
    private BluetoothDevice mBluetoothDevice;


        public void onClickLeft(View view){
                this.command="l";
        }

        public void onClickRight(View view) {
                this.command= "r";
        }

        public void onClickUp(View view){
            this.command="u";
        }

        public void onClickDown (View view){
            this.command="d";
        }

        public String getCommand(){
            return this.command;
        }

        public void onClickStop(View view){
            this.command="s";

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.threadIsRunning.set(false);
            Toast.makeText(this, "Connection stopped, press START on server then PAIR the client again.",Toast.LENGTH_LONG).show();
            this.command="u";

        }

        public void onClickPair(View view){
            thread1 = new ConnectThread(mBluetoothDevice);
            thread1.start();
            Toast.makeText(this,"Connected to "+ mBluetoothDevice.getName(),Toast.LENGTH_LONG).show();

        }



    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private Spinner spinner;



        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            threadIsRunning.set(true);
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                return;
            }
            try {
                manageConnectedSocket();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }



       void manageConnectedSocket() throws IOException, InterruptedException {
           PrintWriter out = null;
           out = new PrintWriter(mmSocket.getOutputStream(), true);
           if (out == null)
               throw new IOException();
           while(threadIsRunning.get()==true){
               out.println(getCommand());
               sleep(100);
           }
        cancel();
       }
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
        else {
            getPairedDevices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==0){
            finish();
            System.exit(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "Bluetooth enabled received", Toast.LENGTH_LONG);

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_LONG);
            getPairedDevices();
        }
    }
    protected void getPairedDevices() {
        final Set<BluetoothDevice> pairedDevices =mBluetoothAdapter.getBondedDevices();
        this.spinner = (Spinner) findViewById(R.id.Spinner);
        final List<String> s = new ArrayList<String>();
        for(BluetoothDevice bt : pairedDevices) {
            s.add(bt.getName());
        }


        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, s);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.spinner.setAdapter(adapter);
        this.spinner.setSelection(0, false);
        String selectedBluetoothDevice = (String) this.spinner.getItemAtPosition(0);
        for(BluetoothDevice bt : pairedDevices) {
            if (bt.getName().equals(selectedBluetoothDevice)) {
                mBluetoothDevice=bt;
                Toast.makeText(getApplicationContext(),getString(R.string.Toast_getPaired),Toast.LENGTH_LONG).show();

            }

        }
        this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
               String selectedBluetoothDevice = (String) parent.getItemAtPosition(pos);
                for(BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals(selectedBluetoothDevice)) {
                        mBluetoothDevice=bt;
                        Toast.makeText(getApplicationContext(),getString(R.string.Toast_getPaired),Toast.LENGTH_LONG).show();

                    }

                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                      }

        });
    }
}
