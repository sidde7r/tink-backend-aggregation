package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.Saml2PostDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.StepDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.MembershipType;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public abstract class BpceStorage {

    private static final String BANK_ID = "BANK_ID";
    private static final String AUTH_TRANSACTION_PATH = "AUTH_TRANSACTION_PATH";
    private static final String CREDENTIALS_RESPONSE = "CREDENTIALS_RESPONSE";
    private static final String SAML_POST_ACTION = "SAML_POST_ACTION";
    private static final String OAUTH2_TOKEN = "OAUTH2_TOKEN";
    private static final String COULD_AUTO_AUTHENTICATE = "COULD_AUTO_AUTHENTICATE";
    private static final String TERM_ID = "TERM_ID";
    private static final String MEMBERSHIP_TYPE = "MEMBERSHIP_TYPE";

    protected final PersistentStorage persistentStorage;

    public String getBankId() {
        return getOrThrowException(BANK_ID, String.class);
    }

    public void storeBankId(String bankId) {
        persistentStorage.put(BANK_ID, bankId);
    }

    public String getAuthTransactionPath() {
        return getOrThrowException(AUTH_TRANSACTION_PATH, String.class);
    }

    public void storeAuthTransactionPath(String authTransactionPath) {
        persistentStorage.put(AUTH_TRANSACTION_PATH, authTransactionPath);
    }

    public StepDto getCredentialsResponse() {
        return getOrThrowException(CREDENTIALS_RESPONSE, StepDto.class);
    }

    public void storeCredentialsResponse(StepDto stepDto) {
        persistentStorage.put(CREDENTIALS_RESPONSE, stepDto);
    }

    public Saml2PostDto getSamlPostAction() {
        return getOrThrowException(SAML_POST_ACTION, Saml2PostDto.class);
    }

    public void storeSamlPostAction(Saml2PostDto saml2PostDto) {
        persistentStorage.put(SAML_POST_ACTION, saml2PostDto);
    }

    public Optional<OAuth2Token> getOAuth2Token() {
        return persistentStorage.get(OAUTH2_TOKEN, OAuth2Token.class);
    }

    public void storeOAuth2Token(OAuth2Token oAuth2Token) {
        persistentStorage.put(OAUTH2_TOKEN, oAuth2Token);
    }

    public Optional<Boolean> getCouldAutoAuthenticate() {
        return persistentStorage.get(COULD_AUTO_AUTHENTICATE, Boolean.class);
    }

    public void storeCouldAutoAuthenticate(boolean couldAutoAuthenticate) {
        persistentStorage.put(COULD_AUTO_AUTHENTICATE, couldAutoAuthenticate);
    }

    public String getTermId() {
        return getOrThrowException(TERM_ID, String.class);
    }

    public void storeTermId(String termId) {
        persistentStorage.put(TERM_ID, termId);
    }

    public MembershipType getMembershipType() {
        return getOrThrowException(MEMBERSHIP_TYPE, MembershipType.class);
    }

    public void storeMembershipType(MembershipType membershipType) {
        persistentStorage.put(MEMBERSHIP_TYPE, membershipType);
    }

    protected <T> T getOrThrowException(String name, Class<T> clazz) {
        return persistentStorage
                .get(name, clazz)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        name + " has not been found in the storage."));
    }
}
