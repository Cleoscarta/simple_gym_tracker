package com.example.gym_tracker.repository;

import com.example.gym_tracker.model.Exercise;
import com.example.gym_tracker.model.User;
import com.example.gym_tracker.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserAndDate(User user, LocalDate date);


    boolean existsByExercise(Exercise exercise);


    @Query("SELECT COALESCE(SUM(w.durationInMinutes), 0) FROM Workout w WHERE w.user = :user AND w.date BETWEEN :start AND :end")
    int sumDurationByUserAndDateBetween(@Param("user") User user,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);
}

