package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.entities.ChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.entities.ValidateExternalTransferResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.TrustedBeneficiariesResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.AccountIdentifier;

public class IngExternalTransferExecutor {
    private final IngApiClient apiClient;
    private final LoginResponseEntity loginResponse;
    private final PersistentStorage persistentStorage;
    private final IngTransferHelper ingTransferHelper;

    public IngExternalTransferExecutor(IngApiClient apiClient, LoginResponseEntity loginResponse,
            PersistentStorage persistentStorage, IngTransferHelper ingTransferHelper) {
        this.apiClient = apiClient;
        this.loginResponse = loginResponse;
        this.persistentStorage = persistentStorage;
        this.ingTransferHelper = ingTransferHelper;
    }

    public void executeExternalTransfer(Transfer transfer, AccountEntity sourceAccount) {
        assertTransfersToExternalAccountsAllowed(sourceAccount);

        Optional<BeneficiaryEntity> savedBeneficiary = tryFindSavedBeneficiary(transfer.getDestination());

        if (savedBeneficiary.isPresent()) {
            executeTrustedTransfer(transfer, sourceAccount, savedBeneficiary.get());
            return;
        }

        executeThirdPartyTransfer(transfer, sourceAccount);
    }

    private void executeTrustedTransfer(Transfer transfer, AccountEntity sourceAccount,
            BeneficiaryEntity destinationAccount) {
        ValidateExternalTransferResponseEntity validateExternalTransferResponseEntity = apiClient
                .validateTrustedTransfer(loginResponse, transfer, sourceAccount, destinationAccount.getIbanNumber());

        ingTransferHelper.verifyTransferValidationJsonResponse(validateExternalTransferResponseEntity);

        int otp = calcOtp(validateExternalTransferResponseEntity.getSignature().getChallenges());

        BaseMobileResponseEntity response =
                apiClient.executeTrustedTransfer(validateExternalTransferResponseEntity, otp);

        ingTransferHelper.ensureTransferExecutionWasSuccess(response.getReturnCode());
    }

    private void executeThirdPartyTransfer(Transfer transfer, AccountEntity sourceAccount) {
        String transferDestinationName = transfer.getDestination().getName()
                .orElseThrow(() -> ingTransferHelper
                        .buildTranslatedTransferException(
                                IngConstants.EndUserMessage.MISSING_DESTINATION_NAME.getKey().get(),
                                SignableOperationStatuses.CANCELLED));

        String validatedDestinationName = getDestinationNameWithinMaxLength(transferDestinationName);

        ValidateExternalTransferResponseEntity validateExternalTransferResponseEntity = apiClient
                .validateThirdPartyTransfer(loginResponse, transfer, sourceAccount,
                        transfer.getDestination().getIdentifier(), validatedDestinationName);

        ingTransferHelper.verifyTransferValidationJsonResponse(validateExternalTransferResponseEntity);

        int otp = calcOtp(validateExternalTransferResponseEntity.getSignature().getChallenges());

        BaseMobileResponseEntity response =
                apiClient.executeThirdPartyTransfer(validateExternalTransferResponseEntity, otp);

        ingTransferHelper.ensureTransferExecutionWasSuccess(response.getReturnCode());
    }

    private Optional<BeneficiaryEntity> tryFindSavedBeneficiary(AccountIdentifier accountIdentifier) {
        return apiClient.getBeneficiaries(loginResponse)
                .map(TrustedBeneficiariesResponse::getBeneficiaries)
                .flatMap(beneficiaries -> beneficiaries.stream()
                        .filter(be -> isSavedBeneficiary(be, accountIdentifier))
                        .findFirst());
    }

    private boolean isSavedBeneficiary(BeneficiaryEntity beneficiary,
            AccountIdentifier destinationAccountIdentifier) {
        String destinationIban = destinationAccountIdentifier.getIdentifier();
        return Objects.equals(beneficiary.getIbanNumber(), destinationIban);
    }

    private int calcOtp(List<ChallengeEntity> challengeList) {
        Preconditions.checkNotNull(challengeList, "No challenge list received from bank when signing transfer");
        String challenge1 = Preconditions.checkNotNull(challengeList.get(0).getChallenge());
        String challenge2 = Preconditions.checkNotNull(challengeList.get(1).getChallenge());
        int otpCounter = Integer.parseInt(persistentStorage.get(IngConstants.Storage.OTP_COUNTER));
        byte[] otpKey = EncodingUtils.decodeHexString(persistentStorage.get(IngConstants.Storage.OTP_KEY_HEX));

        int otp = IngCryptoUtils.calcOtpForSigningTransfer(otpKey, otpCounter, challenge1, challenge2);
        otpCounter++;
        persistentStorage.put(IngConstants.Storage.OTP_COUNTER, otpCounter);

        return otp;
    }

    private String getDestinationNameWithinMaxLength(String rawDestinationName) {
        return rawDestinationName.length() > 35 ? rawDestinationName.substring(0, 35) : rawDestinationName;
    }

    private void assertTransfersToExternalAccountsAllowed(AccountEntity account) {
        // If destination account isn't found among user's own accounts we must verify that the source
        // account isn't restricted to only allowing transfers to user's own accounts.
        if (!IngConstants.AccountTypes.TRANSFER_TO_OWN_RULE.equalsIgnoreCase(account.getRulesCode())) {
            // Account can transfer to external accounts
            return;
        }

        ingTransferHelper.cancelTransfer(
                IngConstants.EndUserMessage.TRANSFER_TO_EXTERNAL_ACCOUNTS_NOT_ALLOWED.getKey().get());
    }
}
