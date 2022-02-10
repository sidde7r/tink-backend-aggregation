package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankConstants.PollStatus;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankDecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail.PostbankLegacyDecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.EmbeddedFieldBuilder;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
@Slf4j
public class PostbankAuthenticationController implements TypedAuthenticator {

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;
    protected final PostbankAuthenticator authenticator;
    protected final EmbeddedFieldBuilder embeddedFieldBuilder;
    private final RandomValueGenerator randomValueGenerator;

    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        validateReceivedCredentials(credentials);
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        AuthorizationResponse initValues = authenticator.init(username, password);

        handleSca(initValues, username);

        authenticator.validateAndStoreConsentDetails();
    }

    protected void handleSca(AuthorizationResponse initValues, String username) {
        ScaMethodEntity chosenScaMethod = initValues.getChosenScaMethod();

        if (chosenScaMethod != null && !isSupported(chosenScaMethod)) {
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        }

        if (chosenScaMethod == null) {
            List<ScaMethodEntity> supportedScaMethods =
                    getOnlySupportedScaMethods(initValues.getScaMethods());
            if (supportedScaMethods.isEmpty()) {
                throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
            }

            chosenScaMethod = collectScaMethod(supportedScaMethods);
            initValues =
                    authenticator.selectScaMethod(
                            chosenScaMethod.getAuthenticationMethodId(),
                            username,
                            initValues.getLinks().getScaStatus());
        }

        authenticateUsingChosenScaMethod(username, initValues, chosenScaMethod);
    }

    private void authenticateUsingChosenScaMethod(
            String username, AuthorizationResponse initValues, ScaMethodEntity chosenScaMethod) {
        String authenticationType = chosenScaMethod.getAuthenticationType();
        log.info("[Postbank 2FA] User for authenticationType {} started 2FA", authenticationType);
        switch (AuthenticationType.fromString(authenticationType)
                .orElseThrow(LoginError.NOT_SUPPORTED::exception)) {
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

    protected void validateReceivedCredentials(Credentials credentials) {
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
        return scaMethods == null
                ? Collections.emptyList()
                : scaMethods.stream().filter(this::isSupported).collect(Collectors.toList());
    }

    private boolean isSupported(ScaMethodEntity scaMethod) {
        return !scaMethod.getAuthenticationMethodId().toLowerCase().contains("optical");
    }

    private ScaMethodEntity collectScaMethod(List<ScaMethodEntity> scaMethods) {
        if (scaMethods.size() == 1) {
            return scaMethods.get(0);
        }

        Field scaMethodField = embeddedFieldBuilder.getChooseScaMethodField(scaMethods);
        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(scaMethodField);
        String selectedValue = supplementalInformation.get(CommonFields.Selection.getFieldKey());
        if (StringUtils.isNumeric(selectedValue)) {
            int index = Integer.parseInt(selectedValue) - 1;
            if (index >= 0 && index < scaMethods.size()) {
                return scaMethods.get(index);
            }
        }
        throw SupplementalInfoError.NO_VALID_CODE.exception(
                "Could not map user input to list of available options.");
    }

    private void finishWithAcceptingPush(AuthorizationResponse previousResponse, String username) {
        showInfo(previousResponse.getChosenScaMethod());
        poll(username, previousResponse.getLinks().getScaStatus());
    }

    private void showInfo(ScaMethodEntity scaMethod) {
        // NZG-1100
        // This implementation introduces the new way of showing this template in A/B way
        // This should be only temporary, and later we will make this class receive just one
        // instance of DecoupledFieldBuilder into constructor.
        DecoupledFieldBuilder decoupledFieldBuilder;
        boolean shouldUseNewTemplate = randomValueGenerator.randomInt(2) == 0;
        if (shouldUseNewTemplate) {
            log.info("[Postbank 2FA] Decoupled auth using new SDK Templates!");
            decoupledFieldBuilder = new PostbankDecoupledFieldBuilder(catalog);
        } else {
            log.info("[Postbank 2FA] Decoupled auth using old Info screen!");
            decoupledFieldBuilder = new PostbankLegacyDecoupledFieldBuilder(catalog);
        }

        List<Field> informationFields = decoupledFieldBuilder.getInstructionsField(scaMethod);

        try {
            supplementalInformationController.askSupplementalInformationSync(
                    informationFields.toArray(new Field[0]));
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private void poll(String username, String url) {
        for (int i = 0; i < PollStatus.MAX_POLL_ATTEMPTS; i++) {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);

            AuthorizationResponse response = authenticator.checkAuthorisationStatus(username, url);
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
            AuthorizationResponse previousResponse, String username) {
        AuthorizationResponse response =
                authenticator.authoriseWithOtp(
                        collectOtp(previousResponse),
                        username,
                        previousResponse.getLinks().getAuthoriseTransaction());

        switch (response.getScaStatus()) {
            case PollStatus.FINALISED:
                break;
            case PollStatus.FAILED:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.NOT_SUPPORTED.exception();
        }
    }

    private String collectOtp(AuthorizationResponse authResponse) {
        List<Field> fields =
                embeddedFieldBuilder.getOtpFields(
                        authResponse.getChosenScaMethod(), authResponse.getChallengeData());

        String inputFieldName =
                fields.stream()
                        .filter(field -> !field.isImmutable())
                        .map(Field::getName)
                        .findFirst()
                        .orElse(null);

        String otp =
                supplementalInformationController
                        .askSupplementalInformationSync(fields.toArray(new Field[0]))
                        .get(inputFieldName);
        if (otp == null) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    "Supplemental info did not come with otp code!");
        } else {
            return otp;
        }
    }
}
