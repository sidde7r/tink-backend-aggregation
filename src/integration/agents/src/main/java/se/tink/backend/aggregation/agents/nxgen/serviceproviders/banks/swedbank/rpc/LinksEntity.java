package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkEntity edit;
    private LinkEntity next;
    private LinkEntity self;
    private LinkEntity sign;
    private LinkEntity delete;

    public LinkEntity getEdit() {
        return edit;
    }

    public LinkEntity getNext() {
        return next;
    }

    public LinkEntity getSelf() {
        return self;
    }

    public LinkEntity getSign() {
        return sign;
    }

    public LinkEntity getDelete() {
        return delete;
    }

    @JsonIgnore
    public LinkEntity getNextOrThrow() {
        return Optional.ofNullable(next).orElseThrow(IllegalStateException::new);
    }

    @JsonIgnore
    public LinkEntity getSignOrThrow() {
        return Optional.ofNullable(sign).orElseThrow(IllegalStateException::new);
    }
}
