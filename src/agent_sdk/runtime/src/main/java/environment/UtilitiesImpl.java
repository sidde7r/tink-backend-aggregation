package se.tink.agent.runtime.environment;

import se.tink.agent.sdk.environment.Utilities;
import se.tink.agent.sdk.utils.RandomGenerator;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.agent.sdk.utils.SupplementalInformationHelper;
import se.tink.agent.sdk.utils.TimeGenerator;
import se.tink.agent.sdk.utils.signer.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class UtilitiesImpl implements Utilities {
    private final RandomGenerator randomGenerator;
    private final TimeGenerator timeGenerator;
    private final Sleeper sleeper;
    private final TinkHttpClient tinkHttpClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final QsealcSigner qsealcSigner;

    public UtilitiesImpl(
            RandomGenerator randomGenerator,
            TimeGenerator timeGenerator,
            Sleeper sleeper,
            TinkHttpClient tinkHttpClient,
            SupplementalInformationHelper supplementalInformationHelper,
            QsealcSigner qsealcSigner) {
        this.randomGenerator = randomGenerator;
        this.timeGenerator = timeGenerator;
        this.sleeper = sleeper;
        this.tinkHttpClient = tinkHttpClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.qsealcSigner = qsealcSigner;
    }

    @Override
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    @Override
    public TimeGenerator getTimeGenerator() {
        return timeGenerator;
    }

    @Override
    public Sleeper getSleeper() {
        return sleeper;
    }

    @Override
    public TinkHttpClient getHttpClient() {
        return tinkHttpClient;
    }

    @Override
    public SupplementalInformationHelper getSupplementalInformationHelper() {
        return supplementalInformationHelper;
    }

    @Override
    public QsealcSigner getQsealcSigner() {
        return qsealcSigner;
    }
}
