package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.identity.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MemberEntity {
    private String name;
    private String userId;
}
