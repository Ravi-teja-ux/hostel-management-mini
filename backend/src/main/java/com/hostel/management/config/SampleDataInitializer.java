package com.hostel.management.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

@Component
@Profile("!prod")
public class SampleDataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final FeeRepository feeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public SampleDataInitializer(AdminRepository adminRepository, StudentRepository studentRepository,
            RoomRepository roomRepository, FeeRepository feeRepository,
            LeaveRequestRepository leaveRequestRepository, PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.feeRepository = feeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!adminRepository.existsByAdminId("admin")) {
            adminRepository.save(new Admin("admin", passwordEncoder.encode("admin123"), "Hostel Admin"));
        }

        roomRepository.findByRoomNumberAndBlock("204", "Block B")
                .orElseGet(() -> roomRepository.save(new Room("204", "Block B", "Shared", 2, 2)));
        roomRepository.findByRoomNumberAndBlock("205", "Block B")
                .orElseGet(() -> roomRepository.save(new Room("205", "Block B", "Shared", 2, 1)));

        Student ravi = getOrCreateRenamedStudent(
                "STU2026", "26215A0535", "Ravi Teja", "Computer Science", 2, "204", "98765 43210");
        Student rohith = getOrCreateRenamedStudent(
                "STU2027", "26215A0536", "Rohith", "Information Technology", 2, "205", "98765 12340");

        if (feeRepository.findByStudentStudentId(ravi.getStudentId()).isEmpty()) {
            feeRepository.save(new Fee(ravi, new BigDecimal("64000.00"), new BigDecimal("32000.00")));
        }
        if (feeRepository.findByStudentStudentId(rohith.getStudentId()).isEmpty()) {
            feeRepository.save(new Fee(rohith, new BigDecimal("64000.00"), new BigDecimal("64000.00")));
        }

        if (leaveRequestRepository.count() == 0) {
            leaveRequestRepository.save(new LeaveRequest(
                    ravi, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5), "Visiting family"));
        }
    }

    private Student getOrCreateRenamedStudent(String previousStudentId, String studentId, String name, String course,
            int year, String roomNumber, String phone) {
        Optional<Student> renamed = studentRepository.findByStudentId(studentId);
        Student student;
        if (renamed.isPresent()) {
            student = renamed.get();
        } else {
            Optional<Student> previous = studentRepository.findByStudentId(previousStudentId);
            if (previous.isPresent()) {
                student = previous.get();
                updateStudentIdPreservingReferences(student, previousStudentId, studentId);
            } else {
                student = new Student(
                        studentId,
                        passwordEncoder.encode("student123"),
                        name,
                        course,
                        year,
                        roomNumber,
                        phone,
                        "Active");
            }
        }
        student.setName(name);
        return studentRepository.save(student);
    }

    private void updateStudentIdPreservingReferences(Student student, String previousStudentId, String studentId) {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            jdbcTemplate.update("UPDATE fees SET student_id = ? WHERE student_id = ?", studentId, previousStudentId);
            jdbcTemplate.update(
                    "UPDATE leave_requests SET student_id = ? WHERE student_id = ?", studentId, previousStudentId);
            student.setStudentId(studentId);
            studentRepository.saveAndFlush(student);
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

}
