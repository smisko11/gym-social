package smisko11.gym_social.catalog.gym;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smisko11.gym_social.catalog.gym.dto.GymRequest;
import smisko11.gym_social.catalog.gym.dto.GymResponse;
import smisko11.gym_social.common.exception.ConflictException;
import smisko11.gym_social.common.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymService {

    private final GymRepository gymRepository;
    private final GymMapper gymMapper;

    @Transactional
    public GymResponse create(GymRequest request) {
        if (gymRepository.existsByNameIgnoreCaseAndAddressIgnoreCase(
                request.name().trim(), request.address().trim())) {
            throw new ConflictException("Teretana sa tim nazivom i adresom već postoji");
        }
        Gym saved = gymRepository.save(gymMapper.toEntity(request));
        return gymMapper.toResponse(saved);
    }

    public GymResponse getById(Long id) {
        return gymMapper.toResponse(getEntityById(id));
    }

    public Page<GymResponse> findAll(String city, Pageable pageable) {
        Page<Gym> page;
        // null provera mora prva, inače isBlank() puca na null
        if (city == null || city.isBlank()) {
            page = gymRepository.findAll(pageable);
        } else {
            page = gymRepository.findByCityIgnoreCase(city.trim(), pageable);
        }
        return page.map(gymMapper::toResponse);
    }

    @Transactional
    public GymResponse update(Long id, GymRequest request) {
        Gym gym = getEntityById(id);

        if (gymRepository.existsByNameIgnoreCaseAndAddressIgnoreCaseAndIdNot(
                request.name().trim(), request.address().trim(), id)) {
            throw new ConflictException("Teretana sa tim nazivom i adresom već postoji");
        }

        gymMapper.updateEntity(gym, request);
        return gymMapper.toResponse(gym);
    }

    @Transactional
    public void delete(Long id) {
        Gym gym = getEntityById(id);

        try {
            gymRepository.delete(gym);
            gymRepository.flush();   // pošalji DELETE odmah, da greška stranog ključa padne u ovaj catch
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Teretana se ne može obrisati jer je koriste zajednički treninzi");
        }
    }

    public Gym getEntityById(Long id) {
        return gymRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Teretana", id));
    }
}
