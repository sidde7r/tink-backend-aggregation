package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Product;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Products;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public final class BawagPskTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BawagPskApiClient apiClient;
    private static final Logger logger = LoggerFactory.getLogger(BawagPskTransactionalAccountFetcher.class);

    public BawagPskTransactionalAccountFetcher(BawagPskApiClient bawagPskApiClient) {
        this.apiClient = bawagPskApiClient;
    }

    private GetAccountInformationListResponse fetchAccountInformationResponse(
            final Products products,
            final String sessionID,
            final String qid) {
        final GetAccountInformationListRequest request = new GetAccountInformationListRequest(
                sessionID,
                qid,
                products.getProductList().stream()
                        .map(Product::getProductID)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        final String requestString = request.getXml();
        return apiClient.getGetAccountInformationListResponse(requestString);
    }

    private Collection<TransactionalAccount> toTransactionalAccounts(
            final GetAccountInformationListResponse response,
            final Map<String, String> accountNosToProductCodes) {
        return response.toTransactionalAccounts(accountNosToProductCodes);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String errorMsg = "Could not find products in session storage needed for fetching accounts";
        final Products products = BawagPskUtils.xmlToEntity(
                apiClient.getFromStorage(BawagPskConstants.Storage.PRODUCTS.name())
                        .orElseThrow(() -> new IllegalStateException(errorMsg)),
                Products.class);
        final String serverSessionId = apiClient.getFromStorage(
                BawagPskConstants.Storage.SERVER_SESSION_ID.name()).orElseThrow(IllegalStateException::new);
        final String qid = apiClient.getFromStorage(
                BawagPskConstants.Storage.QID.name()).orElseThrow(IllegalStateException::new);

        final GetAccountInformationListResponse accountResponse = fetchAccountInformationResponse(
                products,
                serverSessionId,
                qid);

        for (final IbanIdentifier iban : accountResponse.getInvalidIbans()) {
            logger.warn("Retrieved invalid BIC/IBAN: {}/{}", iban.getBic(), iban.getIban());
        }

        final Map<String, String> accountNosToProductCodes = apiClient.getProductCodes();

        return toTransactionalAccounts(accountResponse, accountNosToProductCodes);
    }
}
