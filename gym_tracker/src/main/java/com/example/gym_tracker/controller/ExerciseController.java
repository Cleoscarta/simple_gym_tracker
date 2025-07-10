package com.example.gym_tracker.controller;

import com.example.gym_tracker.model.Exercise;
import com.example.gym_tracker.model.User;
import com.example.gym_tracker.model.Workout;
import com.example.gym_tracker.repository.ExerciseRepository;
import com.example.gym_tracker.repository.UserRepository;
import com.example.gym_tracker.repository.WorkoutRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public ExerciseController(ExerciseRepository exerciseRepository,
                              WorkoutRepository workoutRepository,
                              UserRepository userRepository) {
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/choose-exercise")
    public String showChooseExerciseForm(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day, Model model) {
        List<Exercise> exercises = exerciseRepository.findAll();
        model.addAttribute("exercises", exercises);
        model.addAttribute("selectedDate", day);
        return "choose-exercise"; // nowa nazwa widoku
    }

    @PostMapping("/add-exercise")
    public String saveWorkout(@RequestParam Long exerciseId,
                              @RequestParam int durationInMinutes,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              HttpSession session) {

        String login = (String) session.getAttribute("loggedUser");
        if (login == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByLogin(login).orElseThrow();
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setExercise(exercise);
        workout.setDate(date);
        workout.setDurationInMinutes(durationInMinutes);

        workoutRepository.save(workout);

        return "redirect:/dashboard?day=" + date;
    }

    // Biblioteka ćwiczeń
    @GetMapping("/exercises")
    public String showLibrary(@RequestParam(required = false) String category, Model model) {
        List<Exercise> exercises = (category == null || category.isEmpty())
                ? exerciseRepository.findAllByOrderByNameAsc()
                : exerciseRepository.findByCategoryOrderByNameAsc(category);

        List<String> categories = exerciseRepository.findAll().stream()
                .map(Exercise::getCategory)
                .filter(cat -> cat != null && !cat.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("exercises", exercises);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);

        return "exercise-library";
    }

    @GetMapping("/exercises/add")
    public String showAddForm(Model model) {
        model.addAttribute("exercise", new Exercise());

        List<String> categories = exerciseRepository.findAll().stream()
                .map(Exercise::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("categories", categories);

        return "exercise-add";
    }

    @PostMapping("/exercises/add")
    public String addExercise(@ModelAttribute Exercise exercise) {
        exerciseRepository.save(exercise);
        return "redirect:/exercises";
    }

    @GetMapping("/exercises/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow();
        model.addAttribute("exercise", exercise);
        return "exercise-edit";
    }

    @PostMapping("/exercises/edit/{id}")
    public String updateExercise(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String category) {
        Exercise ex = exerciseRepository.findById(id).orElseThrow();
        ex.setName(name);
        ex.setCategory(category);
        exerciseRepository.save(ex);
        return "redirect:/exercises";
    }

    @PostMapping("/exercises/delete/{id}")
    public String deleteExercise(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseRepository.findById(id).orElse(null);
        if (exercise == null) {
            return "redirect:/exercises"; // lub komunikat: nie znaleziono
        }

        boolean isUsedInWorkout = workoutRepository.existsByExercise(exercise);
        if (isUsedInWorkout) {
            model.addAttribute("error", "Nie można usunąć ćwiczenia, które jest przypisane do treningu.");
            return showLibrary(null, model); // ponowne załadowanie widoku z błędem
        }

        exerciseRepository.delete(exercise);
        return "redirect:/exercises";
    }
    @PostMapping("/workouts/delete/{id}")
    public String deleteWorkout(@PathVariable Long id, @RequestParam("day") String day) {
        workoutRepository.deleteById(id);
        return "redirect:/dashboard?day=" + day;
    }
}
