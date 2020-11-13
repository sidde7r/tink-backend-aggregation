package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment.rpc.ValidateGiroRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment.rpc.ValidateGiroResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment.rpc.ValidateOCRRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment.rpc.ValidateOCRResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.AcceptSignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.AcceptSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.SignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.DanskeRequestHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.i18n.Catalog;

public class DanskeBankSEApiClient extends DanskeBankApiClient {
    DanskeBankSEApiClient(
            TinkHttpClient client,
            DanskeBankSEConfiguration configuration,
            Credentials credentials,
            Catalog catalog) {
        super(client, configuration, credentials, catalog);
    }

    public InitResponse initiateBankIdLogin(String logonPackage) {
        String response =
                client.request(DanskeBankConstants.Urls.BANK_ID_INIT_LOGON_URL)
                        .header(
                                DanskeRequestHeaders.REFERRER,
                                DanskeBankConstants.Urls.BANK_ID_INIT_LOGON_URL)
                        .post(String.class, InitRequest.createFromMessage(logonPackage));

        return DanskeBankDeserializer.convertStringToObject(response, InitResponse.class);
    }

    public PollResponse pollBankId(String reference) {
        String response =
                client.request(DanskeBankConstants.Urls.BANKID_POLL_URL)
                        .header(
                                DanskeRequestHeaders.REFERRER,
                                DanskeBankConstants.Urls.BANKID_POLL_URL)
                        .post(String.class, PollRequest.createFromReference(reference));

        return DanskeBankDeserializer.convertStringToObject(response, PollResponse.class);
    }

    public ListPayeesResponse getBeneficiaries(ListPayeesRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.LIST_PAYEES_URL, ListPayeesResponse.class, request);
    }

    public CreditorResponse creditorName(CreditorRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.CREDITOR_NAME_URL, CreditorResponse.class, request);
    }

    public CreditorResponse creditorBankName(CreditorRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.CREDITOR_BANK_NAME_URL, CreditorResponse.class, request);
    }

    public ValidatePaymentDateResponse validatePaymentDate(ValidatePaymentDateRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.VALIDATE_PAYMENT_REQUEST_URL,
                ValidatePaymentDateResponse.class,
                request);
    }

    public RegisterPaymentResponse registerPayment(RegisterPaymentRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.REGISTER_PAYMENT_URL,
                RegisterPaymentResponse.class,
                request);
    }

    public PollResponse signPayment(SignRequest request) {
        return client.request(DanskeBankConstants.Urls.BANKID_POLL_URL)
                .header(DanskeRequestHeaders.REFERRER, DanskeBankConstants.Urls.BANKID_POLL_URL)
                .post(PollResponse.class, request);
    }

    public AcceptSignatureResponse acceptSignature(
            String signatureType, AcceptSignatureRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.getAcceptSignatureUrl(signatureType),
                AcceptSignatureResponse.class,
                request);
    }

    public ValidateGiroResponse validateGiroRequest(ValidateGiroRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.VALIDATE_GIRO_REQUEST_URL,
                ValidateGiroResponse.class,
                request);
    }

    public ValidateOCRResponse validateOcr(ValidateOCRRequest request) {
        return postRequest(
                DanskeBankConstants.Urls.VALIDATE_OCR_REQUEST_URL,
                ValidateOCRResponse.class,
                request);
    }
}
