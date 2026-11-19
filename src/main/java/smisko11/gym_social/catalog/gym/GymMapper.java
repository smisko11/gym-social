package smisko11.gym_social.catalog.gym;

import org.springframework.stereotype.Component;
import smisko11.gym_social.catalog.gym.dto.GymRequest;
import smisko11.gym_social.catalog.gym.dto.GymResponse;

@Component
public class GymMapper {

    public Gym toEntity(GymRequest request) {
        Gym gym = new Gym();
        updateEntity(gym, request);
        return gym;
    }

    public void updateEntity(Gym gym, GymRequest request) {
        gym.setName(request.name().trim());
        gym.setCity(request.city().trim());
        gym.setAddress(request.address().trim());
        gym.setLatitude(request.latitude());
        gym.setLongitude(request.longitude());
    }

    public GymResponse toResponse(Gym gym) {
        return new GymResponse(
                gym.getId(),
                gym.getName(),
                gym.getCity(),
                gym.getAddress(),
                gym.getLatitude(),
                gym.getLongitude(),
                gym.getCreatedAt()
        );
    }
}