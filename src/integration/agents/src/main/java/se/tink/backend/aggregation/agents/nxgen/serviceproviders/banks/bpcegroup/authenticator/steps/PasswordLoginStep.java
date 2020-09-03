package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthorizeResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.StepDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ValidationUnitResponseItemDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.VirtualKeyboardDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.MembershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.ValidationType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.BpceValidationHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizeHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@Slf4j
@RequiredArgsConstructor
public class PasswordLoginStep extends AuthenticateBaseStep {

    public static final String STEP_ID = "passwordLoginStep";

    private final BpceApiClient bpceApiClient;

    private final BpceStorage bpceStorage;

    private final ImageRecognizeHelper imageRecognizeHelper;

    private final BpceValidationHelper validationHelper;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final String passwordFromCredentials =
                Strings.emptyToNull(request.getCredentials().getField(Key.PASSWORD));
        final String username =
                Strings.emptyToNull(request.getCredentials().getField(Key.USERNAME));
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(passwordFromCredentials);

        final MembershipType membershipType = bpceStorage.getMembershipType();
        final AuthorizeResponseDto authorizeResponseDto =
                bpceApiClient.sendAuthorizeRequest(
                        bpceStorage.getBankId(), username, membershipType);
        final String authTransactionPath =
                bpceApiClient.getAuthTransactionPath(authorizeResponseDto);
        final AuthTransactionResponseDto authTransactionResponseDto =
                bpceApiClient.getAuthTransaction(authTransactionPath);

        final String password =
                getPasswordString(
                        membershipType, authTransactionResponseDto, passwordFromCredentials);
        final AuthTransactionResponseDto sendPasswordResponse =
                sendPassword(authTransactionPath, authTransactionResponseDto, password);

        if (isOtpNeeded(sendPasswordResponse)) {
            bpceStorage.storeCouldAutoAuthenticate(false);

            validateCredentialsResponse(sendPasswordResponse);

            bpceStorage.storeAuthTransactionPath(authTransactionPath);
            bpceStorage.storeCredentialsResponse(sendPasswordResponse);
            return AuthenticationStepResponse.executeStepWithId(SmsOtpStep.STEP_ID);
        }

        validateAuthenticationSucceeded(sendPasswordResponse);

        bpceStorage.storeCouldAutoAuthenticate(true);
        bpceStorage.storeSamlPostAction(sendPasswordResponse.getResponse().getSaml2Post());

        return AuthenticationStepResponse.executeStepWithId(AuthConsumeStep.STEP_ID);
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }

    private AuthTransactionResponseDto sendPassword(
            String authTransactionPath,
            AuthTransactionResponseDto authTransactionResponseDto,
            String password) {
        final String validationId = validationHelper.getValidationId(authTransactionResponseDto);
        final String validationUnitId =
                validationHelper.getValidationUnitId(authTransactionResponseDto, validationId);

        return bpceApiClient.sendPassword(
                validationId, validationUnitId, authTransactionPath, password);
    }

    private static boolean isOtpNeeded(AuthTransactionResponseDto sendPasswordResponse) {
        return Objects.isNull(sendPasswordResponse.getResponse())
                && Objects.nonNull(sendPasswordResponse.getPhase())
                && "AUTHENTICATION".equalsIgnoreCase(sendPasswordResponse.getPhase().getState());
    }

    private static void validateCredentialsResponse(
            AuthTransactionResponseDto sendPasswordResponse) {
        if (Objects.isNull(sendPasswordResponse.getPhase())
                || Objects.isNull(sendPasswordResponse.getValidationUnits())
                || sendPasswordResponse.getValidationUnits().size() != 1) {
            throw LoginError.NOT_SUPPORTED.exception();
        }

        final List<ValidationUnitResponseItemDto> items =
                sendPasswordResponse.getValidationUnits().get(0).values().stream()
                        .findFirst()
                        .orElseThrow(LoginError.NOT_SUPPORTED::exception);

        if (items.size() != 1) {
            throw LoginError.NOT_SUPPORTED.exception();
        }

        final String validationType = items.get(0).getType();

        if (!ValidationType.SMS.getName().equalsIgnoreCase(validationType)) {
            log.error("Unknown validation type: " + validationType);
            throw LoginError.NOT_SUPPORTED.exception();
        }
    }

    private String getPasswordString(
            MembershipType membershipType,
            AuthTransactionResponseDto authTransactionResponseDto,
            String password) {

        if (MembershipType.PRO == membershipType) {
            final String imagesUrl =
                    getKeyboardImagesUrl(authTransactionResponseDto)
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Path to keyboard images could not be determined."));

            return getVirtualKeyboardPassword(password, imagesUrl);
        } else if (MembershipType.PART == membershipType) {
            return password;
        } else {
            throw new IllegalArgumentException(
                    "Could not determine authentication method for membership type UNKNOWN");
        }
    }

    private String getVirtualKeyboardPassword(String password, String imagesUrl) {
        final Map<String, byte[]> keyboardImages = bpceApiClient.getKeyboardImages(imagesUrl);

        final Map<Integer, String> digitKeyMap =
                keyboardImages.entrySet().stream()
                        .map(this::convertImageToDigitKeyMap)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return convertPasswordToKeyString(password, digitKeyMap);
    }

    private Map.Entry<Integer, String> convertImageToDigitKeyMap(
            Map.Entry<String, byte[]> keyboardImage) {
        final int parsedDigit = imageRecognizeHelper.parseDigit(keyboardImage);
        return new AbstractMap.SimpleEntry<>(parsedDigit, keyboardImage.getKey());
    }

    private String convertPasswordToKeyString(String password, Map<Integer, String> digitToKeyMap) {
        final List<Integer> passwordDigits =
                password.chars()
                        .mapToObj(c -> (char) c)
                        .map(Character::getNumericValue)
                        .collect(Collectors.toList());

        return passwordDigits.stream().map(digitToKeyMap::get).collect(Collectors.joining(" "));
    }

    private static Optional<String> getKeyboardImagesUrl(
            AuthTransactionResponseDto authTransactionResponseDto) {
        final StepDto stepDto = authTransactionResponseDto.getStep();

        if (Objects.isNull(stepDto)) {
            return Optional.empty();
        }

        final List<String> paths =
                stepDto.getValidationUnits().stream()
                        .map(Map::values)
                        .flatMap(Collection::stream)
                        .flatMap(Collection::stream)
                        .filter(
                                virtualKeyboardValidationItem ->
                                        Objects.equals(
                                                virtualKeyboardValidationItem.getType(),
                                                "PASSWORD"))
                        .map(ValidationUnitResponseItemDto::getVirtualKeyboard)
                        .filter(Objects::nonNull)
                        .map(VirtualKeyboardDto::getExternalRestMediaApiUrl)
                        .collect(Collectors.toList());

        if (paths.size() > 1) {
            return Optional.empty();
        }

        return paths.stream().findFirst();
    }
}
