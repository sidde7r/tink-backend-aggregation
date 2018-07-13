package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateRecipientRequest {

    private String toAccountNumber;
    private String toClearingNumber;

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public String getToClearingNumber() {
        return toClearingNumber;
    }

    public void setToClearingNumber(String toClearingNumber) {
        this.toClearingNumber = toClearingNumber;
    }

    public static ValidateRecipientRequest create(AccountIdentifier destinationIdentifier) {
        ValidateRecipientRequest request = new ValidateRecipientRequest();

        if (destinationIdentifier.is(AccountIdentifier.Type.SE)) {
            SwedishIdentifier swedishIdentifier = destinationIdentifier.to(SwedishIdentifier.class);

            request.setToClearingNumber(swedishIdentifier.getClearingNumber());
            request.setToAccountNumber(swedishIdentifier.getAccountNumber());
        } else if (destinationIdentifier.is(AccountIdentifier.Type.SE_SHB_INTERNAL)) {
            request.setToAccountNumber(destinationIdentifier.getIdentifier(new DefaultAccountIdentifierFormatter()));
            // Set isHandeslbanken true ?
        }
        return request;
    }
}
