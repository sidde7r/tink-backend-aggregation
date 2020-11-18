package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankTransactionalAccountFetcherTest {

    private static final String ACCOUNT_ENTITIES_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources/accountEntities.json";

    private DanskeBankTransactionalAccountFetcher danskeBankTransactionalAccountFetcher;
    private List<AccountEntity> accounts;

    @Before
    public void setup() {
        DanskeBankApiClient apiClient = mock(DanskeBankApiClient.class);
        DanskeBankConfiguration configuration = mock(DanskeBankConfiguration.class);
        AccountEntityMapper accountEntityMapper = mock(AccountEntityMapper.class);
        danskeBankTransactionalAccountFetcher =
                new DanskeBankTransactionalAccountFetcher(
                        apiClient, configuration, accountEntityMapper);
        accounts =
                SerializationUtils.deserializeFromString(
                                new File(ACCOUNT_ENTITIES_FILE_PATH), ListAccountsResponse.class)
                        .getAccounts();
    }

    @Test
    public void shouldLogDuplicates() {
        // given & when
        List<AccountEntity> result =
                danskeBankTransactionalAccountFetcher.logDuplicatedAccountNoExt(accounts);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getAccountNoExt().equals(result.get(1).getAccountNoExt()))
                .isTrue();
    }

    @Test
    public void shouldNotLogIfThereAreNoDuplicates() {
        // given
        // remove duplicate
        accounts.remove(1);

        // when
        List<AccountEntity> result =
                danskeBankTransactionalAccountFetcher.logDuplicatedAccountNoExt(accounts);

        // then
        assertThat(result.isEmpty()).isTrue();
    }
}
