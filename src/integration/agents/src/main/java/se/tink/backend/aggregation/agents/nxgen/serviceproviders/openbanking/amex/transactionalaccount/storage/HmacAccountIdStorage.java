package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class HmacAccountIdStorage {

    private static final String TMP_HMAC_ACCOUNT_IDS_KEY = "tmp_hmac_account_ids";

    private final SessionStorage sessionStorage;

    public Optional<HmacAccountIds> get() {
        return sessionStorage.get(TMP_HMAC_ACCOUNT_IDS_KEY, HmacAccountIds.class);
    }

    public void store(HmacAccountIds hmacAccountIds) {
        sessionStorage.put(TMP_HMAC_ACCOUNT_IDS_KEY, hmacAccountIds);
    }
}
