package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingEInvoicesResponse extends AbstractResponse  {
    @JsonProperty("eInvoicesGrouped")
    private List<EInvoicesGroupedEntity> eInvoicesGrouped;

    public List<EInvoicesGroupedEntity> geteInvoicesGrouped() {
        return eInvoicesGrouped;
    }

    public void seteInvoicesGrouped(List<EInvoicesGroupedEntity> eInvoicesGrouped) {
        this.eInvoicesGrouped = eInvoicesGrouped;
    }
}
