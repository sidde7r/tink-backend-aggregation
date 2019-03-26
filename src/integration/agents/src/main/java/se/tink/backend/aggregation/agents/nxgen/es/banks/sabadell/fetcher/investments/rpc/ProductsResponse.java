package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.InvestmentProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductsResponse {
    // This class also provides information about :
    //  - Accounts
    //  - Cards
    //  - Financial products
    //  - Additional products
    //  - Other
    //  - Insurance
    // But almost all of them have specific endpoints and are processed in their respective RPC
    // entities, at the time of writing, we only use this class to get information about
    // investments.

    private InvestmentProductEntity investmentProduct;

    public InvestmentProductEntity getInvestmentProduct() {
        return investmentProduct;
    }
}
