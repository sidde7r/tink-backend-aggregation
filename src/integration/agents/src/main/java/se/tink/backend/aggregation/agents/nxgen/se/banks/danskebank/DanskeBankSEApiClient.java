package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.CreditorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ListPayeesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.ValidatePaymentDateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class DanskeBankSEApiClient extends DanskeBankApiClient {
    DanskeBankSEApiClient(
            TinkHttpClient client,
            DanskeBankSEConfiguration configuration,
            Credentials credentials) {
        super(client, configuration, credentials);
    }

    public InitResponse initiateBankIdLogin(String logonPackage) {
        String response =
                client.request(constants.getBankidInitLogonUrl())
                        .header("Referer", constants.getBankidInitLogonUrl())
                        .post(String.class, InitRequest.createFromMessage(logonPackage));

        return DanskeBankDeserializer.convertStringToObject(response, InitResponse.class);
    }

    public PollResponse pollBankId(String reference) {
        String response =
                client.request(constants.getBankidPollUrl())
                        .header("Referer", constants.getBankidPollUrl())
                        .post(String.class, PollRequest.createFromReference(reference));

        return DanskeBankDeserializer.convertStringToObject(response, PollResponse.class);
    }

    public ListPayeesResponse listPayees(ListPayeesRequest request) {
        return postRequest(constants.getListPayeesUrl(), ListPayeesResponse.class, request);
    }

    public CreditorResponse creditorName(CreditorRequest request) {
        return postRequest(constants.getCreditorNameUrl(), CreditorResponse.class, request);
    }

    public CreditorResponse creditorBankName(CreditorRequest request) {
        return postRequest(constants.getCreditorBankNameUrl(), CreditorResponse.class, request);
    }

    public ValidatePaymentDateResponse validatePaymentDate(ValidatePaymentDateRequest request) {
        return postRequest(
                constants.getValidatePaymentRequestUrl(),
                ValidatePaymentDateResponse.class,
                request);
    }

    public RegisterPaymentResponse registerPayment(RegisterPaymentRequest request) {
        return postRequest(
                constants.getRegisterPaymentUrl(), RegisterPaymentResponse.class, request);
    }
}
