package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ProductTypeEntity {
    private String category;
    private String displayCategory;
    private int displayCategoryIndex;
    private String ccbId;
    private String productName;
    private String productBranch;
}
