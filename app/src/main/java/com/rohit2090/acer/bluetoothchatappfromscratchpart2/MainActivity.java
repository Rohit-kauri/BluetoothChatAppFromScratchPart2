package com.rohit2090.acer.bluetoothchatappfromscratchpart2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    final static String TAG = "Rohit";
    BluetoothAdapter bluetoothAdapter;                                  //object for working with bluetooth
    Button discoverable;
    Button discover;
    ArrayList<BluetoothDevice> bTDevices;
    DeviceAdapter deviceAdapter;
    ListView listView;
    ListAdapter listAdapter;
    ArrayList<String> listString;
    ArrayAdapter<String> arrayAdapter;

    BluetoothConnectionService mBluetoothConnection;
    Button btnRead;
    Button btnStartConnection;
    Button btnSend;

    EditText etSend;

    TextToSpeech t1;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button onOff = (Button) findViewById(R.id.btnSwitch);
        discoverable = (Button) findViewById(R.id.btnDiscoverable);
        listString = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        bTDevices = new ArrayList<>();
        discover = (Button) findViewById(R.id.discover);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();  //gets the default bluetooth adapter
        t1  = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if(status != TextToSpeech.ERROR)
                {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        btnRead = (Button) findViewById(R.id.read);
        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.editText);

        listView.setOnItemClickListener(MainActivity.this);
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, intentFilter);


        onOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "On/Off button:On click executed");
                enableDisable();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSend.setText(Data.data);
                t1.speak(Data.data,TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    //create method for starting connection
//***remember the conncction will fail and app will crash if you haven't paired first
    public void startConnection() {
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device, uuid);
    }

    // Create a BroadcastReceiver for ACTION_STATE_CHANGED
    //Take the action according to the change found and transfered by the intent filter
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "onReceive: Bluetooth turning off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "onReceive: Bluetooth turning on");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "onReceive: Bluetooth turned off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "onReceive: Bluetooth turned on");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, bluetoothAdapter.ERROR);
                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.i(TAG, "onReceive2: Bluetooth discoverable");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.i(TAG, "onReceive2: Bluetooth Connected");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.i(TAG, "onReceive2: Bluetooth Connecting");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.i(TAG, "onReceive2: Able to be connected");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.i(TAG, "onReceive2: Not discoverable and connectable");
                        break;

                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "On receive 3: Action found");
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bTDevices.add(device);
                listString.add(device.getName() + " " + device.getAddress());
                Log.i(TAG, "On action found: Device name = " + device.getName() + "Address = " + device.getAddress());
                arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listString);
                deviceAdapter = new DeviceAdapter(context, R.layout.device_list_view, bTDevices);
                listView.setAdapter(arrayAdapter);
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "On receive 4: Bond State Changed");
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.i(TAG, "On Receive 4: Bond Bonded ");
                    //inside BroadcastReceiver4
                    mBTDevice = device;
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.i(TAG, "On Receive 4: Bond Bonding");
                }
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.i(TAG, "On Receive 4: Bond None ");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        Log.i(TAG, "Bluetooth State: on Destroy called");
    }

    public void enableDisable() {
        if (bluetoothAdapter == null) {
            Log.i(TAG, "Not supported bluetooth");
        }

        /*Enabling the bluetooth*/
        if (!bluetoothAdapter.isEnabled()) {
            Log.i(TAG, "Bluetooth Enabled");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);

            IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);        //If there is any change
            registerReceiver(mBroadcastReceiver, btIntent);                                         //Register to broadcast reciver
        }

        /*Disabling the bluetooth*/
        if (bluetoothAdapter.isEnabled()) {
            Log.i(TAG, "Bluetooth disabled");
            bluetoothAdapter.disable();

            IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver, btIntent);


        }
    }

    public void btnDiscoverable(View view) {
        Log.i(TAG, "Button discoverable State: Discovering devices for 300sec");
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(i);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(View view) {
        Log.i(TAG, "Button discover: Discover the devices looking for unpaired devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "Button isDiscovering: Cancel  discovery");

            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, intentFilter);
        }
        if (!bluetoothAdapter.isDiscovering()) {
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            Log.i(TAG, "Button Start Discovering: Start discovering");
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            Log.i(TAG, "Button Start Discovering: Call broad cast receiver");
            registerReceiver(mBroadcastReceiver3, intentFilter);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     * <p>
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.i(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        bluetoothAdapter.cancelDiscovery();
        Log.i(TAG, "on item click: You clicked on item");

        String deviceName = bTDevices.get(position).getName();
        String deviceAddress = bTDevices.get(position).getAddress();
        Log.i(TAG, "onItemClick: deviceName = " + deviceName);
        Log.i(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            bTDevices.get(position).createBond();

            mBTDevice = bTDevices.get(position);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }

    public static class Data{
        public static String data;
    }
}




