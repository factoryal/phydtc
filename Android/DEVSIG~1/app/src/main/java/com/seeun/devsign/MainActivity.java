package com.seeun.devsign;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    static SharedPreferences sharedPreference;
    static DBHelper dbHelper;

    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private String mDeviceAddress0;
    HomeFragment homeFrag;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    byte[] bytes = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction.replace(R.id.content, new HomeFragment()).commit();
                    return true;
                case R.id.navigation_history:
                    transaction.replace(R.id.content, new HistoryFragment()).commit();
                    return true;
                case R.id.navigation_information:
                    transaction.replace(R.id.content, new InformationFragment()).commit();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String getCount = "gc\n";
        try {
            bytes = getCount.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        dbHelper = new DBHelper(this, "DEVICES.db", null, 1);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        /*블루투스 어댑터 초기화*/
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        /*장치에서 블루투스가 지원되는지 확인*/
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        homeFrag = new HomeFragment();
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new HomeFragment()).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mDeviceAddress0=dbHelper.getAddress(0);
        activateBLE1();
    }

    public void Btn1Click(View view) {
        Intent i = new Intent(this, AddDeviceActivity.class);
        i.putExtra("btnnum","1");
        startActivity(i);
    }
    public void Btn2Click(View view) {
        Intent i = new Intent(this, AddDeviceActivity.class);
        i.putExtra("btnnum","2");
        startActivity(i);
    }
    public void Btn3Click(View view) {
        Intent i = new Intent(this, AddDeviceActivity.class);
        i.putExtra("btnnum","3");
        startActivity(i);
    }
    public void Btn4Click(View view) {
        Intent i = new Intent(this, AddDeviceActivity.class);
        i.putExtra("btnnum","4");
        startActivity(i);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            /*초기화가 성공적이면 자동으로 장치에 연결.*/
            try {
                mBluetoothLeService.connect(mDeviceAddress0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.e("이런","서비스 디스커넥트됨");
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.e("context-- : ",""+context.toString());
            Log.e("intent내용 : ",""+intent.toString());
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                homeFrag.imBtn0.setImageResource(R.drawable.dumbbell_100_red_connected);
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
                Log.e("ACTION_GATT_CONNECTED","ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                homeFrag.imBtn0.setImageResource(R.drawable.dumbbell_100_red);
                activateBLE1();
                Log.e("GATT_DISCONNECTED","ACTION_GATT_DISCONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.e("SERVICES_DISCOVERED","ACTION_GATT_SERVICES_DISCOVERED");
                //mWriteCharacteristic.setValue("gc\n");
                //mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, bytes);
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e("ACTION_DATA_AVAILABLE","ACTION_DATA_AVAILABLE");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
            Log.e("ACtion",""+action);
        }
    };

    private void displayData(String data) {
        try {
            if (data != null) {
                StringTokenizer st1 = new StringTokenizer(data, "\n");
                data = st1.nextToken().substring(5); //  0/10
                StringTokenizer st2 = new StringTokenizer(data, "/");
                data = st2.nextToken();
                Log.e("data", "" + data);
                homeFrag.setValue0(data);
            } else {
                Log.e("data", "null Pointer Exception");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    protected void activateBLE1() {
        Log.e(this.getClass().getName(), "** activateBLE1()");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*장치에서 블루투스가 활성화 되었는지 확인.
          블루투스가 비활성화 상태인 경우,
          사용자에게 활성화 할 수 있는 권한을 부여하는 대화상자 표시.*/
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress0);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService.disconnect();
        mBluetoothLeService = null;
    }

    /*지원되는 GATT 서비스/특성을 반복하는 방법을 보임.
    * 이 샘플에서는 UI의 ExpandableListView에 바인딩 된 데이터 구조체를 채움.*/
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        //mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        /*사용 가능한 GATT 서비스를 통해 반복함.*/
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
           // currentServiceData.put(
          //          LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
          //  currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            /*사용 가능한 특성을 반복함.*/
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
              //  currentCharaData.put(
             //           LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
             //   currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            //mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void disClick(View view) {
        mBluetoothLeService.disconnect();
    }
}
