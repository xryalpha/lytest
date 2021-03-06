package roy.studio.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import static java.lang.Boolean.FALSE;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private UsbManager mUsbmanager;
    UsbDevice mUsbDevice;
    private GsonA mGa=null;
    private int btfind=9;
    private ConnectThread connectThread;
    private USBConnectThread usbconnectThread;
    boolean btlink=false, flgsave=false;
    String tv1s="",add="",btconfig="";
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        //??????????????????
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,filter);
        IntentFilter filter2=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver,filter2);
        IntentFilter filter3=new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mReceiver,filter3);
        IntentFilter filter4=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver,filter4);
        IntentFilter filter5=new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mReceiver,filter5);
        IntentFilter filter6=new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver,filter6);
        IntentFilter filter7=new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mReceiver,filter7);
        //??????????????????
        EventBus.getDefault().register(this);
        //????????????????????????
        try {
            FileInputStream fis=openFileInput("btconfig.txt");
            byte [] buffer=new  byte[fis.available()];
            fis.read(buffer);
            btconfig=new String(buffer);
            mGa= GsonA.objectFromData(btconfig);
            Log.e("read",mGa.getName());
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("read","fail");
        }
        if(Usbexist()==0) UsbLink();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanQrcode(view); //????????????
                if(!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA
                }, 0
        );
    }
    @Override  //??????????????????
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "???????????????", Toast.LENGTH_LONG).show();
            } else {
                try {
                    EventBus.getDefault().post(new SwitchEvent(1, false));
                    btlink=false;
                    EventBus.getDefault().post(new ConnectEvent(10,""));
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    btconfig=result.getContents();
                    mGa=GsonA.objectFromData(btconfig);
                    EventBus.getDefault().post(new MessageEvent("???????????????..."));
                    mBluetoothAdapter.startDiscovery();
                    btfind=9;
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_LONG).show();
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void onDestroy() {
        super.onDestroy();
        //????????????????????????
        unregisterReceiver(mReceiver);
        //??????????????????????????????
        EventBus.getDefault().unregister(this);
        //??????????????????
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    //??????????????????
    private BroadcastReceiver mReceiver=new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action=intent.getAction();
            BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.e("Lyt", action);
            switch (action){
                case BluetoothDevice.ACTION_FOUND:
                    Log.e("????????????:", "["+device.getName()+"]"+":"+device.getAddress());
                    try {
                        if (device.getName().equals(mGa.getName())){
                            add=device.getAddress();
                            mBluetoothAdapter.cancelDiscovery();
                            if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                                btfind=1;
                                ClsUtils.createBond(device.getClass(), device);
                            }else{
                                btfind=0;
                                Toast.makeText(getApplicationContext(), "???????????????", Toast.LENGTH_LONG).show();
                                btsave();
                            }
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    try {
                        //1.????????????//????????????????????????????????????
//                    ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                        //2.??????????????????
                        abortBroadcast();//???????????????????????????????????????????????????????????????????????????
                        //3.??????setPin??????????????????...
                        ClsUtils.setPin(device.getClass(), device, mGa.getPin());
//                    Log.e(device.getName(),"pair");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if(btfind==9){
                        Toast.makeText(getApplicationContext(), "???????????????", Toast.LENGTH_LONG).show();
                        EventBus.getDefault().post(new SwitchEvent(1, false));
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE); //????????????????????????
                    if(state==10) Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_LONG).show();
                    if(state==11) Toast.makeText(getApplicationContext(), "?????????...", Toast.LENGTH_LONG).show();
                    if(state==12){
                        Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_LONG).show();
                        btsave();
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    if(Usbexist()==0) UsbLink();
                    break;
                case ACTION_USB_PERMISSION:
//                    Log.e("usb","link");
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)) {
                        if (mUsbDevice != null) {
                            //??????????????????????????????????????????
                            UsbLink();
                        } else {
//                            Toast.makeText(context, "????????????USB", Toast.LENGTH_LONG).show();
                            EventBus.getDefault().post(new MessageEvent("????????????USB??????"));
                        }
                    } else {
//                        Toast.makeText(context, "????????????USB??????", Toast.LENGTH_LONG).show();
                        EventBus.getDefault().post(new MessageEvent("????????????USB??????"));
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.e("USB","out");
                    break;
            }
        }
    };
    public void onScanQrcode(View v){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("???????????????");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }
    public void btsave(){
        btlink=true;
        handlerbtlink.sendEmptyMessageDelayed(0, 0);
        FileOutputStream fos;
        try {
            fos=openFileOutput("btconfig.txt",MODE_PRIVATE);
            fos.write(btconfig.getBytes());//????????????????????????????????????????????????
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("HandlerLeak")
    private Handler handlerbtlink = new Handler() {
        public void handleMessage(android.os.Message msg) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(add);
            UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            //????????????
            try {
                //??????Socket
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(BT_UUID);
                //??????????????????
                connectThread = new ConnectThread(socket, true);
                Thread Tssb = new Thread(connectThread);
                Tssb.start();
            } catch (IOException e) { }
        }
    };
    public int Usbexist(){
        int back=9;
        try {
            Context context = getBaseContext();
            mUsbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = mUsbmanager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                mUsbDevice = deviceIterator.next();
                // ????????????????????????????????????
//                Log.e("USB", mUsbDevice.getVendorId() + ":" + mUsbDevice.getProductId());
                if (mUsbDevice.getVendorId() == 1155 && mUsbDevice.getProductId() == 22336) {
//                    Log.e("USB", "exist");
                    back=1;
                    if (!mUsbmanager.hasPermission(mUsbDevice)) {
                        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        mUsbmanager.requestPermission(mUsbDevice, permissionIntent);
                    }else back = 0;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e("USB",""+back);
        return back;
    }
    public void UsbLink(){
        try {
            mUsbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
            //??????????????????
            usbconnectThread = new USBConnectThread(mUsbmanager, mUsbDevice);
            Thread Tssa = new Thread(usbconnectThread);
            Tssa.start();
        } catch (Exception e) { }
    }
    @Subscribe
    public void onConnectEvent(ConnectEvent event) {
        switch (event.state){
            case 0:btfind=0;
                EventBus.getDefault().post(new MessageEvent("??????????????????"));
                EventBus.getDefault().post(new SwitchEvent(1, true));
                break;
            case 1:
                EventBus.getDefault().post(new ScaleEvent(1,event.msg));
            case 21:
//                EventBus.getDefault().post(new MessageEvent(event.msg));
                EventBus.getDefault().post(new ScaleEvent(2,event.msg));
                break;
            case 2:
                EventBus.getDefault().post(new MessageEvent("??????????????????"));
                break;
            case 3:
                EventBus.getDefault().post(new MessageEvent("??????????????????,?????????..."));
                handlerbtlink.sendEmptyMessageDelayed(0, 5000);
                break;
            case 20:
                EventBus.getDefault().post(new MessageEvent(event.msg));
                break;
        }
    }
    @Subscribe
    public void onSwitchEvent(SwitchEvent event) {
        if(event.config==0){
            if(event.sw) {
                if (mGa == null) {
                    EventBus.getDefault().post(new SwitchEvent(1, false));
                    EventBus.getDefault().post(new MessageEvent("??????????????????????????????????????????????????????"));
                } else {
                    EventBus.getDefault().post(new MessageEvent("???????????????..."));
                    mBluetoothAdapter.startDiscovery();
                    btfind = 9;
                }
            }else{
                try {
                    btlink=false;
                    EventBus.getDefault().post(new ConnectEvent(10,""));
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }if(event.config==10){
            flgsave=event.sw;
//            Log.e("Save",String.valueOf(flgsave));
        }
    }
    @Subscribe
    public void onScaleEvent(ScaleEvent event) {
        if(event.config==0){
            EventBus.getDefault().post(new ScaleEvent(1,""));
            EventBus.getDefault().post(new ScaleEvent(2,""));
            try {
                String msg="{\"t\":\""+formatter.format(new Date(System.currentTimeMillis()))+"\",\"scale\":\"1\"}";
                EventBus.getDefault().post(new ConnectEvent(11,msg));
            }catch (Exception e){}
        }
    }
}