package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CategorizationsEntity {
    private LinksEntity links;
    private List<CategoryEntity> categories;

    public LinksEntity getLinks() {
        return links;
    }

    public List<CategoryEntity> getCategories() {
        return categories;
    }
}
