package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.ConsentResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ConsentFixtures {

    private static final String CONSENTS =
            "{\"Data\": {\"Permissions\": [ \"ReadTransactionsCredits\", \"ReadBalances\", \"ReadTransactionsDetail\", \"ReadBeneficiariesDetail\", \"ReadTransactionsDebits\", \"ReadParty\", \"ReadAccountsDetail\"],\"ConsentId\": \"DUMMY\",\"Status\": \"Authorised\",\"CreationDateTime\": \"2021-11-22T09:39:07.000Z\",\"StatusUpdateDateTime\": \"2021-11-22T09:40:22.628Z\"}}";
    private static final String EMPTY_CONSENT_STATUS =
            "{\"Data\": {\"Permissions\": [ \"ReadTransactionsCredits\", \"ReadBalances\", \"ReadTransactionsDetail\", \"ReadBeneficiariesDetail\", \"ReadTransactionsDebits\", \"ReadParty\", \"ReadAccountsDetail\"],\"ConsentId\": \"DUMMY\",\"Status\": \"\",\"CreationDateTime\": \"2021-11-22T09:39:07.000Z\",\"StatusUpdateDateTime\": \"2021-11-22T09:40:22.628Z\"}}";

    public static ConsentResponse authorisedConsent() {
        return SerializationUtils.deserializeFromString(CONSENTS, ConsentResponse.class);
    }

    public static ConsentResponse emptyConsent() {
        return SerializationUtils.deserializeFromString(
                EMPTY_CONSENT_STATUS, ConsentResponse.class);
    }
}
