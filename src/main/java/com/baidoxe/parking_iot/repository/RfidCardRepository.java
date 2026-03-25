package com.baidoxe.parking_iot.repository;

import com.baidoxe.parking_iot.entity.RfidCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RfidCardRepository extends JpaRepository<RfidCard, String> {
}