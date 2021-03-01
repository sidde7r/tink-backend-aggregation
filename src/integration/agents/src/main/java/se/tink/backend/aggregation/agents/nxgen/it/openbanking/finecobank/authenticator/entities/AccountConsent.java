package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import com.google.common.base.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountConsent {

    private String iban;
    private String maskedPan;
    private String currency;
    private String pan;
    private String msisdn;

    // Used in serialization
    private AccountConsent() {}

    public AccountConsent(String iban, String maskedPan) {
        this.iban = iban;
        this.maskedPan = maskedPan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountConsent)) {
            return false;
        }
        AccountConsent that = (AccountConsent) o;
        return Objects.equal(iban, that.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(iban);
    }
}
