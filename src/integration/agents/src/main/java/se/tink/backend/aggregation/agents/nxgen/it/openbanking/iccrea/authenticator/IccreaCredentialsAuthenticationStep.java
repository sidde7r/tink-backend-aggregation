package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiCredentialsAuthenticatable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.CredentialDetailDefinition;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.CredentialDetailValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.PsuCredentialsDefinition;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.PsuCredentialsValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdatePsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class IccreaCredentialsAuthenticationStep {

    private final CbiGlobeAuthApiClient authApiClient;
    private final URL baseUrlForOperation;
    private final String username;
    private final String password;

    public IccreaCredentialsAuthenticationStep(
            CbiGlobeAuthApiClient authApiClient, Credentials credentials, URL baseUrlForOperation) {
        this.authApiClient = authApiClient;
        this.baseUrlForOperation = baseUrlForOperation;
        this.username = credentials.getField(Key.USERNAME);
        this.password = credentials.getField(Key.PASSWORD);
    }

    public <T> T authenticate(
            CbiCredentialsAuthenticatable authenticatable, Class<T> responseClass) {
        validateStoredCredentials();

        UpdatePsuCredentialsRequest request = buildRequest(authenticatable.getPsuCredentials());

        return authApiClient.updatePsuCredentials(
                baseUrlForOperation.concat(authenticatable.getUpdatePsuAuthenticationLink()),
                request,
                responseClass);
    }

    private void validateStoredCredentials() {
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private UpdatePsuCredentialsRequest buildRequest(
            PsuCredentialsDefinition psuCredentialsDefinition) {
        List<CredentialDetailDefinition> credentialsDetails =
                Optional.ofNullable(psuCredentialsDefinition)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Psu credentials must not be null"))
                        .getCredentialsDetails();

        String usernameCredentialsId =
                findCredentialsIdByPredicate(
                        credentialsDetails,
                        credentialDetailDefinition -> !credentialDetailDefinition.isSecret());

        String passwordCredentialsId =
                findCredentialsIdByPredicate(
                        credentialsDetails, CredentialDetailDefinition::isSecret);

        return new UpdatePsuCredentialsRequest(
                new PsuCredentialsValues(
                        psuCredentialsDefinition.getAspspProductCode(),
                        Arrays.asList(
                                new CredentialDetailValue(usernameCredentialsId, username),
                                new CredentialDetailValue(passwordCredentialsId, password))));
    }

    private String findCredentialsIdByPredicate(
            List<CredentialDetailDefinition> credentialsDetails,
            Predicate<CredentialDetailDefinition> predicate) {
        return credentialsDetails.stream()
                .filter(predicate)
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "No credentials detail matching predicate"))
                .getCredentialDetailId();
    }
}
