package com.swaped.security;

import com.swaped.entity.Student;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails adapter wrapping a SwapEd Student entity.
 */
public class CustomUserDetails implements UserDetails {

    private final Long studentId;
    private final String fullName;
    private final String email;
    private final String collegeId;
    private final String department;
    private final String course;
    private final String passwordHash;
    private final boolean emailVerified;

    public CustomUserDetails(Student student) {
        this.studentId = student.getStudentId();
        this.fullName = student.getFullName();
        this.email = student.getEmail();
        this.collegeId = student.getCollegeId();
        this.department = student.getDepartment();
        this.course = student.getCourse();
        this.passwordHash = student.getPasswordHash();
        this.emailVerified = student.isEmailVerified();
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getCollegeId() {
        return collegeId;
    }

    public String getDepartment() {
        return department;
    }

    public String getCourse() {
        return course;
    }

    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "ST";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}
