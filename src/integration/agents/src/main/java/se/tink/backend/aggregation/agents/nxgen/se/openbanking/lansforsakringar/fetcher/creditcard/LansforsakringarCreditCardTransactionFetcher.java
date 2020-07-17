package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.creditcard;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.LansforsakringarTransactionFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class LansforsakringarCreditCardTransactionFetcher
        extends LansforsakringarTransactionFetcher<CreditCardAccount> {
    public LansforsakringarCreditCardTransactionFetcher(
            LansforsakringarApiClient apiClient, LocalDateTimeSource localDateTimeSource) {
        super(apiClient, localDateTimeSource);
    }
}
