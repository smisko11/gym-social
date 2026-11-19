package smisko11.gym_social.user;

import org.springframework.stereotype.Component;
import smisko11.gym_social.user.dto.UserResponse;

@Component
public class UserMapper {

    public UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}