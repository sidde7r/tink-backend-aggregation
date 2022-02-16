package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.converter;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.BaseAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.DebtorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.InstructedAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentInitiation.DomesticPaymentInitiationBuilder;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.account.identifiers.PaymPhoneNumberIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public abstract class PaymentConverterBase {

    private static final GenericTypeMapper<String, AccountIdentifierType>
            PAYMENT_SCHEME_TYPE_MAPPER =
                    GenericTypeMapper.<String, AccountIdentifierType>genericBuilder()
                            .put("UK.OBIE.Paym", AccountIdentifierType.PAYM_PHONE_NUMBER)
                            .put("UK.OBIE.PAN", AccountIdentifierType.PAYMENT_CARD_NUMBER)
                            .put("UK.OBIE.SortCodeAccountNumber", AccountIdentifierType.SORT_CODE)
                            .put("UK.OBIE.IBAN", AccountIdentifierType.IBAN)
                            .build();

    private static final TypeMapper<PaymentStatus> PAYMENT_STATUS_TYPE_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "Consumed", "Authorised", "Pending")
                    .put(PaymentStatus.CREATED, "AwaitingAuthorisation")
                    .put(PaymentStatus.REJECTED, "Rejected")
                    .put(
                            PaymentStatus.PAID,
                            "AcceptedSettlementCompleted",
                            "AcceptedSettlementInProcess")
                    .put(PaymentStatus.SETTLEMENT_COMPLETED, "AcceptedCreditSettlementCompleted")
                    .build();

    private static final String PAYMENT_CREDITOR_DEFAULT_NAME = "Payment Receiver";

    /**
     * https://openbankinguk.github.io/read-write-api-site3/v3.1.8/references/namespaced-enumerations.html#obexternallocalinstrument1code
     */
    public static final String FASTER_PAYMENTS_LOCAL_INSTRUMENT_CODE = "UK.OBIE.FPS";

    public static final String FI_LOCAL_INSTRUMENT_CODE = "DK.DanskeBank.FI.TransferFunds";
    public static final String SE_LOCAL_INSTRUMENT_CODE = "DK.DanskeBank.SE.TransferFunds";
    public static final String DK_LOCAL_INSTRUMENT_CODE = "DK.DanskeBank.DK.TransferFunds";
    public static final String NO_LOCAL_INSTRUMENT_CODE = "DK.DanskeBank.NO.TransferFunds";
    public static final String NO_LOCAL_INSTRUMENT_CODE_KID = "DK.DanskeBank.NO.TransferFunds.KID";

    public DebtorAccount getDebtorAccount(Payment payment) {
        return Optional.ofNullable(payment.getDebtor())
                .map(this::convertDebtorToDebtorAccount)
                .orElse(null);
    }

    public CreditorAccount getCreditorAccount(Payment payment) {
        return Optional.ofNullable(payment.getCreditor())
                .map(this::convertCreditorToCreditorAccount)
                .orElse(null);
    }

    public RemittanceInformationDto getRemittanceInformationDto(Payment payment) {
        final String value =
                Optional.ofNullable(payment.getRemittanceInformation())
                        .map(RemittanceInformation::getValue)
                        .orElse("");

        final RemittanceInformationType type =
                Optional.ofNullable(payment.getRemittanceInformation())
                        .map(RemittanceInformation::getType)
                        .orElse(null);

        if (type == RemittanceInformationType.UNSTRUCTURED) {
            return RemittanceInformationDto.builder().unstructured(value).build();
        } else if (type == RemittanceInformationType.REFERENCE) {
            return RemittanceInformationDto.builder().reference(value).build();
        } else {
            return RemittanceInformationDto.builder().unstructured(value).reference(value).build();
        }
    }

    public InstructedAmount getInstructedAmount(Payment payment) {
        return Optional.ofNullable(payment.getExactCurrencyAmountFromField())
                .map(InstructedAmount::new)
                .orElse(null);
    }

    public String getLocalInstrument(Payment payment) {
        if (payment.getPaymentScheme() != PaymentScheme.FASTER_PAYMENTS) {
            log.info("Unsupported payment scheme, we allow it for now");
            return null;
        }

        return FASTER_PAYMENTS_LOCAL_INSTRUMENT_CODE;
    }

    public void getEuLocalInstrument(
            DomesticPaymentInitiationBuilder domesticPaymentInitiation,
            Payment payment,
            String providerMarketCode) {
        MarketCode marketCode = MarketCode.valueOf(providerMarketCode.toUpperCase());
        switch (marketCode) {
            case FI:
                domesticPaymentInitiation.localInstrument(FI_LOCAL_INSTRUMENT_CODE);
                return;
            case NO:
                if (getRemittanceInformationType(payment) == RemittanceInformationType.REFERENCE) {
                    domesticPaymentInitiation.localInstrument(NO_LOCAL_INSTRUMENT_CODE_KID);
                } else {
                    domesticPaymentInitiation.localInstrument(NO_LOCAL_INSTRUMENT_CODE);
                }
                return;
            case SE:
                domesticPaymentInitiation.localInstrument(SE_LOCAL_INSTRUMENT_CODE);
                return;
            case DK:
                domesticPaymentInitiation.localInstrument(DK_LOCAL_INSTRUMENT_CODE);
                return;
            default:
        }
    }

    public ExactCurrencyAmount convertInstructedAmountToExactCurrencyAmount(
            InstructedAmount instructedAmount) {
        return new ExactCurrencyAmount(
                new BigDecimal(instructedAmount.getAmount()), instructedAmount.getCurrency());
    }

    public Debtor convertDebtorAccountToDebtor(DebtorAccount debtorAccount) {
        return Optional.ofNullable(debtorAccount)
                .map(
                        account -> {
                            log.info(
                                    "Received Valid Debtor and Converting to Tink Account Identifier");
                            return createAccountIdentifier(account);
                        })
                .map(Debtor::new)
                .orElse(null);
    }

    protected RemittanceInformation createRemittanceInformation(
            RemittanceInformationDto remittanceInformationDto) {
        final RemittanceInformation remittanceInformation = new RemittanceInformation();

        if (StringUtils.isNotBlank(remittanceInformationDto.getUnstructured())) {
            remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
            remittanceInformation.setValue(remittanceInformationDto.getUnstructured());
        } else {
            remittanceInformation.setType(RemittanceInformationType.REFERENCE);
            remittanceInformation.setValue(remittanceInformationDto.getReference());
        }

        return remittanceInformation;
    }

    protected PaymentStatus convertResponseStatusToPaymentStatus(String responseStatus) {
        return PAYMENT_STATUS_TYPE_MAPPER
                .translate(responseStatus)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "%s unknown payment status!", responseStatus)));
    }

    protected Creditor convertCreditorAccountToCreditor(CreditorAccount creditorAccount) {
        return new Creditor(createAccountIdentifier(creditorAccount), creditorAccount.getName());
    }

    private DebtorAccount convertDebtorToDebtorAccount(Debtor debtor) {
        return Optional.ofNullable(debtor.getAccountIdentifier())
                .map(
                        accountIdentifier -> {
                            log.info(
                                    "Received Valid Debtor and Converting to Tink Account Identifier");
                            return DebtorAccount.builder()
                                    .schemeName(getSchemeName(accountIdentifier.getType()))
                                    .identification(accountIdentifier.getIdentifier())
                                    .build();
                        })
                .orElse(null);
    }

    private CreditorAccount convertCreditorToCreditorAccount(Creditor creditor) {
        final String schemeName = getSchemeName(creditor.getAccountIdentifierType());
        final String name =
                StringUtils.isBlank(creditor.getName())
                        ? PAYMENT_CREDITOR_DEFAULT_NAME
                        : creditor.getName();

        return CreditorAccount.builder()
                .schemeName(schemeName)
                .identification(creditor.getAccountNumber())
                .name(name)
                .build();
    }

    private AccountIdentifier createAccountIdentifier(BaseAccount baseAccount) {
        final String schemeName = baseAccount.getSchemeName();
        final String identification = baseAccount.getIdentification();

        switch (schemeName) {
            case "UK.OBIE.SortCodeAccountNumber":
                return new SortCodeIdentifier(identification);
            case "UK.OBIE.Paym":
                return new PaymPhoneNumberIdentifier(identification);
            case "UK.OBIE.IBAN":
                return new IbanIdentifier(identification);
            case "PAN":
                return new MaskedPanIdentifier(identification);

            default:
                throw new IllegalArgumentException(
                        String.format(
                                "%s unknown schemeName, identification: %s!",
                                schemeName, identification));
        }
    }

    private static String getSchemeName(AccountIdentifierType accountIdentifier) {
        return PAYMENT_SCHEME_TYPE_MAPPER
                .translate(accountIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Scheme name cannot be null!"));
    }

    private static RemittanceInformationType getRemittanceInformationType(Payment payment) {
        return Optional.ofNullable(payment.getRemittanceInformation())
                .map(RemittanceInformation::getType)
                .orElse(null);
    }
}
