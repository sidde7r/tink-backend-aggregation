package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.abnamro.client.model.RejectedContractEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionAccountsResponse extends ErrorResponse {

    private Long id;
    private List<RejectedContractEntity> rejectedContracts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isError() {
        return id == null;
    }

    public List<RejectedContractEntity> getRejectedContracts() {

        if (rejectedContracts == null) {
            return Lists.newArrayList();
        }

        return rejectedContracts;
    }

    public void setRejectedContracts(List<RejectedContractEntity> rejectedContracts) {
        this.rejectedContracts = rejectedContracts;
    }

    public int getNumberOfRejectedContracts() {
        return rejectedContracts == null ? 0 : rejectedContracts.size();
    }
}
