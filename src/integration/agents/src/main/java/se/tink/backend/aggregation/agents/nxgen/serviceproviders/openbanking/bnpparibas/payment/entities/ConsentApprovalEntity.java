package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentApprovalEntity {

    @JsonProperty("href")
    private String url;
}
