package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.BelfiusPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.SignProtocolResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.date.CountryDateUtils;
import se.tink.libraries.i18n.Catalog;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils.createTransferSignature;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils.getFormattedAmount;

public class BelfiusTransferExecutor implements BankTransferExecutor {

    private final BelfiusSessionStorage belfiusSessionStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final CountryDateUtils countryDateUtils;
    private final Catalog catalog;
    private BelfiusApiClient apiClient;

    public BelfiusTransferExecutor(BelfiusApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            BelfiusSessionStorage belfiusSessionStorage, Catalog catalog) {
        this.catalog = catalog;
        this.apiClient = apiClient;
        this.belfiusSessionStorage = belfiusSessionStorage;
        countryDateUtils = CountryDateUtils.getBelgianDateUtils();
        this.supplementalInformationController = supplementalInformationController;
    }

    public static String formatStructuredMessage(String structuredMessage) {
        structuredMessage = structuredMessage.replace("+++", "");
        String[] parts = structuredMessage.split("/");
        String special = Character.toString((char) 92);

        return parts[0] + special + "/" + parts[1] + special + "/" + parts[2];
    }

    @Override
    public void executeTransfer(Transfer transfer) throws TransferExecutionException {
        validateDates(transfer);

        Collection<TransactionalAccount> accounts = getTransactionalAccounts();
        getSourceAccount(transfer, accounts);
        boolean ownAccount = tryFindAccount(accounts, transfer.getDestination()).isPresent();

        if (transfer.getMessageType().equals(MessageType.STRUCTURED)) {
            transfer.setDestinationMessage(formatStructuredMessage(transfer.getDestinationMessage()));
        }

        BelfiusPaymentResponse paymentResponse = apiClient
                .executePayment(ownAccount, transfer, createClientSha(transfer),
                        transfer.getMessageType().equals(MessageType.STRUCTURED));

        boolean signed = false;

        // In the app(07.04.003) signing required two times with confirmation but ends up with technical error
        if (paymentResponse.weeklyBeneficiaryLimitReached()) {
            throw createCancelledTransferException(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT_FOR_BENEFICIARY,
                    TransferExecutionException.EndUserMessage.EXCESS_AMOUNT_FOR_BENEFICIARY);
        }

        if (paymentResponse.requireSignOfBeneficiary()) {
            addBeneficiary(transfer, transfer.getMessageType().equals(MessageType.STRUCTURED));
            signed = true;
        }

        if (paymentResponse.isDoublePayment()) {
            apiClient.doublePayment();
        }

        if (!signed && paymentResponse.requireSign()) {
            signPayments();
        }
    }


    private TransactionalAccount getSourceAccount(Transfer transfer, Collection<TransactionalAccount> accounts) {
        return tryFindAccount(accounts, transfer.getSource())
                .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE.getKey().get())
                    .build());
    }


    public void signPayments() {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response;

        try {
            response = waitForSignCode(transferSignChallenge.getChallenge(), transferSignChallenge.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }

        SignProtocolResponse signProtocolResponse = apiClient.signTransfer(response);

        if (!signProtocolResponse.signOk()) {
            multiSignTransfer(signProtocolResponse);
        }
    }

    public void checkThrowableErrors(SignProtocolResponse signProtocolResponse) throws TransferExecutionException {
        if (signProtocolResponse.weeklyCardLimitReached()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }

        if (signProtocolResponse.invalidBeneficiarySign() || signProtocolResponse.signError()) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }
    }

    public void multiSignTransfer(SignProtocolResponse signProtocolResponse) throws TransferExecutionException {
        boolean success = false;
        if (signProtocolResponse.signTempError()) {
            try {
                success = doubleSignedPayment();
            } catch (SupplementalInfoException e) {
                throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
            }
        }

        if (!success) {
            checkThrowableErrors(signProtocolResponse);
        }
    }

    public TransferExecutionException createFailedTransferException(TransferExecutionException.EndUserMessage message,
            TransferExecutionException.EndUserMessage endUserMessage){
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(catalog.getString(message))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }

    public TransferExecutionException createCancelledTransferException(TransferExecutionException.EndUserMessage message,
            TransferExecutionException.EndUserMessage endUserMessage){
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(catalog.getString(message))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }

    public boolean doubleSignedPayment() throws SupplementalInfoException, TransferExecutionException {
        apiClient.getSignProtocol().cardReaderAllowed();
        SignProtocolResponse transferSignChallenge = apiClient.getTransferSignChallenge();
        String response = waitForSignCode(transferSignChallenge.getChallenge(), transferSignChallenge.getSignType());
        SignProtocolResponse signProtocolResponse = apiClient.doubleSignTransfer(response);

        if (signProtocolResponse.signOk()) {
            return true;
        } else if (signProtocolResponse.signError()) {
            signProtocolResponse = apiClient.doubleClickPayment();
            response = waitForSignCode(signProtocolResponse.getChallenge(), signProtocolResponse.getSignType());
            signProtocolResponse = apiClient.doubleSignTransfer(response);
            checkThrowableErrors(signProtocolResponse);
            return signProtocolResponse.signOk();
        }

        return false;
    }

    public void addBeneficiary(Transfer transfer, boolean isStructuredMessage) throws TransferExecutionException {
        String response;
        try {
            String name = transfer.getDestination().getName().orElse(null);
            if (name == null) {
                name = addBeneficiaryName();
            }
            SignProtocolResponse signProtocolResponse = apiClient.addBeneficiary(transfer, isStructuredMessage, name);
            if (signProtocolResponse.getChallenge().isEmpty() || signProtocolResponse.getSignType().isEmpty()) {
                throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                        TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
            }
            response = waitForSignCode(signProtocolResponse.getChallenge(), signProtocolResponse.getSignType());
        } catch (SupplementalInfoException e) {
            throw createFailedTransferException(TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED,
                    TransferExecutionException.EndUserMessage.SIGN_TRANSFER_FAILED);
        }

        checkThrowableErrors(apiClient.signBeneficiary(response));
    }

    public void validateDates(Transfer transfer) {
        if (transfer.getDueDate() == null) {
            transfer.setDueDate(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (!countryDateUtils.isBusinessDay(transfer.getDueDate())) {
            transfer.setDueDate(countryDateUtils.getNextBusinessDay(transfer.getDueDate()));
        }
    }

    private Optional<TransactionalAccount> tryFindAccount(Collection<TransactionalAccount> accounts,
            AccountIdentifier accountIdentifier) {
        return accounts.stream()
                .filter(account -> matchingAccount(account, accountIdentifier))
                .findFirst();
    }

    private boolean matchingAccount(TransactionalAccount accountEntity, AccountIdentifier accountIdentifier) {
        return accountEntity.getIdentifiers().stream().anyMatch(identifier -> identifier.equals(accountIdentifier));
    }

    private Collection<TransactionalAccount> getTransactionalAccounts() {
        BelfiusTransactionalAccountFetcher accountFetcher = new BelfiusTransactionalAccountFetcher(apiClient);
        return accountFetcher.fetchAccounts();
    }

    public String createClientSha(Transfer transfer) {
        return createTransferSignature(belfiusSessionStorage.getChallenge(),
                "I" + ((SepaEurIdentifier) (transfer.getSource())).getIban(),
                "I" + ((SepaEurIdentifier) (transfer.getDestination())).getIban(),
                getFormattedAmount(transfer.getAmount()),
                transfer.getAmount().getCurrency());
    }

    private String addBeneficiaryName() throws SupplementalInfoException {
        String helpText = BelfiusConstants.InputFieldConstants.ADD_BENEFICIARY_NAME_HELP_TEXT.getKey().get();
        Field inputField = addBeneficiaryField(helpText);

        return supplementalInformationController.askSupplementalInformation(inputField)
                .get(BelfiusConstants.InputFieldConstants.ADD_BENEFICIARY_INP_FIELD.getKey().get());
    }

    private String waitForSignCode(String challenge, String descriptionCode) throws SupplementalInfoException {
        return waitForSupplementalInformation(
                BelfiusConstants.InputFieldConstants.WAIT_FOR_SIGN_CODE_HELP_TEXT_1.getKey().get(),
                challenge,
                BelfiusConstants.InputFieldConstants.WAIT_FOR_SIGN_CODE_HELP_TEXT_2.getKey().get(),
                descriptionCode);
    }

    private String waitForSupplementalInformation(String helpText, String controlCode, String descriptionCodeHelp,
            String descriptionCode) throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformation(
                createDescriptionField(
                        BelfiusConstants.InputFieldConstants.CONTROL_CODE_FIELD_DESCRIPTION.getKey().get(),
                        helpText,
                        controlCode),
                extraDescriptionField(
                        BelfiusConstants.InputFieldConstants.DESCRIPTION_CODE_FIELD_DESCRIPTION.getKey().get(),
                        descriptionCodeHelp,
                        descriptionCode),
                createInputField(BelfiusConstants.MultiFactorAuthentication.CODE))
                .get(BelfiusConstants.MultiFactorAuthentication.CODE);
    }

    private Field addBeneficiaryField(String helpText) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(BelfiusConstants.InputFieldConstants.BENEFICIARY_FIELD_DESCRIPTION.getKey().get());
        field.setHelpText(helpText);
        field.setHint("name");
        field.setName(BelfiusConstants.InputFieldConstants.ADD_BENEFICIARY_INP_FIELD.getKey().get());
        return field;
    }

    private Field createDescriptionField(String descriptionName, String description, String challenge) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(descriptionName);
        field.setName("description");
        field.setHelpText(description);
        field.setValue(challenge);
        field.setImmutable(true);
        return field;
    }

    private Field extraDescriptionField(String descriptionName, String description, String challenge) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(descriptionName);
        field.setHelpText(description);
        field.setValue(challenge);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String name) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(BelfiusConstants.InputFieldConstants.RESPONSE_CODE_FIELD_DESCRIPTION.getKey().get());
        field.setName(name);
        field.setNumeric(true);
        field.setHint("NNNNNNN");
        return field;
    }
}
