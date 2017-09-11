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
import android.graphics.Color;
import android.media.Image;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.seeun.devsign.HomeFragment.dbHelper;
import static com.seeun.devsign.MainActivity.sharedPreference;

public class AddDeviceActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Spinner spinner;
    String[] item;
    Button BTbtn;
    EditText editname;
    static TextView deviceTv, deviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    ImageButton[] typeImages;
    String selectedType;
    int btnnum, image=-1;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mWriteCharateristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //dbHelper = new DBHelper(getApplicationContext(), "DEVICES.db", null, 1);

        btnnum = Integer.parseInt(getIntent().getStringExtra("btnnum"));

        spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        item = new String[]{"선택하세요", "덤벨", "바벨", "상하운동"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        BTbtn = (Button)findViewById(R.id.button);
        BTbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DeviceScanActivity.class);
                startActivity(i);
            }
        });
        typeImages = new ImageButton[6];
        typeImages[0] = (ImageButton)findViewById(R.id.type1red);
        typeImages[1] = (ImageButton) findViewById(R.id.type1blue);
        typeImages[2] = (ImageButton) findViewById(R.id.type2red);
        typeImages[3] = (ImageButton)findViewById(R.id.type2blue);
        typeImages[4] = (ImageButton)findViewById(R.id.type3pink);
        typeImages[5] = (ImageButton)findViewById(R.id.type3yellow);

        deviceTv = (TextView)findViewById(R.id.textView2);
        deviceAddress = (TextView)findViewById(R.id.textView3);
        editname = (EditText)findViewById(R.id.editText);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(id == 1)
            selectedType = item[1];
        else if(id==2)
            selectedType = item[2];
        else if(id==3)
            selectedType = item[3];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void cancelClick(View view) {
        finish();
    }

    public void redDumbClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[0].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 0;
    }

    public void blueDumbClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[1].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 1;
    }

    public void redBarClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[2].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 2;
    }

    public void blueBarClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[3].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 3;
    }

    public void pinkSquatClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[4].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 4;
    }

    public void yellowSquatClick(View view) {
        for(int i =0;i<=5;i++)
            typeImages[i].setBackgroundColor(Color.parseColor("#FFFFFF"));
        typeImages[5].setBackgroundColor(Color.parseColor("#00FF00"));
        image = 5;

    }

    public void saveClick(View view) {
        String address = deviceAddress.getText().toString();
        String name = editname.getText().toString();
        if(address == "" || name == "" || selectedType.equals("선택하세요") || image==-1)
            Toast.makeText(getApplicationContext(), "항목을 모두 확인해주세요", Toast.LENGTH_SHORT).show();
        else {
            try {
                dbHelper.insert(btnnum, address, name, selectedType, image);
                Toast.makeText(getApplicationContext(), "등록 완료", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putBoolean("Registered["+btnnum+"]",true);
                editor.commit();
                finish();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "삽입 오류 발생", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void connectToBLE(){
        Log.e(this.getClass().getName(), "connectToBLE");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
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
                mBluetoothLeService.connect(deviceAddress.getText().toString());
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
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
                Log.e("ACTION_GATT_CONNECTED","ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e("GATT_DISCONNECTED","ACTION_GATT_DISCONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.e("SERVICES_DISCOVERED","ACTION_GATT_SERVICES_DISCOVERED");
                //mWriteCharacteristic.setValue("gc\n");
                //mBluetoothLeService.writeCharacteristic(mWriteCharacteristic, bytes);
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e("ACTION_DATA_AVAILABLE","ACTION_DATA_AVAILABLE");
            }
            Log.e("ACtion",""+action);
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        /*장치에서 블루투스가 활성화 되었는지 확인.
          블루투스가 비활성화 상태인 경우,
          사용자에게 활성화 할 수 있는 권한을 부여하는 대화상자 표시.*/
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(deviceAddress.getText().toString());
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
}
