package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.identitydata.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class NameEntity {
    private String name;
}
