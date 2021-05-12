package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.entities;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CustodyAccountsEntity {
    private List<MiscEntity> misc = Collections.emptyList();
}
