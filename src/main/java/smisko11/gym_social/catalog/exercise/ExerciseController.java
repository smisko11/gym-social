package smisko11.gym_social.catalog.exercise;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smisko11.gym_social.catalog.exercise.dto.ExerciseRequest;
import smisko11.gym_social.catalog.exercise.dto.ExerciseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public List<ExerciseResponse> getAll() {
        return exerciseService.getAll();
    }

    @GetMapping("/{id}")
    public ExerciseResponse getById(@PathVariable Long id) {
        return exerciseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse create(@Valid @RequestBody ExerciseRequest request) {
        return exerciseService.create(request);
    }

    @PutMapping("/{id}")
    public ExerciseResponse update(@PathVariable Long id,
                                   @Valid @RequestBody ExerciseRequest request) {
        return exerciseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}