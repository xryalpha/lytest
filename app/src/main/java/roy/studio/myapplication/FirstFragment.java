package roy.studio.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.zxing.integration.android.IntentIntegrator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FirstFragment extends Fragment {
    private TextView tv1;
    private Switch sw1,sw2;
    boolean swset;
    String tv1s="";
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        tv1=view.findViewById(R.id.textView);
        sw1=view.findViewById(R.id.switch1);
        sw2=view.findViewById(R.id.switch2);
        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//                onScanQrcode(view);
            }
        });
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ScaleEvent(0,""));
            }
        });
        sw1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SwitchEvent(0,sw1.isChecked()));
            }
        });
        sw2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new SwitchEvent(10,sw2.isChecked()));
            }
        });
    }
    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        tv1s=event.message;
        tv1.post(new Runnable() {
            @Override
            public void run() {
                tv1.setText(tv1s);
            }
        });
    }
    @Subscribe
    public void onSwitchEvent(SwitchEvent event) {
        if (event.config == 1){
            swset=event.sw;
            sw1.post(new Runnable() {
                @Override
                public void run() {
                    sw1.setChecked(swset);
                }
            });
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}