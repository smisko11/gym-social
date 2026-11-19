package smisko11.gym_social.catalog.musclegroup;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smisko11.gym_social.catalog.musclegroup.dto.MuscleGroupRequest;
import smisko11.gym_social.catalog.musclegroup.dto.MuscleGroupResponse;
import smisko11.gym_social.common.exception.ConflictException;
import smisko11.gym_social.common.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MuscleGroupService {

    private final MuscleGroupRepository muscleGroupRepository;
    private final MuscleGroupMapper muscleGroupMapper;

    public List<MuscleGroupResponse> getAll() {
        return muscleGroupRepository.findAll(Sort.by("name"))
                .stream()
                .map(muscleGroupMapper::toResponse)
                .toList();
    }

    public MuscleGroupResponse getById(Long id) {
        return muscleGroupMapper.toResponse(getEntityById(id));
    }

    @Transactional
    public MuscleGroupResponse create(MuscleGroupRequest request) {
        if (muscleGroupRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Grupa mišića sa tim nazivom već postoji");
        }
        MuscleGroup saved = muscleGroupRepository.save(muscleGroupMapper.toEntity(request));
        return muscleGroupMapper.toResponse(saved);
    }

    @Transactional
    public MuscleGroupResponse update(Long id, MuscleGroupRequest request) {
        MuscleGroup entity = getEntityById(id);

        if (muscleGroupRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Grupa mišića sa tim nazivom već postoji");
        }

        muscleGroupMapper.updateEntity(entity, request);
        return muscleGroupMapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        MuscleGroup entity = getEntityById(id);

        try {
            muscleGroupRepository.delete(entity);
            muscleGroupRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Grupa mišića se ne može obrisati jer je koriste vežbe, rasporedi ili zajednički treninzi");
        }
    }

    public MuscleGroup getEntityById(Long id) {
        return muscleGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grupa mišića", id));
    }
}