package ru.coursework.artschool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Код аудитории обязателен")
    @Size(max = 64, message = "Слишком длинный код аудитории")
    @Column(nullable = false, unique = true)
    private String code; // например: "Ауд. 101", "Мастерская 2"

    private Integer capacity; // вместимость, можно оставить null

    @Size(max = 1000, message = "Слишком длинное описание")
    private String description;

    public Room() {
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.trim() : null;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }
}
