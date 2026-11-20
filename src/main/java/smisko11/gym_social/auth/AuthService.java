package smisko11.gym_social.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smisko11.gym_social.auth.dto.LoginRequest;
import smisko11.gym_social.auth.dto.RegisterRequest;
import smisko11.gym_social.auth.dto.TokenResponse;
import smisko11.gym_social.common.exception.ConflictException;
import smisko11.gym_social.common.exception.ForbiddenException;
import smisko11.gym_social.common.exception.UnauthorizedException;
import smisko11.gym_social.common.security.JwtService;
import smisko11.gym_social.user.AppUser;
import smisko11.gym_social.user.AppUserRepository;
import smisko11.gym_social.user.UserMapper;
import smisko11.gym_social.user.profile.UserProfile;
import smisko11.gym_social.user.profile.UserProfileRepository;
import smisko11.gym_social.user.dto.UserResponse;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String username = request.username();

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Nalog sa tim email-om već postoji");
        }
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username je zauzet");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        AppUser savedUser = appUserRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setDisplayName(username);
        userProfileRepository.save(profile);

        return userMapper.toResponse(savedUser);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Pogrešan email ili lozinka"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Pogrešan email ili lozinka");
        }

        if (!user.isEnabled()) {
            throw new ForbiddenException("Nalog je onemogućen");
        }

        String token = jwtService.generateToken(user);
        return TokenResponse.bearer(token, jwtService.getExpirationSeconds());
    }

}