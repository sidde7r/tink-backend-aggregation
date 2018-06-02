package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.HandelsbankenV6Agent;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AbstractResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AccountGroupEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AccountListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AmountEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.DetailedPermissions;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.LinkEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransactionListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransferContextResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SHBUtils {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final Function<TransactionListResponse, String> TRANSACTION_LIST_GROUP_TO_ACCOUNT_NUMBER =
            new Function<TransactionListResponse, String>() {
                @Nullable
                @Override
                public String apply(TransactionListResponse group) {
                    return group.getAccount().getNumber();
                }
            };

    private static final ImmutableMap<String, SignableOperationStatuses> SIGNABLE_STATUSES_BY_ERROR_CODE =
            ImmutableMap.of(
                    // "Täckning saknas för överföring av angivet belopp."
                    "1010", SignableOperationStatuses.CANCELLED,
                    //  "För stort belopp, maxbelopp för betalningar överskrids."
                    "11041", SignableOperationStatuses.CANCELLED);

    public static Predicate<TransactionListResponse> getTransactionListGroupsWithAccountNumber(final Credentials credentials) {
        return group -> {
            if (group.getAccount() == null || group.getAccount().getNumber() == null) {
                return false;
            }
            return true;
        };
    }

    public static final Predicate<TransactionEntity> REMOVE_ABANDONED_OR_SUSPENDED_TRANSACTIONS =
            transactionEntity -> transactionEntity != null && !transactionEntity.isAbandonedOrSuspended();

    public static final Predicate<AbstractResponse> getEntitiesWithLinkPredicate(final String rel) {
        return ar -> SHBUtils.findLinkEntity(ar.getLinks(), rel).isPresent();
    }

    public static final Function<AbstractResponse, String> getEntitiesToStrippedLinkFunction(final String rel) {
        return ar -> stripQueryString(findLinkEntity(ar.getLinks(), rel).get().getHref());
    }

    public static Predicate<AbstractResponse> getEntitiesWithLinkPresentInList(final String rel, final List<String> links) {
        return ar -> {
            String toCompare = getEntitiesToStrippedLinkFunction(rel).apply(ar);
            return links.contains(toCompare);
        };
    }

    public static final Function<AmountEntity, String> AMOUNT_TO_POSITIVE_AMOUNT =
            ae -> {
                if (ae.getAmountFormatted() == null) {
                    return null;
                }
                return ae.getAmountFormatted().replace("-", "");
            };

    public static final Function<TransactionEntity, String> TRANSACTION_TO_POSITIVE_AMOUNT =
            t -> AMOUNT_TO_POSITIVE_AMOUNT.apply(t.getAmount());


    public static final Function<TransactionEntity, Transaction> TRANSACTION_ENTITY_TO_TRANSACTION =
            TransactionEntity::toTransaction;

    public static final Function<TransactionEntity, Transaction> TRANSACTION_ENTITY_TO_UPCOMING_TRANSACTION =
            te -> te.toTransaction(true);

    public static final Function<PaymentEntity, Transfer> PAYMENT_ENTITY_TO_TRANSFER =
            PaymentEntity::toTransfer;

    public static final Predicate<PaymentEntity> CHANGABLE_PAYMENTS =
            PaymentEntity::isChangeAllowed;

    public static final Predicate<TransactionEntity> getTransactionsDoneOnDateFilter(final String dateDaily) {
        return te -> Objects.equals(te.getDueDate(), dateDaily);
    }

    public static AccountIdentifier.Type getAccountIdentifierType(String formattedAccountNumber) {
        if (formattedAccountNumber.indexOf("-") > 0) {
            return AccountIdentifier.Type.SE;
        } else {
            return AccountIdentifier.Type.SE_SHB_INTERNAL;
        }
    }

    public static String stripQueryString(String uri) {
        if (uri.indexOf('?') == -1) {
            return uri;
        }
        return uri.substring(0, uri.indexOf('?'));
    }

    public static String unescapeAndCleanTransactionDescription(String input) {
        String unescapedDescription = unescapeTransactionDescription(input);
        return cleanDescription(unescapedDescription);
    }

    private static String unescapeTransactionDescription(String input) {
        // This is not unescapable by StringEscapeUtils for some reason...

        return input.replace("&APOS;", "'").replace("&apos;", "'");
    }

    private static String cleanDescription(String input) {
        // Sometimes the description of transactions in Handelsbanken has strange signs instead of letters.
        // It seems as each sign always represent a certain letter. We have found the ones bellow.

        return input.replace("$","Å").replace("{","Ä").replace("@","Ö");
    }

    public static Optional<PaymentEntity> findEInvoice(String approvalId, List<PaymentEntity> einvoices)
            throws IllegalArgumentException, IllegalStateException {

        if (einvoices == null) {
            return Optional.empty();
        }

        if (Strings.isNullOrEmpty(approvalId)) {
            throw new IllegalArgumentException("Approval Id cannot be null");
        }

        List<PaymentEntity> results = Lists.newArrayList();
        for (PaymentEntity einvoice : einvoices) {
            if (Objects.equals(approvalId, einvoice.getApprovalId())) {
                results.add(einvoice);
            }
        }

        if (results.size() == 0) {
            return Optional.empty();
        } else if (results.size() == 1) {
            return Optional.ofNullable(results.get(0));
        } else {
            throw new IllegalStateException("Cannot use approvalId as unique id as there are multiple "
                    + "einvoices from SHB with same id");
        }
    }

    public static AccountGroupEntity findAccountGroup(String type, List<AccountGroupEntity> groups) {
        for (AccountGroupEntity group : groups) {
            if (type.equals(group.getType())) {
                return group;
            }
        }
        return null;
    }

    public static AccountEntity findAccount(String number, List<AccountEntity> accounts) {
        for (AccountEntity account : accounts) {
            if (number.equals(account.getNumber())) {
                return account;
            }
        }
        return null;
    }

    public static Optional<AccountEntity> findAccount(AccountIdentifier identifier, AccountGroupEntity group) {
        if (group == null) {
            return Optional.empty();
        }

        AccountEntity entity = findAccount(identifier.getIdentifier(DEFAULT_FORMATTER), group.getAccounts());
        if (entity != null) {
            return Optional.of(entity);
        }

        if (identifier.getType() == AccountIdentifier.Type.SE) {
            SwedishIdentifier swedish = identifier.to(SwedishIdentifier.class);
            entity = findAccount(swedish.getAccountNumber(), group.getAccounts());
            if (entity != null) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    public static Optional<AccountEntity> findTransferSource(Transfer transfer,
            AccountListResponse accountListResponse) {

        AccountIdentifier identifier = transfer.getSource();

        AccountGroupEntity group = findAccountGroup(HandelsbankenV6Agent.ACCOUNT_GROUP_TYPE_OWN,
                accountListResponse.getAccountGroups());

        return findAccount(identifier, group);
    }

    public static boolean isTransferToOwnAccount(Transfer transfer, TransferContextResponse contextResponse) {
        return findTransferDestinationWithinOwn(transfer, contextResponse).isPresent();
    }

    public static Optional<AccountEntity> findTransferDestination(Transfer transfer,
            TransferContextResponse contextResponse) {

        Optional<AccountEntity> destination = findTransferDestinationWithinOwn(transfer, contextResponse);
        if (destination.isPresent()) {
            return destination;
        }

        return findTransferDestinationWithinOther(transfer, contextResponse);
    }

    private static Optional<AccountEntity> findTransferDestinationWithinOwn(Transfer transfer,
            TransferContextResponse contextResponse) {
        AccountIdentifier destination = transfer.getDestination();

        AccountGroupEntity group = findAccountGroup(HandelsbankenV6Agent.ACCOUNT_GROUP_TYPE_OWN,
                contextResponse.getToAccounts().getAccountGroups());

        return findAccount(destination, group);
    }

    private static Optional<AccountEntity> findTransferDestinationWithinOther(Transfer transfer,
            TransferContextResponse contextResponse) {
        AccountIdentifier identifier = transfer.getDestination();
        AccountGroupEntity group = findAccountGroup(HandelsbankenV6Agent.ACCOUNT_GROUP_TYPE_OTHER,
                contextResponse.getToAccounts().getAccountGroups());

        return findAccount(identifier, group);
    }

    public static Transfer getOriginalTransfer(Transfer transfer) throws IOException {

        Optional<String> value = transfer.getPayloadValue(TransferPayloadType.ORIGINAL_TRANSFER);
        if (!value.isPresent()) {
            throw new IllegalStateException("Original transfer is not set correctly on Transfer payload");
        }

        return MAPPER.readValue(value.get(), Transfer.class);
    }

    public static void validateUpdateIsPermitted(Catalog catalog, Transfer transfer, Transfer originalTransfer,
            PaymentEntity detailedEInvoice) {

        if (Objects.equals(transfer.getHash(), originalTransfer.getHash())) {
            return;
        }

        if (!detailedEInvoice.isChangeAllowed()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change this e-invoice")
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_NOT_ALLOWED))
                    .build();
        }

        DetailedPermissions permissions = detailedEInvoice.getDetailedPermissions();

        if (!permissions.isChangeAmount() &&
                !transfer.getAmount().equals(originalTransfer.getAmount())) {

            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change amount")
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_AMOUNT))
                    .build();
        }

        String newDueDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transfer.getDueDate());
        String originalDueDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(originalTransfer.getDueDate());

        if (!permissions.isChangeDate() &&
                !newDueDate.equals(originalDueDate)) {

            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change date")
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DUEDATE))
                    .build();
        }

        if (!permissions.isChangeMessage() &&
                !Objects.equals(transfer.getDestinationMessage(), originalTransfer.getDestinationMessage())) {

            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change destination message")
                    .setEndUserMessage(catalog.getString(
                            TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION_MESSAGE))
                    .build();
        }

        if (!permissions.isChangeFromAccount() &&
                !Objects.equals(transfer.getSource(), originalTransfer.getSource())) {

            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change source account")
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_SOURCE))
                    .build();
        }

        // Source message never editable in SHB API, therefore we always fail if user tries to edit.

        if (!Objects.equals(transfer.getSourceMessage(), originalTransfer.getSourceMessage())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Not allowed to change source message")
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_SOURCE_MESSAGE))
                    .build();
        }
    }

    public static Optional<LinkEntity> findLinkEntity(List<LinkEntity> links, final String rel) {
        if (links == null) {
            return Optional.empty();
        }

        return links.stream().filter(le -> rel.equals(le.getRel())).findFirst();
    }

    public static SignableOperationStatuses getSignableOperationStatusForErrorCode(String errorCode) {
        if (SIGNABLE_STATUSES_BY_ERROR_CODE.containsKey(errorCode)) {
            return SIGNABLE_STATUSES_BY_ERROR_CODE.get(errorCode);
        } else {
            return SignableOperationStatuses.FAILED;
        }
    }
}
