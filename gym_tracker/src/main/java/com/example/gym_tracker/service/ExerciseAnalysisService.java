package com.example.gym_tracker.service;

import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExerciseAnalysisService {

    public static class ExerciseEntry {
        public LocalDate date;
        public String exercise;
        public int count;

        public ExerciseEntry(LocalDate date, String exercise, int count) {
            this.date = date;
            this.exercise = exercise;
            this.count = count;
        }
    }

    public static class MaxExercisePeriodResult {
        public String exercise;
        public int maxSum;
        public LocalDate startDate;
        public LocalDate endDate;

        public MaxExercisePeriodResult(String exercise, int maxSum, LocalDate startDate, LocalDate endDate) {
            this.exercise = exercise;
            this.maxSum = maxSum;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public MaxExercisePeriodResult maxExercisePeriodWithExclusions(List<ExerciseEntry> data, String exerciseName, int windowDays, Set<DayOfWeek> excludedDays) {
        // Filter for given exercise and exclude specified days
        List<ExerciseEntry> filtered = data.stream()
                .filter(e -> e.exercise.equalsIgnoreCase(exerciseName))
                .filter(e -> !excludedDays.contains(e.date.getDayOfWeek()))
                .sorted(Comparator.comparing(e -> e.date))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return null;
        }

        List<LocalDate> uniqueDates = filtered.stream()
                .map(e -> e.date)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int maxSum = 0;
        LocalDate bestStart = null;
        LocalDate bestEnd = null;

        // Slide window over dates
        for (int i = 0; i <= uniqueDates.size() - windowDays; i++) {
            LocalDate startDate = uniqueDates.get(i);
            LocalDate endDate = startDate.plusDays(windowDays - 1);

            // Check if the window contains excluded days
            boolean hasExcluded = false;
            for (int d = 0; d < windowDays; d++) {
                LocalDate checkDate = startDate.plusDays(d);
                if (excludedDays.contains(checkDate.getDayOfWeek())) {
                    hasExcluded = true;
                    break;
                }
            }
            if (hasExcluded) continue;

            int windowSum = filtered.stream()
                    .filter(e -> !e.date.isBefore(startDate) && !e.date.isAfter(endDate))
                    .mapToInt(e -> e.count)
                    .sum();

            if (windowSum > maxSum) {
                maxSum = windowSum;
                bestStart = startDate;
                bestEnd = endDate;
            }
        }

        return new MaxExercisePeriodResult(exerciseName, maxSum, bestStart, bestEnd);
    }
}