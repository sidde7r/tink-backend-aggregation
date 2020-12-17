package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Setter
public class SecurityNumberSeedResponse {
    private String seed;

    @JsonIgnore
    public List<Integer> indexPositions() {
        return Stream.of(seed.split(",")).map(Integer::valueOf).collect(Collectors.toList());
    }
}
