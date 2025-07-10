package com.example.gym_tracker.controller;

import com.example.gym_tracker.model.User;
import com.example.gym_tracker.model.Workout;
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
import java.util.List;

@Controller
public class DashboardController {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public DashboardController(WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
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

        model.addAttribute("weeklyMinutes", weeklyMinutes);
        model.addAttribute("weekDates", weekDates);
        model.addAttribute("selectedDay", day);
        model.addAttribute("workoutsForSelectedDay", workoutsForDay);

        return "dashboard";
    }
}
