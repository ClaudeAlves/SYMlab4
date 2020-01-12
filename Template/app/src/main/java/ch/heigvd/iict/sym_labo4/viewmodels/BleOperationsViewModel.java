package ch.heigvd.iict.sym_labo4.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.data.Data;

/**
 * Project: Labo4
 * Created by fabien.dutoit on 09.08.2019
 * (C) 2019 - HEIG-VD, IICT
 * Modifications : Benjamin Thomas, Gabriel Arzur Catel Torres, Alves Claude-André
 *
 * Ajout de divers observer (température, nombre de click date du calendrier)
 * ajout d'une fonction permettant de convertir une date au format Data en format Calendar
 *          pour une utilisation facilité
 * ajout de divers fonctions pour les différentes lectures( température, heure, nombre de clicks)
 *
 */
public class BleOperationsViewModel extends AndroidViewModel {

    private static final String TAG = BleOperationsViewModel.class.getSimpleName();

    private MySymBleManager ble = null;
    private BluetoothGatt mConnection = null;

    //UUID
    private final String timeServiceUUID =  "00001805-0000-1000-8000-00805f9b34fb";
    private final String symServiceUUID =  "3c0a1000-281d-4b48-b2a7-f15579a1c38f";
    private final String currentTimeCharUUID = "00002A2B-0000-1000-8000-00805f9b34fb";
    private final String integerCharUUID = "3c0a1001-281d-4b48-b2a7-f15579a1c38f";
    private final String temperatureCharUUID = "3c0a1002-281d-4b48-b2a7-f15579a1c38f";
    private final String buttonClickCharUUID = "3c0a1003-281d-4b48-b2a7-f15579a1c38f";

    //live data - observer
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();
    private final MutableLiveData<Integer> mTemperature = new MutableLiveData<>();
    private final MutableLiveData<Integer> mClickCount = new MutableLiveData<>();
    private final MutableLiveData<Calendar> mCalendarDate = new MutableLiveData<>();
    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }
    public LiveData<Integer> getTemperature(){return mTemperature;}
    public LiveData<Integer> getClickCount(){return mClickCount;}
    public LiveData<Calendar> getCalendarDate(){return mCalendarDate;}

    //references to the Services and Characteristics of the SYM Pixl
    private BluetoothGattService timeService = null, symService = null;
    private BluetoothGattCharacteristic currentTimeChar = null, integerChar = null, temperatureChar = null, buttonClickChar = null;

    public BleOperationsViewModel(Application application) {
        super(application);
        this.mIsConnected.setValue(false); //to be sure that it's never null
        this.ble = new MySymBleManager();
        this.ble.setGattCallbacks(this.bleManagerCallbacks);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared");
        this.ble.disconnect();
    }

    public void connect(BluetoothDevice device) {
        Log.d(TAG, "User request connection to: " + device);
        if(!mIsConnected.getValue()) {
            this.ble.connect(device)
                    .retry(1, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    public void disconnect() {
        Log.d(TAG, "User request disconnection");
        this.ble.disconnect();
        if(mConnection != null) {
            mConnection.disconnect();
        }
    }
    /* TODO
        vous pouvez placer ici les différentes méthodes permettant à l'utilisateur
        d'interagir avec le périphérique depuis l'activité
     */
    // cette fonction permet de convertir une date dans le format décrit dans la donnée en type Calendar
    public Calendar convertDataToCalendar(Data data) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, data.getIntValue(Data.FORMAT_UINT16, 0));
            calendar.set(Calendar.MONTH, data.getIntValue(Data.FORMAT_UINT8, 2));
            calendar.set(Calendar.DAY_OF_MONTH, data.getIntValue(Data.FORMAT_UINT8, 3));
            calendar.set(Calendar.HOUR_OF_DAY, data.getIntValue(Data.FORMAT_UINT8, 4));
            calendar.set(Calendar.MINUTE, data.getIntValue(Data.FORMAT_UINT8, 5));
            calendar.set(Calendar.SECOND, data.getIntValue(Data.FORMAT_UINT8, 6));
            calendar.set(Calendar.DAY_OF_WEEK, data.getIntValue(Data.FORMAT_UINT8, 7));

        /*
        Soucis ici le set du jour du mois ne marche pas
        System.out.println(data.getIntValue(Data.FORMAT_UINT8, 3).toString()); renvoi 12 (dernier test dimanche 12 janvier)
        System.out.println(calendar.get(Calendar.DAY_OF_MONTH)); renvoi 15 (dernier test dimanche 12 janvier)
         */


        return calendar;

    }
    public boolean writeCurrentTime() {
        if(!isConnected().getValue() || currentTimeChar == null) return false;
        return ble.writeCurrentTime();
    }
    public boolean readTemperature() {
        if(!isConnected().getValue() || temperatureChar == null) return false;
        return ble.readTemperature();
    }
    public boolean writeByte(int value) {
        if(!isConnected().getValue() || integerChar == null) return false;
        return ble.writeByte(value);
    }

    private BleManagerCallbacks bleManagerCallbacks = new BleManagerCallbacks() {
        @Override
        public void onDeviceConnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceConnected(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnected");
            mIsConnected.setValue(true);
        }

        @Override
        public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceDisconnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceDisconnected");
            mIsConnected.setValue(false);
        }

        @Override
        public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onLinkLossOccurred");
        }

        @Override
        public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
            Log.d(TAG, "onServicesDiscovered");
        }

        @Override
        public void onDeviceReady(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceReady");
        }

        @Override
        public void onBondingRequired(@NonNull BluetoothDevice device) {
            Log.w(TAG, "onBondingRequired");
        }

        @Override
        public void onBonded(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onBonded");
        }

        @Override
        public void onBondingFailed(@NonNull BluetoothDevice device) {
            Log.e(TAG, "onBondingFailed");
        }

        @Override
        public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
            Log.e(TAG, "onError:" + errorCode);
        }

        @Override
        public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
            Log.e(TAG, "onDeviceNotSupported");
            Toast.makeText(getApplication(), "Device not supported", Toast.LENGTH_SHORT).show();
        }
    };

    /*
     *  This class is used to implement the protocol to communicate with the BLE device
     */
    private class MySymBleManager extends BleManager<BleManagerCallbacks> {

        private MySymBleManager() {
            super(getApplication());
        }

        @Override
        public BleManagerGattCallback getGattCallback() { return mGattCallback; }

        /**
         * BluetoothGatt callbacks object.
         */
        private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

            @Override
            public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
                mConnection = gatt; //trick to force disconnection
                Log.d(TAG, "isRequiredServiceSupported - discovered services:");
                timeService = mConnection.getService(UUID.fromString(timeServiceUUID));
                symService = mConnection.getService(UUID.fromString(symServiceUUID));
                currentTimeChar = timeService.getCharacteristic(UUID.fromString(currentTimeCharUUID));
                integerChar = symService.getCharacteristic(UUID.fromString(integerCharUUID));
                buttonClickChar = symService.getCharacteristic(UUID.fromString(buttonClickCharUUID));
                temperatureChar = symService.getCharacteristic(UUID.fromString(temperatureCharUUID));
                return timeService != null && symService != null &&
                        currentTimeChar != null && integerChar != null &&
                        buttonClickChar != null && temperatureChar != null;
            }

            @Override
            protected void initialize() {
                //mise en place des callbacks pour la date et le nombre de clicks
                // ceux-ci sont mis à jour en permanence
                enableNotifications(buttonClickChar).enqueue();
                enableNotifications(currentTimeChar).enqueue();

                setNotificationCallback(buttonClickChar).with((device, data) -> {
                    mClickCount.setValue(data.getIntValue(Data.FORMAT_UINT8, 0));
                });

                setNotificationCallback(currentTimeChar).with((device, data) -> {
                    mCalendarDate.setValue(convertDataToCalendar(data));
                });

            }

            @Override
            protected void onDeviceDisconnected() {
                //we reset services and characteristics
                timeService = null;
                currentTimeChar = null;

                symService = null;
                integerChar = null;
                temperatureChar = null;
                buttonClickChar = null;
            }
        };

        // permet la lecture de la température
        public boolean readTemperature() {
            readCharacteristic(temperatureChar).with((device, data) -> {
                int temperature = data.getIntValue(Data.FORMAT_UINT16, 0);
                mTemperature.setValue(temperature/10);
            }).enqueue();
            return true;
        }
        // permet l'écriture de la
        public boolean writeCurrentTime( ) {
            Calendar now = Calendar.getInstance();

            int year = now.get(Calendar.YEAR);

            byte[] currentByteTime = new byte[10];
            // année en little endian
            currentByteTime[0] = (byte) (year);
            currentByteTime[1] = (byte) (year >> 8);
            // le format ne correspond pas les mois vont de 0 à 11 il faut donc ajouter 1
            // pour avoir le nombre du mois comme dans le calendrier Gégorien
            currentByteTime[2] = (byte) (now.get(Calendar.MONTH) + 1);
            currentByteTime[3] = (byte) (now.get(Calendar.DAY_OF_MONTH));
            currentByteTime[4] = (byte) (now.get(Calendar.HOUR_OF_DAY));
            currentByteTime[5] = (byte) (now.get(Calendar.MINUTE));
            currentByteTime[6] = (byte) (now.get(Calendar.SECOND));
            currentByteTime[7] = (byte) (now.get(Calendar.DAY_OF_WEEK));


            writeCharacteristic(currentTimeChar, currentByteTime).enqueue();
            return true;
        }
        public boolean writeByte(int value) {
            writeCharacteristic(integerChar, Data.from(Integer.toString(value))).enqueue();
            return true;
        }
    }
}
