package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.updatepayment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

@JsonObject
public class PaymentDetailsResponse {
    @JsonIgnore
    private static final AccountIdentifierFormatter DEFAULT_FORMAT =
            new DefaultAccountIdentifierFormatter();

    private ConfirmedTransactionEntity transaction;
    private TransactionOptionEntity editTransactionOption;

    public ConfirmedTransactionEntity getTransaction() {
        return transaction;
    }

    public TransactionOptionEntity getEditTransactionOption() {
        return editTransactionOption;
    }
}
