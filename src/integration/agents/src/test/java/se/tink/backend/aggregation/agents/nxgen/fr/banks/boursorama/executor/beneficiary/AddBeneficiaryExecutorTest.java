package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.beneficiary;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.assertions.BeneficiaryAssert;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.assertions.CreateBeneficiaryAssert;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.AddBeneficiaryExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.AccountInformation;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.AdditionalMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.ChallengeDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.ChallengeParametersEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.CheckBeneficiaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.ConfirmBeneficiaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.ConfirmBeneficiaryUnauthorizedResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.PrepareBeneficiaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.ScaInformation;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc.StartOtpResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

public class AddBeneficiaryExecutorTest {

    private static final String MESSAGE_ENTITY_ID = "testAdditionalMessageEntityId";
    private static final String IBAN = "FR6812739000301532282681R60";
    private static final String FULL_NAME = "Pierre St Martin";
    private static final String BENEFICIARY_ID = "testBeneficiaryId";
    private static final String OTP_NUMBER = "otpNumber";
    private AddBeneficiaryExecutor addBeneficiaryExecutor;
    private BoursoramaApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private SessionStorage storage;

    @Before
    public void init() {
        apiClient = mock(BoursoramaApiClient.class);
        storage = new SessionStorage();
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        addBeneficiaryExecutor =
                new AddBeneficiaryExecutor(apiClient, storage, supplementalInformationHelper);
    }

    @Test
    @SneakyThrows
    public void testCreateBeneficiary() {
        // given
        givenApiClientMocksForCreation();
        CreateBeneficiary createBeneficiary = givenCreateBeneficiaryInfo();
        // when
        CreateBeneficiaryResponse response =
                addBeneficiaryExecutor.createBeneficiary(
                        new CreateBeneficiaryRequest(createBeneficiary, storage));
        // then
        verify(apiClient, times(1)).ackMessage(eq(MESSAGE_ENTITY_ID));
        assertCorrectResponse(response, CreateBeneficiaryStatus.INITIATED);
    }

    @Test
    @SneakyThrows
    public void testSign() {
        // given
        givenApiClientMocksForSigning();
        givenSupplementalInfoHelperMocks();
        CreateBeneficiary createBeneficiary = givenCreateBeneficiaryInfo();
        storage.put("beneficiaryId", BENEFICIARY_ID);
        // when
        CreateBeneficiaryMultiStepResponse response =
                addBeneficiaryExecutor.sign(
                        new CreateBeneficiaryMultiStepRequest(
                                createBeneficiary,
                                storage,
                                "testStep",
                                Collections.emptyList(),
                                Collections.emptyList()));
        // then
        assertCorrectResponse(response, CreateBeneficiaryStatus.CREATED);
    }

    private void givenApiClientMocksForSigning() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(401);
        when(response.getBody(eq(ConfirmBeneficiaryUnauthorizedResponse.class)))
                .thenReturn(
                        new ConfirmBeneficiaryUnauthorizedResponse(
                                new ScaInformation(
                                        ImmutableList.of(
                                                new ChallengeDetailsEntity(
                                                        "brs-otp-sms",
                                                        new ChallengeParametersEntity(OTP_NUMBER)),
                                                new ChallengeDetailsEntity(
                                                        "brs-otp-email",
                                                        new ChallengeParametersEntity(
                                                                OTP_NUMBER))))));
        HttpResponseException exception =
                new HttpResponseException(mock(HttpRequest.class), response);
        when(apiClient.confirmBeneficiary(eq(BENEFICIARY_ID)))
                .thenThrow(exception)
                .thenReturn(
                        new ConfirmBeneficiaryResponse(true, new AccountInformation("XXX", IBAN)));

        when(apiClient.startSms(eq(OTP_NUMBER))).thenReturn(new StartOtpResponse(true));
        when(apiClient.startEmail(eq(OTP_NUMBER))).thenReturn(new StartOtpResponse(true));
    }

    private void assertCorrectResponse(
            CreateBeneficiaryResponse beneficiary, CreateBeneficiaryStatus expectedStatus) {
        assertThat(beneficiary).isNotNull();
        CreateBeneficiaryAssert.assertThat(beneficiary.getBeneficiary())
                .isNotNull()
                .hasOwnerAccountNumber("")
                .hasStatus(expectedStatus);
        BeneficiaryAssert.assertThat(beneficiary.getBeneficiary().getBeneficiary())
                .isNotNull()
                .hasAccountNumber(IBAN)
                .hasName(FULL_NAME)
                .hasAccountNumberType(AccountIdentifierType.IBAN);
    }

    private void givenSupplementalInfoHelperMocks() {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put("Sms-otp-code", "123456");
        supplementalInformation.put("Email-otp-code", "654321");
        when(supplementalInformationHelper.askSupplementalInformation(any(), any()))
                .thenReturn(supplementalInformation);
    }

    private void givenApiClientMocksForCreation() {
        when(apiClient.prepareBeneficiary())
                .thenReturn(new PrepareBeneficiaryResponse(BENEFICIARY_ID));

        when(apiClient.checkBeneficiary(
                        eq(BENEFICIARY_ID),
                        eq(FULL_NAME),
                        eq("Pierre"),
                        eq("St Martin"),
                        eq(IBAN),
                        eq("bank")))
                .thenReturn(
                        new CheckBeneficiaryResponse(
                                "testBic",
                                Collections.singletonList(
                                        new AdditionalMessageEntity(MESSAGE_ENTITY_ID))));

        doNothing().when(apiClient).ackMessage(eq(MESSAGE_ENTITY_ID));
    }

    private CreateBeneficiary givenCreateBeneficiaryInfo() {
        Beneficiary beneficiary =
                Beneficiary.builder()
                        .accountNumber(IBAN)
                        .accountNumberType(AccountIdentifierType.IBAN)
                        .name(FULL_NAME)
                        .build();
        return CreateBeneficiary.builder().beneficiary(beneficiary).ownerAccountNumber("").build();
    }
}
