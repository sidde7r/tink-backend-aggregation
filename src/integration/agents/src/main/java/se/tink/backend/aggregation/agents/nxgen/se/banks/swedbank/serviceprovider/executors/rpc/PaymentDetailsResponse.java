package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

@JsonObject
@Getter
public class PaymentDetailsResponse {
    @JsonIgnore
    private static final AccountIdentifierFormatter DEFAULT_FORMAT =
            new DefaultAccountIdentifierFormatter();

    private ConfirmedTransactionEntity transaction;
    private TransactionOptionEntity editTransactionOption;
}
