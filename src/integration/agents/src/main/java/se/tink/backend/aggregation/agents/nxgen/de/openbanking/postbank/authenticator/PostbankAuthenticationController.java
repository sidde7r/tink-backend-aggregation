package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants.PollStatus;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ChallengeData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PostbankAuthenticationController implements TypedAuthenticator {
    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode:\\s(\\d+)");

    private static final String CHIP_OTP = "CHIP_OTP";
    private static final String SMS_OTP = "SMS_OTP";
    private static final String PUSH_OTP = "PUSH_OTP";
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SupplementalRequester supplementalRequester;
    private final PostbankAuthenticator authenticator;

    public PostbankAuthenticationController(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            SupplementalRequester supplementalRequester,
            PostbankAuthenticator authenticator) {
        this.catalog = Preconditions.checkNotNull(catalog);
        this.supplementalInformationHelper =
                Preconditions.checkNotNull(supplementalInformationHelper);
        this.supplementalRequester = supplementalRequester;
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

        List<ScaMethod> scaMethods = getOnlySupportedScaMethods(initValues.getScaMethods());
        ScaMethod chosenScaMethod = initValues.getChosenScaMethod();

        // End process if auto-selected method is not supported
        if (scaMethods.isEmpty() || (chosenScaMethod != null && !isSupported(chosenScaMethod))) {
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        }

        // Select SCA method when user has more than one device.
        if (chosenScaMethod == null) {
            chosenScaMethod = collectScaMethod(scaMethods);
            initValues =
                    authenticator.selectScaMethod(
                            chosenScaMethod.getAuthenticationMethodId(),
                            username,
                            initValues.getLinks().getScaStatus().getHref());
        }

        authenticateUsingChosenScaMethod(credentials, username, initValues, chosenScaMethod);
        authenticator.storeConsentDetails();
    }

    private void authenticateUsingChosenScaMethod(
            Credentials credentials,
            String username,
            AuthorisationResponse initValues,
            ScaMethod chosenScaMethod) {
        switch (chosenScaMethod.getAuthenticationType().toUpperCase()) {
            case PUSH_OTP:
                finishWithAcceptingPush(initValues, username, credentials);
                break;
            case SMS_OTP:
            case CHIP_OTP:
                finishWithOtpAuthorisation(initValues, username);
                break;
            default:
                throw LoginError.NOT_SUPPORTED.exception();
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

    private List<ScaMethod> getOnlySupportedScaMethods(List<ScaMethod> scaMethods) {
        return scaMethods.stream().filter(this::isSupported).collect(Collectors.toList());
    }

    private boolean isSupported(ScaMethod scaMethod) {
        return !scaMethod.getAuthenticationMethodId().toLowerCase().contains("optical");
    }

    private ScaMethod collectScaMethod(List<ScaMethod> scaMethods) {
        if (scaMethods.size() == 1) {
            return scaMethods.get(0);
        }

        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(
                        CommonFields.Selection.build(
                                catalog,
                                scaMethods.stream()
                                        .map(ScaMethod::getName)
                                        .collect(Collectors.toList())));
        int index =
                Integer.parseInt(supplementalInformation.get(CommonFields.Selection.getFieldKey()))
                        - 1;
        return scaMethods.get(index);
    }

    private void finishWithAcceptingPush(
            AuthorisationResponse previousResponse, String username, Credentials credentials) {
        showInfo(previousResponse.getChosenScaMethod().getName(), credentials);
        poll(username, previousResponse.getLinks().getScaStatus().getHref());
    }

    private void showInfo(String deviceName, Credentials credentials) {
        Field informationField =
                CommonFields.Information.build(
                        PostbankConstants.InfoScreen.FIELD_KEY,
                        catalog.getString(PostbankConstants.InfoScreen.DESCRIPTION),
                        deviceName,
                        catalog.getString(PostbankConstants.InfoScreen.HELP_TEXT));

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(informationField)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        supplementalRequester.requestSupplementalInformation(credentials, true);
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

    private void finishWithOtpAuthorisation(
            AuthorisationResponse previousResponse, String username) {
        AuthorisationResponse response =
                authenticator.authoriseWithOtp(
                        collectOtp(previousResponse),
                        username,
                        previousResponse.getLinks().getAuthoriseTransaction().getHref());

        switch (response.getScaStatus()) {
            case PollStatus.FINALISED:
                break;
            case PollStatus.FAILED:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.NOT_SUPPORTED.exception();
        }
    }

    private String collectOtp(AuthorisationResponse authResponse) {
        String scaMethodName = authResponse.getChosenScaMethod().getName();
        List<Field> fields = new LinkedList<>();
        extractStartcode(authResponse)
                .ifPresent(x -> fields.add(GermanFields.Startcode.build(catalog, x)));
        fields.add(GermanFields.Tan.build(catalog, scaMethodName));
        return supplementalInformationHelper
                .askSupplementalInformation(fields.toArray(new Field[0]))
                .get(GermanFields.Tan.getFieldKey());
    }

    private Optional<String> extractStartcode(AuthorisationResponse authResponse) {
        return Optional.ofNullable(authResponse.getChallengeData())
                .map(ChallengeData::getAdditionalInformation)
                .map(this::extractStartCodeFromChallengeString);
    }

    private String extractStartCodeFromChallengeString(String challengeString) {
        Matcher matcher = STARTCODE_CHIP_PATTERN.matcher(challengeString);
        return matcher.find() ? matcher.group(1) : null;
    }
}
