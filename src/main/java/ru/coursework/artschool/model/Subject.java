package ru.coursework.artschool.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.ManyToMany;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Код дисциплины, например "PNT-101"
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Код дисциплины обязателен")
    @Size(max = 32, message = "Код слишком длинный")
    private String code;

    // Название, например "Живопись (базовый курс)"
    @Column(nullable = false)
    @NotBlank(message = "Название дисциплины обязательно")
    @Size(max = 255, message = "Название слишком длинное")
    private String name;

    // Краткое описание
    @Column(length = 2000)
    @Size(max = 2000, message = "Описание слишком длинное")
    private String description;

    // Кол-во академических часов (опционально)
    private Integer hours;

    @ManyToMany(mappedBy = "subjects")
    private Set<Teacher> teachers = new HashSet<>();

    public Set<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(Set<Teacher> teachers) {
        this.teachers = teachers;
    }

    public Subject() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }
}
