package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.loan.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class OwnersEntity {
    private String name;
}
