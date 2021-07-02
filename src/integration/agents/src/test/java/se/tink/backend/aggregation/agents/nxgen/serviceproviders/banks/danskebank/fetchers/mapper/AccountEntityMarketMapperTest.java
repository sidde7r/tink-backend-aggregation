package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.libraries.account.AccountIdentifier;

@RunWith(JUnitParamsRunner.class)
public class AccountEntityMarketMapperTest {

    @Test
    @Parameters(method = "uniqueIdTestParams")
    public void shouldReturnCorrectUniqueNumber(
            String accountNoExt, String expectedUniqueId, String market) {
        // given
        AccountEntityMarketMapper marketMapper = new AccountEntityMarketMapper(market);
        // and
        AccountEntity accountEntity = accountEntity(accountNoExt);

        // when
        String uniqueId = marketMapper.getUniqueIdentifier(accountEntity);

        // then
        assertThat(uniqueId).isEqualTo(expectedUniqueId);
    }

    @SuppressWarnings("unused")
    private static Object[] uniqueIdTestParams() {
        return AccountEntityMarketMapperTestParams.getUniqueIdTestParams();
    }

    @Test
    @Parameters(method = "accountOwnersTestParams")
    public void shouldReturnCorrectAccountOwners(
            List<String> originalAccountOwners, List<String> expectedAccountOwners, String market) {
        // given
        AccountEntityMarketMapper marketMapper = new AccountEntityMarketMapper(market);
        // and
        AccountDetailsResponse accountDetailsResponse =
                accountDetailsResponse(originalAccountOwners);

        // when
        List<String> accountOwners = marketMapper.getAccountOwners(accountDetailsResponse);

        // then
        assertThat(accountOwners).isEqualTo(expectedAccountOwners);
    }

    @SuppressWarnings("unused")
    private static Object[] accountOwnersTestParams() {
        return AccountEntityMarketMapperTestParams.getAccountOwnersTestParams();
    }

    @Test
    @Parameters(method = "accountIdentifierTestParams")
    public void shouldReturnCorrectAccountIdentifiers(
            AccountEntity accountEntity,
            AccountDetailsResponse accountDetailsResponse,
            List<AccountIdentifier> expectedIdentifiers,
            String market) {
        // given
        AccountEntityMarketMapper marketMapper = new AccountEntityMarketMapper(market);

        // when
        List<AccountIdentifier> accountIdentifiers =
                marketMapper.getAccountIdentifiers(accountEntity, accountDetailsResponse);

        // then
        assertThat(accountIdentifiers).containsExactlyInAnyOrderElementsOf(expectedIdentifiers);
    }

    @SuppressWarnings("unused")
    private static Object[] accountIdentifierTestParams() {
        return AccountEntityMarketMapperTestParams.getMarketIdentifierTestParams();
    }

    private static AccountEntity accountEntity(String accountNoExt) {
        AccountEntity accountEntity = mock(AccountEntity.class);
        when(accountEntity.getAccountNoExt()).thenReturn(accountNoExt);
        return accountEntity;
    }

    private static AccountDetailsResponse accountDetailsResponse(List<String> accountOwners) {
        AccountDetailsResponse accountDetailsResponse = mock(AccountDetailsResponse.class);
        when(accountDetailsResponse.getAccountOwners()).thenReturn(accountOwners);
        return accountDetailsResponse;
    }
}
