package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.CreatePaymentRequestValidator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BpceGroupPaymentApiClientTest {

    private BpceGroupPaymentApiClient apiClient;
    private TinkHttpClient client;
    private CreatePaymentRequestValidator createPaymentRequestValidator;

    @Before
    public void setUp() {
        client = Mockito.mock(TinkHttpClient.class);
        SessionStorage sessionStorage = Mockito.mock((SessionStorage.class));
        BpceGroupConfiguration configuration = Mockito.mock(BpceGroupConfiguration.class);
        BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator =
                Mockito.mock((BpceGroupSignatureHeaderGenerator.class));
        createPaymentRequestValidator = Mockito.mock((CreatePaymentRequestValidator.class));
        apiClient =
                new BpceGroupPaymentApiClient(
                        client,
                        sessionStorage,
                        configuration,
                        bpceGroupSignatureHeaderGenerator,
                        createPaymentRequestValidator);
    }

    @Test(expected = PaymentValidationException.class)
    public void shouldNotSendRequestIfValidationFails() throws PaymentValidationException {
        // given
        CreatePaymentRequest paymentRequest = Mockito.mock(CreatePaymentRequest.class);

        willThrow(new PaymentValidationException("MESSAGE EXCEPTION"))
                .willDoNothing()
                .given(createPaymentRequestValidator)
                .validate(paymentRequest);

        // when
        apiClient.createPayment(paymentRequest);

        // then
        verifyNoInteractions(client);
    }
}
