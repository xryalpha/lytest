package roy.studio.myapplication;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class USBConnectThread implements Runnable {
    //USB连接线程
    private UsbManager manager;
    private boolean activeConnect;
    private UsbDevice mUsbDevice; // 找到的USB设备
    private UsbDeviceConnection mDeviceConnection;
    private UsbEndpoint epOut,epIn,epCmd; //CDC通讯端点
    private String ply="";
    private final int BUFFER_SIZE = 1024;
    public USBConnectThread(UsbManager manager, boolean connect) {
        this.manager = manager;
        this.activeConnect = connect;
    }
    @Override
    public void run() {
        try {
            //如果是自动连接 则调用连接方法
            int qq=9;
            ply="没有设备";
            if (activeConnect) {
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                ArrayList<String> USBDeviceList = new ArrayList<String>(); // 存放USB设备的数量
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    USBDeviceList.add(String.valueOf(device.getVendorId()));
                    USBDeviceList.add(String.valueOf(device.getProductId()));
                    // 在这里添加处理设备的代码
                    Log.e("USB",device.getVendorId()+":"+device.getProductId());
                    if (device.getVendorId() == 1155 && device.getProductId() == 22336 ) {
                        mUsbDevice = device;
//                        UsbDeviceConnection connection = null ;
                        if (manager.hasPermission(mUsbDevice)) {
                            qq=8;
                            // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                            mDeviceConnection = manager.openDevice(mUsbDevice);
                            if (mDeviceConnection == null ) {
                                Log.e("USB","null");
                                return ;
                            }
                            if (mDeviceConnection.claimInterface(mUsbDevice.getInterface(0), true )) {
                                qq=0;
//                                mDeviceConnection = connection;
                                epCmd=mUsbDevice.getInterface(0).getEndpoint(0);
                                epIn=mUsbDevice.getInterface(1).getEndpoint(1);
                                epOut=mUsbDevice.getInterface(1).getEndpoint(0);
                                ply="USB设备已连接\n"+epCmd+"\n"+epIn+"\n"+epOut;
                            } else {
                                mDeviceConnection.close();
                            }
                        } else {
                            Log.e("USB","pp");
                            ply="没有USB权限";
                        }
                    }
                }
            }
            EventBus.getDefault().register(this); //注册事件总线（接收用）
            EventBus.getDefault().post(new ConnectEvent(20,ply)); //发送事件
//            setChanged();
//            notifyObservers(ply);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes=0;
            while (qq==0) {
                //读取数据
                bytes = mDeviceConnection.bulkTransfer(epIn, buffer, buffer.length, 3000 );
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);
                    EventBus.getDefault().post(new ConnectEvent(21,new String(data))); //发送事件
//                    String re=new String(data);
//                    setChanged();
//                    notifyObservers(bytes +":"+re);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ConnectEvent(23,"USB异常中断")); //发送事件
            EventBus.getDefault().unregister(this);
//            setChanged();
//            notifyObservers("异常中断");
        }
    }
//    //发送数据
//    public void sendMsg(byte[] bytesend) {
//        if (mDeviceConnection != null) {
//            try {
//                //发送数据
//                mDeviceConnection.bulkTransfer(epOut, bytesend, bytesend.length, 3000 );
//            } catch (Exception e) { }
//        }
//    }
    @Subscribe  //事件回调函数
    public void onConnectEvent(ConnectEvent event) {
        switch (event.state){
//            case 20:
//                try {
//                    close=true;
//                    mDeviceConnection.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
            case 11:
                if (mDeviceConnection != null) {
                    try {
                        //发送数据
                        mDeviceConnection.bulkTransfer(epOut, event.msg.getBytes(), event.msg.length(), 3000 );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
