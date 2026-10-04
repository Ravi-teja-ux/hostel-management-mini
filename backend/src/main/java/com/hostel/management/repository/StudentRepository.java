package com.hostel.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hostel.management.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
}
