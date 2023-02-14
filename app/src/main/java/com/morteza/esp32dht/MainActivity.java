package com.morteza.esp32dht;

import static android.os.SystemClock.sleep;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.esp32dht.Tools.ToastMaker;
import com.example.esp32dht.Tools.service.MqttAndroidClient;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MainActivity extends AppCompatActivity {
    private final String tempTopic = "esp32_1/balcony/temperature", humTopic = "esp32_1/balcony/humidity";
    private MqttAndroidClient mqttAndroidClient;
    private boolean isOnPause = false;
    private boolean isErrorShown = false;
    private boolean isTopicsSubscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window w = getWindow();
        w.setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w.setDecorFitsSystemWindows(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void mqttStuff() {

        TextView tvTemperatureValue = findViewById(R.id.tvTemperatureValue);
        TextView tvHumidityValue = findViewById(R.id.tvHumidityValue);
        final boolean[] enteredToFailLoop = {false};
        final boolean[] enteredThisLoop = {false};
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName("morteza");
        mqttConnectOptions.setPassword("passwd".toCharArray());
        mqttConnectOptions.setAutomaticReconnect(false);
        String clientID = "ESP32_1_Android";
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), getString(R.string.rootURL), clientID, MqttAndroidClient.Ack.AUTO_ACK);
        new Thread(() -> {
            mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    if (!isOnPause && !enteredThisLoop[0]) {
                        enteredThisLoop[0] = true;
                        sleep(500);
                        new Thread(() -> {
                            if (!isOnPause) {
                                if (!isErrorShown){
                                    isErrorShown = true;
                                    showError("Connection Lost!");
                                }
                                mqttAndroidClient.close();
                                mqttStuff();
                            }
                        }).start();
                    }
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    runOnUiThread(() -> {
                        if (topic.trim().equals(tempTopic))
                            tvTemperatureValue.setText(new String(message.getPayload()) + getString(R.string.degree));
                        else if (topic.trim().equals(humTopic)) tvHumidityValue.setText(new String(message.getPayload()) + "%");
                    });
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        mqttAndroidClient.subscribe(tempTopic, 0);
                    } catch (MqttException ignored) {}
                    try {
                        mqttAndroidClient.subscribe(humTopic, 0);
                    } catch (MqttException ignored) {}
                    isTopicsSubscribed = true;
                    isErrorShown = false;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if (!enteredToFailLoop[0]){
                        enteredToFailLoop[0] = true;
                        sleep(500);
                        new Thread(() -> {
                            if (!isOnPause) {
                                if (!isErrorShown){
                                    isErrorShown = true;
                                    showError("Connection Failure!");
//                                        Log.e("MQTTERROR", exception.getMessage());
                                }
                                mqttAndroidClient.close();
                                mqttStuff();
                            }
                        }).start();
                    }
                }
            });
        }).start();
    }

    private void showError(String error) {
        runOnUiThread(() -> new ToastMaker(getApplicationContext()).msg(error));
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mqttStuff();
        }catch (Exception ignored){}
        isOnPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnPause = true;
        if (isTopicsSubscribed){
            mqttAndroidClient.unsubscribe(tempTopic);
            mqttAndroidClient.unsubscribe(humTopic);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTopicsSubscribed){
            mqttAndroidClient.unsubscribe(tempTopic);
            mqttAndroidClient.unsubscribe(humTopic);
            //                mqttAndroidClient.close();
        }
    }
}