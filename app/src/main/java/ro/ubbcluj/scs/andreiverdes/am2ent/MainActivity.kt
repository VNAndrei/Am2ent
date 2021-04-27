package ro.ubbcluj.scs.andreiverdes.am2ent

import android.os.Bundle
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import java.nio.charset.StandardCharsets
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private  lateinit var client:Mqtt3AsyncClient;
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         client = MqttClient.builder()
            .useMqttVersion3()
            .identifier(UUID.randomUUID().toString())
            .serverHost("264c16b5dc5b4b4b902292e3d2751752.s1.eu.hivemq.cloud")
            .serverPort(8883)
            .sslWithDefaultConfig()
            .buildAsync()


        client.connectWith()
            .simpleAuth()
            .username("verdes.andrei.iot")
            .password("Andrei123".toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { connAck: Mqtt3ConnAck?, throwable: Throwable? ->
                if (throwable != null) {
                    // handle failure
                } else {
                    client.subscribeWith()
                        .topicFilter("am2ent/ambientValues")
                        .callback {publish-> messageArrived(publish) }
                        .send()
                }
            }
        setupActivity()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun messageArrived(publish :Mqtt3Publish) {

        val payload = publish.payload.get()

        val stringCommand: String = StandardCharsets.UTF_8.decode(payload).toString()
        var details = JSONObject(stringCommand)
        runOnUiThread {
            humidityTxt.text = details.getString("air_humidity");
            moistureTxt.text = details.getString("soil_humidity");
            temperatureTxt.text = details.getString("temperature");
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupActivity() {
       setMoistureBtn.setOnClickListener {
           println(moistureTxt.text)
           client.publishWith()
               .topic("am2ent/humidity")
               .payload(moistureValue.text.toString().toByteArray())
               .send()
           }

        startFanBtn.setOnClickListener {
            client.publishWith()
                .topic("am2ent/ventilation")
                .payload(fanTimer.text.toString().toByteArray())
                .send()
        }
    }
}