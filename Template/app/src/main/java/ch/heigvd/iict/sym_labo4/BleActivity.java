package ch.heigvd.iict.sym_labo4;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.Calendar;

import ch.heigvd.iict.sym_labo4.abstractactivies.BaseTemplateActivity;
import ch.heigvd.iict.sym_labo4.adapters.ResultsAdapter;
import ch.heigvd.iict.sym_labo4.viewmodels.BleOperationsViewModel;

/**
 * Project: Labo4
 * Created by fabien.dutoit on 09.08.2019
 * (C) 2019 - HEIG-VD, IICT
 * Modifications : Benjamin Thomas, Gabriel Arzur Catel Torres, Alves Claude-André
 *
 * ajouts d'observer sur certaines données ( température, nombre de click et heure du
 *          périphérique blutooth)
 * ajout d'un bouton pour synchroniser l'heure du périphérique avec le smartphone
 * ajout d'un bouton pour mettre à jour la température
 * ajout d'un bouton pour envoyer un nombre (contrôle de saisie basique) vers le graph du$
 *          périphérique blootooth
 */

public class BleActivity extends BaseTemplateActivity {

    private static final String TAG = BleActivity.class.getSimpleName();

    //system services
    private BluetoothAdapter bluetoothAdapter = null;

    //view model
    private BleOperationsViewModel bleViewModel = null;

    //gui elements
    private View operationPanel = null;
    private View scanPanel = null;

    private ListView scanResults = null;
    private TextView emptyScanResults = null;

    private TextView currentTimeText = null;
    private TextView buttonPressedText = null;
    private TextView smartphoneTemperatureText = null;

    private EditText bytesToSendText = null;

    private Button smartphoneTimeButton = null;
    private Button bytesToSendButton = null;
    private Button temperatureButton = null;


    //menu elements
    private MenuItem scanMenuBtn = null;
    private MenuItem disconnectMenuBtn = null;

    //adapters
    private ResultsAdapter scanResultsAdapter = null;

    //states
    private Handler handler = null;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        this.handler = new Handler();

        //enable and start bluetooth - initialize bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

        //link GUI
        this.operationPanel = findViewById(R.id.ble_operation);
        this.scanPanel = findViewById(R.id.ble_scan);
        this.scanResults = findViewById(R.id.ble_scanresults);
        this.emptyScanResults = findViewById(R.id.ble_scanresults_empty);

        this.currentTimeText = findViewById(R.id.currentTime);
        this.buttonPressedText = findViewById(R.id.buttonPressedNumber);
        this.smartphoneTemperatureText = findViewById(R.id.smartphoneTemperature);

        this.bytesToSendText = findViewById(R.id.bytesToSend);

        this.smartphoneTimeButton = findViewById(R.id.getSmartphoneTime);
        this.bytesToSendButton = findViewById(R.id.bytesToSendButton);
        this.temperatureButton = findViewById(R.id.smartphoneTemperatureButton);

        //manage scanned item
        this.scanResultsAdapter = new ResultsAdapter(this);
        this.scanResults.setAdapter(this.scanResultsAdapter);
        this.scanResults.setEmptyView(this.emptyScanResults);

        //connect to view model
        this.bleViewModel = ViewModelProviders.of(this).get(BleOperationsViewModel.class);

        updateGui();

        //events
        this.scanResults.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            runOnUiThread(() -> {
                //we stop scanning
                scanLeDevice(false);
                //we connect to the clicked device
                bleViewModel.connect(((ScanResult)scanResultsAdapter.getItem(position)).getDevice());
            });
        });

        //ble events
        this.bleViewModel.isConnected().observe(this, (isConnected) -> {
            updateGui();
        });
        // observation de la température
        this.bleViewModel.getTemperature().observe(this, (temperature) -> {
            smartphoneTemperatureText.setText("temperature : " + temperature.toString() + " °C");
        });
        // observation du nombre de clicks
        this.bleViewModel.getClickCount().observe(this, (clickCount) -> {
            buttonPressedText.setText("click count : " + bleViewModel.getClickCount().getValue().toString());
        });
        // observation de l'heure
        this.bleViewModel.getCalendarDate().observe(this, (calendarDate) -> {
            StringBuilder str = new StringBuilder();
            str.append("time on BT device : ");
            str.append(calendarDate.get(Calendar.HOUR_OF_DAY));
            str.append(":");
            str.append(calendarDate.get(Calendar.MINUTE));
            str.append(":");
            str.append(calendarDate.get(Calendar.SECOND));
            currentTimeText.setText(str.toString());
        });

        // button events
        temperatureButton.setOnClickListener((view)-> {
            bleViewModel.readTemperature();

        });

        smartphoneTimeButton.setOnClickListener((view) -> {
            bleViewModel.writeCurrentTime();
        });

        // opération du bouton qui envoi des données pour le graph
        bytesToSendButton.setOnClickListener((view) -> {
            try {
                bleViewModel.writeByte(Integer.parseInt(bytesToSendText.getText().toString()));
            } catch (Exception e) {
                Toast.makeText(getApplication(), "wrong entry", Toast.LENGTH_SHORT).show();
            }
            bytesToSendText.getText().clear();

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ble_menu, menu);
        //we link the two menu items
        this.scanMenuBtn = menu.findItem(R.id.menu_ble_search);
        this.disconnectMenuBtn = menu.findItem(R.id.menu_ble_disconnect);
        //we update the gui
        updateGui();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_ble_search) {
            if(isScanning)
                scanLeDevice(false);
            else
                scanLeDevice(true);
            return true;
        }
        else if (id == R.id.menu_ble_disconnect) {
            bleViewModel.disconnect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(this.isScanning)
            scanLeDevice(false);
        if(isFinishing())
            this.bleViewModel.disconnect();
    }

    /*
     * Method used to update the GUI according to BLE status:
     * - connected: display operation panel (BLE control panel)
     * - not connected: display scan result
     */
    private void updateGui() {
        Boolean isConnected = this.bleViewModel.isConnected().getValue();
        if(isConnected != null && isConnected) {
            this.scanPanel.setVisibility(View.GONE);
            this.operationPanel.setVisibility(View.VISIBLE);

            if(this.scanMenuBtn != null && this.disconnectMenuBtn != null) {
                this.scanMenuBtn.setVisible(false);
                this.disconnectMenuBtn.setVisible(true);
            }
        } else {
            this.operationPanel.setVisibility(View.GONE);
            this.scanPanel.setVisibility(View.VISIBLE);

            if(this.scanMenuBtn != null && this.disconnectMenuBtn != null) {
                this.disconnectMenuBtn.setVisible(false);
                this.scanMenuBtn.setVisible(true);
            }
        }
    }

    //this method need user granted localisation permission, our demo app is requesting it on MainActivity
    private void scanLeDevice(final boolean enable) {
        final BluetoothLeScanner bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (enable) {

            //config
            ScanSettings.Builder builderScanSettings = new ScanSettings.Builder();
            builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            builderScanSettings.setReportDelay(0);

            //we scan for any BLE device
            //we don't filter them based on advertised services...

            //Create a filter list
            ArrayList<ScanFilter> filters = new ArrayList<>();

            //Create a filter of the UUID
            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("3c0a1000-281d-4b48-b2a7-f15579a1c38f")).build();

            //Add the filter to the filter list
            filters.add(scanFilter);

            //reset display
            scanResultsAdapter.clear();

            //Add the filter list to the start scan
            bluetoothScanner.startScan(filters, builderScanSettings.build(), leScanCallback);
            Log.d(TAG,"Start scanning...");
            isScanning = true;

            //we scan only for 15 seconds
            handler.postDelayed(() -> {
                scanLeDevice(false);
            }, 15*1000L);

        } else {
            bluetoothScanner.stopScan(leScanCallback);
            isScanning = false;
            Log.d(TAG,"Stop scanning (manual)");
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            runOnUiThread(() -> {
                scanResultsAdapter.addDevice(result);
            });
        }
    };

}
