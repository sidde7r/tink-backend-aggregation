package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class Mandate {

    private String customerName;

    @JsonProperty("customerNmbr")
    private String customerNumber;

    @JsonProperty("agreementNmbr")
    private String agreementNumber;

    // equals() provided so we can use distinct() on Mandates
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mandate mandate = (Mandate) o;
        return Objects.equals(customerName, mandate.customerName)
                && Objects.equals(customerNumber, mandate.customerNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerName, customerNumber);
    }
}
