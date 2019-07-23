package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsAccountReferenceEntity {

    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBban() {
        return bban;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public static SibsAccountReferenceEntity of(
            Supplier<AccountIdentifier.Type> accountTypeSupplier,
            Supplier<String> accountNumberSupplier) {
        SibsAccountReferenceEntity are = new SibsAccountReferenceEntity();
        switch (accountTypeSupplier.get()) {
            case IBAN:
                are.setIban(accountNumberSupplier.get());
                break;
            case PAYMENT_CARD_NUMBER:
                are.setPan(accountNumberSupplier.get());
                break;
            case PAYM_PHONE_NUMBER:
                are.setMsisdn(accountNumberSupplier.get());
                break;
            default:
                throw new RuntimeException("Not supported payment type");
        }

        return are;
    }

    public Creditor toTinkCreditor() throws PaymentException {
        Creditor creditor = null;
        if (StringUtils.isNotEmpty(iban)) {
            creditor = new Creditor(AccountIdentifier.create(Type.IBAN, iban));
        } else if (StringUtils.isNotEmpty(msisdn)) {
            creditor = new Creditor(AccountIdentifier.create(Type.PAYM_PHONE_NUMBER, iban));
        } else if (StringUtils.isNotEmpty(pan)) {
            creditor = new Creditor(AccountIdentifier.create(Type.PAYMENT_CARD_NUMBER, iban));
        } else {
            throw new PaymentException("Unsupported payment type returned");
        }
        return creditor;
    }

    public Debtor toTinkDebtor() throws PaymentException {
        Debtor debtor = null;
        if (StringUtils.isNotEmpty(iban)) {
            debtor = new Debtor(AccountIdentifier.create(Type.IBAN, iban));
        } else if (StringUtils.isNotEmpty(msisdn)) {
            debtor = new Debtor(AccountIdentifier.create(Type.PAYM_PHONE_NUMBER, iban));
        } else if (StringUtils.isNotEmpty(pan)) {
            debtor = new Debtor(AccountIdentifier.create(Type.PAYMENT_CARD_NUMBER, iban));
        } else {
            throw new PaymentException("Unsupported payment type returned");
        }
        return debtor;
    }
}
