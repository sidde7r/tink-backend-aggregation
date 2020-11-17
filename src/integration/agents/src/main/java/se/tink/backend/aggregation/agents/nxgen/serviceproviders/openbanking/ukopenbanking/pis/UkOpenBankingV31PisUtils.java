package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class UkOpenBankingV31PisUtils {
    private static final List<String> HSBC_FAMILY =
            Arrays.asList("uk-hsbc-oauth2", "uk-firstdirect-oauth2");

    static TransferExecutionException createFailedTransferException() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        "Payment failed - authorisation of payment failed, needs further investigation.")
                .setEndUserMessage("Authorisation of payment failed.")
                .build();
    }

    static void validateRemittanceWithProviderOrThrow(
            String providerName, RemittanceInformation remittanceInformation) {
        if (HSBC_FAMILY.contains(providerName)
                && isRemittanceInformationValueInvalid(remittanceInformation)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage("Invalid Remittance Information Length for HSBC Family")
                    .setEndUserMessage("Invalid Remittance Information Length for HSBC Family")
                    .build();
        }
    }

    private static boolean isRemittanceInformationValueInvalid(
            RemittanceInformation remittanceInformation) {
        return Strings.isNullOrEmpty(remittanceInformation.getValue())
                || remittanceInformation.getValue().length() > 18;
    }
}
