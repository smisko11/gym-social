package smisko11.gym_social.catalog.musclegroup;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smisko11.gym_social.catalog.musclegroup.dto.MuscleGroupRequest;
import smisko11.gym_social.catalog.musclegroup.dto.MuscleGroupResponse;

import java.util.List;

@RestController
@RequestMapping("/api/muscle-groups")
@RequiredArgsConstructor
public class MuscleGroupController {

    private final MuscleGroupService muscleGroupService;

    @GetMapping
    public List<MuscleGroupResponse> getAll() {
        return muscleGroupService.getAll();
    }

    @GetMapping("/{id}")
    public MuscleGroupResponse getById(@PathVariable Long id) {
        return muscleGroupService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MuscleGroupResponse create(@Valid @RequestBody MuscleGroupRequest request) {
        return muscleGroupService.create(request);
    }

    @PutMapping("/{id}")
    public MuscleGroupResponse update(@PathVariable Long id,
                                      @Valid @RequestBody MuscleGroupRequest request) {
        return muscleGroupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        muscleGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}