package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.InitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.PollResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class DanskeBankSEApiClient extends DanskeBankApiClient {
    DanskeBankSEApiClient(TinkHttpClient client, DanskeBankSEConfiguration configuration) {
        super(client, configuration);
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
}
