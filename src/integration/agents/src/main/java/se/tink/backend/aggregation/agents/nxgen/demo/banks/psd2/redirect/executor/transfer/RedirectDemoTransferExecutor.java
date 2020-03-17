package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.transfer;

import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.UK_DEMO_PROVIDER_CANCEL_CASE;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.UK_DEMO_PROVIDER_FAILURE_CASE;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.UK_DEMO_PROVIDER_SUCCESS_CASE;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class RedirectDemoTransferExecutor implements BankTransferExecutor {
    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;
    private final OAuth2AuthenticationController controller;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;

    public RedirectDemoTransferExecutor(
            Credentials credentials,
            SupplementalRequester supplementalRequester,
            OAuth2AuthenticationController controller,
            SupplementalInformationHelper supplementalInformationHelper,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController) {
        this.credentials = credentials;
        this.supplementalRequester = supplementalRequester;
        this.controller = controller;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.thirdPartyAppAuthenticationController = thirdPartyAppAuthenticationController;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) {
        // todo: Temporary fix to unblock sdk
        // controller.init();

        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }

        // ------------------

        String providerName = credentials.getProviderName();
        // This block handles PIS only business use case as source-account will be null in request
        if (UK_DEMO_PROVIDER_SUCCESS_CASE.equals(
                providerName)) { // This block handles PIS only business
            // use case as source-account will not
            // be sent in request

            // not need to throw exception for success case
        } else if (UK_DEMO_PROVIDER_FAILURE_CASE.equals(providerName)) { // FAILED case

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            "The transfer amount is larger than what is available on the account (test)")
                    .setMessage(
                            "The transfer amount is larger than what is available on the account (test)")
                    .build();
        } else if (UK_DEMO_PROVIDER_CANCEL_CASE.equals(providerName)) { // CANCELLED case

            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage("Cancel on payment signing (test)")
                    .setMessage("Cancel on payment signing (test)")
                    .build();
        } else { // This block handles AIS+PIS business use case as source-account will be sent in
            // request
            Optional<String> sourceAccountName = transfer.getSource().getName();

            if (sourceAccountName.isPresent()) {
                String accountName = sourceAccountName.get().toLowerCase().replaceAll("\\s+", "");
                if (accountName.contains("checkingaccounttinkzerobalance")) {
                    // Mock the payment failure for zero balance checking accounts
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setEndUserMessage(
                                    "The transfer amount is larger than what is available on the account (test)")
                            .setMessage(
                                    "The transfer amount is larger than what is available on the account (test)")
                            .build();
                }

                if (accountName.contains("savingsaccount")) {
                    // Mock the user cancel for transfers from the saving accounts
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage("Cancel on payment signing (test)")
                            .setMessage("Cancel on payment signing (test)")
                            .build();
                }
            }
        }
        return Optional.empty();
    }
}
