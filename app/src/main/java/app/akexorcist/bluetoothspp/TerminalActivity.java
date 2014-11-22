/*
 * Copyright (C) 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.akexorcist.bluetoothspp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;

public class TerminalActivity extends Activity {
    BluetoothSPP bt;

    TextView textStatus, textRead;
    EditText etMessage;

    Menu menu;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        Log.i("Check", "onCreate");

        textRead = (TextView)findViewById(R.id.textRead);
        textStatus = (TextView)findViewById(R.id.textStatus);
        etMessage = (EditText)findViewById(R.id.etMessage);

        bt = new BluetoothSPP(this);

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                textRead.append(message + "\n");
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                textStatus.setText("Status : Not connect");
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                textStatus.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                textStatus.setText("Status : Connected to " + name);
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_disconnect) {
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }

    public void setup() {
        Button btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(etMessage.getText().length() != 0) {
                    bt.send(etMessage.getText().toString(), true);
                    etMessage.setText("");
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
