package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.abnamro.client.model.InactiveContractEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionResponse extends ErrorResponse {
    private static final int ACTIVE_STATUS = 1;

    /**
     * The state machine for the customer status at ABN AMRO side.
     * 1) POST /pfmsubscription/the-bc-number    => Subscription created with status `REGISTERED` (0)
     * 2) Sign the terms and conditions          => Subscription updated to status `SUBSCRIPTION_ACCEPTED` (6)
     * 3) PUT /pfmsubscription/the-bc-number     => Subscription updated to status `ACTIVE` (1)
     * 4) DELETE /pfmsubscription/the-bc-number  => Subscription updated to status `INACTIVE` (2)
     */
    private static final ImmutableMap<Integer, String> CUSTOMER_STATUS = ImmutableMap.<Integer, String>builder()
            .put(0, "REGISTERED")
            .put(ACTIVE_STATUS, "ACTIVE")
            .put(2, "INACTIVE")
            .put(6, "SUBSCRIPTION_ACCEPTED")
            .build();

    private String bcNumber;
    private Integer status;
    private List<String> activeContracts;
    private List<InactiveContractEntity> inactiveContracts;

    public String getBcNumber() {
        return bcNumber;
    }

    public void setBcNumber(String bcNumber) {
        this.bcNumber = bcNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<InactiveContractEntity> getInactiveContracts() {
        return inactiveContracts == null ? Collections.emptyList() : inactiveContracts;
    }

    public void setInactiveContracts(List<InactiveContractEntity> inactiveContracts) {
        this.inactiveContracts = inactiveContracts;
    }

    public List<String> getActiveContracts() {
        return activeContracts == null ? Collections.emptyList() : activeContracts;
    }

    public void setActiveContracts(List<String> activeContracts) {
        this.activeContracts = activeContracts;
    }

    public boolean isCustomerActive() {
        return Objects.equals(status, ACTIVE_STATUS);
    }

    public String getCustomerStatus() {
        return CUSTOMER_STATUS.get(status);
    }
}
