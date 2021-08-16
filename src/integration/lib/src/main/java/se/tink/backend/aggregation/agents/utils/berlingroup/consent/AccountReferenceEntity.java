package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@JsonInclude(Include.NON_NULL)
public class AccountReferenceEntity {
    private String iban;
    private String maskedPan;
    private String currency;
}
