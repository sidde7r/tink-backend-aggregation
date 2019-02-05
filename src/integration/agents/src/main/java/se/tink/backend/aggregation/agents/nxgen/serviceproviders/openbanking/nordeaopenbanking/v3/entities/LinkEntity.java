package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String rel;
    private String href;

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }

    @JsonIgnore
    public boolean relMatches(String name) {
        return Optional.ofNullable(name)
                .map(n -> n.equalsIgnoreCase(rel))
                .orElse(false);
    }
}
