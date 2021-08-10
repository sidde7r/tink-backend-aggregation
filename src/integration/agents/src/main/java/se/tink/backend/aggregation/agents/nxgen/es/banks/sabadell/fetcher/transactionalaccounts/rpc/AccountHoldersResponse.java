package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.Intervener;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AccountHoldersResponse {

    private List<Intervener> interveners;
}
