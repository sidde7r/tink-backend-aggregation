package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import static se.tink.libraries.transfer.enums.RemittanceInformationType.UNSTRUCTURED;

import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaDateUtil;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@Getter
public abstract class BasePaymentRequest {
    private final String endToEndIdentification;
    private final AccountEntity debtorAccount;
    private final AmountEntity instructedAmount;
    private final String requestedExecutionDate;

    BasePaymentRequest(Payment payment) {
        endToEndIdentification = RandomUtils.generateRandomAlphabeticString(35);
        this.debtorAccount = new AccountEntity(payment.getDebtor().getAccountNumber());
        this.instructedAmount = new AmountEntity(payment.getExactCurrencyAmount());
        this.requestedExecutionDate = SkandiaDateUtil.getExecutionDate(payment).toString();
    }

    public static BasePaymentRequest of(Payment payment) {
        switch (PaymentProduct.from(payment)) {
            case DOMESTIC_CREDIT_TRANSFERS:
                return new TransferRequest(payment);
            case DOMESTIC_GIROS:
                return new PaymentRequest(payment);
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    protected static RemittanceInformationType mapRemittanceInformationType(Payment payment) {
        return Optional.ofNullable(payment.getRemittanceInformation())
                .map(RemittanceInformation::getType)
                .orElse(UNSTRUCTURED);
    }
}
