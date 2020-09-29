package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;

public class PostbankAuthenticationController implements TypedAuthenticator {
    private static final String OTP_VALUE_FIELD_KEY = "otpValue";
    private static final String CHOSEN_SCA_METHOD = "chosenScaMethod";
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
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
            AuthorisationResponse initValues = authenticator.init(username, password);
            credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
            List<ScaMethodEntity> scaMethods = initValues.getScaMethods();
            ScaMethodEntity chosenScaMethod = initValues.getChosenScaMethodEntity();

            // Select SCA method when user has more than one device.
            if (chosenScaMethod == null && CollectionUtils.isNotEmpty(scaMethods)) {
                Map<String, String> supplementalInformation =
                        supplementalInformationHelper.askSupplementalInformation(
                                getChosenScaMethod(scaMethods));

                int index = Integer.parseInt(supplementalInformation.get(CHOSEN_SCA_METHOD)) - 1;
                chosenScaMethod = scaMethods.get(index);
                initValues =
                        authenticator.selectScaMethod(
                                chosenScaMethod.getAuthenticationMethodId(),
                                username,
                                initValues.getLinksEntity().getScaStatusEntity().getHref());
            }

            if (chosenScaMethod != null && initValues.getChallengeDataEntity() != null) {
                // SMS_OTP or CHIP_OTP is selected
                Map<String, String> supplementalInformation =
                        supplementalInformationHelper.askSupplementalInformation(
                                getOtpField(
                                        initValues.getChallengeDataEntity().getOtpMaxLength(),
                                        initValues
                                                .getChosenScaMethodEntity()
                                                .getAuthenticationType()));
                initValues =
                        authenticator.authenticateWithOtp(
                                supplementalInformation.get(OTP_VALUE_FIELD_KEY),
                                username,
                                initValues
                                        .getLinksEntity()
                                        .getAuthoriseTransactionEntity()
                                        .getHref());

                switch (initValues.getScaStatus()) {
                    case PollStatus.FINALISED:
                        break;
                    case PollStatus.FAILED:
                        throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
                    default:
                        throw LoginError.NOT_SUPPORTED.exception();
                }
            } else {
                // PUSH_OTP is selected
                poll(username, initValues.getLinksEntity().getScaStatusEntity().getHref());
            }
        } else {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private Field getChosenScaMethod(List<ScaMethodEntity> scaMethods) {
        int maxNumber = scaMethods.size();
        int length = Integer.toString(maxNumber).length();
        String description =
                IntStream.range(0, maxNumber)
                        .mapToObj(
                                i -> String.format("(%d) %s", i + 1, scaMethods.get(i).toString()))
                        .collect(Collectors.joining(";\n"));
        String regexForRangePattern = RangeRegex.regexForRange(1, maxNumber);

        return Field.builder()
                .description(String.format("Select from 1 to %d", maxNumber))
                .helpText("Please select SCA method" + "\n" + catalog.getString(description))
                .name(CHOSEN_SCA_METHOD)
                .numeric(true)
                .minLength(1)
                .maxLength(length)
                .pattern(regexForRangePattern)
                .patternError("The chosen SCA method is not valid")
                .build();
    }

    private Field getOtpField(int otpValueLength, String otpType) {
        return Field.builder()
                .description(catalog.getString("Verification code"))
                .helpText(otpType)
                .name(OTP_VALUE_FIELD_KEY)
                .minLength(1)
                .build();
    }

    private void poll(String username, String url) throws ThirdPartyAppException {
        for (int i = 0; i < PollStatus.MAX_POLL_ATTEMPTS; i++) {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);

            AuthorisationResponse response = authenticator.checkStatus(username, url);
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
