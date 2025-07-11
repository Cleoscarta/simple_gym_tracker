package com.example.gym_tracker.controller;

import com.example.gym_tracker.repository.WorkoutRepository;
import com.example.gym_tracker.Service.ExerciseAnalysisService;
import com.example.gym_tracker.model.Workout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/statistic")
public class StatisticController {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ExerciseAnalysisService analysisService;

    @GetMapping
    public String getStatistic(@RequestParam(required = false) String exerciseName,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               Model model) {

        if (exerciseName != null && startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<ExerciseAnalysisService.ExerciseEntry> entries = workoutRepository.findAll().stream()
                    .filter(w -> w.getExercise() != null && w.getExercise().getName().equalsIgnoreCase(exerciseName))
                    .filter(w -> !w.getDate().isBefore(start) && !w.getDate().isAfter(end))
                    .map(w -> new ExerciseAnalysisService.ExerciseEntry(
                            w.getDate(),
                            w.getExercise().getName(),
                            w.getDurationInMinutes() // currently using duration as "count"
                    ))
                    .toList();

            int total = entries.stream().mapToInt(e -> e.count).sum();

            model.addAttribute("exerciseName", exerciseName);
            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);
            model.addAttribute("total", total);
        }

        return "statistic"; // Returns the Thymeleaf template
    }
}