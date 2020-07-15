package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.AbstractBankIdSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSecurityTokenSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.utils.qrcode.QrCodeParser;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankTransferHelper {

    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SwedbankDefaultApiClient apiClient;
    private static final Logger log = LoggerFactory.getLogger(SwedbankTransferHelper.class);
    private final boolean isBankId;

    public SwedbankTransferHelper(
            SupplementalRequester supplementalRequester,
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            SwedbankDefaultApiClient apiClient,
            boolean isBankId) {
        this.supplementalRequester = supplementalRequester;
        this.catalog = catalog;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.apiClient = apiClient;
        this.isBankId = isBankId;
    }

    public LinksEntity collectBankId(AbstractBankIdSignResponse bankIdSignResponse) {
        boolean didOpenBankId = false;

        for (int i = 0; i < SwedbankBaseConstants.BankId.MAX_ATTEMPTS; i++) {
            SwedbankBaseConstants.BankIdResponseStatus signingStatus =
                    bankIdSignResponse.getBankIdStatus();

            switch (signingStatus) {
                case CLIENT_NOT_STARTED:
                case USER_SIGN:
                    if (!didOpenBankId) {
                        if (bankIdSignResponse.isQrCodeSigning()) {
                            final String encodedImage =
                                    apiClient.getQrCodeImageAsBase64EncodedString(
                                            bankIdSignResponse.getImageChallengeData());
                            final String autoStartToken =
                                    QrCodeParser.decodeBankIdQrCode(encodedImage);
                            supplementalRequester.openBankId(autoStartToken, false);
                        } else {
                            supplementalRequester.openBankId(null, false);
                        }
                        didOpenBankId = true;
                    }
                    break;
                case COMPLETE:
                    return bankIdSignResponse.getLinks();
                case CANCELLED:
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage(
                                    catalog.getString(
                                            TransferExecutionException.EndUserMessage
                                                    .BANKID_CANCELLED))
                            .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_CANCELLED)
                            .build();
                case TIMEOUT:
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage(
                                    TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE)
                            .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_TIMEOUT)
                            .build();
                default:
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setEndUserMessage(
                                    TransferExecutionException.EndUserMessage
                                            .BANKID_TRANSFER_FAILED)
                            .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_FAILED)
                            .build();
            }

            LinksEntity links = bankIdSignResponse.getLinks();
            if (links == null) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(
                                TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_FAILED)
                        .build();
            }

            try {
                bankIdSignResponse = apiClient.collectSignBankId(links.getNextOrThrow());
            } catch (HttpResponseException hre) {
                HttpResponse response = hre.getResponse();
                if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    // This means that another BankId session was started WHILE polling
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage(
                                    TransferExecutionException.EndUserMessage
                                            .BANKID_ANOTHER_IN_PROGRESS)
                            .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_CANCELLED)
                            .setException(hre)
                            .build();
                }

                // Re-throw unknown exception.
                throw hre;
            }

            Uninterruptibles.sleepUninterruptibly(
                    SwedbankBaseConstants.BankId.BANKID_SLEEP_INTERVAL, TimeUnit.MILLISECONDS);
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE)
                .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_CANCELLED)
                .build();
    }

    public static void validateAndSetRemittanceInformationTypeFor(Transfer transfer) {
        RemittanceInformation remittanceInformation = transfer.getRemittanceInformation();
        GiroMessageValidator giroValidator =
                GiroMessageValidator.create(OcrValidationConfiguration.softOcr());
        Optional<String> validOcr =
                giroValidator.validate(remittanceInformation.getValue()).getValidOcr();

        if (validOcr.isPresent()) {
            remittanceInformation.setType(RemittanceInformationType.OCR);
        } else {
            remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        }
    }

    public void confirmSuccessfulTransferOrThrow(
            ConfirmTransferResponse confirmTransferResponse, String idToConfirm) {

        if (confirmTransferResponse.isTransferConfirmed(idToConfirm)) {
            return;
        }

        TransactionEntity rejectedTransfer =
                confirmTransferResponse
                        .getRejectedTransfer(idToConfirm)
                        .orElseThrow(
                                () ->
                                        transferFailedWithMessage(
                                                TransferExecutionException.EndUserMessage
                                                        .TRANSFER_CONFIRM_FAILED));

        TransferExecutionException.EndUserMessage endUserMessage =
                rejectedTransfer
                        .getMessageBasedOnRejectionCause()
                        .orElseThrow(
                                () ->
                                        transferCancelledWithMessage(
                                                TransferExecutionException.EndUserMessage
                                                        .TRANSFER_REJECTED));

        throw transferCancelledWithMessage(endUserMessage);
    }

    public static AccountIdentifier getDestinationAccount(Transfer transfer) {
        AccountIdentifier destinationAccount = transfer.getDestination();
        if (destinationAccount == null || !destinationAccount.isValid()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION)
                    .build();
        }

        return destinationAccount;
    }

    public static AccountIdentifier getSourceAccount(Transfer transfer) {
        AccountIdentifier sourceAccount = transfer.getSource();
        if (sourceAccount == null || !sourceAccount.isValid()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_SOURCE)
                    .build();
        }

        return sourceAccount;
    }

    public static void ensureLinksNotNull(
            LinksEntity linksEntity,
            TransferExecutionException.EndUserMessage endUserMessage,
            String message) {
        if (linksEntity == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(endUserMessage)
                    .setMessage(message)
                    .build();
        }
    }

    public String getDestinationName(final Transfer transfer) {
        return transfer.getDestination()
                .getName()
                .orElseGet(
                        () ->
                                requestRecipientNameSupplemental()
                                        .orElseThrow(
                                                () ->
                                                        TransferExecutionException.builder(
                                                                        SignableOperationStatuses
                                                                                .CANCELLED)
                                                                .setMessage(
                                                                        "Could not get recipient name from user")
                                                                .setEndUserMessage(
                                                                        catalog.getString(
                                                                                TransferExecutionException
                                                                                        .EndUserMessage
                                                                                        .NEW_RECIPIENT_NAME_ABSENT))
                                                                .build()));
    }

    private Optional<String> requestRecipientNameSupplemental() {
        // If we're adding the recipient, we need to ask the user to name it.
        try {
            String beneficiary = supplementalInformationHelper.waitForAddBeneficiaryInput();
            if (Strings.isNullOrEmpty(beneficiary)) {
                return Optional.empty();
            }
            return Optional.of(beneficiary);
        } catch (SupplementalInfoException sie) {
            return Optional.empty();
        }
    }

    private Optional<PaymentBaseinfoResponse> getConfirmResponse(LinkEntity linkEntity) {
        try {
            return Optional.of(apiClient.confirmSignNewRecipient(linkEntity));
        } catch (Exception e) {
            log.warn("Failed to confirm new recipient signature", e);
            return Optional.empty();
        }
    }

    /**
     * Signs and confirms the creation of a new recipient or payee. Returns the created recipient or
     * throws an exception if the creation failed.
     */
    public AbstractAccountEntity signAndConfirmNewRecipient(
            LinksEntity linksEntity,
            Function<PaymentBaseinfoResponse, Optional<AbstractAccountEntity>>
                    findNewRecipientFunction) {

        return signNewRecipient(linksEntity.getSign())
                .map(LinksEntity::getNext)
                .flatMap(this::getConfirmResponse)
                .flatMap(findNewRecipientFunction)
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                TransferExecutionException.EndUserMessage
                                                        .NEW_RECIPIENT_FAILED
                                                        .getKey()
                                                        .get())
                                        .setEndUserMessage(
                                                catalog.getString(
                                                        TransferExecutionException.EndUserMessage
                                                                .NEW_RECIPIENT_FAILED))
                                        .build());
    }

    private Optional<LinksEntity> signNewRecipient(LinkEntity signLink) {
        if (isBankId) {
            InitiateSignTransferResponse initiateSignTransfer =
                    apiClient.signExternalTransferBankId(signLink);
            return Optional.ofNullable(collectBankId(initiateSignTransfer));
        } else {
            InitiateSecurityTokenSignTransferResponse initiateSecurityTokenSignTransferResponse =
                    apiClient.signExternalTransferSecurityToken(signLink);
            return collectTokenNewBeneficiary(initiateSecurityTokenSignTransferResponse);
        }
    }

    private Optional<LinksEntity> collectTokenNewBeneficiary(
            InitiateSecurityTokenSignTransferResponse initiateSecurityTokenSignTransferResponse) {
        Optional<String> challengeResponse =
                requestSecurityTokenSignBeneficiaryChallengeSupplemental(
                        initiateSecurityTokenSignTransferResponse.getChallenge());
        if (!challengeResponse.isPresent()) {
            return Optional.empty();
        }
        try {
            RegisterTransferResponse signNewRecipientResponse =
                    apiClient.sendTokenChallengeResponse(
                            initiateSecurityTokenSignTransferResponse.getLinks().getNextOrThrow(),
                            challengeResponse.get(),
                            RegisterTransferResponse.class);
            return Optional.ofNullable(signNewRecipientResponse.getLinks());
        } catch (SupplementalInfoException sie) {
            return Optional.empty();
        }
    }

    public Optional<String> requestSecurityTokenSignBeneficiaryChallengeSupplemental(
            String challenge) {
        try {
            return Optional.ofNullable(
                    supplementalInformationHelper.waitForSignForBeneficiaryChallengeResponse(
                            challenge));
        } catch (SupplementalInfoException e) {
            return Optional.empty();
        }
    }

    public Optional<String> requestSecurityTokenSignTransferChallengeSupplemental(
            String challenge) {
        try {
            return Optional.ofNullable(
                    supplementalInformationHelper.waitForSignForTransferChallengeResponse(
                            challenge));
        } catch (SupplementalInfoException e) {
            return Optional.empty();
        }
    }

    private TransferExecutionException transferFailedWithMessage(
            TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(endUserMessage)
                .setMessage(endUserMessage.getKey().get())
                .build();
    }

    private TransferExecutionException transferCancelledWithMessage(
            TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(endUserMessage)
                .setMessage(endUserMessage.getKey().get())
                .build();
    }

    public LinksEntity tokenSignTransfer(LinksEntity links) {
        InitiateSecurityTokenSignTransferResponse initiateSecurityTokenSignTransferResponse =
                apiClient.signExternalTransferSecurityToken(links.getSignOrThrow());
        String challenge = initiateSecurityTokenSignTransferResponse.getChallenge();
        Optional<String> challengeResponse =
                requestSecurityTokenSignTransferChallengeSupplemental(challenge);
        if (!challengeResponse.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("No security token provided. Transfer failed.")
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.CHALLENGE_NO_RESPONSE)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.CHALLENGE_NO_RESPONSE)
                    .build();
        }

        RegisterTransferResponse registerTransferResponse;
        try {
            registerTransferResponse =
                    apiClient.sendTokenChallengeResponse(
                            initiateSecurityTokenSignTransferResponse.getLinks().getNextOrThrow(),
                            challengeResponse.get(),
                            RegisterTransferResponse.class);
        } catch (SupplementalInfoException sie) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Invalid security token provided. Transfer failed.")
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.TOKEN_SIGN_FAILED)
                    .setException(sie)
                    .build();
        }
        return registerTransferResponse.getLinks();
    }

    public boolean isBankId() {
        return isBankId;
    }
}
