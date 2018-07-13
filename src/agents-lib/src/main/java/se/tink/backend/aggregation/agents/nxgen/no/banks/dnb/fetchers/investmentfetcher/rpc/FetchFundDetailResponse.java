package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities.ProductDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchFundDetailResponse {
    private ProductDetailsEntity productDetails;

    public ProductDetailsEntity getProductDetails() {
        return this.productDetails == null ? new ProductDetailsEntity() : this.productDetails;
    }
}
