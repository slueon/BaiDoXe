package com.baidoxe.parking_iot.repository;

import com.baidoxe.parking_iot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Thằng JpaRepository nó đã bao thầu hết mấy lệnh cơ bản (Lưu, Xóa, Tìm tất cả...) rồi, nhàn tênh!
    // Viết thêm hàm này để sau này làm chức năng Đăng nhập
    User findByUsername(String username);
}