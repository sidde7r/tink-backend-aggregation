package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentProductEntity {
    // This class also provides information about :
    //  - Pension plans
    //  - Investment funds
    //  - Deposits
    //  - Insured plan forecast
    //  - Other
    //  - Saving plans
    //  - Saving insurance
    // But almost all of them have specific endpoints and are processed in their respective
    // RPC/entities entities, at the time of writing, we only use this class to get information
    // about investments.

    private AmountEntity amount;
    private SecuritiesEntity securities;

    public SecuritiesEntity getSecurities() {
        return securities;
    }
}
