package se.tink.backend.aggregation.compliance.regulatory_restrictions;

import com.google.inject.Inject;
import java.util.Optional;
import javax.annotation.Nullable;
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
    public RegulatoryRestrictions(
            @Nullable RegulatoryRestrictionsMetrics regulatoryRestrictionsMetrics) {
        this.regulatoryRestrictionsMetrics = regulatoryRestrictionsMetrics;
    }

    public boolean shouldAccountBeRestricted(
            CredentialsRequest request,
            Account account,
            Optional<Psd2PaymentAccountClassificationResult> paymentAccountClassification) {
        try {
            boolean isRestricted = shouldBeRestricted(request, paymentAccountClassification);
            regulatoryRestrictionsMetrics.regulatoryRestrictions(
                    request.getProvider(), account, isRestricted);
            return isRestricted;
        } catch (RuntimeException e) {
            // this try-catch is just temporary to make regulatory feature safe and not break
            // other processes
            logger.warn("[RegulatoryRestrictions] Unexpected exception occurred: ", e);
            return false;
        }
    }

    private boolean shouldBeRestricted(
            CredentialsRequest request,
            Optional<Psd2PaymentAccountClassificationResult> paymentAccountClassification) {
        return request.getDataFetchingRestrictions().stream()
                .anyMatch(restriction -> isRestricted(paymentAccountClassification, restriction));
    }

    private boolean isRestricted(
            Optional<Psd2PaymentAccountClassificationResult> paymentAccountClassification,
            DataFetchingRestrictions restriction) {
        switch (restriction) {
            case RESTRICT_FETCHING_PSD2_PAYMENT_ACCOUNTS:
                return paymentAccountClassification
                        .map(
                                classification ->
                                        classification
                                                == Psd2PaymentAccountClassificationResult
                                                        .PAYMENT_ACCOUNT)
                        .orElse(false);
            case RESTRICT_FETCHING_PSD2_UNDETERMINED_PAYMENT_ACCOUNTS:
                return paymentAccountClassification
                        .map(
                                classification ->
                                        classification
                                                == Psd2PaymentAccountClassificationResult
                                                        .UNDETERMINED_PAYMENT_ACCOUNT)
                        .orElse(true);
            default:
                logger.warn("Unrecognized restriction: " + restriction);
                return false;
        }
    }
}
