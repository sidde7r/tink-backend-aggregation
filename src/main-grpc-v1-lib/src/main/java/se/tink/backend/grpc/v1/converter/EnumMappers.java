package se.tink.backend.grpc.v1.converter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.consent.core.Action;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Loan;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.ProviderTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.auth.UserPublicKeyType;
import se.tink.backend.core.enums.ApplicationFieldType;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.RateThisAppStatus;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.property.PropertyEvent;
import se.tink.backend.core.property.PropertyType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.firehose.v1.models.Resolution;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.sms.otp.core.SmsOtpVerificationResult;
import se.tink.grpc.v1.models.Account;
import se.tink.grpc.v1.models.AuthenticationKeyType;
import se.tink.grpc.v1.models.AuthenticationSource;
import se.tink.grpc.v1.models.AuthenticationStatus;
import se.tink.grpc.v1.models.BankIdAuthenticationStatus;
import se.tink.grpc.v1.models.ConsentAction;
import se.tink.grpc.v1.models.Credential;
import se.tink.grpc.v1.models.PeriodDateBreakType;
import se.tink.grpc.v1.models.PeriodMode;
import se.tink.grpc.v1.models.Provider;
import se.tink.grpc.v1.models.RateAppStatus;
import se.tink.grpc.v1.models.SignableOperation;
import se.tink.grpc.v1.models.StatisticTree;
import se.tink.grpc.v1.models.Transaction;
import se.tink.grpc.v1.models.Transfer;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.identity.model.IdentityAnswerKey;

public class EnumMappers {
    public static final BiMap<AccountTypes, Account.Type> CORE_ACCOUNT_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<AccountTypes, Account.Type>builder()
                    .put(AccountTypes.CHECKING, Account.Type.TYPE_CHECKING)
                    .put(AccountTypes.CREDIT_CARD, Account.Type.TYPE_CREDIT_CARD)
                    .put(AccountTypes.SAVINGS, Account.Type.TYPE_SAVINGS)
                    .put(AccountTypes.INVESTMENT, Account.Type.TYPE_INVESTMENT)
                    .put(AccountTypes.MORTGAGE, Account.Type.TYPE_MORTGAGE)
                    .put(AccountTypes.LOAN, Account.Type.TYPE_LOAN)
                    .put(AccountTypes.DUMMY, Account.Type.TYPE_DUMMY)
                    .put(AccountTypes.PENSION, Account.Type.TYPE_PENSION)
                    .put(AccountTypes.OTHER, Account.Type.TYPE_OTHER)
                    .put(AccountTypes.EXTERNAL, Account.Type.TYPE_EXTERNAL)
                    .build();

    public static final BiMap<se.tink.backend.firehose.v1.models.Account.Type, Account.Type> FIREHOSE_ACCOUNT_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.firehose.v1.models.Account.Type, Account.Type>builder()
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_CHECKING, Account.Type.TYPE_CHECKING)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_CREDIT_CARD,
                            Account.Type.TYPE_CREDIT_CARD)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_SAVINGS, Account.Type.TYPE_SAVINGS)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_INVESTMENT, Account.Type.TYPE_INVESTMENT)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_MORTGAGE, Account.Type.TYPE_MORTGAGE)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_LOAN, Account.Type.TYPE_LOAN)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_DUMMY, Account.Type.TYPE_DUMMY)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_PENSION, Account.Type.TYPE_PENSION)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_OTHER, Account.Type.TYPE_OTHER)
                    .put(se.tink.backend.firehose.v1.models.Account.Type.TYPE_EXTERNAL, Account.Type.TYPE_EXTERNAL)
                    .build();

    public static final BiMap<CredentialsStatus, Credential.Status> CORE_CREDENTIALS_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<CredentialsStatus, Credential.Status>builder()
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
                    .put(CredentialsStatus.DISABLED, Credential.Status.STATUS_DISABLED)
                    .put(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION,
                            Credential.Status.STATUS_AWAITING_THIRD_PARTY_APP_AUTHENTICATION)
                    .build();

    public static final BiMap<se.tink.backend.firehose.v1.models.Credential.Status, Credential.Status> FIREHOSE_CREDENTIALS_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.firehose.v1.models.Credential.Status, Credential.Status>builder()
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_CREATED,
                            Credential.Status.STATUS_CREATED)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_AUTHENTICATING,
                            Credential.Status.STATUS_AUTHENTICATING)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_UPDATING,
                            Credential.Status.STATUS_UPDATING)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_UPDATED,
                            Credential.Status.STATUS_UPDATED)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_TEMPORARY_ERROR,
                            Credential.Status.STATUS_TEMPORARY_ERROR)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_AUTHENTICATION_ERROR,
                            Credential.Status.STATUS_AUTHENTICATION_ERROR)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_PERMANENT_ERROR,
                            Credential.Status.STATUS_PERMANENT_ERROR)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION,
                            Credential.Status.STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_AWAITING_SUPPLEMENTAL_INFORMATION,
                            Credential.Status.STATUS_AWAITING_SUPPLEMENTAL_INFORMATION)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_AWAITING_THIRD_PARTY_APP_AUTHENTICATION,
                            Credential.Status.STATUS_AWAITING_THIRD_PARTY_APP_AUTHENTICATION)
                    .put(se.tink.backend.firehose.v1.models.Credential.Status.STATUS_DISABLED,
                            Credential.Status.STATUS_DISABLED)
                    .build();

    public static final BiMap<CredentialsTypes, Credential.Type> CORE_CREDENTIALS_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<CredentialsTypes, Credential.Type>builder()
                    .put(CredentialsTypes.PASSWORD, Credential.Type.TYPE_PASSWORD)
                    .put(CredentialsTypes.MOBILE_BANKID, Credential.Type.TYPE_MOBILE_BANKID)
                    .put(CredentialsTypes.KEYFOB, Credential.Type.TYPE_KEYFOB)
                    .put(CredentialsTypes.FRAUD, Credential.Type.TYPE_FRAUD)
                    .build();

    public static final BiMap<se.tink.backend.firehose.v1.models.Credential.Type, Credential.Type> FIREHOSE_CREDENTIALS_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.firehose.v1.models.Credential.Type, Credential.Type>builder()
                    .put(se.tink.backend.firehose.v1.models.Credential.Type.TYPE_PASSWORD,
                            Credential.Type.TYPE_PASSWORD)
                    .put(se.tink.backend.firehose.v1.models.Credential.Type.TYPE_MOBILE_BANKID,
                            Credential.Type.TYPE_MOBILE_BANKID)
                    .put(se.tink.backend.firehose.v1.models.Credential.Type.TYPE_KEYFOB, Credential.Type.TYPE_KEYFOB)
                    .put(se.tink.backend.firehose.v1.models.Credential.Type.TYPE_FRAUD, Credential.Type.TYPE_FRAUD)
                    .build();

    public static final BiMap<ResolutionTypes, PeriodMode> CORE_PERIOD_MODE_TO_GRPC_MAP =
            ImmutableBiMap.<ResolutionTypes, PeriodMode>builder()
                    .put(ResolutionTypes.ALL, PeriodMode.PERIOD_MODE_ALL)
                    .put(ResolutionTypes.DAILY, PeriodMode.PERIOD_MODE_DAILY)
                    .put(ResolutionTypes.MONTHLY, PeriodMode.PERIOD_MODE_MONTHLY)
                    .put(ResolutionTypes.WEEKLY, PeriodMode.PERIOD_MODE_WEEKLY)
                    .put(ResolutionTypes.YEARLY, PeriodMode.PERIOD_MODE_YEARLY)
                    .build();


    public static final BiMap<ProviderStatuses, Provider.Status> CORE_PROVIDER_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<ProviderStatuses, Provider.Status>builder()
                    .put(ProviderStatuses.ENABLED, Provider.Status.STATUS_ENABLED)
                    .put(ProviderStatuses.OBSOLETE, Provider.Status.STATUS_OBSOLETE)
                    .put(ProviderStatuses.DISABLED, Provider.Status.STATUS_DISABLED)
                    .put(ProviderStatuses.TEMPORARY_DISABLED, Provider.Status.STATUS_TEMPORARY_DISABLED)
                    .build();

    public static final BiMap<ProviderTypes, Provider.Type> CORE_PROVIDER_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<ProviderTypes, Provider.Type>builder()
                    .put(ProviderTypes.BANK, Provider.Type.TYPE_BANK)
                    .put(ProviderTypes.CREDIT_CARD, Provider.Type.TYPE_CREDIT_CARD)
                    .put(ProviderTypes.BROKER, Provider.Type.TYPE_BROKER)
                    .put(ProviderTypes.OTHER, Provider.Type.TYPE_OTHER)
                    .put(ProviderTypes.TEST, Provider.Type.TYPE_TEST)
                    .put(ProviderTypes.FRAUD, Provider.Type.TYPE_FRAUD)
                    .build();

    public static final BiMap<se.tink.backend.core.Provider.Capability, Provider.Capability> CORE_PROVIDER_CAPABILITY_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.core.Provider.Capability, Provider.Capability>builder()
                    .put(se.tink.backend.core.Provider.Capability.MORTGAGE_AGGREGATION, Provider.Capability.CAPABILITY_MORTGAGE_AGGREGATION)
                    .put(se.tink.backend.core.Provider.Capability.TRANSFERS, Provider.Capability.CAPABILITY_TRANSFERS)
                    .build();

    public static final BiMap<RateThisAppStatus, RateAppStatus> CORE_RATE_APP_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<RateThisAppStatus, RateAppStatus>builder()
                    .put(RateThisAppStatus.USER_CLICKED_IGNORE, RateAppStatus.RATE_APP_STATUS_CLICKED_IGNORE)
                    .put(RateThisAppStatus.USER_CLICKED_RATE_IN_STORE, RateAppStatus.RATE_APP_STATUS_CLICKED_RATE_IN_STORE)
                    .build();

    public static final BiMap<SignableOperationStatuses, SignableOperation.Status> CORE_SIGNABLE_OPERATION_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<SignableOperationStatuses, SignableOperation.Status>builder()
                    .put(SignableOperationStatuses.CREATED, SignableOperation.Status.STATUS_CREATED)
                    .put(SignableOperationStatuses.EXECUTING, SignableOperation.Status.STATUS_EXECUTING)
                    .put(SignableOperationStatuses.AWAITING_CREDENTIALS,
                            SignableOperation.Status.STATUS_AWAITING_CREDENTIALS)
                    .put(SignableOperationStatuses.CANCELLED, SignableOperation.Status.STATUS_CANCELLED)
                    .put(SignableOperationStatuses.FAILED, SignableOperation.Status.STATUS_FAILED)
                    .put(SignableOperationStatuses.EXECUTED, SignableOperation.Status.STATUS_EXECUTED)
                    .build();

    public static final BiMap<SignableOperationTypes, SignableOperation.Type> CORE_SIGNABLE_OPERATION_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<SignableOperationTypes, SignableOperation.Type>builder()
                    .put(SignableOperationTypes.TRANSFER, SignableOperation.Type.TYPE_TRANSFER)
                    .put(SignableOperationTypes.ACCOUNT_CREATE, SignableOperation.Type.TYPE_ACCOUNT_CREATE)
                    .put(SignableOperationTypes.APPLICATION, SignableOperation.Type.TYPE_APPLICATION)
                    .build();

    public static final BiMap<String, StatisticTree.Type> CORE_STATISTIC_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<String, StatisticTree.Type>builder()
                    .put(Statistic.Types.BALANCES_BY_ACCOUNT, StatisticTree.Type.TYPE_BALANCES_BY_ACCOUNT_ID)
                    .put(Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP,
                            StatisticTree.Type.TYPE_BALANCES_BY_ACCOUNT_GROUP_TYPE)
                    .put(Statistic.Types.LEFT_TO_SPEND, StatisticTree.Type.TYPE_LEFT_TO_SPEND)
                    .put(Statistic.Types.EXPENSES_BY_CATEGORY, StatisticTree.Type.TYPE_EXPENCES_BY_CATEGORY_CODE)
                    .put(Statistic.Types.INCOME_BY_CATEGORY, StatisticTree.Type.TYPE_INCOME_BY_CATEGORY_CODE)
                    .build();

    public static final BiMap<se.tink.backend.firehose.v1.models.SignableOperation.Status, SignableOperation.Status> FIREHOSE_SIGNABLE_OPERATION_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.firehose.v1.models.SignableOperation.Status, SignableOperation.Status>builder()
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Status.STATUS_CREATED,
                            SignableOperation.Status.STATUS_CREATED)
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Status.STATUS_EXECUTING,
                            SignableOperation.Status.STATUS_EXECUTING)
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Status.STATUS_AWAITING_CREDENTIALS,
                            SignableOperation.Status.STATUS_AWAITING_CREDENTIALS)
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Status.STATUS_CANCELLED,
                            SignableOperation.Status.STATUS_CANCELLED)
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Status.STATUS_FAILED,
                            SignableOperation.Status.STATUS_FAILED)
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Status.STATUS_EXECUTED,
                            SignableOperation.Status.STATUS_EXECUTED)
                    .build();

    public static final BiMap<se.tink.backend.firehose.v1.models.SignableOperation.Type, SignableOperation.Type> FIREHOSE_SIGNABLE_OPERATION_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.firehose.v1.models.SignableOperation.Type, SignableOperation.Type>builder()
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Type.TYPE_TRANSFER,
                            SignableOperation.Type.TYPE_TRANSFER)
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Type.TYPE_ACCOUNT_CREATE,
                            SignableOperation.Type.TYPE_ACCOUNT_CREATE)
                    .put(se.tink.backend.firehose.v1.models.SignableOperation.Type.TYPE_APPLICATION,
                            SignableOperation.Type.TYPE_APPLICATION)
                    .build();

    public static final BiMap<TransactionTypes, Transaction.Type> CORE_TRANSACTION_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<TransactionTypes, Transaction.Type>builder()
                    .put(TransactionTypes.DEFAULT, Transaction.Type.TYPE_DEFAULT)
                    .put(TransactionTypes.CREDIT_CARD, Transaction.Type.TYPE_CREDIT_CARD)
                    .put(TransactionTypes.TRANSFER, Transaction.Type.TYPE_TRANSFER)
                    .put(TransactionTypes.PAYMENT, Transaction.Type.TYPE_PAYMENT)
                    .put(TransactionTypes.WITHDRAWAL, Transaction.Type.TYPE_WITHDRAWAL)
                    .build();

    public static final BiMap<se.tink.backend.firehose.v1.models.Transaction.Type, Transaction.Type> FIREHOSE_TRANSACTION_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.firehose.v1.models.Transaction.Type, Transaction.Type>builder()
                    .put(se.tink.backend.firehose.v1.models.Transaction.Type.TYPE_DEFAULT,
                            Transaction.Type.TYPE_DEFAULT)
                    .put(se.tink.backend.firehose.v1.models.Transaction.Type.TYPE_CREDIT_CARD,
                            Transaction.Type.TYPE_CREDIT_CARD)
                    .put(se.tink.backend.firehose.v1.models.Transaction.Type.TYPE_TRANSFER,
                            Transaction.Type.TYPE_TRANSFER)
                    .put(se.tink.backend.firehose.v1.models.Transaction.Type.TYPE_PAYMENT,
                            Transaction.Type.TYPE_PAYMENT)
                    .put(se.tink.backend.firehose.v1.models.Transaction.Type.TYPE_WITHDRAWAL,
                            Transaction.Type.TYPE_WITHDRAWAL)
                    .build();

    public static final BiMap<TransferType, Transfer.Type> CORE_TRANSFER_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<TransferType, Transfer.Type>builder()
                    .put(TransferType.BANK_TRANSFER, Transfer.Type.TYPE_BANK_TRANSFER)
                    .put(TransferType.EINVOICE, Transfer.Type.TYPE_EINVOICE)
                    .put(TransferType.PAYMENT, Transfer.Type.TYPE_PAYMENT)
                    .build();

    public static final Map<Resolution.Type, ResolutionTypes> FIREHOSE_RESOLUTION_TYPE_TO_CORE_MAP =
            ImmutableMap.<Resolution.Type, ResolutionTypes>builder()
                    .put(Resolution.Type.TYPE_DAILY, ResolutionTypes.DAILY)
                    .put(Resolution.Type.TYPE_MONTHLY, ResolutionTypes.MONTHLY)
                    .put(Resolution.Type.TYPE_MONTHLY_ADJUSTED, ResolutionTypes.MONTHLY_ADJUSTED)
                    .put(Resolution.Type.TYPE_WEEKLY, ResolutionTypes.WEEKLY)
                    .put(Resolution.Type.TYPE_YEARLY, ResolutionTypes.YEARLY)
                    .put(Resolution.Type.TYPE_ALL, ResolutionTypes.ALL)
                    .build();
    public static final Map<FirehoseMessage.Type, StreamingResponse.Type> FIREHOSE_MESSAGE_TYPE_TO_GRPC_STREAMING_TYPE_MAP =
            ImmutableMap.<FirehoseMessage.Type, StreamingResponse.Type>builder()
                    .put(FirehoseMessage.Type.CREATE, StreamingResponse.Type.CREATE)
                    .put(FirehoseMessage.Type.READ, StreamingResponse.Type.READ)
                    .put(FirehoseMessage.Type.UPDATE, StreamingResponse.Type.UPDATE)
                    .put(FirehoseMessage.Type.DELETE, StreamingResponse.Type.DELETE)
                    .build();

    public static final BiMap<se.tink.backend.core.auth.AuthenticationStatus, AuthenticationStatus> CORE_AUTHENTICATION_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.core.auth.AuthenticationStatus, AuthenticationStatus>builder()
                    .put(se.tink.backend.core.auth.AuthenticationStatus.AUTHENTICATED,
                            AuthenticationStatus.AUTHENTICATION_STATUS_AUTHENTICATED)
                    .put(se.tink.backend.core.auth.AuthenticationStatus.AUTHENTICATION_ERROR,
                            AuthenticationStatus.AUTHENTICATION_STATUS_AUTHENTICATION_ERROR)
                    .put(se.tink.backend.core.auth.AuthenticationStatus.NO_USER,
                            AuthenticationStatus.AUTHENTICATION_STATUS_NO_USER)
                    .put(se.tink.backend.core.auth.AuthenticationStatus.USER_BLOCKED,
                            AuthenticationStatus.AUTHENTICATION_STATUS_USER_BLOCKED)
                    .put(se.tink.backend.core.auth.AuthenticationStatus.AUTHENTICATED_UNAUTHORIZED_DEVICE,
                            AuthenticationStatus.AUTHENTICATION_STATUS_AUTHENTICATED_UNAUTHORIZED_DEVICE)
                    .put(se.tink.backend.core.auth.AuthenticationStatus.AUTHENTICATION_ERROR_UNAUTHORIZED_DEVICE,
                            AuthenticationStatus.AUTHENTICATION_STATUS_AUTHENTICATION_ERROR_UNAUTHORIZED_DEVICE)
                    .build();

    public static final BiMap<se.tink.backend.core.auth.AuthenticationSource, AuthenticationSource> CORE_AUTHENTICATION_SOURCE_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.core.auth.AuthenticationSource, AuthenticationSource>builder()
                    .put(se.tink.backend.core.auth.AuthenticationSource.TOUCHID,
                            AuthenticationSource.AUTHENTICATION_SOURCE_TOUCHID)
                    .put(se.tink.backend.core.auth.AuthenticationSource.FINGERPRINT,
                            AuthenticationSource.AUTHENTICATION_SOURCE_FINGERPRINT)
                    .put(se.tink.backend.core.auth.AuthenticationSource.FACEID,
                            AuthenticationSource.AUTHENTICATION_SOURCE_FACEID)
                    .build();

    public static final BiMap<UserPublicKeyType, AuthenticationKeyType> CORE_KEY_TYPE_TO_GRPC_MAP =
            ImmutableBiMap.<UserPublicKeyType, AuthenticationKeyType>builder()
                    .put(UserPublicKeyType.RSA, AuthenticationKeyType.KEY_TYPE_RSA)
                    .put(UserPublicKeyType.ECDSA, AuthenticationKeyType.KEY_TYPE_ECDSA)
                    .build();

    public static final BiMap<se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus, BankIdAuthenticationStatus> CORE_BANK_ID_STATUS_TO_GRPC_MAP =
            ImmutableBiMap.<se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus, BankIdAuthenticationStatus>builder()
                    .put(se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus.AUTHENTICATED,
                            BankIdAuthenticationStatus.BANK_ID_AUTHENTICATION_STATUS_AUTHENTICATED)
                    .put(se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus.AUTHENTICATION_ERROR,
                            BankIdAuthenticationStatus.BANK_ID_AUTHENTICATION_STATUS_AUTHENTICATION_ERROR)
                    .put(se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus.AWAITING_BANKID_AUTHENTICATION,
                            BankIdAuthenticationStatus.BANK_ID_AUTHENTICATION_STATUS_AWAITING_BANKID_AUTHENTICATION)
                    .put(se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus.NO_USER,
                            BankIdAuthenticationStatus.BANK_ID_AUTHENTICATION_STATUS_NO_USER)
                .build();

    public static final BiMap<ResolutionTypes, PeriodDateBreakType> CORE_RESOLUTION_TYPE_TO_PERIOD_SETTINGS_GRPC_MAP =
            ImmutableBiMap.<ResolutionTypes, PeriodDateBreakType>builder()
                    .put(ResolutionTypes.MONTHLY, PeriodDateBreakType.PERIOD_DATE_BREAK_TYPE_MONTHLY)
                    .put(ResolutionTypes.MONTHLY_ADJUSTED, PeriodDateBreakType.PERIOD_DATE_BREAK_TYPE_MONTHLY_ADJUSTED)
                    .build();

    public static final BiMap<Action, ConsentAction> CORE_CONSENT_ACTION_TO_GRPC_MAP =
            ImmutableBiMap.<Action, ConsentAction>builder()
                    .put(Action.ACCEPTED, ConsentAction.CONSENT_ACTION_ACCEPTED)
                    .put(Action.DECLINED, ConsentAction.CONSENT_ACTION_DECLINED)
                    .build();

    public static final Map<SmsOtpVerificationResult, se.tink.grpc.v1.models.SmsOtpStatus> CORE_SMS_OTP_STATUS_TO_GRPC_MAP =
            ImmutableMap.<SmsOtpVerificationResult, se.tink.grpc.v1.models.SmsOtpStatus>builder()
                    .put(SmsOtpVerificationResult.CORRECT_CODE,
                            se.tink.grpc.v1.models.SmsOtpStatus.SMS_OTP_STATUS_CORRECT)
                    .put(SmsOtpVerificationResult.INCORRECT_CODE,
                            se.tink.grpc.v1.models.SmsOtpStatus.SMS_OTP_STATUS_INCORRECT)
                    .put(SmsOtpVerificationResult.OTP_EXPIRED,
                            se.tink.grpc.v1.models.SmsOtpStatus.SMS_OTP_STATUS_EXPIRED)
                    .put(SmsOtpVerificationResult.TOO_MANY_VERIFICATION_ATTEMPTS,
                            se.tink.grpc.v1.models.SmsOtpStatus.SMS_OTP_STATUS_TOO_MANY_ATTEMPTS)
                    .build();

    public static final BiMap<IdentityAnswerKey, se.tink.grpc.v1.models.IdentityAnswerKey> IDENTITY_ANSWER_KEY_TO_GRPC_ANSWER_KEY =
            ImmutableBiMap.<IdentityAnswerKey, se.tink.grpc.v1.models.IdentityAnswerKey>builder()
                    .put(IdentityAnswerKey.OK, se.tink.grpc.v1.models.IdentityAnswerKey.IDENTITY_ANSWER_KEY_OK)
                    .put(IdentityAnswerKey.FRAUDULENT, se.tink.grpc.v1.models.IdentityAnswerKey.IDENTITY_ANSWER_KEY_FRAUDULENT)
                    .build();

    public static final BiMap<AuthenticationMethod, se.tink.grpc.v1.models.AuthenticationMethod> AUTHENTICATION_METHOD_TO_GRPC =
            ImmutableBiMap.<AuthenticationMethod, se.tink.grpc.v1.models.AuthenticationMethod>builder()
                    .put(AuthenticationMethod.BANKID, se.tink.grpc.v1.models.AuthenticationMethod.AUTHENTICATION_METHOD_BANKID)
                    .put(AuthenticationMethod.EMAIL_AND_PASSWORD, se.tink.grpc.v1.models.AuthenticationMethod.AUTHENTICATION_METHOD_EMAIL_AND_PASSWORD)
                    .put(AuthenticationMethod.SMS_OTP_AND_PIN6, se.tink.grpc.v1.models.AuthenticationMethod.AUTHENTICATION_METHOD_SMS_OTP_AND_PIN6)
                    .put(AuthenticationMethod.PHONE_NUMBER_AND_PIN6, se.tink.grpc.v1.models.AuthenticationMethod.AUTHENTICATION_METHOD_PHONE_NUMBER_AND_PIN6)
                    .put(AuthenticationMethod.ABN_AMRO_PIN5, se.tink.grpc.v1.models.AuthenticationMethod.AUTHENTICATION_METHOD_ABN_AMRO_PIN5)
                    .build();

    public static final BiMap<ApplicationType, se.tink.grpc.v1.models.ApplicationType> APPLICATION_TYPE_TO_GRPC =
            ImmutableBiMap.<ApplicationType, se.tink.grpc.v1.models.ApplicationType>builder()
                    .put(ApplicationType.OPEN_SAVINGS_ACCOUNT, se.tink.grpc.v1.models.ApplicationType.APPLICATION_TYPE_OPEN_SAVINGS_ACCOUNT)
                    .put(ApplicationType.RESIDENCE_VALUATION, se.tink.grpc.v1.models.ApplicationType.APPLICATION_TYPE_RESIDENCE_VALUATION)
                    .put(ApplicationType.SWITCH_MORTGAGE_PROVIDER, se.tink.grpc.v1.models.ApplicationType.APPLICATION_TYPE_SWITCH_MORTGAGE_PROVIDER)
                    .build();

    public static final BiMap<ApplicationStatusKey, se.tink.grpc.v1.models.ApplicationStatusKey> APPLICATION_STATUS_KEY_TO_GRPC =
            ImmutableBiMap.<ApplicationStatusKey, se.tink.grpc.v1.models.ApplicationStatusKey>builder()
                    .put(ApplicationStatusKey.ABORTED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_ABORTED)
                    .put(ApplicationStatusKey.APPROVED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_APPROVED)
                    .put(ApplicationStatusKey.COMPLETED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_COMPLETED)
                    .put(ApplicationStatusKey.CREATED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_CREATED)
                    .put(ApplicationStatusKey.DELETED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_DELETED)
                    .put(ApplicationStatusKey.DISQUALIFIED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_DISQUALIFIED)
                    .put(ApplicationStatusKey.ERROR, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_ERROR)
                    .put(ApplicationStatusKey.EXECUTED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_EXECUTED)
                    .put(ApplicationStatusKey.EXPIRED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_EXPIRED)
                    .put(ApplicationStatusKey.IN_PROGRESS, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_IN_PROGRESS)
                    .put(ApplicationStatusKey.REJECTED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_REJECTED)
                    .put(ApplicationStatusKey.SIGNED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_SIGNED)
                    .put(ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED, se.tink.grpc.v1.models.ApplicationStatusKey.APPLICATION_STATUS_SUPPLEMENTAL_INFORMATION_REQUIRED)
                    .build();

    public static final BiMap<ApplicationFormStatusKey, se.tink.grpc.v1.models.ApplicationFormStatusKey> APPLICATION_FORM_STATUS_KEY_TO_GRPC =
            ImmutableBiMap.<ApplicationFormStatusKey, se.tink.grpc.v1.models.ApplicationFormStatusKey>builder()
                    .put(ApplicationFormStatusKey.CREATED, se.tink.grpc.v1.models.ApplicationFormStatusKey.APPLICATION_FORM_STATUS_CREATED)
                    .put(ApplicationFormStatusKey.COMPLETED, se.tink.grpc.v1.models.ApplicationFormStatusKey.APPLICATION_FORM_STATUS_COMPLETED)
                    .put(ApplicationFormStatusKey.IN_PROGRESS, se.tink.grpc.v1.models.ApplicationFormStatusKey.APPLICATION_FORM_STATUS_IN_PROGRESS)
                    .put(ApplicationFormStatusKey.ERROR, se.tink.grpc.v1.models.ApplicationFormStatusKey.APPLICATION_FORM_STATUS_ERROR)
                    .put(ApplicationFormStatusKey.DISQUALIFIED, se.tink.grpc.v1.models.ApplicationFormStatusKey.APPLICATION_FORM_STATUS_DISQUALIFIED)
                    .put(ApplicationFormStatusKey.AUTO_SAVED, se.tink.grpc.v1.models.ApplicationFormStatusKey.APPLICATION_FORM_STATUS_AUTO_SAVED)
                    .build();

    public static final BiMap<ApplicationFieldType, se.tink.grpc.v1.models.ApplicationFieldType> APPLICATION_FIELD_TYPE_TO_GRPC =
            ImmutableBiMap.<ApplicationFieldType, se.tink.grpc.v1.models.ApplicationFieldType>builder()
                    .put(ApplicationFieldType.CHECKBOX, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_CHECKBOX)
                    .put(ApplicationFieldType.DATE, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_DATE)
                    .put(ApplicationFieldType.EMAIL, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_EMAIL)
                    .put(ApplicationFieldType.HIDDEN, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_HIDDEN)
                    .put(ApplicationFieldType.MULTI_SELECT, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_MULTI_SELECT)
                    .put(ApplicationFieldType.NUMBER, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_NUMBER)
                    .put(ApplicationFieldType.NUMERIC, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_NUMERIC)
                    .put(ApplicationFieldType.SELECT, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_SELECT)
                    .put(ApplicationFieldType.SIGNATURE, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_SIGNATURE)
                    .put(ApplicationFieldType.TEXT, se.tink.grpc.v1.models.ApplicationFieldType.APPLICATION_FIELD_TYPE_TEXT)
                    .build();

    public static final BiMap<PropertyType, se.tink.grpc.v1.models.Property.Type> PROPERTY_TYPE_TO_GRPC =
            ImmutableBiMap.<PropertyType, se.tink.grpc.v1.models.Property.Type>builder()
                    .put(PropertyType.APARTMENT, se.tink.grpc.v1.models.Property.Type.PROPERTY_TYPE_APARTMENT)
                    .put(PropertyType.HOUSE, se.tink.grpc.v1.models.Property.Type.PROPERTY_TYPE_HOUSE)
                    .put(PropertyType.VACATION_HOUSE, se.tink.grpc.v1.models.Property.Type.PROPERTY_TYPE_VACATION_HOUSE)
                    .build();

    public static final BiMap<PropertyEvent.Type, se.tink.grpc.v1.models.PropertyEvent.Type> PROPERTY_EVENT_TYPE_TO_GRPC =
            ImmutableBiMap.<PropertyEvent.Type, se.tink.grpc.v1.models.PropertyEvent.Type>builder()
                    .put(PropertyEvent.Type.INFO, se.tink.grpc.v1.models.PropertyEvent.Type.PROPERTY_EVENT_TYPE_INFO)
                    .put(PropertyEvent.Type.VALUATION_INCREASE,
                            se.tink.grpc.v1.models.PropertyEvent.Type.PROPERTY_EVENT_TYPE_VALUATION_INCREASE)
                    .put(PropertyEvent.Type.VALUATION_DECREASE,
                            se.tink.grpc.v1.models.PropertyEvent.Type.PROPERTY_EVENT_TYPE_VALUATION_DECREASE)
                    .build();

    public static final BiMap<AccountTypes, se.tink.backend.firehose.v1.models.Account.Type> CORE_TO_FIREHOSE_ACCOUNT_TYPE_MAPPING =
            ImmutableBiMap.<AccountTypes, se.tink.backend.firehose.v1.models.Account.Type>builder()
                    .put(AccountTypes.CHECKING, se.tink.backend.firehose.v1.models.Account.Type.TYPE_CHECKING)
                    .put(AccountTypes.SAVINGS, se.tink.backend.firehose.v1.models.Account.Type.TYPE_SAVINGS)
                    .put(AccountTypes.INVESTMENT, se.tink.backend.firehose.v1.models.Account.Type.TYPE_INVESTMENT)
                    .put(AccountTypes.MORTGAGE, se.tink.backend.firehose.v1.models.Account.Type.TYPE_MORTGAGE)
                    .put(AccountTypes.CREDIT_CARD, se.tink.backend.firehose.v1.models.Account.Type.TYPE_CREDIT_CARD)
                    .put(AccountTypes.LOAN, se.tink.backend.firehose.v1.models.Account.Type.TYPE_LOAN)
                    .put(AccountTypes.DUMMY, se.tink.backend.firehose.v1.models.Account.Type.TYPE_DUMMY)
                    .put(AccountTypes.PENSION, se.tink.backend.firehose.v1.models.Account.Type.TYPE_PENSION)
                    .put(AccountTypes.OTHER, se.tink.backend.firehose.v1.models.Account.Type.TYPE_OTHER)
                    .put(AccountTypes.EXTERNAL, se.tink.backend.firehose.v1.models.Account.Type.TYPE_EXTERNAL)
                    .build();


    public static final BiMap<Loan.Type, se.tink.grpc.v1.models.Loan.Type> CORE_TO_GRPC_LOAN_TYPE_MAPPING =
            ImmutableBiMap.<Loan.Type, se.tink.grpc.v1.models.Loan.Type>builder()
                    .put(Loan.Type.MORTGAGE, se.tink.grpc.v1.models.Loan.Type.TYPE_MORTGAGE)
                    .put(Loan.Type.BLANCO, se.tink.grpc.v1.models.Loan.Type.TYPE_BLANCO)
                    .put(Loan.Type.MEMBERSHIP, se.tink.grpc.v1.models.Loan.Type.TYPE_MEMBERSHIP)
                    .put(Loan.Type.VEHICLE, se.tink.grpc.v1.models.Loan.Type.TYPE_VEHICLE)
                    .put(Loan.Type.LAND, se.tink.grpc.v1.models.Loan.Type.TYPE_LAND)
                    .put(Loan.Type.STUDENT, se.tink.grpc.v1.models.Loan.Type.TYPE_STUDENT)
                    .put(Loan.Type.OTHER, se.tink.grpc.v1.models.Loan.Type.TYPE_OTHER)
                    .build();
}
