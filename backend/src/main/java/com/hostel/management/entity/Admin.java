package com.hostel.management.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false, unique = true, length = 30)
    private String adminId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    protected Admin() {
    }

    public Admin(String adminId, String password, String name) {
        this.adminId = adminId;
        this.password = password;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
