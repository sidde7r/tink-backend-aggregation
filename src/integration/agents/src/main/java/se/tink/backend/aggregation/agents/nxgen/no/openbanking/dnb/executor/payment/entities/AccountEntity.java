package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountEntity {
    private String iban;
    private String bban;

    public AccountEntity() {}

    public AccountEntity(Type accountType, String accountNumber) {
        switch (accountType) {
            case IBAN:
                this.iban = accountNumber;
                break;
            case NO:
                this.bban = accountNumber;
                break;
            default:
                throw new IllegalStateException("Unknowm account type: " + accountType.toString());
        }
    }

    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        return new AccountEntity(
                payment.getCreditor().getAccountIdentifierType(),
                payment.getCreditor().getAccountNumber());
    }

    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        return new AccountEntity(
                payment.getDebtor().getAccountIdentifierType(),
                payment.getDebtor().getAccountNumber());
    }

    @JsonIgnore
    public String getAccountNumber() {
        return Strings.isNullOrEmpty(bban) ? iban : bban;
    }
}
