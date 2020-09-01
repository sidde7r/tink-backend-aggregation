package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class OwnersEntity {

    String name;
}
