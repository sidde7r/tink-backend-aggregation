package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class BawagPskTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BawagPskApiClient bawagPskApiClient;
    private static final Logger logger = LoggerFactory.getLogger(BawagPskTransactionalAccountFetcher.class);

    public BawagPskTransactionalAccountFetcher(BawagPskApiClient bawagPskApiClient) {
        this.bawagPskApiClient = bawagPskApiClient;
    }

    private GetAccountInformationListResponse fetchAccountInformationResponse(final List<ProductID> productIDs) {
        final String serverSessionId = bawagPskApiClient.getFromStorage(
                BawagPskConstants.Storage.SERVER_SESSION_ID.name()).orElseThrow(IllegalStateException::new);
        final String qid = bawagPskApiClient.getFromStorage(
                BawagPskConstants.Storage.QID.name()).orElseThrow(IllegalStateException::new);

        final GetAccountInformationListRequest request = new GetAccountInformationListRequest(
                serverSessionId,
                qid,
                productIDs
        );

        final String requestString;
        requestString = request.getXml();
        return bawagPskApiClient.getGetAccountInformationListResponse(requestString);
    }

    /**
     * The account data is scattered across two responses:
     * @param loginResponse which contains all needed data for all accounts, except balance
     * @param accountResponse which contains balance for all accounts
     * @return A collection of TransactionAccount instances built from these responses
     */
    private static Collection<TransactionalAccount> toTransactionalAccounts(
            final LoginResponse loginResponse,
            final GetAccountInformationListResponse accountResponse) {
        return loginResponse.toTransactionalAccounts(accountResponse.getAccountNumberToBalanceMap());
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final LoginResponse loginResponse = bawagPskApiClient.getLoginResponse()
                .orElseThrow(() -> new IllegalStateException("Login response not found."));

        final GetAccountInformationListResponse accountResponse = fetchAccountInformationResponse(
                loginResponse.getProductIdList()
        );

        for (IbanIdentifier iban : loginResponse.getInvalidIbans()) {
            logger.warn("Retrieved invalid BIC/IBAN: {}/{}", iban.getBic(), iban.getIban());
        }

        return toTransactionalAccounts(loginResponse, accountResponse);
    }
}
