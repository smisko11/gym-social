package smisko11.gym_social.catalog.exercise;

import org.springframework.stereotype.Component;
import smisko11.gym_social.catalog.exercise.dto.ExerciseRequest;
import smisko11.gym_social.catalog.exercise.dto.ExerciseResponse;
import smisko11.gym_social.catalog.exercise.dto.MuscleGroupTargetResponse;
import smisko11.gym_social.catalog.musclegroup.MuscleGroup;

import java.util.Comparator;
import java.util.List;

@Component
public class ExerciseMapper {

    public Exercise toEntity(ExerciseRequest request) {
        Exercise exercise = new Exercise();
        updateEntity(exercise, request);
        return exercise;
    }

    // Samo polja vežbe. Grupe mišića postavlja servis.
    public void updateEntity(Exercise exercise, ExerciseRequest request) {
        exercise.setName(request.name().trim());
        exercise.setDescription(normalize(request.description()));
        exercise.setBodyweight(request.bodyweight());
    }

    // Jedan red srednje tabele. Ključ (id) popunjava @MapsId.
    public ExerciseMuscleGroup toLink(Exercise exercise, MuscleGroup muscleGroup, boolean primary) {
        ExerciseMuscleGroup link = new ExerciseMuscleGroup();
        link.setExercise(exercise);
        link.setMuscleGroup(muscleGroup);
        link.setPrimary(primary);
        return link;
    }

    public ExerciseResponse toResponse(Exercise exercise) {
        List<MuscleGroupTargetResponse> targets = exercise.getMuscleGroups().stream()
                .map(link -> new MuscleGroupTargetResponse(
                        link.getMuscleGroup().getId(),
                        link.getMuscleGroup().getName(),
                        link.isPrimary()))
                .sorted(Comparator.comparing(MuscleGroupTargetResponse::primary).reversed()
                        .thenComparing(MuscleGroupTargetResponse::name))
                .toList();

        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.isBodyweight(),
                targets
        );
    }

    // null ili samo razmaci → null; inače trimovan tekst
    private String normalize(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }
}