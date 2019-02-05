package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngCreditCardFetcher implements AccountFetcher<CreditCardAccount>, TransactionFetcher<CreditCardAccount> {
    private static final AggregationLogger AGGR_LOGGER = new AggregationLogger(IngCreditCardFetcher.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(IngCreditCardFetcher.class);

    private final IngHelper ingHelper;

    public IngCreditCardFetcher(IngHelper ingHelper) {
        this.ingHelper = ingHelper;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return ingHelper.retrieveLoginResponse()
                .map(this::logCreditCards)
                .orElseGet(this::logLoginResponseNotFound);
    }

    private Collection<CreditCardAccount> logCreditCards(LoginResponseEntity loginResponse) {
        // The requests list in the login response doesn't contain the credit card requests if the user
        // doesn't have a credit card.
        //
        // Trying to find the urls and if they are present we log them and the login
        // response. Hopefully we can then find the necessary requests and see their parameters, if there are any.
        try {
            Optional<String> creditCardsRequestUrl = loginResponse.findCreditCardsRequestUrl();
            if (creditCardsRequestUrl.isPresent()) {
                LOGGER.info("{}: {}", IngConstants.Logs.CREDITCARDS, creditCardsRequestUrl.get());

                String serializedLoginResponse = SerializationUtils.serializeToString(loginResponse);
                AGGR_LOGGER.infoExtraLong(serializedLoginResponse, IngConstants.Logs.LOGIN_RESPONSE);
            }

            Optional<String> creditCardTransactionsRequestUrl =
                    loginResponse.findCreditCardTransactionsRequestUrl();
            creditCardTransactionsRequestUrl.ifPresent(url ->
                    LOGGER.info("{}: {}", IngConstants.Logs.CREDITCARD_TRANSACTIONS, url));
        } catch (Exception e) {
            LOGGER.warn("Something went wrong when logging credit card endpoints");
        }

        return Collections.emptyList();
    }

    private Collection<CreditCardAccount> logLoginResponseNotFound() {
        LOGGER.warn("Could not fetch login response when trying to fetch creditcards");
        return Collections.emptyList();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
