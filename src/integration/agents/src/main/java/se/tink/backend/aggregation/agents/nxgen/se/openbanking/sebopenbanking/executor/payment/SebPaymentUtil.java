package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.PaymentValue;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebDateUtil;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SebPaymentUtil {

    public static PaymentType getPaymentType(Payment payment) {
        return payment.getCreditor().getAccountIdentifierType().equals(AccountIdentifierType.IBAN)
                        && !payment.getCreditor().getAccountNumber().startsWith(SebConstants.MARKET)
                ? PaymentType.SEPA
                : PaymentType.DOMESTIC;
    }

    public static PaymentProduct getPaymentProduct(
            PaymentType paymentType, AccountIdentifierType creditorAccountType) {
        switch (paymentType) {
            case SEPA:
                return PaymentProduct.SEPA_CREDIT_TRANSFER;
            case INTERNATIONAL:
                throw new IllegalStateException(ErrorMessages.CROSS_BORDER_PAYMENT_NOT_SUPPORTED);
            case DOMESTIC:
                return getDomesticPaymentProduct(creditorAccountType);
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_PAYMENT_PRODUCT);
        }
    }

    public static String getExecutionDateOrCurrentDate(Payment payment, String paymentProduct) {
        switch (paymentProduct) {
            case SebConstants.PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BNAKGIROS:
            case SebConstants.PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS:
                return SebDateUtil.getTransferDateForBgPg(payment.getExecutionDate());
            case SebConstants.PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS:
                return SebDateUtil.getTransferDate(
                        payment.getExecutionDate(),
                        IntraBankChecker.isSwedishMarketIntraBank(
                                payment.getDebtor().getAccountIdentifier(),
                                payment.getCreditor().getAccountIdentifier()));
            case SebConstants.PaymentProduct.SEPA_CREDIT_TRANSFER:
                return SebDateUtil.getTransferDateForSepa(payment.getExecutionDate());
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_PAYMENT_PRODUCT);
        }
    }

    public static RemittanceInformation validateAndGetRemittanceInformation(
            String paymentProduct, Payment payment) {
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation,
                null,
                RemittanceInformationType.UNSTRUCTURED,
                RemittanceInformationType.OCR);

        if (remittanceInformation.getType() == null
                && StringUtils.containsAny(
                        paymentProduct,
                        PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BANKGIROS.getValue(),
                        PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS.getValue())) {
            remittanceInformation.setType(decideRemittanceInformationType(remittanceInformation));
        }

        return remittanceInformation;
    }

    public static void validateUnStructuredRemittanceInformation(String message)
            throws ReferenceValidationException {
        if (!Strings.isNullOrEmpty(message) && message.length() > PaymentValue.MAX_DEST_MSG_LEN) {
            throw new ReferenceValidationException(
                    String.format(
                            ErrorMessages.PAYMENT_REF_TOO_LONG, PaymentValue.MAX_DEST_MSG_LEN));
        }
    }

    private static PaymentProduct getDomesticPaymentProduct(
            AccountIdentifierType creditorAccountType) {
        switch (creditorAccountType) {
            case SE_BG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BANKGIROS;
            case SE_PG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS;
            default:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS;
        }
    }

    private static RemittanceInformationType decideRemittanceInformationType(
            RemittanceInformation remittanceInformation) {
        return isValidSoftOcr(remittanceInformation.getValue())
                ? RemittanceInformationType.OCR
                : RemittanceInformationType.UNSTRUCTURED;
    }

    private static boolean isValidSoftOcr(String message) {
        OcrValidationConfiguration validationConfiguration = OcrValidationConfiguration.softOcr();
        GiroMessageValidator validator = GiroMessageValidator.create(validationConfiguration);
        return validator.validate(message).getValidOcr().isPresent();
    }
}
