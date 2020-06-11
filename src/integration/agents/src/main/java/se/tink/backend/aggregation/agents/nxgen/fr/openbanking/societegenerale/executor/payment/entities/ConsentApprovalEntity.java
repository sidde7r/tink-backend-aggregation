package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConsentApprovalEntity {
    @JsonProperty("href")
    private String url;
}
