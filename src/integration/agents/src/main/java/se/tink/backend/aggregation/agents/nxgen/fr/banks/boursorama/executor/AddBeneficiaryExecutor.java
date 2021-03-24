package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.ChallengeDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.CheckBeneficiaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.ConfirmBeneficiaryUnauthorizedResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.PrepareBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

@RequiredArgsConstructor
public class AddBeneficiaryExecutor implements CreateBeneficiaryExecutor {

    private static final String SMS_OTP_CODE = "";
    private static final String EMAIL_OTP_CODE = "Email-otp-code";

    private final BoursoramaApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    @Override
    public CreateBeneficiaryResponse createBeneficiary(
            CreateBeneficiaryRequest createBeneficiaryRequest) throws BeneficiaryException {
        CreateBeneficiary beneficiary = createBeneficiaryRequest.getBeneficiary();
        if (beneficiary.getBeneficiary().getAccountNumberType() != AccountIdentifierType.IBAN) {
            throw new BeneficiaryException(
                    "Cannot add beneficiary with account identifier different than IBAN");
        }
        String nickname = beneficiary.getBeneficiary().getName();
        String iban = beneficiary.getBeneficiary().getAccountNumber();
        String[] names = beneficiary.getBeneficiary().getName().split("\\s+");
        String firstName = names[0];
        String surname =
                String.join(" ", Arrays.asList(Arrays.copyOfRange(names, 1, names.length)));
        String bankName = "bank";

        PrepareBeneficiaryResponse prepareBeneficiaryResponse = apiClient.prepareBeneficiary();
        String beneficiaryId = prepareBeneficiaryResponse.getBeneficiaryId();
        sessionStorage.put("beneficiaryId", beneficiaryId);

        CheckBeneficiaryResponse checkBeneficiaryResponse =
                apiClient.checkBeneficiary(
                        beneficiaryId, nickname, firstName, surname, iban, bankName);
        checkBeneficiaryResponse
                .getAdditionalMessages()
                .forEach(message -> apiClient.ackMessage(message.getId()));
        // the bank verifies correctness of IBAN and assigns correct BIC/SWIFT code (among other
        // things).
        // If this is not present then something went wrong.
        if (checkBeneficiaryResponse.getBic() == null
                || checkBeneficiaryResponse.getBic().isEmpty()) {
            throw new BeneficiaryException("Incorrect validation of beneficiary on bank sie");
        }

        CreateBeneficiaryResponse response = CreateBeneficiaryResponse.of(createBeneficiaryRequest);
        response.getBeneficiary().setStatus(CreateBeneficiaryStatus.INITIATED);
        return response;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, AuthenticationException {
        String beneficiaryId = sessionStorage.get("beneficiaryId");
        ConfirmBeneficiaryUnauthorizedResponse confirmBeneficiaryResponse = null;

        try {
            apiClient.confirmBeneficiary(beneficiaryId);
        } catch (HttpResponseException e) {
            // this is hacky solution because it is a normal flow in which the bank returns 401.
            confirmBeneficiaryResponse =
                    e.getResponse().getBody(ConfirmBeneficiaryUnauthorizedResponse.class);
        }
        if (confirmBeneficiaryResponse == null) {
            throw new BeneficiaryException("Could not retrieve SCA details");
        }
        List<ChallengeDetailsEntity> challenges =
                confirmBeneficiaryResponse.getData().getChallenges();
        initializeChallenges(challenges);
        Map<String, String> supplementalInformation = getSupplementalInformation();
        String otpCode =
                challenges.stream()
                        .findAny()
                        .orElseThrow(IllegalArgumentException::new)
                        .getParameters()
                        .getOtpNumber();
        checkOtpCodes(
                otpCode,
                supplementalInformation.get(SMS_OTP_CODE),
                supplementalInformation.get(EMAIL_OTP_CODE));
        apiClient.confirmBeneficiary(beneficiaryId);
        CreateBeneficiaryMultiStepResponse response =
                new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE);
        response.getBeneficiary().setStatus(CreateBeneficiaryStatus.CREATED);
        return response;
    }

    private void initializeChallenges(List<ChallengeDetailsEntity> challenges) {
        challenges.forEach(
                challenge -> {
                    if (challenge.isSmsOtp()) {
                        apiClient.startSms(challenge.getParameters().getOtpNumber());
                    } else if (challenge.isEmailOtp()) {
                        apiClient.startEmail(challenge.getParameters().getOtpNumber());
                    } else {
                        throw new IllegalArgumentException(
                                "Unsupported type of OTP requested " + challenge.getType());
                    }
                });
    }

    private void checkOtpCodes(String otpNumber, String smsCode, String emailCode) {
        try {
            apiClient.checkSms(otpNumber, smsCode);
            apiClient.checkEmail(otpNumber, emailCode);
        } catch (HttpResponseException e) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
        }
    }

    private Map<String, String> getSupplementalInformation() {
        return supplementalInformationHelper.askSupplementalInformation(
                getSmsField(), getEmailField());
    }

    private Field getSmsField() {
        return Field.builder()
                .name(SMS_OTP_CODE)
                .minLength(6)
                .maxLength(6)
                .numeric(true)
                .description("Please input the code you received via SMS")
                .build();
    }

    private Field getEmailField() {
        return Field.builder()
                .name(EMAIL_OTP_CODE)
                .minLength(6)
                .maxLength(6)
                .numeric(true)
                .description("Please input the code you received via e-mail")
                .build();
    }
}
