package com.swaped.service;

import com.swaped.dto.ProfileUpdateDto;
import com.swaped.entity.Student;
import com.swaped.exception.ResourceNotFoundException;
import com.swaped.exception.SwapEdException;
import com.swaped.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages student profile queries and permitted account updates.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student account not found."));
    }

    @Transactional(readOnly = true)
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student account not found for email: " + email));
    }

    @Transactional
    public Student updateProfile(Long studentId, ProfileUpdateDto dto) {
        Student student = getStudentById(studentId);
        student.setFullName(dto.getFullName().trim());
        student.setDepartment(dto.getDepartment().trim());
        student.setCourse(dto.getCourse().trim());
        return studentRepository.save(student);
    }

    @Transactional
    public void changePassword(Long studentId, String currentPassword, String newPassword, String confirmPassword) {
        Student student = getStudentById(studentId);
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, student.getPasswordHash())) {
            throw new SwapEdException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new SwapEdException("New password must be at least 8 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new SwapEdException("New password and confirmation do not match.");
        }
        student.setPasswordHash(passwordEncoder.encode(newPassword));
        studentRepository.save(student);
    }
}
