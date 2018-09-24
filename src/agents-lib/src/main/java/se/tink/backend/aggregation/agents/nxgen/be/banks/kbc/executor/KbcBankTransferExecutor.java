package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.general.GeneralUtils;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcDevice;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignTypesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.SigningChallengeSotpResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.SigningChallengeUcrResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.ValidateTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;

public class KbcBankTransferExecutor implements BankTransferExecutor {

    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final KbcApiClient apiClient;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public KbcBankTransferExecutor(Credentials credentials, PersistentStorage persistentStorage,
            KbcApiClient apiClient, Catalog catalog,
            SupplementalInformationController supplementalInformationController) {
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public void executeTransfer(Transfer transfer) throws TransferExecutionException {
        List<GeneralAccountEntity> ownAccounts = fetchOwnAccounts();

        TransactionalAccount sourceAccount = getSourceAccount(transfer.getSource(), ownAccounts);
        // For immediate transfer it is not allowed to do transfers that are not covered by the balance. Blocked in app.
        if (immediateTransfer(transfer)) {
            validateAmountCoveredByBalance(sourceAccount, transfer.getAmount());
        }

        boolean isTransferToOwnAccount = GeneralUtils.isAccountExisting(transfer.getDestination(), ownAccounts);

        String signType = validateTransfer(transfer, isTransferToOwnAccount);

        try {
            transfer(transfer, signType, isTransferToOwnAccount);
        } catch (AuthenticationException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED))
                    .build();
        }
    }

    private boolean immediateTransfer(Transfer transfer) {
        return transfer.getDueDate() == null;
    }

    private void validateAmountCoveredByBalance(TransactionalAccount sourceAccount, Amount amount) {
        if (sourceAccount.getBalance()
                .isLessThan(amount.doubleValue())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT.getKey().get())
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }
    }

    private List<GeneralAccountEntity> fetchOwnAccounts() {
        List<AgreementDto> accountsForTransferToOwn = apiClient.accountsForTransferToOwn().getAgreements();
        List<AgreementDto> accountsForTransferToOther = apiClient.accountsForTransferToOther().getAgreements();

        return GeneralUtils.concat(accountsForTransferToOwn, accountsForTransferToOther);
    }

    private TransactionalAccount getSourceAccount(final AccountIdentifier accountIdentifier,
            List<GeneralAccountEntity> sourceAccounts) {

        Optional<GeneralAccountEntity> sourceAccount = GeneralUtils.find(accountIdentifier, sourceAccounts);

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                    .build();
        }
        return ((AgreementDto) sourceAccount.get()).toTransactionalAccount();
    }

    private String validateTransfer(Transfer transfer, boolean isTransferToOwnAccount) {
        ValidateTransferResponse response = apiClient.validateTransfer(transfer, isTransferToOwnAccount);
        return response.getSignType();
    }

    private void transfer(Transfer transfer, String signType, boolean isTransferToOwnAccount)
            throws AuthenticationException {
        String signingId = apiClient.prepareTransfer(transfer, isTransferToOwnAccount);

        SignTypesResponse signTypesResponse = apiClient.signingTypes(signingId);
        String signTypeId = signTypesResponse.getSignTypeId(signType);
        String signTypeSigningId = signTypesResponse.getHeader().getSigningId().getEncoded();

        String finalSigningId = signingChallengeAndValidation(signType, signTypeId, signTypeSigningId);

        apiClient.signTransfer(finalSigningId, isTransferToOwnAccount);
    }

    private String signingChallengeAndValidation(String signType, String signTypeId, String signTypeSigningId)
            throws AuthenticationException {
        switch (signType) {
        case KbcConstants.Predicates.SIGN_TYPE_MANUAL:
            return signingChallengeAndValidationManual(signTypeId, signTypeSigningId);
        case KbcConstants.Predicates.SIGN_TYPE_SOTP:
        default:
            return signingChallengeAndValidationSotp(signTypeId, signTypeSigningId);
        }
    }

    private String signingChallengeAndValidationSotp(String signTypeId, String signTypeSigningId) {
        SigningChallengeSotpResponse signingChallengeSotpResponse = apiClient.signingChallengeSotp(signTypeId,
                signTypeSigningId);
        String finalSigningId = signingChallengeSotpResponse.getHeader().getSigningId().getEncoded();
        List<String> dataFields = signingChallengeSotpResponse.getDataFields();

        String signatureOtp = calculateSignatureOtp(dataFields);
        String panNr = credentials.getField(Field.Key.USERNAME);
        apiClient.signingValidationSotp(signatureOtp, panNr, finalSigningId);

        return finalSigningId;
    }

    private String signingChallengeAndValidationManual(String signTypeId, String signTypeSigningId) throws
            AuthenticationException {
        String finalSigningId = signTypeSigningId;

        SigningChallengeUcrResponse signingChallengeUcrResponse = apiClient.signingChallengeUcr(signTypeId,
                signTypeSigningId);
        String challenge = signingChallengeUcrResponse.getChallenge().getValue();

        String signatureOtp = waitForSignCode(challenge);
        String panNr = credentials.getField(Field.Key.USERNAME);
        apiClient.signingValidationUcr(signatureOtp, panNr, signTypeSigningId);

        return finalSigningId;
    }

    private String calculateSignatureOtp(List<String> dataFields) {
        KbcDevice device = persistentStorage.get(KbcConstants.Storage.DEVICE_KEY, KbcDevice.class).orElseThrow(
                () -> new IllegalStateException("Device data not found"));

        String signatureOtp = device.calculateSignatureOtp(dataFields);

        persistentStorage.put(KbcConstants.Storage.DEVICE_KEY, device);

        return signatureOtp;
    }

    private String waitForSignCode(String challenge) throws SupplementalInfoException {
        return waitForSupplementalInformation(KbcConstants.TransferMessage.SIGN_INSTRUCTIONS.getKey().get(),
                challenge);
    }

    private String waitForSupplementalInformation(String helpText, String controlCode)
            throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformation(
                createDescriptionField(helpText, controlCode),
                createInputField(KbcConstants.MultiFactorAuthentication.CODE))
                .get(KbcConstants.MultiFactorAuthentication.CODE);
    }

    private Field createDescriptionField(String description, String challenge) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription("Control Code");
        field.setName("description");
        field.setHelpText(description);
        field.setValue(challenge);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String name) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription("Response Code");
        field.setName(name);
        field.setNumeric(true);
        field.setHint("NNNNNNNN");
        return field;
    }
}
