package smisko11.gym_social.catalog.musclegroup;

import org.springframework.stereotype.Component;
import smisko11.gym_social.catalog.musclegroup.dto.MuscleGroupRequest;
import smisko11.gym_social.catalog.musclegroup.dto.MuscleGroupResponse;

@Component
public class MuscleGroupMapper {

    public MuscleGroup toEntity(MuscleGroupRequest request){
        MuscleGroup entity = new MuscleGroup();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(MuscleGroup entity,MuscleGroupRequest request){
        entity.setName(request.name().trim());
    }

    public MuscleGroupResponse toResponse(MuscleGroup entity){
        return new MuscleGroupResponse(entity.getId(), entity.getName());
    }

}
