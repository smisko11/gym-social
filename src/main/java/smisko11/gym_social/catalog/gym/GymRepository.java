package smisko11.gym_social.catalog.gym;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, Long> {

    boolean existsByNameIgnoreCaseAndAddressIgnoreCase(String name, String address);

    boolean existsByNameIgnoreCaseAndAddressIgnoreCaseAndIdNot(String name, String address, Long id);

    Page<Gym> findByCityIgnoreCase(String city, Pageable pageable);
}