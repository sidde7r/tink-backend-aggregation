package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PermissionsEntity {
    @JsonProperty("View")
    private String view;

    @JsonProperty("Update")
    private String update;

    @JsonProperty("Create")
    private String create;

    @JsonProperty("Delete")
    private String delete;

    public String getView() {
        return view;
    }

    public String getUpdate() {
        return update;
    }

    public String getCreate() {
        return create;
    }

    public String getDelete() {
        return delete;
    }
}
