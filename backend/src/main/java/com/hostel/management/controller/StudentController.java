package com.hostel.management.controller;

import java.util.Map;
import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hostel.management.dto.LeaveRequestCreateRequest;
import com.hostel.management.dto.LoginRequest;
import com.hostel.management.dto.LoginResponse;
import com.hostel.management.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/student/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(studentService.login(request));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<?> getStudent(@PathVariable String studentId, Principal principal) {
        requireOwnRecord(studentId, principal);
        return ResponseEntity.ok(studentService.getStudentDetails(studentId));
    }

    @GetMapping("/students/{studentId}/room")
    public ResponseEntity<?> getRoom(@PathVariable String studentId, Principal principal) {
        requireOwnRecord(studentId, principal);
        return ResponseEntity.ok(studentService.getRoomDetails(studentId));
    }

    @GetMapping("/students/{studentId}/fees")
    public ResponseEntity<?> getFees(@PathVariable String studentId, Principal principal) {
        requireOwnRecord(studentId, principal);
        return ResponseEntity.ok(studentService.getFee(studentId));
    }

    @PostMapping("/leave-requests")
    public ResponseEntity<?> createLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateRequest request, Principal principal) {
        requireOwnRecord(request.studentId(), principal);
        return ResponseEntity.status(201).body(studentService.createLeaveRequest(request));
    }

    private void requireOwnRecord(String studentId, Principal principal) {
        if (principal == null || !principal.getName().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own student records");
        }
    }
}
