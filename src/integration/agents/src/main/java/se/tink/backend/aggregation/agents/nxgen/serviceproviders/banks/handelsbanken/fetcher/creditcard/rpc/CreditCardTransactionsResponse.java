package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.PagableResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class CreditCardTransactionsResponse<CreditCard extends HandelsbankenCreditCard>
        extends BaseResponse implements PagableResponse {
    public abstract List<CreditCardTransaction> tinkTransactions(CreditCard creditcard,
            CreditCardAccount account);

    public abstract List<CreditCardTransaction> tinkTransactions(CreditCardAccount account);

    @Override
    public Optional<URL> getPaginationKey() {
        return searchLink(HandelsbankenConstants.URLS.Links.CARD_MORE_TRANSACTIONS);
    }
}
