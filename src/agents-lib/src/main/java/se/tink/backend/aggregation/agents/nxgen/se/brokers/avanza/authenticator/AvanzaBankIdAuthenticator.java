package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class AvanzaBankIdAuthenticator implements BankIdAuthenticator<BankIdInitResponse> {
    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;

    public AvanzaBankIdAuthenticator(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public BankIdInitResponse init(String ssn) throws BankIdException, AuthorizationException {
        final BankIdInitRequest request = new BankIdInitRequest(ssn);
        final BankIdInitResponse response = apiClient.initBankId(request);

        return response;
    }

    @Override
    public BankIdStatus collect(BankIdInitResponse reference)
            throws AuthenticationException, AuthorizationException {
        final String transactionId = reference.getTransactionId();
        final BankIdCollectResponse response = apiClient.collectBankId(transactionId);
        final BankIdStatus status = response.getBankIdStatus();

        if (status == BankIdStatus.DONE) {
            complete(response)
                    .forEach(
                            r -> {
                                final String session = r.getAuthenticationSession();
                                final String token = r.getSecurityToken();
                                authSessionStorage.put(session, token);
                            });
            temporaryStorage.put(StorageKeys.HOLDER_NAME, response.getName());
        }

        return status;
    }

    public List<BankIdCompleteResponse> complete(BankIdCollectResponse reference)
            throws AuthenticationException, AuthorizationException {
        final String transactionId = reference.getTransactionId();
        final List<BankIdCompleteResponse> collect =
                reference
                        .getLogins()
                        .stream()
                        .map(l -> apiClient.completeBankId(transactionId, l.getCustomerId()))
                        .map(injectSecurityToken())
                        .collect(Collectors.toList());

        return collect;
    }

    private Function<HttpResponse, BankIdCompleteResponse> injectSecurityToken() {
        return r -> {
            final String token = r.getHeaders().getFirst(HeaderKeys.SECURITY_TOKEN);
            final BankIdCompleteResponse response =
                    r.getBody(BankIdCompleteResponse.class).withSecurityToken(token);

            return response;
        };
    }
}
