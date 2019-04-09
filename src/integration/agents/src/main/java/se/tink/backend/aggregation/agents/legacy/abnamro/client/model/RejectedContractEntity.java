package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RejectedContractEntity {

    public static final ImmutableMap<Integer, String> REJECTION_REASONS =
            new ImmutableMap.Builder<Integer, String>()
                    .put(1, "INVALID OWNER")
                    .put(2, "FRS SUBSCRIPTION FAILED")
                    .build();

    private Long contractNumber;

    private Integer rejectedReasonCode;

    public Long getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(Long contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Integer getRejectedReasonCode() {
        return rejectedReasonCode;
    }

    public void setRejectedReasonCode(Integer rejectedReasonCode) {
        this.rejectedReasonCode = rejectedReasonCode;
    }

    public String getRejectionReason() {
        return REJECTION_REASONS.get(rejectedReasonCode);
    }
}
