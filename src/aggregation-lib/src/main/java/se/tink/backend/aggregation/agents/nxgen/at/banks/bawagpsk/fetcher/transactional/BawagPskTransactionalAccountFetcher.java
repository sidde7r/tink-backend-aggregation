package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.fetcher.transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc.GetAccountInformationListRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc.LoginResponse;
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
        try {
            requestString = request.getXml();
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to marshal JAXB ", e);
        }
        return bawagPskApiClient.getGetAccountInformationListResponse(requestString);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final LoginResponse loginResponse = bawagPskApiClient.getLoginResponse()
                .orElseThrow(() -> new IllegalStateException("Login response not found."));

        final GetAccountInformationListResponse accountResponse = fetchAccountInformationResponse(
                loginResponse.getProductIdList()
        );

        for (IbanIdentifier iban : loginResponse.getInvalidIbans()) {
            logger.warn(String.format("Retrieved invalid BIC/IBAN: %s/%s", iban.getBic(), iban.getIban()));
        }

        return loginResponse.getProductList().stream()
                .map(product -> product.toTransactionalAccount(
                        accountResponse.getBalanceFromAccountNumber(product.getAccountNumber())
                ))
                .collect(Collectors.toSet());
    }
}
