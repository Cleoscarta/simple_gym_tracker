package com.example.gym_tracker.repository;

import com.example.gym_tracker.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // ✅ Wszystkie ćwiczenia, posortowane A-Z
    List<Exercise> findAllByOrderByNameAsc();

    // ✅ Filtr: ćwiczenia wg kategorii (np. cardio), posortowane A-Z
    List<Exercise> findByCategoryOrderByNameAsc(String category);
}
