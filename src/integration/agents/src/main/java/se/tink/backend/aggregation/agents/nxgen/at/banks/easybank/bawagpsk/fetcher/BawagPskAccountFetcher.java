package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher;

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
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.pair.Pair;

/**
 * Common class for fetching transactional accounts, credit cards, loans, since they are identical
 * API-wise
 */
public final class BawagPskAccountFetcher {

    private final BawagPskApiClient apiClient;
    private static final Logger logger = LoggerFactory.getLogger(BawagPskAccountFetcher.class);

    public BawagPskAccountFetcher(final BawagPskApiClient bawagPskApiClient) {
        this.apiClient = bawagPskApiClient;
    }

    private GetAccountInformationListResponse fetchAccountInformationResponse(
            final Products products, final String sessionID, final String qid) {
        final GetAccountInformationListRequest request =
                new GetAccountInformationListRequest(
                        sessionID,
                        qid,
                        products.getProductList().stream()
                                .map(Product::getProductID)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));

        final String requestString = request.getXml();

        // If the accounts were already fetched, get them from storage
        return apiClient.getGetAccountInformationListResponse(requestString);
    }

    public Pair<GetAccountInformationListResponse, Map<String, String>> fetchAccountData() {
        final String errorMsg =
                "Could not find products in session storage needed for fetching accounts";
        final Products products =
                BawagPskUtils.xmlToEntity(
                        apiClient
                                .getFromSessionStorage(BawagPskConstants.Storage.PRODUCTS.name())
                                .orElseThrow(() -> new IllegalStateException(errorMsg)),
                        Products.class);
        final String serverSessionId =
                apiClient
                        .getFromSessionStorage(BawagPskConstants.Storage.SERVER_SESSION_ID.name())
                        .orElseThrow(IllegalStateException::new);
        final String qid =
                apiClient
                        .getFromSessionStorage(BawagPskConstants.Storage.QID.name())
                        .orElseThrow(IllegalStateException::new);

        final GetAccountInformationListResponse accountResponse =
                fetchAccountInformationResponse(products, serverSessionId, qid);

        for (final IbanIdentifier iban : accountResponse.getInvalidIbans()) {
            logger.warn("Retrieved invalid BIC/IBAN: {}/{}", iban.getBic(), iban.getIban());
        }

        final Map<String, String> accountNosToProductCodes = apiClient.getProductCodes();

        return new Pair<>(accountResponse, accountNosToProductCodes);
    }
}
