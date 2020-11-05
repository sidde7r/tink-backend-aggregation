package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.util;

import com.google.common.base.Strings;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SwedbankRemittanceInformationUtil {
    private RemittanceInformationStructuredEntity remittanceInformationStructured;
    private String remittanceInformationUnstructured;
    private static final Pattern MESSAGE_PATTERN =
            Pattern.compile("^[a-zA-Z0-9/?:().,'+ -]{1,70}$");
    private static final Pattern RI_INFO_UNSTRUCTURED_INTERBANK_PATTERN =
            Pattern.compile("^[a-zA-Z0-9/?:().,'+ -]{1,10}$");
    private static final Pattern RI_INFO_UNSTRUCTURED_INTRABANK_PATTERN =
            Pattern.compile("^[a-zA-Z0-9/?:().,'+ -]{1,35}$");
    private static final OcrValidationConfiguration OCR_VALIDATION_CONFIGURATION =
            OcrValidationConfiguration.softOcr();

    private SwedbankRemittanceInformationUtil(RemittanceInformation remittanceInformation) {
        this.remittanceInformationStructured =
                new RemittanceInformationStructuredEntity(remittanceInformation);
    }

    private SwedbankRemittanceInformationUtil(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public static SwedbankRemittanceInformationUtil of(PaymentRequest paymentRequest)
            throws ReferenceValidationException {
        final RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        AccountIdentifier creditorAccountIdentifier =
                paymentRequest.getPayment().getCreditor().getAccountIdentifier();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation,
                null,
                RemittanceInformationType.UNSTRUCTURED,
                RemittanceInformationType.OCR);

        boolean isIntraBank =
                IntraBankChecker.isSwedishMarketIntraBank(
                        paymentRequest.getPayment().getDebtor().getAccountIdentifier(),
                        paymentRequest.getPayment().getCreditor().getAccountIdentifier());

        switch (creditorAccountIdentifier.getType()) {
            case SE_BG:
            case SE_PG:
                return validateAndGetSwedbankRemittanceInfoStructured(remittanceInformation);
            case SE:
                return validateAndGetSwedbankRemittanceInfoUnStructured(
                        remittanceInformation.getValue(), isIntraBank);
            default:
                throw new IllegalStateException(
                        String.format(
                                ErrorMessages.INVALID_ACCOUNT_TYPE,
                                creditorAccountIdentifier.getType().toString()));
        }
    }

    private static SwedbankRemittanceInformationUtil validateAndGetSwedbankRemittanceInfoStructured(
            RemittanceInformation remittanceInformation) throws ReferenceValidationException {

        // these 3 lines + method should be removed after customers start using only RI
        if (remittanceInformation.getType() == null) {
            remittanceInformation.setType(decideRemittanceInformationType(remittanceInformation));
        }
        if (RemittanceInformationType.UNSTRUCTURED.equals(remittanceInformation.getType())) {
            validateRemittanceInformationUnStructured(
                    remittanceInformation.getValue(), MESSAGE_PATTERN);
        }
        return new SwedbankRemittanceInformationUtil(remittanceInformation);
    }

    private static SwedbankRemittanceInformationUtil
            validateAndGetSwedbankRemittanceInfoUnStructured(
                    String remittanceInformationValue, boolean isIntraBank)
                    throws ReferenceValidationException {
        final Pattern pattern =
                isIntraBank
                        ? RI_INFO_UNSTRUCTURED_INTRABANK_PATTERN
                        : RI_INFO_UNSTRUCTURED_INTERBANK_PATTERN;
        validateRemittanceInformationUnStructured(remittanceInformationValue, pattern);
        return new SwedbankRemittanceInformationUtil(remittanceInformationValue);
    }

    public RemittanceInformationStructuredEntity getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public String getRemittanceInformationUnStructured() {
        return remittanceInformationUnstructured;
    }

    private static void validateRemittanceInformationUnStructured(String message, Pattern pattern)
            throws ReferenceValidationException {
        if (Strings.isNullOrEmpty(message)
                || (!Strings.isNullOrEmpty(message) && (!pattern.matcher(message).matches()))) {
            throw new ReferenceValidationException(
                    String.format(ErrorMessages.INVALID_REMITTANCE_INFORMATION_VALUE),
                    "",
                    InternalStatus.INVALID_DESTINATION_MESSAGE,
                    new IllegalArgumentException());
        }
    }

    private static RemittanceInformationType decideRemittanceInformationType(
            RemittanceInformation remittanceInformation) {
        return isValidSoftOcr(remittanceInformation.getValue())
                ? RemittanceInformationType.OCR
                : RemittanceInformationType.UNSTRUCTURED;
    }

    private static boolean isValidSoftOcr(String message) {
        GiroMessageValidator validator = GiroMessageValidator.create(OCR_VALIDATION_CONFIGURATION);
        return validator.validate(message).getValidOcr().isPresent();
    }
}
