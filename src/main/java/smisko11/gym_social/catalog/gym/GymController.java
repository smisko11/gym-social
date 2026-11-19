package smisko11.gym_social.catalog.gym;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smisko11.gym_social.catalog.gym.dto.GymRequest;
import smisko11.gym_social.catalog.gym.dto.GymResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gyms")
public class GymController {
    private final GymService gymService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public GymResponse create(@Valid @RequestBody GymRequest request){
        return gymService.create(request);
    }

    @GetMapping("/{id}")
    public GymResponse getById(@PathVariable Long id){
        return gymService.getById(id);
    }

    @GetMapping
    public Page<GymResponse> findAll(@RequestParam(required = false)String city, Pageable pageable){
        return gymService.findAll(city, pageable);
    }

    @PutMapping("/{id}")
    public GymResponse update(@Valid @RequestBody GymRequest request, @PathVariable Long id){
        return gymService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        gymService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
