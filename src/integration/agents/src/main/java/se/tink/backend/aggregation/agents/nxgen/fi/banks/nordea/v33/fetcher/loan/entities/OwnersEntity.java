package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class OwnersEntity {
    private String name;
}
