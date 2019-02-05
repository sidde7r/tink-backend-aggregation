package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollAuthResponseEntity {
    private String code;
    private String state;
    private LinkListEntity links;

    public String getCode() {
        return code;
    }

    public String getState() {
        return state;
    }

    @JsonIgnore
    public Optional<LinkEntity> findLinkByName(String name) {
        return links.findLinkByName(name);
    }
}
