package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities.FundInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundInfoResponse extends AlandsBankenResponse {

    private FundInfoEntity fund;

    public FundInfoEntity getFund() {
        return fund;
    }
}
