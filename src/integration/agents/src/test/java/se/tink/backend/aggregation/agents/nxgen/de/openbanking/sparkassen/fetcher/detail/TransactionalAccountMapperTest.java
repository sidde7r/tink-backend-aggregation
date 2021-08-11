package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.FetcherTestData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountMapperTest {

    @Test
    public void shouldReturnProperlyTransformedAccount() {
        // given
        AccountEntity accountEntity =
                FetcherTestData.getAccountEntity(
                        "TRAN",
                        "EUR",
                        "DE86999999990000001000",
                        "Super Account 001",
                        "John Doe",
                        "Product 007",
                        "30e736a3607445db8c4ff972bbdafc08",
                        "WELADED1PMB");
        FetchBalancesResponse fetchBalancesResponse =
                FetcherTestData.getFetchBalancesResponse("EUR", BigDecimal.valueOf(1234.56));

        // when
        Optional<TransactionalAccount> maybeTinkAccount =
                TransactionalAccountMapper.toTinkAccountWithBalance(
                        accountEntity, fetchBalancesResponse);

        // then
        assertTrue(maybeTinkAccount.isPresent());
        TransactionalAccount account = maybeTinkAccount.get();
        assertThat(account.getAccountFlags()).containsExactly(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(account.getIdentifiers())
                .containsExactlyInAnyOrder(
                        new IbanIdentifier("WELADED1PMB", "DE86999999990000001000"),
                        new BbanIdentifier("999999990000001000"));
        assertThat(account.isUniqueIdentifierEqual("DE86999999990000001000")).isTrue();
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(1234.56, "EUR"));
        assertThat(account.getAccountNumber()).isEqualTo("DE86999999990000001000");
        assertThat(account.getName()).isEqualTo("Super Account 001");
        assertThat(account.getApiIdentifier()).isEqualTo("30e736a3607445db8c4ff972bbdafc08");
        assertThat(account.getHolderName().toString()).isEqualTo("John Doe");
    }
}
