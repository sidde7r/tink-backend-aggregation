package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class NordeaDkApiClient extends NordeaBaseApiClient {

    private static final String SCOPE_WITHOUT_PAYMENT_AND_CREDIT_CARDS =
            "ACCOUNTS_BALANCES,ACCOUNTS_BASIC,ACCOUNTS_DETAILS,ACCOUNTS_TRANSACTIONS";
    private static final String SCOPE_WITHOUT_CREDIT_CARDS =
            SCOPE_WITHOUT_PAYMENT_AND_CREDIT_CARDS + ",PAYMENTS_MULTIPLE";

    public NordeaDkApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, QsealcSigner qsealcSigner) {
        super(client, persistentStorage, qsealcSigner, false);
    }

    @Override
    protected String getScope() {
        return SCOPE_WITHOUT_PAYMENT_AND_CREDIT_CARDS;
    }

    @Override
    protected String getScopeWithoutPayment() {
        return SCOPE_WITHOUT_CREDIT_CARDS;
    }
}
