package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class DebtorAccount {
    private String schemeName;
    private String identification;

    public DebtorAccount() {}

    public DebtorAccount(Debtor debtor) {
        this.schemeName =
                UkOpenBankingV31Constants.PAYMENT_SCHEME_TYPE_MAPPER
                        .translate(debtor.getAccountIdentifierType().toString())
                        .get();
        this.identification = debtor.getAccountNumber();
    }

    public Debtor toDebtor() {
        return new Debtor(
                UkOpenBankingV31Constants.toAccountIdentifier(schemeName, identification));
    }
}
