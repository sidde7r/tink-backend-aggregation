package se.tink.backend.firehose.v1.queue;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.firehose.v1.models.Account;
import se.tink.backend.firehose.v1.models.Activity;
import se.tink.backend.firehose.v1.models.Category;
import se.tink.backend.firehose.v1.models.Credential;
import se.tink.backend.firehose.v1.models.FollowItem;
import se.tink.backend.firehose.v1.models.Resolution;
import se.tink.backend.firehose.v1.models.SignableOperation;
import se.tink.backend.firehose.v1.models.Transaction;
import se.tink.backend.firehose.v1.models.UserConfiguration;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class FirehoseModelConverters {
    private static final ImmutableMap<CredentialsStatus, Credential.Status> CORE_TO_FIREHOSE_CREDENTIALS_STATUS_MAPPING = ImmutableMap.<CredentialsStatus, Credential.Status>builder()
            .put(CredentialsStatus.CREATED, Credential.Status.STATUS_CREATED)
            .put(CredentialsStatus.AUTHENTICATING, Credential.Status.STATUS_AUTHENTICATING)
            .put(CredentialsStatus.UPDATING, Credential.Status.STATUS_UPDATING)
            .put(CredentialsStatus.UPDATED, Credential.Status.STATUS_UPDATED)
            .put(CredentialsStatus.TEMPORARY_ERROR, Credential.Status.STATUS_TEMPORARY_ERROR)
            .put(CredentialsStatus.AUTHENTICATION_ERROR, Credential.Status.STATUS_AUTHENTICATION_ERROR)
            .put(CredentialsStatus.PERMANENT_ERROR, Credential.Status.STATUS_PERMANENT_ERROR)
            .put(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION,
                    Credential.Status.STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION)
            .put(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION,
                    Credential.Status.STATUS_AWAITING_SUPPLEMENTAL_INFORMATION)
            .put(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION,
                    Credential.Status.STATUS_AWAITING_THIRD_PARTY_APP_AUTHENTICATION)
            .put(CredentialsStatus.DISABLED, Credential.Status.STATUS_DISABLED)
            .build();

    private static final ImmutableMap<CredentialsTypes, Credential.Type> CORE_TO_FIREHOSE_CREDENTIALS_TYPE_MAPPING = ImmutableMap.<CredentialsTypes, Credential.Type>builder()
            .put(CredentialsTypes.PASSWORD, Credential.Type.TYPE_PASSWORD)
            .put(CredentialsTypes.KEYFOB, Credential.Type.TYPE_KEYFOB)
            .put(CredentialsTypes.MOBILE_BANKID, Credential.Type.TYPE_MOBILE_BANKID)
            .put(CredentialsTypes.FRAUD, Credential.Type.TYPE_FRAUD)
            .build();

    private static final ImmutableMap<AccountTypes, Account.Type> CORE_TO_FIREHOSE_ACCOUNT_TYPE_MAPPING = ImmutableMap.<AccountTypes, Account.Type>builder()
            .put(AccountTypes.CHECKING, Account.Type.TYPE_CHECKING)
            .put(AccountTypes.SAVINGS, Account.Type.TYPE_SAVINGS)
            .put(AccountTypes.INVESTMENT, Account.Type.TYPE_INVESTMENT)
            .put(AccountTypes.MORTGAGE, Account.Type.TYPE_MORTGAGE)
            .put(AccountTypes.CREDIT_CARD, Account.Type.TYPE_CREDIT_CARD)
            .put(AccountTypes.LOAN, Account.Type.TYPE_LOAN)
            .put(AccountTypes.DUMMY, Account.Type.TYPE_DUMMY)
            .put(AccountTypes.PENSION, Account.Type.TYPE_PENSION)
            .put(AccountTypes.OTHER, Account.Type.TYPE_OTHER)
            .put(AccountTypes.EXTERNAL, Account.Type.TYPE_EXTERNAL)
            .build();

    private static final ImmutableMap<FollowTypes, FollowItem.Type> CORE_TO_FIREHOSE_FOLLOW_ITEMS_TYPE_MAPPING = ImmutableMap.<FollowTypes, FollowItem.Type>builder()
            .put(FollowTypes.SAVINGS, FollowItem.Type.TYPE_SAVINGS)
            .put(FollowTypes.EXPENSES, FollowItem.Type.TYPE_EXPENSES)
            .put(FollowTypes.SEARCH, FollowItem.Type.TYPE_SEARCH)
            .build();

    private static final ImmutableMap<SignableOperationStatuses, SignableOperation.Status> CORE_TO_FIREHOSE_SIGNABLE_OPERATION_STATUS_MAPPING =
            ImmutableMap.<SignableOperationStatuses, SignableOperation.Status>builder()
                    .put(SignableOperationStatuses.CREATED, SignableOperation.Status.STATUS_CREATED)
                    .put(SignableOperationStatuses.EXECUTING, SignableOperation.Status.STATUS_EXECUTING)
                    .put(SignableOperationStatuses.AWAITING_CREDENTIALS,
                            SignableOperation.Status.STATUS_AWAITING_CREDENTIALS)
                    .put(SignableOperationStatuses.CANCELLED, SignableOperation.Status.STATUS_CANCELLED)
                    .put(SignableOperationStatuses.FAILED, SignableOperation.Status.STATUS_FAILED)
                    .put(SignableOperationStatuses.EXECUTED, SignableOperation.Status.STATUS_EXECUTED)
                    .build();

    private static final ImmutableMap<SignableOperation.Status, SignableOperationStatuses> FIREHOSE_TO_CORE_SIGNABLE_OPERATION_STATUS_MAPPING =
            ImmutableMap.<SignableOperation.Status, SignableOperationStatuses>builder()
                    .put(SignableOperation.Status.STATUS_EXECUTING, SignableOperationStatuses.EXECUTING)
                    .put(SignableOperation.Status.STATUS_AWAITING_CREDENTIALS,
                            SignableOperationStatuses.AWAITING_CREDENTIALS)
                    .put(SignableOperation.Status.STATUS_CANCELLED, SignableOperationStatuses.CANCELLED)
                    .put(SignableOperation.Status.STATUS_FAILED, SignableOperationStatuses.FAILED)
                    .put(SignableOperation.Status.STATUS_EXECUTED, SignableOperationStatuses.EXECUTED)
                    .build();

    private static final ImmutableMap<SignableOperationTypes, SignableOperation.Type> CORE_TO_FIREHOSE_SIGNABLE_OPERATION_TYPE_MAPPING =
            ImmutableMap.<SignableOperationTypes, SignableOperation.Type>builder()
                    .put(SignableOperationTypes.TRANSFER, SignableOperation.Type.TYPE_TRANSFER)
                    .put(SignableOperationTypes.ACCOUNT_CREATE, SignableOperation.Type.TYPE_ACCOUNT_CREATE)
                    .put(SignableOperationTypes.APPLICATION, SignableOperation.Type.TYPE_APPLICATION)
                    .build();

    private static final ImmutableMap<SignableOperation.Type, SignableOperationTypes> FIREHOSE_TO_CORE_SIGNABLE_OPERATION_TYPE_MAPPING =
            ImmutableMap.<SignableOperation.Type, SignableOperationTypes>builder()
                    .put(SignableOperation.Type.TYPE_TRANSFER, SignableOperationTypes.TRANSFER)
                    .put(SignableOperation.Type.TYPE_ACCOUNT_CREATE, SignableOperationTypes.ACCOUNT_CREATE)
                    .put(SignableOperation.Type.TYPE_APPLICATION, SignableOperationTypes.APPLICATION)
                    .build();

    private static final ImmutableMap<ResolutionTypes, Resolution.Type> CORE_TO_FIREHOSE_STATISTICS_RESOLUTION_TYPE_MAPPING = ImmutableMap.<ResolutionTypes, Resolution.Type>builder()
            .put(ResolutionTypes.DAILY, Resolution.Type.TYPE_DAILY)
            .put(ResolutionTypes.MONTHLY, Resolution.Type.TYPE_MONTHLY)
            .put(ResolutionTypes.MONTHLY_ADJUSTED, Resolution.Type.TYPE_MONTHLY_ADJUSTED)
            .put(ResolutionTypes.YEARLY, Resolution.Type.TYPE_YEARLY)
            .put(ResolutionTypes.WEEKLY, Resolution.Type.TYPE_WEEKLY)
            .put(ResolutionTypes.ALL, Resolution.Type.TYPE_ALL)
            .build();

    private static final ImmutableMap<TransactionTypes, Transaction.Type> CORE_TO_FIREHOSE_TRANSACTION_TYPE_MAPPING =
            ImmutableMap.<TransactionTypes, Transaction.Type>builder()
                    .put(TransactionTypes.DEFAULT, Transaction.Type.TYPE_DEFAULT)
                    .put(TransactionTypes.CREDIT_CARD, Transaction.Type.TYPE_CREDIT_CARD)
                    .put(TransactionTypes.TRANSFER, Transaction.Type.TYPE_TRANSFER)
                    .put(TransactionTypes.PAYMENT, Transaction.Type.TYPE_PAYMENT)
                    .put(TransactionTypes.WITHDRAWAL, Transaction.Type.TYPE_WITHDRAWAL)
                    .build();

    private static final ImmutableMap<Transaction.Type, TransactionTypes> FIREHOSE_TO_CORE_TRANSACTION_TYPE_MAPPING =
            ImmutableMap.<Transaction.Type, TransactionTypes>builder()
                    .put(Transaction.Type.TYPE_DEFAULT, TransactionTypes.DEFAULT)
                    .put(Transaction.Type.TYPE_CREDIT_CARD, TransactionTypes.CREDIT_CARD)
                    .put(Transaction.Type.TYPE_TRANSFER, TransactionTypes.TRANSFER)
                    .put(Transaction.Type.TYPE_PAYMENT, TransactionTypes.PAYMENT)
                    .put(Transaction.Type.TYPE_WITHDRAWAL, TransactionTypes.WITHDRAWAL)
                    .build();

    private static final ImmutableMap<CategoryTypes, Category.Type> CORE_TO_FIREHOSE_CATEGORY_TYPE_MAPPING =
            ImmutableMap.<CategoryTypes, Category.Type>builder()
                    .put(CategoryTypes.EXPENSES, Category.Type.TYPE_EXPENSES)
                    .put(CategoryTypes.INCOME, Category.Type.TYPE_INCOME)
                    .put(CategoryTypes.TRANSFERS, Category.Type.TYPE_TRANSFERS)
                    .build();

    private static final ImmutableMap<Category.Type, CategoryTypes> FIREHOSE_TO_CORE_CATEGORY_TYPE_MAPPING =
            ImmutableMap.<Category.Type, CategoryTypes>builder()
                    .put(Category.Type.TYPE_EXPENSES, CategoryTypes.EXPENSES)
                    .put(Category.Type.TYPE_INCOME, CategoryTypes.INCOME)
                    .put(Category.Type.TYPE_TRANSFERS, CategoryTypes.TRANSFERS)
                    .build();

    private static class FirehoseTransactionToCoreTransactionPropertyMap extends
            PropertyMap<Transaction, se.tink.backend.core.Transaction> {
        @Override
        protected void configure() {
            // Do not call this setter, because we set a new constructed category.
            skip().setCategory(null);
        }
    }

    public static void addConverters(ModelMapper modelMapper) {
        modelMapper.addMappings(new FirehoseTransactionToCoreTransactionPropertyMap());

        modelMapper.addConverter(new Converter<UUID, String>() {
            @Override
            public String convert(MappingContext<UUID, String> mappingContext) {
                return UUIDUtils.toTinkUUID(mappingContext.getSource());
            }
        });

        modelMapper.addConverter(new Converter<String, UUID>() {
            @Override
            public UUID convert(MappingContext<String, UUID> mappingContext) {
                return UUIDUtils.fromTinkUUID(mappingContext.getSource());
            }
        });

        modelMapper.addConverter(new Converter<CredentialsStatus, Credential.Status>() {
            @Override
            public Credential.Status convert(MappingContext<CredentialsStatus, Credential.Status> mappingContext) {
                return CORE_TO_FIREHOSE_CREDENTIALS_STATUS_MAPPING
                        .getOrDefault(mappingContext.getSource(), Credential.Status.STATUS_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<CredentialsTypes, Credential.Type>() {
            @Override
            public Credential.Type convert(MappingContext<CredentialsTypes, Credential.Type> mappingContext) {
                return CORE_TO_FIREHOSE_CREDENTIALS_TYPE_MAPPING
                        .getOrDefault(mappingContext.getSource(), Credential.Type.TYPE_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<AccountTypes, Account.Type>() {
            @Override
            public Account.Type convert(MappingContext<AccountTypes, Account.Type> mappingContext) {
                return CORE_TO_FIREHOSE_ACCOUNT_TYPE_MAPPING
                        .getOrDefault(mappingContext.getSource(), Account.Type.TYPE_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<FollowTypes, FollowItem.Type>() {
            @Override
            public FollowItem.Type convert(MappingContext<FollowTypes, FollowItem.Type> mappingContext) {
                return CORE_TO_FIREHOSE_FOLLOW_ITEMS_TYPE_MAPPING
                        .getOrDefault(mappingContext.getSource(), FollowItem.Type.TYPE_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<SignableOperationTypes, SignableOperation.Type>() {
            @Override
            public SignableOperation.Type convert(
                    MappingContext<SignableOperationTypes, SignableOperation.Type> mappingContext) {
                return CORE_TO_FIREHOSE_SIGNABLE_OPERATION_TYPE_MAPPING
                        .getOrDefault(mappingContext.getSource(), SignableOperation.Type.TYPE_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<SignableOperation.Type, SignableOperationTypes>() {
            @Override public SignableOperationTypes convert(
                    MappingContext<SignableOperation.Type, SignableOperationTypes> mappingContext) {
                return FIREHOSE_TO_CORE_SIGNABLE_OPERATION_TYPE_MAPPING.get(mappingContext.getSource());
            }
        });

        modelMapper.addConverter(new Converter<SignableOperationStatuses, SignableOperation.Status>() {
            @Override
            public SignableOperation.Status convert(
                    MappingContext<SignableOperationStatuses, SignableOperation.Status> mappingContext) {
                return CORE_TO_FIREHOSE_SIGNABLE_OPERATION_STATUS_MAPPING
                        .getOrDefault(mappingContext.getSource(), SignableOperation.Status.STATUS_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<SignableOperation.Status, SignableOperationStatuses>() {
            @Override public SignableOperationStatuses convert(
                    MappingContext<SignableOperation.Status, SignableOperationStatuses> mappingContext) {
                return FIREHOSE_TO_CORE_SIGNABLE_OPERATION_STATUS_MAPPING.get(mappingContext.getSource());
            }
        });

        modelMapper.addConverter(new Converter<ResolutionTypes, Resolution.Type>() {
            @Override
            public Resolution.Type convert(
                    MappingContext<ResolutionTypes, Resolution.Type> mappingContext) {
                return CORE_TO_FIREHOSE_STATISTICS_RESOLUTION_TYPE_MAPPING
                        .getOrDefault(mappingContext.getSource(), Resolution.Type.TYPE_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<TransactionTypes, Transaction.Type>() {
            @Override
            public Transaction.Type convert(
                    MappingContext<TransactionTypes, Transaction.Type> mappingContext) {
                return CORE_TO_FIREHOSE_TRANSACTION_TYPE_MAPPING
                        .getOrDefault(mappingContext.getSource(), Transaction.Type.TYPE_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<CategoryTypes, Category.Type>() {
            @Override
            public Category.Type convert(
                    MappingContext<CategoryTypes, Category.Type> mappingContext) {
                return CORE_TO_FIREHOSE_CATEGORY_TYPE_MAPPING
                        .getOrDefault(mappingContext.getSource(), Category.Type.TYPE_UNKNOWN);
            }
        });

        modelMapper.addConverter(new Converter<Transaction.Type, TransactionTypes>() {
            @Override public TransactionTypes convert(
                    MappingContext<Transaction.Type, TransactionTypes> mappingContext) {
                return FIREHOSE_TO_CORE_TRANSACTION_TYPE_MAPPING.get(mappingContext.getSource());
            }
        });

        modelMapper.addConverter(new Converter<Category.Type, CategoryTypes>() {
            @Override public CategoryTypes convert(
                    MappingContext<Category.Type, CategoryTypes> mappingContext) {
                return FIREHOSE_TO_CORE_CATEGORY_TYPE_MAPPING.get(mappingContext.getSource());
            }
        });

        modelMapper.addConverter(new AbstractConverter<se.tink.backend.core.Account, Account>() {
            @Override
            protected Account convert(se.tink.backend.core.Account account) {
                Account.Builder builder = modelMapper.map(account, Account.Builder.class);
                if (account.getImages() != null) {
                    Optional.ofNullable(account.getImages().getIcon()).ifPresent(builder::setIconUrl);
                    Optional.ofNullable(account.getImages().getBanner()).ifPresent(builder::setBannerUrl);
                }

                return builder.build();
            }
        });

        modelMapper.addConverter(new AbstractConverter<se.tink.backend.core.Credentials, Credential>() {
            @Override
            protected Credential convert(se.tink.backend.core.Credentials credentials) {
                Credential.Builder builder = modelMapper.map(credentials, Credential.Builder.class);

                builder.putAllFields(credentials.getFields());
                ConverterUtils.setIfPresent(credentials::getSupplementalInformation, builder::setSupplementalInformation);

                return builder.build();
            }
        });

        modelMapper.addConverter(new AbstractConverter<UserProfile, UserConfiguration.I18NConfiguration>() {
            @Override
            protected UserConfiguration.I18NConfiguration convert(UserProfile userProfile) {
                UserConfiguration.I18NConfiguration.Builder builder = UserConfiguration.I18NConfiguration.newBuilder();
                Optional.ofNullable(userProfile.getCurrency()).ifPresent(builder::setCurrencyCode);
                Optional.ofNullable(userProfile.getLocale()).ifPresent(builder::setLocaleCode);
                Optional.ofNullable(userProfile.getMarket()).ifPresent(builder::setMarketCode);
                Optional.ofNullable(userProfile.getTimeZone()).ifPresent(builder::setTimezoneCode);
                return builder.build();
            }
        });
    }

    public static Activity fromCoreToFirehose(se.tink.backend.core.Activity coreActivity) {
        Activity.Builder activityBuilder = Activity.newBuilder();

        ConverterUtils.setIfPresent(coreActivity::getKey, activityBuilder::setKey);
        ConverterUtils.setIfPresent(coreActivity.getDate(), activityBuilder::setDate);
        ConverterUtils.setIfPresent(coreActivity::getId, activityBuilder::setId);
        ConverterUtils.setIfPresent(coreActivity::getImportance, activityBuilder::setImportance);
        ConverterUtils.setIfPresent(coreActivity::getMessage, activityBuilder::setMessage);
        ConverterUtils.setIfPresent(coreActivity::getTitle, activityBuilder::setTitle);
        ConverterUtils.setIfPresent(coreActivity::getType, activityBuilder::setType);
        ConverterUtils.setIfPresent(coreActivity::getUserId, activityBuilder::setUserId);
        ConverterUtils.setValueIfPresent(SerializationUtils.serializeToString(coreActivity.getContent()),
                activityBuilder::setContent);

        return activityBuilder.build();
    }
}
