package se.tink.backend.aggregation.compliance.regulatory_restrictions;

import com.google.inject.Inject;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;

public class RegulatoryRestrictions {
    private final RegulatoryRestrictionsMetrics regulatoryRestrictionsMetrics;
    private static final Logger logger = LoggerFactory.getLogger(RegulatoryRestrictions.class);

    @Inject
    public RegulatoryRestrictions(RegulatoryRestrictionsMetrics regulatoryRestrictionsMetrics) {
        this.regulatoryRestrictionsMetrics = regulatoryRestrictionsMetrics;
    }

    public boolean shouldAccountBeRestricted(
            CredentialsRequest request,
            Account account,
            Optional<Psd2PaymentAccountClassificationResult> paymentAccountClassification) {
        try {
            boolean isRestricted = isRestricted(request, account, paymentAccountClassification);
            regulatoryRestrictionsMetrics.regulatoryRestrictions(
                    request.getProvider(), account, isRestricted);
            return isRestricted;
        } catch (
                RuntimeException
                        e) { // this is just temporary just to make regulatory feature not fail
            // other processes
            logger.warn("[RegulatoryRestrictions] Unexpected exception occurred: ", e);
            return false;
        }
    }

    private boolean isRestricted(
            CredentialsRequest request,
            Account account,
            Optional<Psd2PaymentAccountClassificationResult> paymentAccountClassification) {
        return request.getDataFetchingRestrictions().stream()
                .anyMatch(
                        restriction -> {
                            if (restriction
                                    == DataFetchingRestrictions
                                            .RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS) {
                                return paymentAccountClassification.isPresent()
                                        && paymentAccountClassification.get()
                                                == Psd2PaymentAccountClassificationResult
                                                        .PAYMENT_ACCOUNT;
                            } else if (restriction
                                    == DataFetchingRestrictions
                                            .RESTRICT_FETCHING_PSD2_UNDETERMINED_PAYMENT_ACCOUNTS) {
                                return !paymentAccountClassification.isPresent()
                                        || paymentAccountClassification.get()
                                                == Psd2PaymentAccountClassificationResult
                                                        .UNDETERMINED_PAYMENT_ACCOUNT;
                            }
                            return false;
                        });
    }
}
