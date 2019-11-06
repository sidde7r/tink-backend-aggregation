package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorAgentEntity {
    private IdentificationEntity identification;
    private String bic;

    public CreditorAgentEntity(IdentificationEntity identification) {
        this.identification = identification;
    }

    public CreditorAgentEntity() {}

    public static CreditorAgentEntity ofIdentification(String code, String type) {
        return new CreditorAgentEntity(new IdentificationEntity(code, type));
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }
}
