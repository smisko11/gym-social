package smisko11.gym_social.catalog.exercise;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smisko11.gym_social.catalog.exercise.dto.ExerciseRequest;
import smisko11.gym_social.catalog.exercise.dto.ExerciseResponse;
import smisko11.gym_social.catalog.exercise.dto.MuscleGroupTargetRequest;
import smisko11.gym_social.catalog.musclegroup.MuscleGroup;
import smisko11.gym_social.catalog.musclegroup.MuscleGroupService;
import smisko11.gym_social.common.exception.BadRequestException;
import smisko11.gym_social.common.exception.ConflictException;
import smisko11.gym_social.common.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;
    private final MuscleGroupService muscleGroupService;

    public List<ExerciseResponse> getAll() {
        return exerciseRepository.findAll(Sort.by("name"))
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    public ExerciseResponse getById(Long id) {
        return exerciseMapper.toResponse(getEntityById(id));
    }

    @Transactional
    public ExerciseResponse create(ExerciseRequest request) {
        if (exerciseRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Vežba sa tim nazivom već postoji");
        }
        validateTargets(request.muscleGroups());

        Exercise exercise = exerciseMapper.toEntity(request);
        applyMuscleGroups(exercise, request.muscleGroups());

        Exercise saved = exerciseRepository.save(exercise);   // cascade sačuva i redove
        return exerciseMapper.toResponse(saved);
    }

    @Transactional
    public ExerciseResponse update(Long id, ExerciseRequest request) {
        Exercise exercise = getEntityById(id);

        if (exerciseRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Vežba sa tim nazivom već postoji");
        }
        validateTargets(request.muscleGroups());

        exerciseMapper.updateEntity(exercise, request);

        exercise.getMuscleGroups().clear();   // orphanRemoval: stari redovi će biti obrisani
        exerciseRepository.flush();           // obriši ih ODMAH, pre dodavanja novih
        applyMuscleGroups(exercise, request.muscleGroups());

        return exerciseMapper.toResponse(exercise);
    }

    @Transactional
    public void delete(Long id) {
        Exercise exercise = getEntityById(id);
        try {
            exerciseRepository.delete(exercise);
            exerciseRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Vežba se ne može obrisati jer se koristi u rasporedima ili treninzima");
        }
    }

    public Exercise getEntityById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vežba", id));
    }

    // Pravila za listu grupa: bez duplikata, bar jedna glavna
    private void validateTargets(List<MuscleGroupTargetRequest> targets) {
        Set<Long> seen = new HashSet<>();
        for (MuscleGroupTargetRequest target : targets) {
            if (!seen.add(target.muscleGroupId())) {
                throw new BadRequestException(
                        "Grupa mišića sa id " + target.muscleGroupId() + " je navedena više puta");
            }
        }

        boolean hasPrimary = targets.stream().anyMatch(MuscleGroupTargetRequest::primary);
        if (!hasPrimary) {
            throw new BadRequestException("Vežba mora imati bar jednu glavnu grupu mišića");
        }
    }

    // Za svaki target: učitaj grupu (ili 404), napravi red, dodaj ga vežbi
    private void applyMuscleGroups(Exercise exercise, List<MuscleGroupTargetRequest> targets) {
        for (MuscleGroupTargetRequest target : targets) {
            MuscleGroup muscleGroup = muscleGroupService.getEntityById(target.muscleGroupId());
            exercise.getMuscleGroups().add(
                    exerciseMapper.toLink(exercise, muscleGroup, target.primary()));
        }
    }
}