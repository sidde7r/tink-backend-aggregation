package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {
    @JsonProperty("Lista")
    private List<AccountDetailsEntity> list;

    @JsonProperty("Selected")
    private String selected;

    public List<AccountDetailsEntity> getList() {
        return list;
    }

    public String getSelected() {
        return selected;
    }
}
