package com.baidoxe.parking_iot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Parking_Spots")
@Data
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spot_id")
    private Integer spotId;

    @Column(name = "spot_name", nullable = false, length = 20)
    private String spotName;

    @Column(name = "sensor_id", unique = true, length = 50)
    private String sensorId;

    @Column(name = "is_occupied")
    private Boolean isOccupied = false;

    @Column(name = "last_updated", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;
}