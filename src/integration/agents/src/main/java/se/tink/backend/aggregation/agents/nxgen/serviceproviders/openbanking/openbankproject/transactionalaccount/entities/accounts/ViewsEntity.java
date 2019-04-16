package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ViewsEntity {

    private String id;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("is_public")
    private Boolean isPublic;

    public String getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public Boolean getPublic() {
        return isPublic;
    }
}
