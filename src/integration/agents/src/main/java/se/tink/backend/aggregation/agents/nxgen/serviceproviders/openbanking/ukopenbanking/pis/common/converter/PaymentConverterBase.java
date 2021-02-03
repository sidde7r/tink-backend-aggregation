package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.converter;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.BaseAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.DebtorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.InstructedAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PaymPhoneNumberIdentifier;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public abstract class PaymentConverterBase {

    private static final GenericTypeMapper<String, AccountIdentifier.Type>
            PAYMENT_SCHEME_TYPE_MAPPER =
                    GenericTypeMapper.<String, AccountIdentifier.Type>genericBuilder()
                            .put("UK.OBIE.Paym", AccountIdentifier.Type.PAYM_PHONE_NUMBER)
                            .put("UK.OBIE.PAN", AccountIdentifier.Type.PAYMENT_CARD_NUMBER)
                            .put("UK.OBIE.SortCodeAccountNumber", AccountIdentifier.Type.SORT_CODE)
                            .put("UK.OBIE.IBAN", AccountIdentifier.Type.IBAN)
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

    public ExactCurrencyAmount convertInstructedAmountToExactCurrencyAmount(
            InstructedAmount instructedAmount) {
        return new ExactCurrencyAmount(
                new BigDecimal(instructedAmount.getAmount()), instructedAmount.getCurrency());
    }

    public Debtor convertDebtorAccountToDebtor(DebtorAccount debtorAccount) {
        return Optional.ofNullable(debtorAccount)
                .map(this::createAccountIdentifier)
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

    private DebtorAccount convertDebtorToDebtorAccount(Debtor debtor) {
        return Optional.ofNullable(debtor.getAccountIdentifier())
                .map(
                        accountIdentifier ->
                                DebtorAccount.builder()
                                        .schemeName(getSchemeName(accountIdentifier.getType()))
                                        .identification(accountIdentifier.getIdentifier())
                                        .build())
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

    protected Creditor convertCreditorAccountToCreditor(CreditorAccount creditorAccount) {
        return new Creditor(createAccountIdentifier(creditorAccount), creditorAccount.getName());
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
                return new PaymentCardNumberIdentifier(identification);

            default:
                throw new IllegalArgumentException(
                        String.format(
                                "%s unknown schemeName, identification: %s!",
                                schemeName, identification));
        }
    }

    private static String getSchemeName(AccountIdentifier.Type accountIdentifier) {
        return PAYMENT_SCHEME_TYPE_MAPPER
                .translate(accountIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Scheme name cannot be null!"));
    }
}
