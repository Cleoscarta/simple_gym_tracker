package com.example.gym_tracker.controller;

import com.example.gym_tracker.model.Exercise;
import com.example.gym_tracker.model.User;
import com.example.gym_tracker.model.Workout;
import com.example.gym_tracker.repository.ExerciseRepository; // Dodaj import
import com.example.gym_tracker.repository.UserRepository;
import com.example.gym_tracker.repository.WorkoutRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository; // Dodaj repozytorium ćwiczeń

    public DashboardController(WorkoutRepository workoutRepository, UserRepository userRepository, ExerciseRepository exerciseRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository; // Zainicjuj
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
                            Model model, HttpSession session) {

        String login = (String) session.getAttribute("loggedUser");
        if (login == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if (day == null) {
            day = LocalDate.now();
        }

        LocalDate startOfWeek = day.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = day.with(DayOfWeek.SUNDAY);

        int weeklyMinutes = workoutRepository.sumDurationByUserAndDateBetween(user, startOfWeek, endOfWeek);
        List<LocalDate> weekDates = startOfWeek.datesUntil(endOfWeek.plusDays(1)).toList();
        List<Workout> workoutsForDay = workoutRepository.findByUserAndDate(user, day);

        // --- Logika obliczania streaka ---
        Map<String, Integer> exerciseStreaks = new HashMap<>();
        List<Exercise> allExercises = exerciseRepository.findAll(); // Pobierz wszystkie ćwiczenia

        for (Exercise exercise : allExercises) {
            int streak = calculateExerciseStreak(user, exercise);
            if (streak > 0) { // Tylko ćwiczenia z aktywnym streakem
                exerciseStreaks.put(exercise.getName(), streak);
            }
        }
        // --- Koniec logiki streaka ---

        model.addAttribute("weeklyMinutes", weeklyMinutes);
        model.addAttribute("weekDates", weekDates);
        model.addAttribute("selectedDay", day);
        model.addAttribute("workoutsForSelectedDay", workoutsForDay);
        model.addAttribute("exerciseStreaks", exerciseStreaks); // Dodaj streaki do modelu

        return "dashboard";
    }

    private int calculateExerciseStreak(User user, Exercise exercise) {
        List<Workout> workouts = workoutRepository.findByUserAndExerciseOrderByDateDesc(user, exercise);

        if (workouts.isEmpty()) {
            return 0; // Brak treningów dla tego ćwiczenia
        }

        int streak = 0;
        LocalDate currentDate = LocalDate.now();

        // Sprawdzamy, czy ostatni trening był dzisiaj lub wczoraj
        // i liczymy streak wstecz
        for (Workout workout : workouts) {
            if (workout.getDate().equals(currentDate)) {
                streak++;
            } else if (workout.getDate().equals(currentDate.minusDays(1))) {
                streak++;
            } else {
                // Jeśli jest przerwa większa niż 1 dzień, streak się kończy
                break;
            }
            currentDate = workout.getDate().minusDays(1); // Przechodzimy do poprzedniego dnia
        }
        return streak;
    }
}