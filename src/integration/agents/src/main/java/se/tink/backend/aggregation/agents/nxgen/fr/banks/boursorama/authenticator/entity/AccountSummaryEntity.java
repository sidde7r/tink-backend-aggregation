package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountSummaryEntity {
    private String description;
    private String id;
    private String label;
    private List<Object> opportunities;
    private List<BalancesEntity> balances;
    private List<CategoriesEntity> categories;

    public List<CategoriesEntity> getCategories() {
        return categories;
    }
}
