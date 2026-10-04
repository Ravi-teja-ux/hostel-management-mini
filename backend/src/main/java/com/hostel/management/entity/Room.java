package com.hostel.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(nullable = false, length = 50)
    private String block;

    @Column(name = "room_type", nullable = false, length = 30)
    private String roomType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "occupied_beds", nullable = false)
    private Integer occupiedBeds;

    @Column(name = "available_beds", nullable = false)
    private Integer availableBeds;

    @Column(nullable = false, length = 30)
    private String status;

    protected Room() {
    }

    public Room(String roomNumber, String block, String roomType, Integer capacity, Integer occupiedBeds) {
        this.roomNumber = roomNumber;
        this.block = block;
        this.roomType = roomType;
        this.capacity = capacity;
        this.occupiedBeds = occupiedBeds;
        updateAvailability();
    }

    @PrePersist
    @PreUpdate
    public void updateAvailability() {
        if (capacity != null && occupiedBeds != null) {
            availableBeds = Math.max(0, capacity - occupiedBeds);
            status = availableBeds == 0 ? "Full" : occupiedBeds == 0 ? "Available" : "Partially Occupied";
        }
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
        updateAvailability();
    }

    public Integer getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
        updateAvailability();
    }

    public Integer getAvailableBeds() {
        return availableBeds;
    }

    public String getStatus() {
        return status;
    }
}
