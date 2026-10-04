package com.hostel.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.management.dto.AdminDashboardResponse;
import com.hostel.management.dto.AdminLoginRequest;
import com.hostel.management.dto.FeeResponse;
import com.hostel.management.dto.FeeUpdateRequest;
import com.hostel.management.dto.LeaveRequestResponse;
import com.hostel.management.dto.LoginResponse;
import com.hostel.management.dto.RoomRequest;
import com.hostel.management.dto.StudentRequest;
import com.hostel.management.entity.Room;
import com.hostel.management.entity.Student;
import com.hostel.management.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getStudents() {
        return ResponseEntity.ok(adminService.getStudents());
    }

    @PostMapping("/students")
    public ResponseEntity<Student> createStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createStudent(request));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(adminService.updateStudent(id, request));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getRooms() {
        return ResponseEntity.ok(adminService.getRooms());
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createRoom(request));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(adminService.updateRoom(id, request));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        adminService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fees")
    public ResponseEntity<List<FeeResponse>> getFees() {
        return ResponseEntity.ok(adminService.getFees());
    }

    @PutMapping("/fees/{id}")
    public ResponseEntity<FeeResponse> updateFee(@PathVariable Long id, @Valid @RequestBody FeeUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateFee(id, request));
    }

    @GetMapping("/leave-requests")
    public ResponseEntity<List<LeaveRequestResponse>> getLeaveRequests() {
        return ResponseEntity.ok(adminService.getLeaveRequests());
    }

    @PutMapping("/leave-requests/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approveLeaveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.updateLeaveRequestStatus(id, "Approved"));
    }

    @PutMapping("/leave-requests/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> rejectLeaveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.updateLeaveRequestStatus(id, "Rejected"));
    }
}
