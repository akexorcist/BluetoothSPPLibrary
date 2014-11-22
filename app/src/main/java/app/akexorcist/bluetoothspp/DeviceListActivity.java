/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.akexorcist.bluetoothspp;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class DeviceListActivity extends Activity {
    BluetoothSPP bt;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicelist);

        bt = new BluetoothSPP(this);

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.i("Check", "Length : " + data.length);
                Log.i("Check", "Message : " + message);
            }
        });

        Button btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(DeviceListActivity.this, DeviceList.class);
                    intent.putExtra("bluetooth_devices", "Bluetooth devices");
                    intent.putExtra("no_devices_found", "No device");
                    intent.putExtra("scanning", "Scanning");
                    intent.putExtra("scan_for_devices", "Search");
                    intent.putExtra("select_device", "Select");
                    intent.putExtra("layout_list", R.layout.device_layout_list);
                    intent.putExtra("layout_text", R.layout.device_layout_text);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            bt.enable();
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void setup() {
        Button btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                bt.send("Text", true);
            }
        });
    }
}
