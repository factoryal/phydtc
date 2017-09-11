/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.seeun.devsign;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * 주어진 Bleetooth LE 장치에서 호스팅되는 GATT 서버와의 연결 및 데이터 통신을 관리하기 위한 서비스.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    boolean status = false;
    private BluetoothGattCharacteristic mWriteCharacteristic; //쓰기 위한 캐릭터리스틱 선언'
    private int mConnectionState = STATE_DISCONNECTED;
    static byte[] stdata = null;
    private ArrayList<BluetoothGattService> mGattServices
            = new ArrayList<BluetoothGattService>();
    private ArrayList<BluetoothGattCharacteristic> mGattCharacteristics
            = new ArrayList<BluetoothGattCharacteristic>();
    private ArrayList<BluetoothGattCharacteristic> mWritableCharacteristics
            = new ArrayList<BluetoothGattCharacteristic>();
    private BluetoothGattCharacteristic mDefaultChar = null;

    /*BluetoothGattCallback은 client의 연결 상태나 client 운영에 대한 결과를 전달함.
    * 하나의 액티비티가 연결하고 데이터를 표시하고 GATT 서비스와 특성들을 표시함.
    * 사용자 입력에 기반하여 BLuetoothLeservice로 불리는 서비스아 통신을 수행해도
    * AndroidBLE API를 통해 장치와 상호연동 함.*/
    static int cnt=0;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_DATA_WRITTEN =
            "com.example.bluetooth.le.ACTION_DATA_WRITTEN";

    public final static UUID UUID_DEVSIGN_DEVICE1 =
            UUID.fromString(SampleGattAttributes.DEVSIGN1);
    public final static UUID UUID_DEVSIGN_NUMBER =
            UUID.fromString(SampleGattAttributes.NUMBER);
    public final static UUID UUID_DEVSIGN_WRITE =
            UUID.fromString(SampleGattAttributes.UUID_WRITE);


    /*응용 프로그램이 관심을 갖는 GATT 이벤트에 대한 콜백 메소드를 구현.
    * 예를 들어, 연결 상태 변경 및 발견된 서비스.*/
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                /*연결이 성공한 후 서비스를 검색하려고 시도함.*/
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.e("writCHaracterSize : ",""+checkGattServices(gatt.getServices()));
                //getNotifyCharacteristic();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        public List<BluetoothGattService> getSupportedGattServices() {
            if (mBluetoothGatt == null) return null;
            return mBluetoothGatt.getServices();
        }

        void getNotifyCharacteristic(){
            List<BluetoothGattService> gattServices = getSupportedGattServices();
            String uuid = null;
            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    //Log.w(TAG, "##gattCharacteristic.getUuid() "+uuid);
                    if(uuid.compareTo(SampleGattAttributes.NUMBER)==0){
                        //mNotifyCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(gattCharacteristic, true);
                        //return;
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("야 이거 호출하긴하냐?","");
            if(status == BluetoothGatt.GATT_SUCCESS){
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                gatt.writeCharacteristic(characteristic);
                Log.e("브로드도 호출하네","");
            }
            super.onCharacteristicWrite(gatt,characteristic,status);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if (UUID_DEVSIGN_DEVICE1.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }




    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
       /*주어진 장치를 사용한 후에는 BluetoothGatt.close()가 호출되어
       * 자원이 제대로 정리오디었는지 확인해야함.
       * 이 예에서는 UI가 서비스와 연결되어 있지 않으면 close()가 호출됨.*/
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * 로컬  Bluetooth 어댑터에 대한 참조를 초기화.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {

        /*API 레벨 18 이상인 경우 BluetoothManager를 통해
        BluetoothAdapter에 대한 참조를 가져옴.*/
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Bluetooth LE 장치에 호스트 된 GATT 서버에 연결.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");

            return false;
        }

       /*이전에 연결된 장치에 다시 연결.*/
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        /*장치에 직접 연결하려고 하므로 autoConnect 매개 변수를 false로 설정함.*/
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * 주어진 BLE 장치를 사용한 후에, 앱은 리소스가 적절하게 해제되도록
     * 이 메소드를 호출해야함.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 튻ㅇ에 대한 알림을 사용하거나 사용하지 않도록 설정함.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_DEVSIGN_DEVICE1.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.NUMBER));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 연결된 장치에서 지원되는 GATT 서비스 목록을 검색.
     * This should be invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private int checkGattServices(List<BluetoothGattService> gattServices) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d("","# BluetoothAdapter not initialized");
            return -1;
        }

        for (BluetoothGattService gattService : gattServices) {
            // Default service info
            Log.d("","# GATT Service: "+gattService.toString());

            // Remember service
            mGattServices.add(gattService);

            // Extract characteristics
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                // Remember characteristic
                mGattCharacteristics.add(gattCharacteristic);
                Log.d("","# GATT Char: "+gattCharacteristic.toString());

                boolean isWritable = isWritableCharacteristic(gattCharacteristic);
                if(isWritable) {
                    mWritableCharacteristics.add(gattCharacteristic);
                    if(gattCharacteristic.getUuid().equals(UUID_DEVSIGN_NUMBER)){
                        writeRemoteCharacteristic(gattCharacteristic);
                    }
                    Log.e("writable캐릭터리스틱에","추가함");
                }

                boolean isReadable = isReadableCharacteristic(gattCharacteristic);
                if(isReadable) {
                    readCharacteristic(gattCharacteristic);
                }

                if(isNotificationCharacteristic(gattCharacteristic)) {
                    setCharacteristicNotification(gattCharacteristic, true);
                    if(isWritable && isReadable) {
                        mDefaultChar = gattCharacteristic;
                    }
                }
            }
        }

        return mWritableCharacteristics.size();
    }
    private boolean isWritableCharacteristic(BluetoothGattCharacteristic chr) {
        if(chr == null) return false;

        final int charaProp = chr.getProperties();
        if (((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
            Log.d("","# Found writable characteristic");
            mWriteCharacteristic = chr;
            byte[] data=new byte[3];
            data[0] = (byte)0x02;
            data[1] = (byte)0x42;
            data[2] = (byte)0x03;
//            writeCharacteristic(mWritableCharacteristics.get(1),data)
            writeRemoteCharacteristic(mWriteCharacteristic);
            return true;
        } else {
            Log.d("","# Not writable characteristic");
            return false;
        }
    }

    private boolean isReadableCharacteristic(BluetoothGattCharacteristic chr) {
        if(chr == null) return false;

        final int charaProp = chr.getProperties();
        if((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            Log.d("","# Found readable characteristic");
            return true;
        } else {
            Log.d("","# Not readable characteristic");
            return false;
        }
    }

    private boolean isNotificationCharacteristic(BluetoothGattCharacteristic chr) {
        if(chr == null) return false;

        final int charaProp = chr.getProperties();
        if((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            Log.d("","# Found notification characteristic");
            return true;
        } else {
            Log.d("","# Not notification characteristic");
            return false;
        }
    }

    public void writeRemoteCharacteristic(BluetoothGattCharacteristic characteristic){
        if(mBluetoothAdapter==null || mBluetoothGatt == null){
            Log.e(TAG, "BluetoothAdaper 초기화 안됨");
            return;
        }
        if(SampleGattAttributes.UUID_WRITE.equals(characteristic.getUuid().toString())){
            Log.e("UUDI",""+characteristic.getUuid().toString());
            byte[] data=new byte[3];
            data[0] = (byte)0x02;
            data[1] = (byte)0x42;
            data[2] = (byte)0x03;
            //String data = "hi"+"\n";
            System.out.println(data);
            characteristic.setValue(data);
            boolean status = mBluetoothGatt.writeCharacteristic(characteristic); //이거 다음 oncharac..write 콜백
            Log.e("STATUATATUS : ",""+status);
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,String intensity) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        //characteristic.setValue(intensity, BluetoothGattCharacteristic.FOR, 0);
        characteristic.setValue(intensity);
        if(characteristic !=null) {
            mBluetoothGatt.writeCharacteristic(characteristic);
            boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
            Log.e("STAatus", "" + status);
        }else{
            Log.e("NULL이잖아 이","");
        }
    }
}
