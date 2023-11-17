package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    private static final double LIMIT = 1000;
    private EditText brokerAddressEditText;
    private EditText brokerPortEditText;
    private EditText gasTopicEditText;
    private EditText thresholdEditText;
    private TextView gasValueTextView;

    private MqttClient mqttClient;
    private double thresholdValue = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        brokerAddressEditText = findViewById(R.id.brokerAddressEditText);
        brokerPortEditText = findViewById(R.id.brokerPortEditText);
        gasTopicEditText = findViewById(R.id.gasTopicEditText);
        thresholdEditText = findViewById(R.id.thresholdEditText);
        gasValueTextView = findViewById(R.id.gasValueTextView);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the threshold value
                String thresholdStr = thresholdEditText.getText().toString();
                if (!thresholdStr.isEmpty()) {
                    thresholdValue = Double.parseDouble(thresholdStr);
                }

                // Connect to MQTT broker
                startButtonClick(view);
            }
        });
    }


    public void startButtonClick(View view) {
        // Set the threshold value
        String thresholdStr = thresholdEditText.getText().toString();
        if (!thresholdStr.isEmpty()) {
            thresholdValue = Double.parseDouble(thresholdStr);

            // Check if the entered threshold exceeds a certain limit
            if (thresholdValue > LIMIT) {
                // Display an alert to the user
                showAlert("Warning", "The entered threshold exceeds the recommended limit.");

                // Optionally, you can choose to return from the method or take other actions
                return;
            }
        }

        // Connect to MQTT broker
        connectToMQTTBroker();

        // ... (Optionally, you can add additional logic here if needed)
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // You can add additional logic here if needed
                        showToast("Alert acknowledged");
                    }
                })
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void connectToMQTTBroker() {
        String brokerAddress = brokerAddressEditText.getText().toString();
        String brokerPort = brokerPortEditText.getText().toString();
        final String broker = "tcp://" + brokerAddress + ":" + brokerPort;
        final String clientId = "AndroidCl" + System.currentTimeMillis();

        try {
            mqttClient = new MqttClient(broker, clientId, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // Handle connection lost
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    // Handle incoming messages
                    if (topic.equals(gasTopicEditText.getText().toString())) {
                        String gasValueStr = new String(message.getPayload());
                        displayGasValue(gasValueStr);

                        // Check if the gas value exceeds the threshold
                        double gasValue = Double.parseDouble(gasValueStr);
                        if (gasValue > thresholdValue) {
                            handleGasExceedingThreshold(gasValue);
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used in this example
                }
            });

            mqttClient.subscribe(gasTopicEditText.getText().toString(), 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void displayGasValue(final String value) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                gasValueTextView.setText(value);
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGasExceedingThreshold(final double gasValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Display a notification
                if (gasValue > thresholdValue) {
                   showAlert("Gas Exceeding Threshold", "Gas value exceeds threshold: " + gasValue);
                    displayGasValue(String.valueOf(gasValue));
                   vibrateDevice();

                }
                else{
                    showGasExceedingThresholdNotification(gasValue);
                    vibrateDevice();
                }
            }
        });
    }

    private void vibrateDevice() {
        // Get the Vibrator service
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Check if the device supports vibration
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 500 milliseconds
            vibrator.vibrate(5000);
        }
    }
    private void showGasExceedingThresholdNotification(double gasValue) {
        // Create a notification channel if the device is running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "GasNotificationChannel";
            CharSequence channelName = "Gas Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "GasNotificationChannel")
                .setContentTitle("Gas Alert!")
                .setContentText("Gas value exceeds threshold: " + gasValue)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }







}
