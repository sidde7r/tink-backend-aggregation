package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc.BankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.utils.signicat.SignicatParsingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class EnterCardAuthenticator implements BankIdAuthenticator<BankIdInitResponse> {

    private final EnterCardApiClient apiClient;
    private final EnterCardConfiguration config;

    public EnterCardAuthenticator(EnterCardApiClient apiClient, EnterCardConfiguration config) {
        this.apiClient = apiClient;
        this.config = config;
    }

    @Override
    public BankIdInitResponse init(String ssn) throws BankIdException {
        String bankIdInitPage = apiClient.fetchBankIdInitPage();
        String signicatServiceUrl = SignicatParsingUtils.parseBankIdServiceUrl(bankIdInitPage);

        BankIdInitResponse bankIdResponse = apiClient.orderBankId(signicatServiceUrl, ssn);

        if (bankIdResponse.isError()) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }

        return bankIdResponse;
    }

    @Override
    public BankIdStatus collect(BankIdInitResponse reference)
            throws AuthenticationException, AuthorizationException {
        try {
            final BankIdCollectRequest collectRequest =
                    new BankIdCollectRequest(reference.getOrderRef());
            final BankIdCollectResponse collectResponse =
                    apiClient.collectBankId(reference.getCollectUrl(), collectRequest);

            final BankIdStatus bankIdStatus = collectResponse.getBankIdStatus();

            if (bankIdStatus != BankIdStatus.DONE) return bankIdStatus;

            final String completeResponse =
                    apiClient.completeBankId(collectResponse.getCompleteUrl(), collectRequest);

            Document document = Jsoup.parse(completeResponse);
            Element formElement = document.getElementById(EnterCardConstants.RESPONSE_FORM_ID);
            apiClient.auth(formElement);
            apiClient.roundTripTest(config.getServiceHost());

            return bankIdStatus;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw BankIdError.INTERRUPTED.exception();
            }
            throw e;
        }
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }
}
