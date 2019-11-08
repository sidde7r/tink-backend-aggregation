package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorAgentEntity {
    private IdentificationEntity identification;
    private String bic;

    @JsonCreator
    public CreditorAgentEntity(
            @JsonProperty IdentificationEntity identification, @JsonProperty String bic) {
        this.identification = identification;
        this.bic = bic;
    }

    public static CreditorAgentEntity ofIdentification(String code, String type) {
        return new CreditorAgentEntity(new IdentificationEntity(code, type), null);
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }
}
