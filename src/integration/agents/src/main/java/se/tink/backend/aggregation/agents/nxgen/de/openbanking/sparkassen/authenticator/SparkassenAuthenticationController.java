package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.PollStatus;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;

public class SparkassenAuthenticationController implements MultiFactorAuthenticator {
    private static final String OTP_VALUE_FIELD_KEY = "otpValue";
    private static final String CHOSEN_SCA_METHOD = "chosenScaMethod";
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SparkassenAuthenticator authenticator;

    public SparkassenAuthenticationController(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            SparkassenAuthenticator authenticator) {
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
                !Objects.equals(credentials.getType(), this.getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        if (Strings.isNullOrEmpty(username) && Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        InitAuthorizationResponse initValues = this.authenticator.init(username, password);

        List<ScaMethodEntity> scaMethods = initValues.getScaMethods();

        // Select SCA method when user has more than one device.
        // If there are multiple sca methods status will be "psuAuthenticated"
        // If there is only one sca method status will automatically go to "scaMethodSelected"
        if (scaMethods != null && scaMethods.size() > 1) {
            Map<String, String> supplementalInformation =
                    this.supplementalInformationHelper.askSupplementalInformation(
                            this.getChosenScaMethod(scaMethods));

            int index = Integer.valueOf(supplementalInformation.get(CHOSEN_SCA_METHOD)) - 1;
            ScaMethodEntity chosenScaMethod = scaMethods.get(index);

            SelectAuthenticationMethodResponse selectAuthenticationMethodResponse =
                    this.authenticator.selectScaMethod(chosenScaMethod.getAuthenticationMethodId());

            if (selectAuthenticationMethodResponse != null
                    && selectAuthenticationMethodResponse.getChallengeData() != null) {
                // SMS_OTP or CHIP_OTP is selected
                getOtpCode(
                        selectAuthenticationMethodResponse.getChallengeData().getOtpMaxLength(),
                        selectAuthenticationMethodResponse
                                .getChosenScaMethod()
                                .getAuthenticationType());
            }

            return;
        }

        getOtpCode(
                initValues.getChallengeData().getOtpMaxLength(),
                initValues.getScaMethods().stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new)
                        .getAuthenticationType());
    }

    private void getOtpCode(int otpValueLength, String otpType)
            throws AuthenticationException, AuthorizationException {
        Map<String, String> supplementalInformation =
                this.supplementalInformationHelper.askSupplementalInformation(
                        this.getOtpField(otpValueLength, otpType));

        FinalizeAuthorizationResponse finalizeAuthorizationResponse =
                this.authenticator.authenticateWithOtp(
                        supplementalInformation.get(OTP_VALUE_FIELD_KEY));

        switch (finalizeAuthorizationResponse.getScaStatus()) {
            case PollStatus.FINALISED:
                break;
            case PollStatus.FAILED:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.NOT_SUPPORTED.exception();
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
                .description(this.catalog.getString(description))
                .helpText("Please select SCA method")
                .name(CHOSEN_SCA_METHOD)
                .numeric(true)
                .minLength(1)
                .maxLength(length)
                .hint(String.format("Select from 1 to %d", maxNumber))
                .pattern(regexForRangePattern)
                .patternError("The chosen SCA method is not valid")
                .build();
    }

    private Field getOtpField(int otpValueLength, String otpType) {
        return Field.builder()
                .description(this.catalog.getString("Verification code"))
                .helpText(otpType)
                .name(OTP_VALUE_FIELD_KEY)
                .numeric(true)
                .minLength(otpValueLength)
                .maxLength(otpValueLength)
                .hint(StringUtils.repeat("N", otpValueLength))
                .pattern(String.format("([0-9]{%d})", otpValueLength))
                .patternError("The code you entered is not valid")
                .build();
    }
}
