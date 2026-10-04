package com.hostel.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hostel.management.entity.Fee;

public interface FeeRepository extends JpaRepository<Fee, Long> {
    Optional<Fee> findByStudentStudentId(String studentId);
    List<Fee> findAllByOrderByStudentStudentIdAsc();
    void deleteByStudent_Id(Long studentId);
}
