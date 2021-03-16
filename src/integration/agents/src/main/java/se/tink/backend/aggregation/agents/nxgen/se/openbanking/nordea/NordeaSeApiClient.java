package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.DecoupledAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Urls;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    public NordeaSeApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner,
            String providerName) {
        super(client, persistentStorage, qsealcSigner, providerName, false);
    }

    public DecoupledAuthenticationResponse authenticateDecoupled(String ssn) {
        String requestBody =
                SerializationUtils.serializeToString(new DecoupledAuthenticationRequest(ssn));
        return createRequest(Urls.DECOUPLED_AUTHENTICATION, HttpMethod.POST, requestBody)
                .body(requestBody, MediaType.APPLICATION_JSON)
                .post(DecoupledAuthenticationResponse.class);
    }

    public DecoupledAuthenticationResponse getDecoupledAuthenticationStatus(String sessionId) {
        return createRequest(
                        Urls.DECOUPLED_AUTHENTICATION.concatWithSeparator(sessionId),
                        HttpMethod.GET,
                        null)
                .get(DecoupledAuthenticationResponse.class);
    }

    public void authorizePsuAccounts(String code) {
        String requestBody =
                SerializationUtils.serializeToString(
                        DecoupledAuthorizationRequest.builder()
                                .code(code)
                                .scope(
                                        ImmutableList.copyOf(
                                                Stream.of(getScopes().split(","))
                                                        // We get 400 for CARD scopes
                                                        .filter(s -> s.matches("^[AP].*"))
                                                        .collect(Collectors.toList())))
                                .build());
        createRequest(Urls.DECOUPLED_AUTHORIZATION, HttpMethod.POST, requestBody)
                .body(requestBody, MediaType.APPLICATION_JSON)
                .post();
    }
}
