package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.HomesEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.entities.TotalEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MortgageResponse {
    private TotalEntity total;
    private List<HomesEntity> homes = Collections.emptyList();
}
