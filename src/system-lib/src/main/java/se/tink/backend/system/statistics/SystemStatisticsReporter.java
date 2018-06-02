package se.tink.backend.system.statistics;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.math.BigInteger;
import java.util.Map;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.UserState;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class SystemStatisticsReporter {
    private static final MetricId AMOUNT_PERCENTAGE = MetricId.newId("categorization_amount_percentage");
    private static final MetricId TRANSACTION_PERCENTAGE = MetricId.newId("categorization_transactions_percentage");
    private static final MetricId AMOUNT_LESS_THAN_10K_PERCENTAGE = MetricId
            .newId("categorization_amount_expenses_under_10k_percentage");

    private static final MetricId MERCHANTIFICATION_INITIAL = MetricId.newId("merchantification_initial");
    private static final MetricId MERCHANTIFICATION_WITH_LOCATION_INITIAL = MetricId
            .newId("merchantification_with_location_initial");
    private static final MetricId MERCHANTIFICATION = MetricId.newId("merchantification");
    private static final MetricId MERCHANTIFICATION_WITH_LOCATION = MetricId.newId("merchantification_with_location");

    private final Histogram categorizationAmountLevelHistgram;
    private final Histogram categorizationCountLevelHistogram;
    private final Histogram expensesLessThan10kCategorizationLevelHistgram;
    private final Histogram initialMerchantificationLevelHistogram;
    private final Histogram initialMerchantificationWithLocationLevelHistogram;
    private final Histogram merchantificationLevelHistogram;
    private final Histogram merchantificationWithLocationLevelHistogram;

    private CredentialsRepository credentialsRepository;
    private UserRepository userRepository;
    private UserStateRepository userStateRepository;
    private MetricRegistry metricRegistry;

    @Inject
    public SystemStatisticsReporter(MetricRegistry metricRegistry, UserRepository userRepository,
                                    UserStateRepository userStateRepository, CredentialsRepository credentialsRepository) {
        this.metricRegistry = metricRegistry;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.credentialsRepository = credentialsRepository;

        categorizationAmountLevelHistgram = metricRegistry.histogram(AMOUNT_PERCENTAGE);
        categorizationCountLevelHistogram = metricRegistry.histogram(TRANSACTION_PERCENTAGE);
        expensesLessThan10kCategorizationLevelHistgram = metricRegistry.histogram(AMOUNT_LESS_THAN_10K_PERCENTAGE);

        // Merchants
        initialMerchantificationLevelHistogram = metricRegistry.histogram(MERCHANTIFICATION_INITIAL);
        initialMerchantificationWithLocationLevelHistogram = metricRegistry.histogram(MERCHANTIFICATION_WITH_LOCATION_INITIAL);
        merchantificationLevelHistogram = metricRegistry.histogram(MERCHANTIFICATION);
        merchantificationWithLocationLevelHistogram = metricRegistry.histogram(MERCHANTIFICATION_WITH_LOCATION);
    }

    public void collectStatistics() {

        userRepository.streamAll().flatMapIterable(user -> {
            UserState item = userStateRepository.findOne(user.getId());
            return item == null ? ImmutableList.<UserState>of() : ImmutableList.of(item);
        }).forEach(userState -> {

            if (userState.getAmountCategorizationLevel() != null && userState.getAmountCategorizationLevel() != 0) {
                categorizationAmountLevelHistgram.update(userState.getAmountCategorizationLevel());
            }

            if (userState.getTransactionCategorizationLevel() != null
                    && userState.getTransactionCategorizationLevel() != 0) {
                categorizationCountLevelHistogram.update(userState.getTransactionCategorizationLevel());
            }

            if (userState.getExpensesLessThan10kCategorizationLevel() != null
                    && userState.getExpensesLessThan10kCategorizationLevel() != 0) {
                expensesLessThan10kCategorizationLevelHistgram.update(userState
                        .getExpensesLessThan10kCategorizationLevel());
            }

            if(userState.getInitialMerchantificationLevel() != null
                    && userState.getInitialMerchantificationLevel() != 0){
                initialMerchantificationLevelHistogram.update(userState.getInitialMerchantificationLevel());
            }

            if(userState.getInitialMerchantificationWithLocationLevel() != null
                    && userState.getInitialMerchantificationWithLocationLevel() != 0){
                initialMerchantificationWithLocationLevelHistogram.update(userState.getInitialMerchantificationWithLocationLevel());
            }

            if(userState.getMerchantificationLevel() != null
                    && userState.getMerchantificationLevel() != 0){
                merchantificationLevelHistogram.update(userState.getMerchantificationLevel());
            }

            if(userState.getMerchantificationWithLocationLevel() != null
                    && userState.getMerchantificationWithLocationLevel() != 0){
                merchantificationWithLocationLevelHistogram.update(userState.getMerchantificationWithLocationLevel());
            }

        });

        for (Map.Entry<String, Map<CredentialsStatus, BigInteger>> providerEntry : credentialsRepository
                .findStatusDistribution().entrySet()) {
            for (Map.Entry<CredentialsStatus, BigInteger> credentialsEntry : providerEntry.getValue().entrySet()) {
                MetricId label = MetricId.newId("credential_statuses")
                        .label("status", credentialsEntry.getKey().toString())
                        .label("provider", MetricsUtils.cleanMetricName(providerEntry.getKey()));
                metricRegistry.lastUpdateGauge(label).update(credentialsEntry.getValue());
            }
        }

        metricRegistry.lastUpdateGauge(MetricId.newId("system_users")).update(userRepository.count());
    }
}
