package br.com.eterniaserver.ffut.domain.challenge.rest;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import br.com.eterniaserver.ffut.domain.challenge.models.*;
import br.com.eterniaserver.ffut.domain.challenge.services.ChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/challenge/")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService service;

    @GetMapping("list/")
    @ResponseStatus(HttpStatus.OK)
    public ListChallengeResponse list(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
        return service.list(page.orElse(0), size.orElse(10));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateChallengeResponse create(@RequestBody @Valid CreateChallengeRequest request) {
        ChallengeEntity entity = service.create(request);

        return new CreateChallengeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getChallengeVersion(),
                entity.getCode(),
                entity.getDifficulty()
        );
    }

    @GetMapping("{id}/")
    @ResponseStatus(HttpStatus.OK)
    public ReadChallengeResponse read(@PathVariable String id) {
        return service.read(id);
    }

    @PutMapping("{id}/")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable String id, @RequestBody @Valid UpdateChallengeRequest request) {
        service.update(id, request);
    }

    @DeleteMapping("{id}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

}
