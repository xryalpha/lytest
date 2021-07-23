package roy.studio.myapplication;

import android.bluetooth.BluetoothSocket;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Observable;

public class ConnectThread implements Runnable {
    //蓝牙连接线程
    private BluetoothSocket socket;
    private boolean activeConnect,close=false;
    InputStream inputStream;
    OutputStream outputStream;
    private final int BUFFER_SIZE = 1024;

    public ConnectThread(BluetoothSocket socket, boolean connect) {
        this.socket = socket;
        this.activeConnect = connect;
    }

    @Override
    public void run() {
        try {
            //如果是自动连接 则调用连接方法
            if (activeConnect) {
                socket.connect();
            }
            EventBus.getDefault().register(this); //注册事件总线（接收用）
            EventBus.getDefault().post(new ConnectEvent(0,"")); //发送事件
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;
            while (true) {
                //读取数据
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);
                    EventBus.getDefault().post(new ConnectEvent(1,new String(data)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(close) EventBus.getDefault().post(new ConnectEvent(2,""));
            else EventBus.getDefault().post(new ConnectEvent(3,""));
            EventBus.getDefault().unregister(this);
        }
    }
    @Subscribe  //事件回调函数
    public void onConnectEvent(ConnectEvent event) {
        switch (event.state){
            case 10:
                try {
                    close=true;
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 11:
                if (outputStream != null) {
                    try {
                        //发送数据
                        outputStream.write(event.msg.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
