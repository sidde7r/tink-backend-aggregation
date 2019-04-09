package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InactiveContractEntity {

    private static final ImmutableMap<Integer, String> INACTIVE_REASONS =
            new ImmutableMap.Builder<Integer, String>()
                    .put(2, "INACTIVE")
                    .put(4, "FRS SUBSCRIPTION FAILED")
                    .put(9, "FAILURE DUE TO TECHNICAL REASONS")
                    .build();

    private String contractNumber;
    private Integer status;

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusReason() {
        return INACTIVE_REASONS.get(status);
    }
}
