package smisko11.gym_social.catalog.exercise;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @EntityGraph(attributePaths = {"muscleGroups", "muscleGroups.muscleGroup"})
    List<Exercise> findAll(Sort sort);
}