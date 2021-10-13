package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {
    private LinkEntity edit;
    private LinkEntity next;
    private LinkEntity self;
    private LinkEntity sign;
    private LinkEntity delete;

    @JsonIgnore
    public LinkEntity getNextOrThrow() {
        return Optional.ofNullable(next).orElseThrow(IllegalStateException::new);
    }

    @JsonIgnore
    public LinkEntity getSignOrThrow() {
        return Optional.ofNullable(sign).orElseThrow(IllegalStateException::new);
    }
}
