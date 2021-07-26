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
    private UsbDevice device; // 找到的USB设备
    private UsbDeviceConnection mDeviceConnection;
    private UsbEndpoint epOut,epIn,epCmd; //CDC通讯端点
    private String ply="";
    private final int BUFFER_SIZE = 1024;
    public USBConnectThread(UsbManager manager, UsbDevice device) {
        this.manager = manager;
        this.device = device;
    }
    @Override
    public void run() {
        try {
            //如果是自动连接 则调用连接方法
            int qq=9;
//            ply="没有设备";
            if (manager.hasPermission(device)) {
                qq=8;
                // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
                mDeviceConnection = manager.openDevice(device);
                if (mDeviceConnection == null ) {
                    return ;
                }
                if (mDeviceConnection.claimInterface(device.getInterface(0), true )) {
                    qq=0;
                    epCmd=device.getInterface(0).getEndpoint(0);
                    epIn=device.getInterface(1).getEndpoint(1);
                    epOut=device.getInterface(1).getEndpoint(0);
                    ply="USB设备已连接";
                } else {
                    mDeviceConnection.close();
                }
            } else {
                ply="没有USB权限";
            }
            EventBus.getDefault().register(this); //注册事件总线（接收用）
            EventBus.getDefault().post(new ConnectEvent(20,ply)); //发送事件
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes=0;
            while (qq==0) {
                //读取数据
                bytes = mDeviceConnection.bulkTransfer(epIn, buffer, buffer.length, 3000 );
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);
                    EventBus.getDefault().post(new ConnectEvent(21,new String(data))); //发送事件
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ConnectEvent(23,"USB异常中断")); //发送事件
            EventBus.getDefault().unregister(this);
        }
    }
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
