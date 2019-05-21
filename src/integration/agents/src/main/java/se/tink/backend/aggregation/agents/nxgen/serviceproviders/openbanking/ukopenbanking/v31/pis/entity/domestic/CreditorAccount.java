package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CreditorAccount {
    private String schemeName;
    private String identification;
    private String name;

    public CreditorAccount() {}

    public CreditorAccount(Creditor creditor) {
        this.schemeName =
                UkOpenBankingV31Constants.PAYMENT_SCHEME_TYPE_MAPPER
                        .translate(creditor.getAccountIdentifierType().toString())
                        .orElseThrow(() -> new IllegalStateException("SchemeName cannot be null!"));
        this.identification = creditor.getAccountNumber();
        this.name = creditor.getName();
    }

    public Creditor toCreditor() {
        return new Creditor(
                UkOpenBankingV31Constants.toAccountIdentifier(schemeName, identification), name);
    }
}
