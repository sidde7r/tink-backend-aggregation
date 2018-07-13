package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapSerializer;

public class InitBankIdConfirmPaymentsTransData {

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String confirmTransType;

    public String getConfirmTransType() {
        return confirmTransType;
    }

    public void setConfirmTransType(String confirmTransType) {
        this.confirmTransType = confirmTransType;
    }
}
