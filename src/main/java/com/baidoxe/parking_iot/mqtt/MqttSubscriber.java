package com.baidoxe.parking_iot.mqtt;

import com.baidoxe.parking_iot.entity.ParkingSpot;
import com.baidoxe.parking_iot.repository.ParkingSpotRepository;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqttSubscriber implements MqttCallback {

    // Đây là Broker test miễn phí. Sau này làm đồ án ông có thể cài Mosquitto broker trên máy chạy local.
    private static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();
    
    // Đăng ký nghe kênh (topic) này. Cảm biến phải gửi tin nhắn vào kênh này.
    private static final String TOPIC_FILTER = "baidoxe/sensor/#";

    private MqttClient client;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository; // Gọi thợ xây ra để chuẩn bị update DB

    // Hàm này sẽ tự động chạy ngay khi Spring Boot khởi động xong
    @PostConstruct
    public void startListening() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.setCallback(this); // Khai báo class này sẽ là người xử lý tin nhắn
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            
            client.connect(options);
            client.subscribe(TOPIC_FILTER);
            
            System.out.println("Da ket noi MQTT Broker thanh cong! Dang doi topic: " + TOPIC_FILTER);
        } catch (MqttException e) {
            System.err.println("Loi! Khong ket noi đuoc MQTT: " + e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Mat ket noi MQTT! Ly do: " + cause.getMessage());
        // TODO: Đoạn này ông có thể code thêm hàm tự động reconnect nếu muốn xịn
    }

    // Hàm này cực quan trọng: Mỗi khi có cảm biến nào đẩy dữ liệu lên, hàm này sẽ tự động nhảy vào chạy!
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        System.out.println("Co bien! Topic: " + topic + " | Du lieu cam bien: " + payload);

        // Quy ước gửi: topic = baidoxe/sensor/S1 (S1 là ID của cảm biến gắn ở ô đỗ đó)
        // payload = "1" (có xe), "0" (trống)
        String[] parts = topic.split("/");
        if (parts.length == 3) {
            String sensorId = parts[2]; // Lấy chữ S1 ra
            
            // Tìm ô đỗ xe trong Database xem ô nào đang gắn con cảm biến S1 này
            ParkingSpot spot = parkingSpotRepository.findBySensorId(sensorId);
            
            if (spot != null) {
                // Có xe = 1, Không xe = 0
                boolean isOccupied = payload.equals("1"); 
                spot.setIsOccupied(isOccupied);
                
                // Ra lệnh cho thợ xây cập nhật trạng thái vào MySQL
                parkingSpotRepository.save(spot);
                System.out.println("   -> Da update o do " + spot.getSpotName() + " thanh: " + (isOccupied ? "DAY" : "TRONG"));
            } else {
                System.out.println("   -> Khong tim thay o đo nao xai cam bien " + sensorId + " trong database!");
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Chỉ dùng khi gửi tin nhắn, ở đây mình chỉ nghe nên kệ nó
    }
}