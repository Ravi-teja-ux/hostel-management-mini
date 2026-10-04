package com.hostel.management.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hostel.management.dto.LeaveRequestCreateRequest;
import com.hostel.management.dto.LeaveRequestResponse;
import com.hostel.management.dto.LoginRequest;
import com.hostel.management.dto.LoginResponse;
import com.hostel.management.dto.FeeResponse;
import com.hostel.management.entity.Fee;
import com.hostel.management.entity.LeaveRequest;
import com.hostel.management.entity.Student;
import com.hostel.management.repository.FeeRepository;
import com.hostel.management.repository.LeaveRequestRepository;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.StudentRepository;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final FeeRepository feeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, RoomRepository roomRepository, FeeRepository feeRepository,
            LeaveRequestRepository leaveRequestRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.feeRepository = feeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Student student = findStudent(request.studentId());
        if (!passwordEncoder.matches(request.password(), student.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid student ID or password");
        }
        return new LoginResponse("Login successful", "STUDENT", student.getStudentId(), student.getName());
    }

    public Map<String, Object> getStudentDetails(String studentId) {
        Student student = findStudent(studentId);
        return Map.of(
                "id", student.getId(),
                "studentId", student.getStudentId(),
                "name", student.getName(),
                "course", student.getCourse() == null ? "" : student.getCourse(),
                "year", student.getYear() == null ? 0 : student.getYear(),
                "roomNumber", student.getRoomNumber() == null ? "" : student.getRoomNumber(),
                "phone", student.getPhone() == null ? "" : student.getPhone(),
                "status", student.getStatus());
    }

    public Map<String, Object> getRoomDetails(String studentId) {
        Student student = findStudent(studentId);
        if (student.getRoomNumber() == null || student.getRoomNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No room is assigned to this student");
        }
        var room = roomRepository.findFirstByRoomNumberOrderByIdAsc(student.getRoomNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room details were not found"));
        return Map.of(
                "studentId", student.getStudentId(),
                "roomNumber", room.getRoomNumber(),
                "block", room.getBlock(),
                "roomType", room.getRoomType(),
                "capacity", room.getCapacity(),
                "occupiedBeds", room.getOccupiedBeds(),
                "availableBeds", room.getAvailableBeds(),
                "status", room.getStatus());
    }

    public FeeResponse getFee(String studentId) {
        Fee fee = feeRepository.findByStudentStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee record was not found"));
        return toFeeResponse(fee);
    }

    @Transactional
    public LeaveRequestResponse createLeaveRequest(LeaveRequestCreateRequest request) {
        if (!request.returnDate().isAfter(request.leaveDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return date must be after leave date");
        }
        if (request.leaveDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave date cannot be in the past");
        }
        Student student = findStudent(request.studentId());
        LeaveRequest saved = leaveRequestRepository.save(new LeaveRequest(
                student,
                request.leaveDate(),
                request.returnDate(),
                request.reason().trim()));
        return toLeaveRequestResponse(saved);
    }

    private Student findStudent(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student was not found"));
    }

    private FeeResponse toFeeResponse(Fee fee) {
        return new FeeResponse(
                fee.getId(),
                fee.getStudent().getStudentId(),
                fee.getStudent().getName(),
                fee.getTotalFee(),
                fee.getPaidAmount(),
                fee.getPendingAmount(),
                fee.getPaymentStatus());
    }

    private LeaveRequestResponse toLeaveRequestResponse(LeaveRequest request) {
        return new LeaveRequestResponse(
                request.getId(),
                request.getStudent().getStudentId(),
                request.getStudent().getName(),
                request.getLeaveDate(),
                request.getReturnDate(),
                request.getReason(),
                request.getStatus());
    }
}
