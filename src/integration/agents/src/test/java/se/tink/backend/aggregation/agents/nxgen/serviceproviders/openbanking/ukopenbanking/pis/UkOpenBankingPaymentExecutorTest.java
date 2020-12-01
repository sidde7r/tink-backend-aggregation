package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentExecutor.CLIENT_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCredentials;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentRequestForNotExecutedPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticScheduledPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createOAuth2Token;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentMultiStepRequestForExecutePaymentStep;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponseForConsent;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingTestValidator.validatePaymentResponsesForDomesticPaymentAreEqual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domesticscheduled.DomesticScheduledPaymentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UkOpenBankingPaymentExecutorTest {

    private UkOpenBankingPaymentExecutor ukOpenBankingPaymentExecutor;
    private UkOpenBankingPaymentApiClient apiClientMock;
    private PersistentStorage persistentStorageMock;

    @Before
    public void setUp() {
        final SoftwareStatementAssertion softwareStatementAssertionMock =
                mock(SoftwareStatementAssertion.class);
        final ClientInfo clientInfoMock = mock(ClientInfo.class);
        final SupplementalInformationHelper supplementalInformationHelperMock =
                mock(SupplementalInformationHelper.class);
        final Credentials credentialsMock = createCredentials();
        final StrongAuthenticationState strongAuthenticationStateMock =
                mock(StrongAuthenticationState.class);
        final RandomValueGenerator randomValueGeneratorMock = mock(RandomValueGenerator.class);
        apiClientMock = mock(UkOpenBankingPaymentApiClient.class);
        persistentStorageMock = mock(PersistentStorage.class);

        ukOpenBankingPaymentExecutor =
                new UkOpenBankingPaymentExecutor(
                        softwareStatementAssertionMock,
                        clientInfoMock,
                        apiClientMock,
                        supplementalInformationHelperMock,
                        credentialsMock,
                        strongAuthenticationStateMock,
                        randomValueGeneratorMock,
                        persistentStorageMock);
    }

    @Test
    public void shouldCreatePaymentConsent() throws PaymentException {
        // given
        final PaymentRequest paymentRequestMock =
                createDomesticPaymentRequestForNotExecutedPayment();
        final DomesticPaymentConsentResponse paymentConsentResponseMock =
                createDomesticPaymentConsentResponse();
        final OAuth2Token clientTokenMock = createOAuth2Token();

        when(apiClientMock.requestClientCredentials()).thenReturn(clientTokenMock);
        when(apiClientMock.createDomesticPaymentConsent(any(DomesticPaymentConsentRequest.class)))
                .thenReturn(paymentConsentResponseMock);

        // when
        final PaymentResponse returned = ukOpenBankingPaymentExecutor.create(paymentRequestMock);

        // then
        final PaymentResponse expected = createPaymentResponseForConsent();

        validatePaymentResponsesForDomesticPaymentAreEqual(returned, expected);
        verify(persistentStorageMock).put(CLIENT_TOKEN, clientTokenMock);
        verify(apiClientMock).instantiatePisAuthFilter(clientTokenMock);
    }

    @Test
    public void shouldRunExecutePaymentStep() throws PaymentException {
        // given
        final PaymentMultiStepRequest request =
                createPaymentMultiStepRequestForExecutePaymentStep();

        final DomesticScheduledPaymentResponse responseMock =
                createDomesticScheduledPaymentResponse();
        when(apiClientMock.executeDomesticScheduledPayment(
                        any(DomesticScheduledPaymentRequest.class)))
                .thenReturn(responseMock);

        // when
        final PaymentMultiStepResponse returned = ukOpenBankingPaymentExecutor.sign(request);

        // then
        assertThat(returned.getStep()).isEqualTo(AuthenticationStepConstants.STEP_FINALIZE);
    }
}
