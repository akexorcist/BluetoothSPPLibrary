package app.akexorcist.bluetoothspplistener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import app.akexorcist.bluetoothspp.BluetoothSPP;
import app.akexorcist.bluetoothspp.BluetoothSPP.BluetoothStateListener;
import app.akexorcist.bluetoothspp.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetoothspp.BluetoothState;
import app.akexorcist.bluetoothspp.DeviceList;
import app.akexorcist.bluetoothspp.BluetoothSPP.AutoConnectionListener;
import app.akexorcist.bluetoothspp.BluetoothSPP.BluetoothConnectionListener;

public class Main extends Activity {
	BluetoothSPP bt;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		bt = new BluetoothSPP(this);

		if(!bt.isBluetoothAvailable()) {
			Toast.makeText(getApplicationContext()
					, "Bluetooth is not available"
					, Toast.LENGTH_SHORT).show();
            finish();
		}
		
		bt.setBluetoothStateListener(new BluetoothStateListener() {			
			public void onServiceStateChanged(int state) {
				if(state == BluetoothState.STATE_CONNECTED)
					Log.i("Check", "State : Connected");
				else if(state == BluetoothState.STATE_CONNECTING)
					Log.i("Check", "State : Connecting");
				else if(state == BluetoothState.STATE_LISTEN)
					Log.i("Check", "State : Listen");
				else if(state == BluetoothState.STATE_NONE)
					Log.i("Check", "State : None");
			}
		});
		
		bt.setOnDataReceivedListener(new OnDataReceivedListener() {
			public void onDataReceived(byte[] data, String message) {
				Log.i("Check", "Message : " + message);
			}
		});
		
		bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
			public void onDeviceConnected(String name, String address) {
				Log.i("Check", "Device Connected!!");
			}

			public void onDeviceDisconnected() {
				Log.i("Check", "Device Disconnected!!");
			}

			public void onDeviceConnectionFailed() {
				Log.i("Check", "Unable to Connected!!");
			}
		});
		
		bt.setAutoConnectionListener(new AutoConnectionListener() {
			public void onNewConnection(String name, String address) {
				Log.i("Check", "New Connection - " + name + " - " + address);
			}
			
			public void onAutoConnectionStarted() {
				Log.i("Check", "Auto connection started");
			}
		});
		
		Button btnConnect = (Button)findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
        			bt.disconnect();
        		} else {
                    Intent intent = new Intent(Main.this, DeviceList.class);
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
	
	public void setup() { }
}
