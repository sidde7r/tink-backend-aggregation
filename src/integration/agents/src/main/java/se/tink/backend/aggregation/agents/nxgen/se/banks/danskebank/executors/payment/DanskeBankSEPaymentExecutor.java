package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment;

import java.util.Date;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferPayType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.TransferType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.DanskeBankExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.AcceptSignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc.RegisterPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class DanskeBankSEPaymentExecutor implements PaymentExecutor {

    private final DanskeBankSEApiClient apiClient;
    private final DanskeBankSEConfiguration configuration;
    private final DanskeBankExecutorHelper executorHelper;

    public DanskeBankSEPaymentExecutor(
            DanskeBankSEApiClient apiClient,
            DanskeBankConfiguration configuration,
            DanskeBankExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.configuration = (DanskeBankSEConfiguration) configuration;
        this.executorHelper = executorHelper;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        RemittanceInformation remittanceInformation = validateAndGetRemittanceInformation(transfer);

        ListAccountsResponse accounts =
                apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        String giroName = executorHelper.validateGiro(transfer);

        Date paymentDate =
                executorHelper.validatePaymentDate(
                        transfer, TransferAccountType.GIRO, TransferPayType.GIRO);

        executorHelper.validateOCR(transfer);

        HttpResponse injectJsCheckStep = this.apiClient.collectDynamicChallengeJavascript();

        RegisterPaymentResponse registerPaymentResponse =
                executorHelper
                        .registerPayment(
                                transfer, accounts, giroName, paymentDate, remittanceInformation)
                        .validate();

        executorHelper.signPayment(registerPaymentResponse);

        apiClient
                .acceptSignature(
                        TransferType.GIRO,
                        AcceptSignatureRequest.builder()
                                .signatureId(registerPaymentResponse.getSignatureId())
                                .signaturePackage(
                                        executorHelper.getSignaturePackage(
                                                injectJsCheckStep,
                                                registerPaymentResponse.getUserId(),
                                                registerPaymentResponse.getSignatureText()))
                                .language(configuration.getLanguageCode())
                                .build())
                .validate();
    }

    private RemittanceInformation validateAndGetRemittanceInformation(final Transfer transfer) {
        RemittanceInformation remittanceInformation = transfer.getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation,
                null,
                RemittanceInformationType.UNSTRUCTURED,
                RemittanceInformationType.OCR);

        if (remittanceInformation.getType() == null) {
            remittanceInformation.setType(decideRemittanceInformationType(remittanceInformation));
        }

        return remittanceInformation;
    }

    private RemittanceInformationType decideRemittanceInformationType(
            RemittanceInformation remittanceInformation) {
        return isValidSoftOcr(remittanceInformation.getValue())
                ? RemittanceInformationType.OCR
                : RemittanceInformationType.UNSTRUCTURED;
    }

    private boolean isValidSoftOcr(String message) {
        OcrValidationConfiguration validationConfiguration = OcrValidationConfiguration.softOcr();
        GiroMessageValidator validator = GiroMessageValidator.create(validationConfiguration);
        return validator.validate(message).getValidOcr().isPresent();
    }
}
