package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
public class LabelMonetaryAccountEntity {

    private String value;
    private String type;
    private String name;

    @JsonInclude(Include.NON_NULL)
    private String iban;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("display_name")
    private String displayName;

    public LabelMonetaryAccountEntity() {}

    public LabelMonetaryAccountEntity(String ibanValue, String name) {
        this.value = ibanValue;
        this.type = "IBAN";
        this.name = name;
    }

    public static LabelMonetaryAccountEntity of(Creditor creditor) throws PaymentException {
        if (creditor.getAccountIdentifierType() != Type.IBAN) {
            throw new PaymentException(
                    "Unsupported creditor account identifier type : "
                            + creditor.getAccountIdentifierType().toString());
        }
        return new LabelMonetaryAccountEntity(creditor.getAccountNumber(), creditor.getName());
    }

    public String getIban() {
        return iban;
    }

    public String getDisplayName() {
        return displayName;
    }
}
