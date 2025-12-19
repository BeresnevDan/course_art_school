package ru.coursework.artschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.coursework.artschool.model.Room;
import ru.coursework.artschool.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public Room create(Room room) {
        if (roomRepository.existsByCodeIgnoreCase(room.getCode())) {
            throw new IllegalArgumentException("Аудитория с таким кодом уже существует");
        }
        return roomRepository.save(room);
    }

    @Transactional
    public Room update(Long id, Room form) {
        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аудитория не найдена"));

        if (!existing.getCode().equalsIgnoreCase(form.getCode())
                && roomRepository.existsByCodeIgnoreCase(form.getCode())) {
            throw new IllegalArgumentException("Аудитория с таким кодом уже существует");
        }

        existing.setCode(form.getCode());
        existing.setCapacity(form.getCapacity());
        existing.setDescription(form.getDescription());

        return roomRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }

}
