package com.hostel.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hostel.management.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumberAndBlock(String roomNumber, String block);
    Optional<Room> findFirstByRoomNumberOrderByIdAsc(String roomNumber);
    boolean existsByRoomNumberAndBlock(String roomNumber, String block);
}
