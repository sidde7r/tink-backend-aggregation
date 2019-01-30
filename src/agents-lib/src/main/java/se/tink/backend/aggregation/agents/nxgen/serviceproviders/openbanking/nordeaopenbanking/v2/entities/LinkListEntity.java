package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkListEntity extends ArrayList<LinkEntity> {

    @JsonIgnore
    public Optional<LinkEntity> findLinkByName(String name) {
        return this.stream()
                .filter(link -> link.relMatches(name))
                .findFirst();
    }
}
