package se.tink.backend.main.rpc;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.utils.TransferUtils;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferEnricherException;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.GiroIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;
import static se.tink.backend.main.validators.exception.AbstractTransferException.EndUserMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;

public class TransferEnricher {
    private static final DisplayAccountIdentifierFormatter DISPLAY_FORMATTER = new DisplayAccountIdentifierFormatter();

    private final TransferDestinationPatternRepository transferDestinationPatternRepository;
    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final FraudDetailsRepository fraudDetailsRepository;

    @Inject
    public TransferEnricher(TransferDestinationPatternRepository patternRepository, AccountRepository accountRepository,
            CredentialsRepository credentialsRepository, FraudDetailsRepository fraudDetailsRepository) {
        if (patternRepository == null) {
            throw new InstantiationException(this, "No TransferDestinationPatternRepository provided");
        } else if (accountRepository == null) {
            throw new InstantiationException(this, "No AccountRepository provided");
        } else if (credentialsRepository == null) {
            throw new InstantiationException(this, "No CredentialsRepository provided");
        } else if (fraudDetailsRepository == null) {
            throw new InstantiationException(this, "No FraudDetailsRepository provided");
        }

        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.transferDestinationPatternRepository = patternRepository;
        this.fraudDetailsRepository = fraudDetailsRepository;
    }

    public Credentials enrichAndGetCredentials(Transfer transfer, Catalog catalog)
            throws TransferNotFoundException, TransferValidationException, TransferEnricherException {
        if (transfer == null) {
            throw new TransferNotFoundException();
        } else if (transfer.getUserId() == null) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_USER_ID)
                    .setEndUserMessage(EndUserMessage.ACCESS_DENIED)
                    .build(SignableOperationStatuses.FAILED);
        }

        Account sourceAccount = findAccount(transfer.getUserId(), transfer.getSource()).orElse(null);
        Preconditions.checkState(sourceAccount != null, "Source account should be present");

        Credentials credentials = findCredentialsFor(sourceAccount, transfer);
        transfer.setCredentialsId(UUIDUtils.fromTinkUUID(credentials.getId()));

        List<TransferDestinationPattern> patterns = findTransferDestinationPatternsFor(sourceAccount, transfer);

        refineDestinationIdentifierFor(transfer, patterns);
        refineSourceIdentifierFor(transfer, sourceAccount);

        if (transfer.isOfType(TransferType.BANK_TRANSFER)) {
            // Exists if destination account belongs to the user making the transfer
            Optional<Account> destinationAccount = findAccount(transfer.getUserId(), transfer.getDestination());

            enrichSourceMessage(transfer, destinationAccount, catalog);
            enrichDestinationMessage(transfer, sourceAccount, destinationAccount, catalog);
        } else {
            enrichIfGiroIdentifierWithOcr(transfer);
        }

        return credentials;
    }

    /**
     * This is a special case for e.g. Collector, where it results in a PAYMENT, although it in the app looks like a
     * BANK_TRANSFER. Those accounts have a BG/PG identifier with a fixed OCR (<GIROID>/<OCR> as identifier).
     *
     * Thus they will be missing destination message, and most probably dueDate as well. So enrich that.
     */
    private void enrichIfGiroIdentifierWithOcr(Transfer transfer) throws TransferValidationException {
        Optional<String> ocr = getOcrFrom(transfer.getDestination());

        if (ocr.isPresent()) {
            enrichPaymentOcr(transfer, ocr.get());
            enrichDueDate(transfer);
        }
    }

    private Optional<String> getOcrFrom(AccountIdentifier destination) {
        if (destination.is(AccountIdentifier.Type.SE_BG) || destination.is(AccountIdentifier.Type.SE_PG)) {
            return destination.to(GiroIdentifier.class).getOcr();
        }

        return Optional.empty();
    }

    private void enrichDueDate(Transfer transfer) {
        if (transfer.getDueDate() == null) {
            transfer.setDueDate(DateUtils.getNextBusinessDay());
        }
    }

    private void enrichPaymentOcr(Transfer transfer, String ocr) throws TransferValidationException {
        String destinationMessage = transfer.getDestinationMessage();

        if (!Strings.isNullOrEmpty(destinationMessage) && !destinationMessage.equals(ocr)) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.INVALID_DESTINATION)
                    .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                    .build(SignableOperationStatuses.FAILED);
        }

        transfer.setDestinationMessage(ocr);
    }

    private void enrichSourceMessage(Transfer transfer, Optional<Account> destinationAccount, Catalog catalog) {
        if (!Strings.isNullOrEmpty(transfer.getSourceMessage())) {
            // Not empty, don't do anything
            return;
        }

        Optional<String> name = transfer.getDestination().getName();

        String newMessage = null;

        if (destinationAccount.isPresent()) {
            // Only if the user owns the destination account
            // "Till " was added due to https://github.com/tink-ab/tink-backend/pull/4810
            String format = catalog.getString("To {0}");
            newMessage = Catalog.format(format, destinationAccount.get().getName());
        }

        if (Strings.isNullOrEmpty(newMessage) && name.isPresent()) {
            newMessage = name.get();
        }

        if (Strings.isNullOrEmpty(newMessage)) {
            newMessage = transfer.getDestination().getIdentifier(DISPLAY_FORMATTER);
        }

        transfer.setGeneratedSourceMessage(newMessage);
    }

    private void enrichDestinationMessage(Transfer transfer, Account sourceAccount, Optional<Account>
            destinationAccount, Catalog catalog) {
        if (!Strings.isNullOrEmpty(transfer.getDestinationMessage())) {
            // Not empty, don't do anything
            return;
        }

        List<FraudDetails> allIdentities = fraudDetailsRepository.findAllByUserIdAndType(
                UUIDUtils.toTinkUUID(transfer.getUserId()), FraudDetailsContentType.IDENTITY);

        String newMessage = null;

        if (destinationAccount.isPresent()) {
            // only if the user owns the destination account
            // "Från " was added due to https://github.com/tink-ab/tink-backend/pull/4810
            String format = catalog.getString("From {0}");
            newMessage = Catalog.format(format, sourceAccount.getName());
        }

        if (Strings.isNullOrEmpty(newMessage) && allIdentities != null && allIdentities.size() > 0) {
            FraudDetails mostRecentIdentity = allIdentities.stream().max(Orderings.FRAUD_DETAILS_DATE).get();
            FraudIdentityContent identityContent = (FraudIdentityContent) mostRecentIdentity.getContent();

            String firstName = !Strings.isNullOrEmpty(identityContent.getGivenName()) ?
                    identityContent.getGivenName() :
                    identityContent.getFirstName();

            newMessage = String.format("%s %s", firstName, identityContent.getLastName());
        }

        if (Strings.isNullOrEmpty(newMessage)) {
            newMessage = transfer.getSource().getIdentifier(DISPLAY_FORMATTER);
        }

        transfer.setGeneratedDestinationMessage(newMessage);
    }

    private List<Account> getAccounts(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(UUIDUtils.toTinkUUID(userId));

        return accounts != null ? accounts : Lists.<Account>newArrayList();
    }

    private void refineSourceIdentifierFor(Transfer transfer, Account sourceAccount)
            throws TransferEnricherException {
        AccountIdentifier identifier = sourceAccount.getPreferredIdentifier(transfer.getDestination().getType());

        if (identifier == null) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.NOT_FOUND_PREFERRED_SOURCE_IDENTIFIER)
                    .setEndUserMessage(EndUserMessage.INVALID_SOURCE_ACCOUNT)
                    .build(SignableOperationStatuses.FAILED);

        } else if (!identifier.isValid()) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.INVALID_PREFERRED_SOURCE)
                    .setEndUserMessage(EndUserMessage.INVALID_SOURCE_ACCOUNT)
                    .build(SignableOperationStatuses.FAILED);
        }

        transfer.setSource(identifier);
    }

    private Optional<Account> findAccount(UUID userId, AccountIdentifier identifier) {
        List<Account> accounts = getAccounts(userId);

        return TransferUtils.findAccountDefinedByIdentifier(accounts, identifier);
    }

    private void refineDestinationIdentifierFor(Transfer transfer, List<TransferDestinationPattern> patterns) throws TransferEnricherException {
        AccountIdentifier destination = transfer.getDestination();

        if (destination == null) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_DESTINATION)
                    .setEndUserMessage(EndUserMessage.MISSING_DESTINATION)
                    .build(SignableOperationStatuses.FAILED);

        } else if (!destination.isValid()) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.INVALID_DESTINATION)
                    .setEndUserMessage(EndUserMessage.MISSING_DESTINATION)
                    .build(SignableOperationStatuses.FAILED);
        }

        if (Objects.equals(destination.getType(), AccountIdentifier.Type.TINK)) {
            refineTinkDestinationIdentifier(transfer, patterns);
        } else {
            if (!TransferUtils.matchesAny(patterns, destination)) {
                throw TransferEnricherException.builder(transfer)
                        .setLogMessage(LogMessage.NO_MATCH_IDENTIFIER_PATTERNS)
                        .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                        .build(SignableOperationStatuses.FAILED);
            }

            transfer.setDestination(destination);
        }
    }

    private void refineTinkDestinationIdentifier(Transfer transfer, List<TransferDestinationPattern> patterns) throws TransferEnricherException {
        AccountIdentifier destination = transfer.getDestination();

        Optional<Account> destinationAccount = findTinkAccountByIdentifier(destination);

        if (!destinationAccount.isPresent()) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.NOT_FOUND_ACCOUNT)
                    .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                    .build(SignableOperationStatuses.FAILED);
        }

        List<AccountIdentifier> destinationIdentifiers = destinationAccount.get().getIdentifiers();

        if (destinationIdentifiers.isEmpty()) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.NOT_FOUND_ACCOUNT_IDENTIFIERS)
                    .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                    .build(SignableOperationStatuses.FAILED);
        }

        Optional<AccountIdentifier> identifier = TransferUtils.findFirstMatch(patterns, destinationIdentifiers);

        if (!identifier.isPresent()) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.NO_MATCH_ACCOUNTS_PATTERNS)
                    .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                    .build(SignableOperationStatuses.FAILED);
        }

        transfer.setDestination(identifier.get());
        transfer.setType(TransferType.typeOf(transfer.getDestination()));
    }

    private List<TransferDestinationPattern> findTransferDestinationPatternsFor(Account account, Transfer transfer) throws TransferEnricherException {
        String userId = UUIDUtils.toTinkUUID(transfer.getUserId());
        String accountId = account.getId();

        if (Strings.isNullOrEmpty(accountId)) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_ACCOUNT_ID)
                    .setEndUserMessage(EndUserMessage.ACCESS_DENIED)
                    .build(SignableOperationStatuses.FAILED);
        }

        List<TransferDestinationPattern> patterns = transferDestinationPatternRepository
                .findAllByUserIdAndAccountId(userId, accountId);

        if (patterns == null || patterns.isEmpty()) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.NOT_FOUND_PATTERNS)
                    .setEndUserMessage(EndUserMessage.ACCESS_DENIED)
                    .build(SignableOperationStatuses.FAILED);
        }

        return patterns;
    }

    private Credentials findCredentialsFor(Account account, Transfer transfer) throws TransferEnricherException {
        String credentialsId = account.getCredentialsId();

        if (Strings.isNullOrEmpty(credentialsId)) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.MISSING_CREDENTIALS_ID)
                    .setEndUserMessage(EndUserMessage.ACCESS_DENIED)
                    .build(SignableOperationStatuses.FAILED);
        }

        Credentials credentials = credentialsRepository.findOne(credentialsId);

        if (credentials == null) {
            throw TransferEnricherException.builder(transfer)
                    .setLogMessage(LogMessage.NOT_FOUND_CREDENTIALS)
                    .setEndUserMessage(EndUserMessage.ACCESS_DENIED)
                    .build(SignableOperationStatuses.FAILED);
        }

        return credentials;
    }

    private Optional<Account> findTinkAccountByIdentifier(AccountIdentifier destination) {
        return Optional.ofNullable(accountRepository.findOne(destination.getIdentifier()));
    }
}
