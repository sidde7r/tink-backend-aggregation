package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonObject
public class AccountConsent {
    private String iban;
    private String maskedPan;
}
