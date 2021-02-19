package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SocieteGeneraleTestFixtures {

    public static final String SIGNATURE = "DUMMY_SIGNATURE";
    public static final String ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    public static final String BEARER_HEADER_VALUE = "Bearer " + ACCESS_TOKEN;
    public static final String CLIENT_ID = "DUMMY_CLIENT_ID";
    public static final String NEXT_PAGE_PATH = "/trusted-beneficiaries?page=2";
    public static final String REDIRECT_URL = "https://127.0.0.1:7357/api/v1/thirdparty/callback";
}
