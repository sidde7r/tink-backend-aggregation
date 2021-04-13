package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
@Slf4j
public class PostbankAuthenticationController implements TypedAuthenticator {

    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode:\\s(\\d+)");

    private static final String CHIP_OTP = "CHIP_OTP";
    private static final String SMS_OTP = "SMS_OTP";
    private static final String PUSH_OTP = "PUSH_OTP";

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;
    private final PostbankAuthenticator authenticator;

    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        validateReceivedCredentials(credentials);
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        AuthorisationResponse initValues = authenticator.init(username, password);

        ScaMethod chosenScaMethod = initValues.getChosenScaMethod();

        if (chosenScaMethod != null && !isSupported(chosenScaMethod)) {
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        }

        if (chosenScaMethod == null) {
            List<ScaMethod> supportedScaMethods =
                    getOnlySupportedScaMethods(initValues.getScaMethods());
            if (supportedScaMethods.isEmpty()) {
                throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
            }

            chosenScaMethod = collectScaMethod(supportedScaMethods);
            initValues =
                    authenticator.selectScaMethod(
                            chosenScaMethod.getAuthenticationMethodId(),
                            username,
                            initValues.getLinks().getScaStatus().getHref());
        }

        authenticateUsingChosenScaMethod(username, initValues, chosenScaMethod);
        authenticator.storeConsentDetails();
    }

    private void authenticateUsingChosenScaMethod(
            String username, AuthorisationResponse initValues, ScaMethod chosenScaMethod) {
        String authenticationType = chosenScaMethod.getAuthenticationType();
        log.info("[Postbank 2FA] User for authenticationType {} started 2FA", authenticationType);
        switch (authenticationType.toUpperCase()) {
            case PUSH_OTP:
                finishWithAcceptingPush(initValues, username);
                break;
            case SMS_OTP:
            case CHIP_OTP:
                finishWithOtpAuthorisation(initValues, username);
                break;
            default:
                throw LoginError.NOT_SUPPORTED.exception();
        }
        log.info(
                "[Postbank 2FA] User for authenticationType {} successfully passed 2FA",
                authenticationType);
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
        return scaMethods == null
                ? Collections.emptyList()
                : scaMethods.stream().filter(this::isSupported).collect(Collectors.toList());
    }

    private boolean isSupported(ScaMethod scaMethod) {
        return !scaMethod.getAuthenticationMethodId().toLowerCase().contains("optical");
    }

    private ScaMethod collectScaMethod(List<ScaMethod> scaMethods) {
        if (scaMethods.size() == 1) {
            return scaMethods.get(0);
        }

        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(
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

    private void finishWithAcceptingPush(AuthorisationResponse previousResponse, String username) {
        showInfo(previousResponse.getChosenScaMethod().getName());
        poll(username, previousResponse.getLinks().getScaStatus().getHref());
    }

    private void showInfo(String deviceName) {
        Field informationField =
                CommonFields.Instruction.build(
                        catalog.getString(PostbankConstants.InfoScreen.INSTRUCTIONS, deviceName));

        try {
            supplementalInformationController.askSupplementalInformationSync(informationField);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
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
        String authenticationType = authResponse.getChosenScaMethod().getAuthenticationType();
        ChallengeData challengeData = authResponse.getChallengeData();
        List<Field> fields = new LinkedList<>();
        extractStartcode(authResponse)
                .ifPresent(x -> fields.add(GermanFields.Startcode.build(catalog, x)));

        fields.add(
                GermanFields.Tan.build(
                        catalog,
                        authenticationType,
                        scaMethodName,
                        challengeData != null ? challengeData.getOtpMaxLength() : null,
                        challengeData != null ? challengeData.getOtpFormat() : null));

        return supplementalInformationController
                .askSupplementalInformationSync(fields.toArray(new Field[0]))
                .get(fields.get(fields.size() - 1).getName());
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
