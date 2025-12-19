package ru.coursework.artschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.coursework.artschool.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByCodeIgnoreCase(String code);
}
