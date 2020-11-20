package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CreditorAccount {
    private String schemeName;
    private String identification;
    private String name;

    // Used in serialization unit tests
    protected CreditorAccount() {}

    public CreditorAccount(Creditor creditor) {
        this.schemeName =
                UkOpenBankingV31PaymentConstants.PAYMENT_SCHEME_TYPE_MAPPER
                        .translate(creditor.getAccountIdentifierType())
                        .orElseThrow(() -> new IllegalStateException("SchemeName cannot be null!"));
        this.identification = creditor.getAccountNumber();
        this.name =
                Strings.isNullOrEmpty(creditor.getName())
                        ? UkOpenBankingV31PaymentConstants.FormValues.PAYMENT_CREDITOR_DEFAULT_NAME
                        : creditor.getName();
    }

    public Creditor toCreditor() {
        return new Creditor(
                UkOpenBankingV31PaymentConstants.toAccountIdentifier(schemeName, identification),
                name);
    }
}
