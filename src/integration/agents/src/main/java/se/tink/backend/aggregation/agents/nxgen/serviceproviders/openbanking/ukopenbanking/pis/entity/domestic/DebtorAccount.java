package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingV31PaymentConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DebtorAccount {
    private String schemeName;
    private String identification;

    // Used in serialization unit tests
    protected DebtorAccount() {}

    public DebtorAccount(Debtor debtor) {
        this.schemeName =
                UkOpenBankingV31PaymentConstants.PAYMENT_SCHEME_TYPE_MAPPER
                        .translate(debtor.getAccountIdentifierType())
                        .orElseThrow(() -> new IllegalStateException("SchemeName cannot be null!"));
        this.identification = debtor.getAccountNumber();
    }

    public Debtor toDebtor() {
        return new Debtor(
                UkOpenBankingV31PaymentConstants.toAccountIdentifier(schemeName, identification));
    }
}
