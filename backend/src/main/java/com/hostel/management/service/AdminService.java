package com.hostel.management.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.hostel.management.dto.AdminDashboardResponse;
import com.hostel.management.dto.AdminLoginRequest;
import com.hostel.management.dto.FeeResponse;
import com.hostel.management.dto.FeeUpdateRequest;
import com.hostel.management.dto.LeaveRequestResponse;
import com.hostel.management.dto.LoginResponse;
import com.hostel.management.dto.RoomRequest;
import com.hostel.management.dto.StudentRequest;
import com.hostel.management.entity.Admin;
import com.hostel.management.entity.Fee;
import com.hostel.management.entity.LeaveRequest;
import com.hostel.management.entity.Room;
import com.hostel.management.entity.Student;
import com.hostel.management.repository.AdminRepository;
import com.hostel.management.repository.FeeRepository;
import com.hostel.management.repository.LeaveRequestRepository;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.security.JwtService;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final FeeRepository feeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminService(AdminRepository adminRepository, StudentRepository studentRepository,
            RoomRepository roomRepository, FeeRepository feeRepository,
            LeaveRequestRepository leaveRequestRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.feeRepository = feeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByAdminId(request.adminId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin ID or password"));
        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin ID or password");
        }
        return new LoginResponse(
                "Login successful",
                "ADMIN",
                admin.getAdminId(),
                admin.getName(),
                jwtService.createToken(admin.getAdminId(), "ADMIN"),
                jwtService.getExpirationSeconds());
    }

    public AdminDashboardResponse getDashboard() {
        List<Room> rooms = roomRepository.findAll();
        return new AdminDashboardResponse(
                studentRepository.count(),
                rooms.size(),
                rooms.stream().filter(room -> room.getOccupiedBeds() > 0).count(),
                rooms.stream().filter(room -> room.getAvailableBeds() > 0).count(),
                leaveRequestRepository.countByStatusIgnoreCase("Pending"));
    }

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    @Transactional
    public Student createStudent(StudentRequest request) {
        if (studentRepository.existsByStudentId(request.studentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student ID already exists");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required for a new student");
        }
        Student student = new Student(
                request.studentId().trim(),
                passwordEncoder.encode(request.password()),
                request.name().trim(),
                request.course(),
                request.year(),
                request.roomNumber(),
                request.phone(),
                request.status().trim());
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long id, StudentRequest request) {
        Student student = findStudent(id);
        if (!student.getStudentId().equals(request.studentId())
                && studentRepository.existsByStudentId(request.studentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student ID already exists");
        }
        student.setStudentId(request.studentId().trim());
        student.setName(request.name().trim());
        student.setCourse(request.course());
        student.setYear(request.year());
        student.setRoomNumber(request.roomNumber());
        student.setPhone(request.phone());
        student.setStatus(request.status().trim());
        if (request.password() != null && !request.password().isBlank()) {
            student.setPassword(passwordEncoder.encode(request.password()));
        }
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = findStudent(id);
        feeRepository.deleteByStudent_Id(id);
        leaveRequestRepository.deleteByStudent_Id(id);
        studentRepository.delete(student);
    }

    public List<Room> getRooms() {
        return roomRepository.findAll();
    }

    @Transactional
    public Room createRoom(RoomRequest request) {
        validateRoom(request);
        if (roomRepository.existsByRoomNumberAndBlock(request.roomNumber(), request.block())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room already exists in this block");
        }
        return roomRepository.save(new Room(
                request.roomNumber().trim(),
                request.block().trim(),
                request.roomType().trim(),
                request.capacity(),
                request.occupiedBeds()));
    }

    @Transactional
    public Room updateRoom(Long id, RoomRequest request) {
        validateRoom(request);
        Room room = findRoom(id);
        boolean duplicate = roomRepository.findByRoomNumberAndBlock(request.roomNumber(), request.block())
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room already exists in this block");
        }
        room.setRoomNumber(request.roomNumber().trim());
        room.setBlock(request.block().trim());
        room.setRoomType(request.roomType().trim());
        room.setCapacity(request.capacity());
        room.setOccupiedBeds(request.occupiedBeds());
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.delete(findRoom(id));
    }

    public List<FeeResponse> getFees() {
        return feeRepository.findAllByOrderByStudentStudentIdAsc().stream()
                .map(this::toFeeResponse)
                .toList();
    }

    @Transactional
    public FeeResponse updateFee(Long id, FeeUpdateRequest request) {
        if (request.paidAmount().compareTo(request.totalFee()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paid amount cannot exceed total fee");
        }
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fee record was not found"));
        fee.setTotalFee(request.totalFee());
        fee.setPaidAmount(request.paidAmount());
        return toFeeResponse(feeRepository.save(fee));
    }

    public List<LeaveRequestResponse> getLeaveRequests() {
        return leaveRequestRepository.findAllByOrderByLeaveDateDesc().stream()
                .map(this::toLeaveRequestResponse)
                .toList();
    }

    @Transactional
    public LeaveRequestResponse updateLeaveRequestStatus(Long id, String status) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request was not found"));
        request.setStatus(status);
        return toLeaveRequestResponse(leaveRequestRepository.save(request));
    }

    private void validateRoom(RoomRequest request) {
        if (request.occupiedBeds() > request.capacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Occupied beds cannot exceed room capacity");
        }
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student was not found"));
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room was not found"));
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
