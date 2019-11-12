package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundCategoryEntity {
    private String categoryId;
    private String categoryName;
    private List<FundEntity> items;

    public List<FundEntity> getItems() {
        return items;
    }
}
