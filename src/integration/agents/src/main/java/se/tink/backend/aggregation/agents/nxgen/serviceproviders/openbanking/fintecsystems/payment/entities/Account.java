package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@Getter
@Setter
@JsonObject
public class Account {
    private String bic;

    @JsonProperty("bank_name")
    private String bankName;
}
