package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.model;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CategoryEntity {

    private CategoryType accountCategory;

    public CategoryEntity setAccountCategory(CategoryType accountCategory) {
        this.accountCategory = accountCategory;
        return this;
    }

    public enum CategoryType {
        CURRENT_ACCOUNT,
        SAVINGS_ACCOUNT
    }
}
