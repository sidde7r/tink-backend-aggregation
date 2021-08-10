package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class AccountReferenceEntity {
    private String iban;
    private String maskedPan;
}
