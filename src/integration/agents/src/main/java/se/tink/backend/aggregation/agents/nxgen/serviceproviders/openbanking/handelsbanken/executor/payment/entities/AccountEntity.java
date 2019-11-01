package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.iban4j.IbanUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountEntity {

    @JsonProperty("value")
    private String accountNumber;

    private String accountType;
    private String currency;
    private String country;
    private String text;

    public AccountEntity(String accountNumber, String accountType, String currency) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currency = currency;
        this.country = mapAccountTypeToCountry();
    }

    public AccountEntity(String accountNumber, String accountType, String text, String currency) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.text = text;
        this.currency = currency;
        this.country = mapAccountTypeToCountry();
    }

    @JsonIgnore
    private String mapAccountTypeToCountry() {
        if (accountType.equalsIgnoreCase(Type.SE.toString())
                || accountType.equalsIgnoreCase(Type.BE.toString())
                || accountType.equalsIgnoreCase(Type.SE_PG.toString())
                || accountType.equalsIgnoreCase(Type.BBAN.toString())) {
            return Market.SWEDEN;
        } else if (accountType.equalsIgnoreCase(Type.IBAN.toString())) {
            return IbanUtil.getCountryCode(accountNumber);
        } else {
            throw new IllegalStateException("Account type couldn't be mapped to country.");
        }
    }

    @JsonIgnore
    public static AccountEntity creditorOf(PaymentRequest paymentRequest) {
        Creditor creditor = paymentRequest.getPayment().getCreditor();
        return new AccountEntity(
                creditor.getAccountNumber(),
                creditor.getAccountIdentifierType().toString().toUpperCase(),
                paymentRequest.getPayment().getCurrency());
    }

    @JsonIgnore
    public static AccountEntity debtorOf(PaymentRequest paymentRequest) {
        Debtor debtor = paymentRequest.getPayment().getDebtor();

        return new AccountEntity(
                debtor.getAccountNumber(),
                debtor.getAccountIdentifierType().toString().toUpperCase(),
                paymentRequest.getPayment().getReference().getValue(),
                paymentRequest.getPayment().getCurrency());
    }
}
