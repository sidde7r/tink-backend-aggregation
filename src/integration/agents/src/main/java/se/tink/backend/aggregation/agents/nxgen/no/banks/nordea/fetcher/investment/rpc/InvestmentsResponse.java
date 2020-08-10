package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.investment.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.investment.entity.InvestmentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvestmentsResponse {
    private List<InvestmentEntity> accounts;
}
