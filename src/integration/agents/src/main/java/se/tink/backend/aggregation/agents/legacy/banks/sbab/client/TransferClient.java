package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.agents.banks.sbab.entities.SavedRecipientEntity;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.TransferAccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.TransferEntity;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.InitiateSignProcessRequest;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.InitiateSignResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.SavedRecipientsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.SignProcessResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.TransferAccountsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.TransferResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.ValidateRecipientRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferClient extends SBABClient {

    private final Catalog catalog;

    private TransferAccountsResponse cachedTransferAccounts;
    private SavedRecipientsResponse cachedSavedRecipients;

    public TransferClient(
            Client client, Credentials credentials, Catalog catalog, String userAgent) {
        super(client, credentials, userAgent);

        this.catalog = catalog;
    }

    public Optional<TransferAccountEntity> findSourceAccount(Transfer transfer) {
        return findUsersOwnAccount(transfer.getSource().getIdentifier());
    }

    public boolean isBetweenUserAccounts(Transfer transfer) {
        String destinationAccountNumber = transfer.getDestination().getIdentifier();
        Optional<TransferAccountEntity> destinationAccount =
                findUsersOwnAccount(destinationAccountNumber);

        return destinationAccount.isPresent();
    }

    private Optional<TransferAccountEntity> findUsersOwnAccount(String accountNumber) {
        return getTransferAccounts().stream()
                .filter(
                        transferAccountEntity ->
                                accountNumber.equals(transferAccountEntity.getAccountNumber()))
                .findFirst();
    }

    public boolean isSavedRecipient(Transfer transfer) {
        String destinationAccountNumber = transfer.getDestination().getIdentifier();

        Optional<SavedRecipientEntity> savedRecipient =
                getSavedRecipients().stream()
                        .filter(
                                savedRecipientEntity ->
                                        destinationAccountNumber.equals(
                                                savedRecipientEntity.getAccountNumber()))
                        .findFirst();

        return savedRecipient.isPresent();
    }

    private TransferAccountsResponse getTransferAccounts() {
        if (cachedTransferAccounts == null) {
            cachedTransferAccounts =
                    createJsonRequestWithBearer(SBABConstants.Url.TRANSFER_ACCOUNTS)
                            .get(TransferAccountsResponse.class);
        }

        return cachedTransferAccounts;
    }

    public List<SavedRecipientEntity> getSavedRecipients() {
        if (cachedSavedRecipients == null) {
            cachedSavedRecipients =
                    createJsonRequestWithBearer(SBABConstants.Url.SAVED_RECIPIENTS)
                            .get(SavedRecipientsResponse.class);
        }

        return cachedSavedRecipients;
    }

    public void validateRecipient(ValidateRecipientRequest request) {
        try {
            createJsonRequestWithBearer(SBABConstants.Url.VALIDATE_RECIPIENT).post(request);
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();

            if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                ErrorResponse errorResponse = response.getEntity(ErrorResponse.class);

                errorResponse.handleRecipientValidationErrors(catalog);
            }

            throw e;
        }
    }

    public void validateTransfer(TransferRequest transferRequest) {
        try {
            createJsonRequestWithBearer(SBABConstants.Url.VALIDATE_TRANSFER)
                    .post(TransferResponse.class, transferRequest);
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();

            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                ErrorResponse errorResponse = response.getEntity(ErrorResponse.class);

                errorResponse.handleTransferValidationErrors(catalog);
            }

            throw e;
        }
    }

    public TransferEntity confirmTransfer(TransferRequest transferRequest) {
        try {
            return createJsonRequestWithBearer(SBABConstants.Url.CONFIRM_TRANSFER)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(TransferEntity.class, transferRequest);
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();

            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                ErrorResponse errorResponse = response.getEntity(ErrorResponse.class);

                errorResponse.handleTransferConfirmationErrors(catalog);
            }

            throw e;
        }
    }

    public SignProcessResponse initiateSignProcess(TransferRequest transferRequest) {
        return createJsonRequestWithBearer(SBABConstants.Url.INIT_SIGN_PROCESS)
                .post(SignProcessResponse.class, transferRequest);
    }

    public InitiateSignResponse initiateBankIdSign(String bankIdRef) {
        return createJsonRequestWithBearer(SBABConstants.Url.SIGN_TRANSFER + bankIdRef)
                .post(InitiateSignResponse.class, InitiateSignProcessRequest.create());
    }

    public PollBankIdResponse pollBankId(String bankIdRef) {
        return createJsonRequestWithBearer(SBABConstants.Url.SIGN_TRANSFER + bankIdRef)
                .get(PollBankIdResponse.class);
    }
}
