package com.hostel.management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hostel.management.entity.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findAllByOrderByLeaveDateDesc();
    long countByStatusIgnoreCase(String status);
    void deleteByStudent_Id(Long studentId);
}
