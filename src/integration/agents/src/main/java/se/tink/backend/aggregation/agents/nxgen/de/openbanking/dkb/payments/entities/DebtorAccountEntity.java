package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class DebtorAccountEntity {
    private String iban;
    private String bban;

    @JsonIgnore
    private DebtorAccountEntity(
            @JsonProperty("iban") String iban, @JsonProperty("bban") String bban) {
        this.iban = iban;
        this.bban = bban;
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        return iban != null
                ? new Debtor(new IbanIdentifier(iban))
                : new Debtor(
                        AccountIdentifier.create(
                                AccountIdentifierType.DK,
                                bban)); // should be DE, but there isn't one for DE
    }

    @JsonIgnore
    public static DebtorAccountEntity of(PaymentRequest paymentRequest) {
        String accountNumber = paymentRequest.getPayment().getDebtor().getAccountNumber();
        if (paymentRequest.getPayment().getDebtor().getAccountIdentifierType()
                == AccountIdentifierType.IBAN) {
            return new DebtorAccountEntity(accountNumber, null);
        } else {
            return new DebtorAccountEntity(null, accountNumber);
        }
    }
}
