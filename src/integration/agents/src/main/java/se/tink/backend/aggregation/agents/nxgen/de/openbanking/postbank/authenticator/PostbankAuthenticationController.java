package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants.PollStatus;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n.Catalog;

public class PostbankAuthenticationController implements TypedAuthenticator {
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PostbankAuthenticator authenticator;

    public PostbankAuthenticationController(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            PostbankAuthenticator authenticator) {
        this.catalog = Preconditions.checkNotNull(catalog);
        this.supplementalInformationHelper =
                Preconditions.checkNotNull(supplementalInformationHelper);
        this.authenticator = authenticator;
    }

    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        validateReceivedCredentials(credentials);
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        AuthorisationResponse initValues = authenticator.init(username, password);

        List<ScaMethodEntity> scaMethods = getOnlySupportedScaMethods(initValues.getScaMethods());
        ScaMethodEntity chosenScaMethod = initValues.getChosenScaMethodEntity();

        // End process if auto-selected method is not supported
        if (scaMethods.isEmpty() || (chosenScaMethod != null && !isSupported(chosenScaMethod))) {
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        }

        // Select SCA method when user has more than one device.
        if (chosenScaMethod == null && CollectionUtils.isNotEmpty(scaMethods)) {
            chosenScaMethod = collectScaMethod(scaMethods);
            initValues =
                    authenticator.selectScaMethod(
                            chosenScaMethod.getAuthenticationMethodId(),
                            username,
                            initValues.getLinksEntity().getScaStatusEntity().getHref());
        }

        if (chosenScaMethod != null && initValues.getChallengeDataEntity() != null) {
            // SMS_OTP or CHIP_OTP is selected, embedded approach till the end
            initValues =
                    authenticator.authoriseWithOtp(
                            collectOtp(initValues),
                            username,
                            initValues.getLinksEntity().getAuthoriseTransactionEntity().getHref());

            switch (initValues.getScaStatus()) {
                case PollStatus.FINALISED:
                    break;
                case PollStatus.FAILED:
                    throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
                default:
                    throw LoginError.NOT_SUPPORTED.exception();
            }
        } else {
            // PUSH_OTP is selected, need to switch to decoupled
            poll(username, initValues.getLinksEntity().getScaStatusEntity().getHref());
        }
    }

    private void validateReceivedCredentials(Credentials credentials) {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        if (Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME))
                || Strings.isNullOrEmpty(credentials.getField(Field.Key.PASSWORD))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private List<ScaMethodEntity> getOnlySupportedScaMethods(List<ScaMethodEntity> scaMethods) {
        return scaMethods.stream().filter(this::isSupported).collect(Collectors.toList());
    }

    private boolean isSupported(ScaMethodEntity scaMethod) {
        return !scaMethod.getAuthenticationType().equalsIgnoreCase("CHIP_OTP");
    }

    private ScaMethodEntity collectScaMethod(List<ScaMethodEntity> scaMethods) {
        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(
                        CommonFields.Selection.build(
                                catalog,
                                scaMethods.stream()
                                        .map(ScaMethodEntity::getName)
                                        .collect(Collectors.toList())));
        int index =
                Integer.parseInt(supplementalInformation.get(CommonFields.Selection.getFieldKey()))
                        - 1;
        return scaMethods.get(index);
    }

    private String collectOtp(AuthorisationResponse authResponse) {
        String scaMethodName = authResponse.getChosenScaMethodEntity().getName();
        return supplementalInformationHelper
                .askSupplementalInformation(GermanFields.Tan.build(catalog, scaMethodName))
                .get(GermanFields.Tan.getFieldKey());
    }

    private void poll(String username, String url) throws ThirdPartyAppException {
        for (int i = 0; i < PollStatus.MAX_POLL_ATTEMPTS; i++) {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);

            AuthorisationResponse response = authenticator.checkAuthorisationStatus(username, url);
            switch (response.getScaStatus()) {
                case PollStatus.FINALISED:
                    return;
                case PollStatus.FAILED:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    break;
            }
        }
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }
}
