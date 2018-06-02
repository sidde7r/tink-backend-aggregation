package se.tink.backend.main.validators;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Amount;
import se.tink.backend.core.Currency;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.main.utils.TransferUtils;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;
import static se.tink.backend.main.validators.exception.AbstractTransferException.EndUserMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.EndUserMessageParametrized;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;
import static se.tink.backend.main.validators.exception.AbstractTransferException.LogMessageParametrized;

public class TransferValidator {
    private final SignableOperationRepository signableOperationRepository;
    private final TransferEventRepository transferEventRepository;
    private final TransfersConfiguration transferConfiguration;
    private final CacheClient cacheClient;
    private final ImmutableMap<String, Currency> currenciesByCode;
    private final AccountRepository accountRepository;

    @Inject
    public TransferValidator(TransfersConfiguration transferConfiguration, CacheClient cacheClient,
            ImmutableMap<String, Currency> curreciesByCode, TransferEventRepository transferEventRepository,
            SignableOperationRepository signableOperationRepository, AccountRepository accountRepository)
            throws InstantiationException {
        if (transferConfiguration == null) {
            throw new InstantiationException(this, "No TransfersConfiguration provided");
        } else if (cacheClient == null) {
            throw new InstantiationException(this, "No MemcachedClient provided");
        } else if (curreciesByCode == null || curreciesByCode.isEmpty()) {
            throw new InstantiationException(this, "No currencies provided");
        } else if (transferEventRepository == null) {
            throw new InstantiationException(this, "No TransferEventRepository provided");
        } else if (signableOperationRepository == null) {
            throw new InstantiationException(this, "No SignableOperationRepository provided");
        } else if (accountRepository == null) {
            throw new InstantiationException(this, "No AccountRepository provided");
        }

        this.accountRepository = accountRepository;
        this.transferEventRepository = transferEventRepository;
        this.signableOperationRepository = signableOperationRepository;
        this.transferConfiguration = transferConfiguration;
        this.cacheClient = cacheClient;
        this.currenciesByCode = curreciesByCode;
    }

    public void validate(Transfer transfer) throws TransferNotFoundException, TransferValidationException {
        if (transfer == null) {
            throw new TransferNotFoundException();
        }

        validateAmount(transfer);
        validateSourceBelongsToUser(transfer);
        validateDestination(transfer);
        validateDestinationAndSourceNotSameAccount(transfer);
        validateNotDuplicateTransfer(transfer);
    }

    private void validateDestinationAndSourceNotSameAccount(Transfer transfer) throws TransferValidationException {
        AccountIdentifier sourceAccount = transfer.getSource();
        AccountIdentifier destinationAccount = transfer.getDestination();

        if (Objects.equal(sourceAccount, destinationAccount)) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.SAME_ACCOUNT_AS_SOURCE_AND_DESTINATION)
                    .setEndUserMessage(EndUserMessage.SAME_ACCOUNT_AS_SOURCE_AND_DESTINATION)
                    .build(SignableOperationStatuses.FAILED);
        }
    }

    private void validateSourceBelongsToUser(Transfer transfer) throws TransferValidationException {
        AccountIdentifier sourceIdentifier = Preconditions.checkNotNull(transfer.getSource());
        Preconditions.checkArgument(sourceIdentifier.isValid(), "Need a valid identifier");

        List<Account> accounts = accountRepository.findByUserId(UUIDUtils.toTinkUUID(transfer.getUserId()));

        if (accounts.size() == 0) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.NOT_FOUND_ACCOUNTS)
                    .setEndUserMessage(EndUserMessage.INVALID_SOURCE_ACCOUNT)
                    .build(SignableOperationStatuses.FAILED);
        }

        Optional<Account> sourceAccount = TransferUtils.findAccountDefinedByIdentifier(accounts, sourceIdentifier);

        if (!sourceAccount.isPresent()) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.NO_MATCH_ACCOUNTS)
                    .setEndUserMessage(EndUserMessage.INVALID_SOURCE_ACCOUNT)
                    .build(SignableOperationStatuses.FAILED);
        }
    }

    private void validateDestination(Transfer transfer) throws TransferValidationException {
        if (!TransferType.accountIdentifierIsCompatibleWith(transfer.getDestination(), transfer.getType())) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES)
                    .setEndUserMessage(EndUserMessage.FAILED_EXECUTE_TRANSFER)
                    .build(SignableOperationStatuses.FAILED);
        }
    }

    /**
     * First look in the memcache if the user has done a transfer like the incoming transfer recently. If the user has done
     * that it's still not a duplicate if the last operation on that transfer led to it being failed or cancelled.
     * <p>
     * This is since if the user e.g. has some temporary issue with the bank or if he/she cancelled in BankID it should
     * still be possible to resubmit the transfer again, which is okay if it's guaranteed to not be executed (not a duplicate).
     */
    private boolean isDuplicateTransfer(Transfer transfer) {
        String storedTransferId = (String) cacheClient.get(CacheScope.TRANSFER_BY_HASH, transfer.getHash());

        return !Strings.isNullOrEmpty(storedTransferId) && !transferIsFailedOrCancelled(storedTransferId,
                transfer.getUserId());
    }

    private boolean transferIsFailedOrCancelled(final String storedTransferId, UUID userId) {
        List<SignableOperation> userTransferOperations = signableOperationRepository
                .findAllByUserIdAndType(UUIDUtils.toTinkUUID(userId), SignableOperationTypes.TRANSFER);

        List<SignableOperation> transferOperationsUpdatedAsc = userTransferOperations.stream()
                .filter(signableOperation -> {
                    String transferId = UUIDUtils.toTinkUUID(signableOperation.getUnderlyingId());
                    return Objects.equal(transferId, storedTransferId);
                }).sorted(Comparator.comparing(SignableOperation::getUpdated)).collect(Collectors.toList());

        Optional<SignableOperation> lastOperation = transferOperationsUpdatedAsc.stream().reduce((a, b) -> b);

        if (lastOperation.isPresent()) {
            switch (lastOperation.get().getStatus()) {
            case CANCELLED:
            case FAILED:
                return true;
            default:
                return false;
            }
        }

        return false;
    }

    private void validateNotDuplicateTransfer(Transfer transfer) throws TransferValidationException {
        if (isDuplicateTransfer(transfer)) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessageParametrized.DUPLICATE_TRANSFER
                            .with(transferConfiguration.getDuplicateTime()))
                    .setEndUserMessage(EndUserMessage.DUPLICATE_TRANSFER)
                    .build(SignableOperationStatuses.FAILED);
        }
    }

    private void validateAmount(Transfer transfer) throws TransferValidationException {
        if (transfer.getAmount().isEmpty()) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.INVALID_AMOUNT_UNEXPECTED)
                    .setEndUserMessage(EndUserMessage.ACCESS_DENIED)
                    .build(SignableOperationStatuses.FAILED);
        }

        validateCurrency(transfer);
        validateAmountTooLarge(transfer);
    }

    private void validateCurrency(Transfer transfer) throws TransferValidationException {
        if (!currenciesByCode.containsKey(transfer.getAmount().getCurrency())) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessage.CURRENCY_NOT_AVAILABLE)
                    .setEndUserMessage(EndUserMessageParametrized.CURRENCY_NOT_AVAILABLE
                            .cloneWith(transfer.getAmount().getCurrency()))
                    .build(SignableOperationStatuses.FAILED);
        }
    }

    private void validateAmountTooLarge(Transfer transfer) throws TransferValidationException {
        int threshold = getAmountThresholdFor(transfer.getType());
        Amount amount = transfer.getAmount();

        if (amount.isGreaterThan(threshold)) {
            String thresholdAndCurrency = threshold + " " + amount.getCurrency();

            if (transfer.isOfType(TransferType.BANK_TRANSFER)) {
                throw TransferValidationException.builder(transfer)
                        .setLogMessage(LogMessageParametrized.TO_LARGE_AMOUNT_BANK_TRANSFER
                                .with(thresholdAndCurrency))
                        .setEndUserMessage(EndUserMessageParametrized.TO_LARGE_AMOUNT_BANK_TRANSFER
                                .cloneWith(thresholdAndCurrency))
                        .build();
            } else {
                throw TransferValidationException.builder(transfer)
                        .setLogMessage(LogMessageParametrized.TO_LARGE_AMOUNT_PAYMENT
                                .with(thresholdAndCurrency))
                        .setEndUserMessage(EndUserMessageParametrized.TO_LARGE_AMOUNT_PAYMENT
                                .cloneWith(thresholdAndCurrency))
                        .build();
            }
        } else {
            validateCumulativeTransferAmount(transfer, threshold);
        }
    }

    private void validateCumulativeTransferAmount(Transfer transfer, int threshold) throws TransferValidationException {
        final double amount = transfer.getAmount().getValue();
        Date cutOffDate = DateUtils.addMinutes(new Date(), -transferConfiguration.getAggregationTime());

        Iterable<SignableOperation> signableOperations = getExecutedSignableOperationsFrom(cutOffDate,
                transfer.getUserId());
        double totalAmountToBeTransferred =
                amount + getTotalAmountTransferredFor(signableOperations, transfer.getType());

        if (totalAmountToBeTransferred > threshold) {
            throw TransferValidationException.builder(transfer)
                    .setLogMessage(LogMessageParametrized.TO_LARGE_TOTAL_AMOUNT_TRANSFERRED.
                            with(totalAmountToBeTransferred, threshold))
                    .setEndUserMessage(EndUserMessage.TO_LARGE_TOTAL_AMOUNT_TRANSFERRED)
                    .build();
        }
    }

    public void rememberTransferToExecute(Transfer transfer) {
        // We want to do transfer.getHash() on transfer since that's the hash needed for duplication check if user would post another transfer very soon...
        // ...but to be able to check status on the transfer from db we need to save the value of transfer.getId() to be able to lookup the transfer from db and check status or similar.
        cacheClient.set(CacheScope.TRANSFER_BY_HASH, transfer.getHash(), transferConfiguration.getDuplicateTime() * 60,
                UUIDUtils.toTinkUUID(transfer.getId()));
    }

    private double getTotalAmountTransferredFor(Iterable<SignableOperation> signableOperations,
            TransferType transferType) {
        double totalAmountTransferred = 0;

        for (SignableOperation executedSignableOperaton : signableOperations) {
            totalAmountTransferred += getAmountTransferredFor(executedSignableOperaton, transferType);
        }

        return totalAmountTransferred;
    }

    private double getAmountTransferredFor(SignableOperation executedSignableOperaton, TransferType transferType) {
        List<TransferEvent> transferEvents = transferEventRepository
                .findAllByUserIdAndTransferId(executedSignableOperaton.getUserId(),
                        executedSignableOperaton.getUnderlyingId());

        if (transferEvents != null) {
            return transferEvents.stream()
                    .filter(transferEvent -> transferEvent.getStatus() == SignableOperationStatuses.EXECUTED)
                    .filter(transferEvent -> transferEvent.getTransferType() == transferType)
                    .findFirst()
                    .map(transferEvent -> transferEvent.getAmount()).orElse(0D);
        }

        return 0D;
    }

    private Iterable<SignableOperation> getExecutedSignableOperationsFrom(final Date cutOffDate, UUID userId) {
        return Iterables.filter(signableOperationRepository.findAllByUserIdAndType(UUIDUtils.toTinkUUID(userId),
                SignableOperationTypes.TRANSFER), input -> input.getUpdated().after(cutOffDate) &&
                input.getStatus() == SignableOperationStatuses.EXECUTED);
    }

    private int getAmountThresholdFor(TransferType transferType) {
        return transferType.equals(TransferType.BANK_TRANSFER) ?
                transferConfiguration.getBankTransferThreshold() : transferConfiguration.getPaymentThreshold();
    }
}
