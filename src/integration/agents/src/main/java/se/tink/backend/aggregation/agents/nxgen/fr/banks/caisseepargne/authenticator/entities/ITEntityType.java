package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ITEntityType {

    @JsonProperty("code")
    private String code;

    @JsonProperty("label")
    private String label;
}
