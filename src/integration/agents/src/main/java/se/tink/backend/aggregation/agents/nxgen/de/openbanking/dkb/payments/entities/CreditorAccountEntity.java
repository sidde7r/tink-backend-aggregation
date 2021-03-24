package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorAccountEntity {
    private String iban;
    private String bban;

    @JsonCreator
    private CreditorAccountEntity(
            @JsonProperty("iban") String iban, @JsonProperty("bban") String bban) {
        this.iban = iban;
        this.bban = bban;
    }

    @JsonIgnore
    public static CreditorAccountEntity of(PaymentRequest paymentRequest) {
        String accountNumber = paymentRequest.getPayment().getCreditor().getAccountNumber();
        if (paymentRequest.getPayment().getCreditor().getAccountIdentifierType()
                == AccountIdentifierType.IBAN) {
            return new CreditorAccountEntity(accountNumber, null);
        } else {
            return new CreditorAccountEntity(null, accountNumber);
        }
    }

    @JsonIgnore
    public Creditor toTinkCreditor() {
        return iban != null
                ? new Creditor(new IbanIdentifier(iban))
                : new Creditor(
                        AccountIdentifier.create(
                                AccountIdentifierType.DK,
                                bban)); // should be DE but there isn't one for DE
    }
}
