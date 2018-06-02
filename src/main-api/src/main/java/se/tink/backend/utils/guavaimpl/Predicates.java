package se.tink.backend.utils.guavaimpl;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import se.tink.backend.core.Account;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Device;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.Merchant;
import se.tink.libraries.date.Period;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.property.Property;
import se.tink.backend.core.property.PropertyStatus;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.DateUtils;

public class Predicates {

    public static final <T> Predicate<T> not(Predicate<T> predicate) {
        return com.google.common.base.Predicates.not(predicate);
    }

    public static final <T> Predicate<T> or(Predicate<T> predicate1, Predicate<T> predicate2) {
        return com.google.common.base.Predicates.or(predicate1, predicate2);
    }

    public static final Predicate<Credentials> CREDENTIALS_IS_UPDATING_OR_UPDATED = c ->
            c.getStatus() == CredentialsStatus.UPDATED || c.getStatus() == CredentialsStatus.UPDATING;

    public static final Predicate<Merchant> MERCHANT_HAS_CITY = merchant -> !Strings.isNullOrEmpty(merchant.getCity());

    public static final Predicate<User> USERS_WITH_FRAUD_PERSONNUMBER = user -> user.getProfile().getFraudPersonNumber()
            != null;

    public static final Predicate<Device> ANDROID_DEVICE = d -> Objects.equals(d.getType(), "android");

    public static final Predicate<Device> IOS_DEVICE = d -> Objects.equals(d.getType(), "ios");

    public static final Predicate<FollowItem> FOLLOW_ITEMS_WITH_POSITIVE_PROGRESS = FollowItem::isProgressPositive;

    public static final Predicate<Period> PERIOD_IS_CLEAN = Period::isClean;

    public static final Predicate<Credentials> CREDENTIALS_HAS_EVER_BEEN_UPDATED = c -> c.getUpdated() != null;

    public static final Predicate<Statistic> DAILY_ACCOUNT_BALANCE_STATISTICS = s ->
            Objects.equals(s.getResolution(), ResolutionTypes.DAILY)
                    && Objects.equals(s.getType(), Statistic.Types.BALANCES_BY_ACCOUNT);

    public static Predicate<Credentials> IS_DEMO_CREDENTIALS = Credentials::isDemoCredentials;

    public static final Predicate<ProductArticle> PRODUCT_ARTICLE_WITH_INTEREST_RATE = article -> article
            .hasProperty(ProductPropertyKey.INTEREST_RATE);

    public static final Predicate<Credentials> CREDENTIALS_IS_UPDATING = c ->
            c.getStatus() == CredentialsStatus.UPDATING;

    public static Predicate<Property> ACTIVE_PROPERTY = property -> Objects
            .equals(PropertyStatus.ACTIVE, property.getStatus());

    public static Predicate<Transfer> transferIsOfType(final TransferType type) {
        return transfer -> Objects.equals(type, transfer.getType());
    }

    public static Predicate<Transfer> transferBelongsToCredentials(final UUID credentialsId) {
        return transfer -> {
            if (transfer == null) {
                return false;
            }
            return Objects.equals(credentialsId, transfer.getCredentialsId());
        };
    }

    public static Predicate<String> containsCaseInsensitive(final String string) {
        final String stringLowerCase = string != null ? string.toLowerCase() : null;

        return value -> {
            if (value == null && string == null) {
                return true;
            } else if (value == null || string == null) {
                return false;
            }

            return Objects.equals(value, string) || Objects.equals(value.toLowerCase(), stringLowerCase);
        };
    }

    public static Predicate<String> startsWith(final String s) {
        return input -> input.startsWith(s);
    }

    public static Predicate<Transaction> filterTransactionsOnIsPending(final boolean pending) {
        return t -> Objects.equals(t.isPending(), pending);
    }

    public static final Predicate<Transfer> TRANSFER_EINVOICES = t -> Objects
            .equals(t.getType(), TransferType.EINVOICE);

    public static Predicate<Transfer> transferHashIncluded(final Set<String> hashes) {
        return transfer -> hashes.contains(transfer.getHashIgnoreSource());
    }

    public static Predicate<Transfer> transferHashExcluded(final Set<String> hashes) {
        return com.google.common.base.Predicates.not(transferHashIncluded(hashes));
    }

    public static Predicate<SearchResult> removeUpcomingTransactions(
            final Map<String, Date> updatedDateByCredentialsId) {
        return input -> {
            Date updatedDate = updatedDateByCredentialsId.get(input.getTransaction().getCredentialsId());
            return updatedDate != null && input.getTransaction().getOriginalDate().before(updatedDate);
        };
    }

    public static Predicate<Transaction> transactionsWithCategoryType(final CategoryTypes categoryType) {
        return t -> Objects.equals(t.getCategoryType(), categoryType);
    }

    public static Predicate<Transaction> filterTransactionOnDate(final Date cutOffDate) {
        return t -> t.getDate().before(cutOffDate);
    }

    public static Predicate<Transaction> filterOutTransactionsBeforeCertainDate(final List<Account> accounts) {
        return new Predicate<Transaction>() {
            @Override
            public boolean apply(Transaction transaction) {
                Optional<Account> account = findAccountFor(transaction.getAccountId());

                if (!account.isPresent()) {
                    return false;
                }

                Date certainDate = account.get().getCertainDate();
                return certainDate == null || DateUtils.daysBetween(certainDate, transaction.getDate()) >= 0;
            }

            private Optional<Account> findAccountFor(String accountId) {
                for (Account account : accounts) {
                    if (Objects.equals(account.getId(), accountId)) {
                        return Optional.of(account);
                    }
                }

                return Optional.empty();
            }
        };
    }

    public static Predicate<Transaction> filterOutTransactionsWithOriginalDateBefore(final Date cutOffDate) {
        return transaction -> DateUtils.daysBetween(cutOffDate, transaction.getOriginalDate()) >= 0;
    }

    public static Predicate<Transaction> filterOutTransactionsOnCategoryId(final String categoryId) {
        return t -> !Objects.equals(t.getCategoryId(), categoryId);
    }

    public static Predicate<Transaction> filterOutTransactionsForExcludedAccounts(
            final Map<String, Account> accountsById) {
        return t -> {
            Account account = accountsById.get(t.getAccountId());
            return account != null && !account.isExcluded();
        };
    }

    public static Predicate<Statistic> statisticForTypeAndResolution(final String statisticType,
            final ResolutionTypes resolutionType) {
        return s -> Objects.equals(s.getType(), statisticType) && Objects.equals(s.getResolution(), resolutionType);
    }

    public static Predicate<Statistic> statisticForDescription(final String description) {
        return s -> Objects.equals(description, s.getDescription());
    }

    public static Predicate<SignableOperation> signableOperationsWithUnderlyingId(final UUID underlyingId) {
        return s -> Objects.equals(s.getUnderlyingId(), underlyingId);
    }

    public static Predicate<SignableOperation> signableOperationsOfType(final SignableOperationTypes type) {
        return s -> Objects.equals(s.getType(), type);
    }

    public static Predicate<SignableOperation> signableOperationsCreatedAfter(final Date cutOff) {
        return s -> s.getCreated().after(cutOff);
    }

    public static Predicate<Transfer> transfersWithIdInSet(final Set<UUID> ids) {
        return t -> ids.contains(t.getId());
    }

    public static Predicate<ApplicationForm> applicationFormOfStatus(final ApplicationFormStatusKey key) {
        return form -> Objects.equals(form.getStatus().getKey(), key);
    }

    public static Predicate<ApplicationForm> applicationFormOfName(final String formName) {
        return form -> Objects.equals(form.getName(), formName);
    }

    public static Predicate<ApplicationForm> applicationFormById(final UUID formId) {
        return form -> Objects.equals(form.getId(), formId);
    }

    public static Predicate<ApplicationField> applicationFieldOfTemplateName(final String fieldName) {
        return field -> Objects.equals(field.getTemplateName(), fieldName);
    }

    public static Predicate<ApplicationField> applicationFieldOfName(final String fieldName) {
        return field -> Objects.equals(field.getName(), fieldName);
    }

    public static Predicate<ApplicationField> APPLICATION_FIELD_HAS_ERROR = ApplicationField::hasError;

    public static Predicate<FraudDetails> fraudDetailsOfType(final FraudDetailsContentType type) {
        return details -> Objects.equals(details.getType(), type);
    }

    public static java.util.function.Predicate<Provider> providersByMarket(final String market) {
        return provider -> Objects.equals(provider.getMarket().toLowerCase(), market.toLowerCase());
    }

    public static java.util.function.Predicate<Provider> providersOfStatus(final ProviderStatuses status) {
        return provider -> Objects.equals(provider.getStatus(), status);
    }

    public static Predicate<ApplicationForm> applicationFormByParentId(final UUID parentId) {
        return form -> Objects.equals(form.getParentId(), parentId);
    }

    public static Predicate<GenericApplicationFieldGroup> fieldGroupByName(final String fieldGroupName) {
        return fieldGroup -> Objects.equals(fieldGroup.getName(), fieldGroupName);
    }

    public static Predicate<ApplicationFieldOption> fieldOptionByLabel(final String label) {
        return option -> label.equalsIgnoreCase(option.getLabel());
    }

    public static Predicate<OAuth2Client> oauth2ClientById(final String clientId) {
        return client -> Objects.equals(client.getId(), clientId);
    }

    public static Predicate<? super Application> applicationIsOfType(final ApplicationType switchMortgageProvider) {
        return (Predicate<Application>) application -> Objects.equals(application.getType(), switchMortgageProvider);
    }

    public static Predicate<Transaction> filterOutTransactionsAfter(final Date cutOffDate) {
        return transaction -> DateUtils.daysBetween(cutOffDate, transaction.getDate()) <= 0;
    }
}
