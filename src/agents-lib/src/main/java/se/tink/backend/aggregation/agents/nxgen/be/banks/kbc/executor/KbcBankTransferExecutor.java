package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor;

import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.amount.Amount;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.pair.Pair;

import java.util.List;
import java.util.Optional;

public class KbcBankTransferExecutor implements BankTransferExecutor {

    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final KbcApiClient apiClient;
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public KbcBankTransferExecutor(
            final Credentials credentials,
            final PersistentStorage persistentStorage,
            final KbcApiClient apiClient,
            final Catalog catalog,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        List<GeneralAccountEntity> ownAccounts = fetchOwnAccounts();

        Pair<TransactionalAccount, AgreementDto> sourceAccountAgreement =
                getSourceAccountAndAgreement(transfer.getSource(), ownAccounts);

        // For instant transfers it is not allowed to exceed current balance. Blocked in app.
        boolean instantTransfer = instantTransfer(transfer);
        if (instantTransfer) {
            validateAmountCoveredByBalance(sourceAccountAgreement.first, transfer.getAmount());
        }

        boolean isTransferToOwnAccount = GeneralUtils.isAccountExisting(transfer.getDestination(), ownAccounts);

        String signType = validateTransfer(transfer, sourceAccountAgreement.second, isTransferToOwnAccount);

        KbcConstants.Url url = getTransferUrl(isTransferToOwnAccount, instantTransfer);

        try {
            return transfer(transfer, signType, isTransferToOwnAccount, url, sourceAccountAgreement.second);
        } catch (AuthenticationException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED))
                    .build();
        }
    }

    private KbcConstants.Url getTransferUrl(boolean isTransferToOwnAccount, boolean instantTransfer) {
        if (instantTransfer) {
            return isTransferToOwnAccount
                    ? KbcConstants.Url.TRANSFER_TO_OWN_INSTANT
                    : KbcConstants.Url.TRANSFER_TO_OTHER_INSTANT;
        } else if (isTransferToOwnAccount) {
            return KbcConstants.Url.TRANSFER_TO_OWN;
        }
        return KbcConstants.Url.TRANSFER_TO_OTHER;
    }

    private boolean instantTransfer(Transfer transfer) {
        return transfer.getDueDate() == null;
    }

    private void validateAmountCoveredByBalance(TransactionalAccount sourceAccount, Amount amount) {
        if (sourceAccount.getBalance()
                .isLessThan(amount.doubleValue())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT.getKey().get()))
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }
    }

    private List<GeneralAccountEntity> fetchOwnAccounts() {
        List<AgreementDto> accountsForTransferToOwn = apiClient.accountsForTransferToOwn().getAgreements();
        List<AgreementDto> accountsForTransferToOther = apiClient.accountsForTransferToOther().getAgreements();

        return GeneralUtils.concat(accountsForTransferToOwn, accountsForTransferToOther);
    }

    private Pair<TransactionalAccount, AgreementDto> getSourceAccountAndAgreement(
            final AccountIdentifier accountIdentifier,
            List<GeneralAccountEntity> sourceAccounts)
    {
        Optional<GeneralAccountEntity> sourceAccount = GeneralUtils.find(accountIdentifier, sourceAccounts);

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                    .build();
        }
        return new Pair<>(((AgreementDto) sourceAccount.get()).toTransactionalAccount(), (AgreementDto) sourceAccount.get());
    }

    private String validateTransfer(Transfer transfer, AgreementDto sourceAccount, boolean isTransferToOwnAccount) {
        ValidateTransferResponse response = apiClient.validateTransfer(transfer, sourceAccount, isTransferToOwnAccount);
        return response.getSignType();
    }

    private Optional<String> transfer(Transfer transfer,
                                      String signType,
                                      boolean isTransferToOwnAccount,
                                      KbcConstants.Url url,
                                      AgreementDto sourceAccount)
            throws AuthenticationException {
        String signingId = apiClient.prepareTransfer(transfer, isTransferToOwnAccount, url, sourceAccount);

        SignTypesResponse signTypesResponse = apiClient.signingTypes(signingId);
        String signTypeId = signTypesResponse.getSignTypeId(signType);
        String signTypeSigningId = signTypesResponse.getHeader().getSigningId().getEncoded();

        String finalSigningId = signingChallengeAndValidation(signType, signTypeId, signTypeSigningId);

        apiClient.signTransfer(finalSigningId, url);
        return Optional.empty();
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

        String response = supplementalInformationHelper.waitForSignCodeChallengeResponse(challenge);
        String panNr = credentials.getField(Field.Key.USERNAME);
        apiClient.signingValidationUcr(response, panNr, signTypeSigningId);

        return finalSigningId;
    }

    private String calculateSignatureOtp(List<String> dataFields) {
        KbcDevice device = persistentStorage.get(KbcConstants.Storage.DEVICE_KEY, KbcDevice.class).orElseThrow(
                () -> new IllegalStateException("Device data not found"));

        String signatureOtp = device.calculateSignatureOtp(dataFields);

        persistentStorage.put(KbcConstants.Storage.DEVICE_KEY, device);

        return signatureOtp;
    }
}
