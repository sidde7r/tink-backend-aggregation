package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.ResultSignResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.SignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.SignatureResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaBankIdSignHelper {
    private final NordeaBaseApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;

    public NordeaBankIdSignHelper(
            NordeaBaseApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
    }

    public String sign(String signingItemId) throws BankIdException {
        SignatureRequest signatureRequest = new SignatureRequest();
        signatureRequest.add(new SignatureEntity(signingItemId));
        SignatureResponse signatureResponse = apiClient.signTransfer(signatureRequest);
        if (signatureResponse.getSignatureState().equals(BankIdStatus.WAITING)) {
            supplementalInformationController.openMobileBankIdAsync(null);
            poll(signatureResponse.getOrderReference());
            return signatureResponse.getOrderReference();
        } else {
            throw BankIdError.UNKNOWN.exception();
        }
    }

    private void poll(String orderRef) throws BankIdException {
        for (int i = 1; i < NordeaBaseConstants.Transfer.MAX_POLL_ATTEMPTS; i++) {
            try {
                ResultSignResponse signResponse = apiClient.pollSign(orderRef, i);

                switch (signResponse.getBankIdStatus()) {
                    case DONE:
                        return;
                    case WAITING:
                        break;
                    case CANCELLED:
                        throw BankIdError.CANCELLED.exception();
                    default:
                        throw BankIdError.UNKNOWN.exception();
                }
                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                    throw BankIdError.ALREADY_IN_PROGRESS.exception(e);
                }
            }
        }
        throw BankIdError.TIMEOUT.exception();
    }
}
