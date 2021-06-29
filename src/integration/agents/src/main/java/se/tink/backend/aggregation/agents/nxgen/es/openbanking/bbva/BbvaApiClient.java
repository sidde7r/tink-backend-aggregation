package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc.ErrorResponse;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.signableoperation.enums.InternalStatus;

// TODO IFD-2563
// this class was prepared to bypass error on bank side
// it should be removed after BBVA will fix their implementation
// right now after signing of payment they return error with RESOURCE_EXPIRED
// even though payment was succesfully executed and they should return
// status of transaction as for example accepted
@Slf4j
public class BbvaApiClient extends RedsysApiClient {

    public BbvaApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            AspspConfiguration aspspConfiguration,
            EidasIdentity eidasIdentity,
            CredentialsRequest request) {
        super(
                client,
                sessionStorage,
                persistentStorage,
                aspspConfiguration,
                eidasIdentity,
                request);
    }

    @Override
    public PaymentStatusResponse fetchPaymentStatus(PaymentScheme paymentProduct, String paymentId)
            throws PaymentException {
        try {
            return createSignedRequest(
                            makeApiUrl(
                                    Urls.FETCH_PAYMENT_STATUS,
                                    RedsysPaymentType.fromTinkPaymentType(paymentProduct),
                                    paymentId))
                    .get(PaymentStatusResponse.class);
        } catch (HttpResponseException e) {
            final ErrorResponse error = ErrorResponse.fromResponse(e.getResponse());
            if (error.hasErrorCode(ErrorCodes.RESOURCE_EXPIRED)) {
                log.info("Setting transaction status to ACCC due to bank implementation issue.");
                PaymentStatusResponse statusResponse = new PaymentStatusResponse();
                statusResponse.setTransactionStatus(RedsysTransactionStatus.ACCC);
                return statusResponse;
            }
            throw new PaymentException(
                    String.format(
                            "Error with code %s and message %s", error.getCode(), error.getText()),
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
    }
}
