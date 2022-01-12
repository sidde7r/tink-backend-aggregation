package se.tink.agent.sdk.environment;

import se.tink.agent.sdk.utils.RandomGenerator;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.agent.sdk.utils.SupplementalInformationHelper;
import se.tink.agent.sdk.utils.TimeGenerator;
import se.tink.agent.sdk.utils.signer.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public interface Utilities {
    RandomGenerator getRandomGenerator();

    TimeGenerator getTimeGenerator();

    Sleeper getSleeper();

    TinkHttpClient getHttpClient();

    SupplementalInformationHelper getSupplementalInformationHelper();

    QsealcSigner getQsealcSigner();
}
