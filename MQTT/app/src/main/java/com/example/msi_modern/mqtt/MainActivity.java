package com.example.msi_modern.mqtt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    MQTTService mqttService;

    TextView textView;
    Button button;
    EditText textBox;
    GraphView graphTemperature;
    DataPoint[] tempList = new DataPoint[1];
    int time = 0;
    LineGraphSeries<DataPoint> seriesTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        textBox = findViewById(R.id.textBox);
        graphTemperature = findViewById(R.id.graphTemperature);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataMQTT(textBox.getText().toString());
            }
        });


        mqttService = new MQTTService(this);
        mqttService.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(topic, message.toString());
                if (time == 0) {
                    tempList[0] = new DataPoint(time, Integer.valueOf(message.toString()));
                    seriesTemp = new LineGraphSeries<>(tempList);
                    showDataOnGraph(seriesTemp, graphTemperature);
                } else {
                    DataPoint newPoint = new DataPoint(time, Integer.valueOf(message.toString()));
                    seriesTemp.appendData(newPoint, false, 1000, true);
                    showDataOnGraph(seriesTemp, graphTemperature);
                }
                time += 1;
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private void sendDataMQTT(String data){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);

        byte[] b = data.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        Log.d("MQTT","Publish :" + msg);
        try {
            mqttService.mqttAndroidClient.publish("[feed]", msg);
        }catch (MqttException e) {
            Log.d("MQTT", "sendDataMQTT: cannot send message");
        }
    }

    private void showDataOnGraph(LineGraphSeries<DataPoint> series, GraphView graph){
        if(graph.getSeries().size() > 0){
            graph.getSeries().remove(0);
        }
        graph.addSeries(series);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
    }
}
