package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.CreditCardTestData;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardTransactionsResponseTest {

    @Test
    public void shouldReturnCanFetchMoreIfPageIsFull() {
        // given
        CreditCardTransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        new File(CreditCardTestData.CREDIT_CARD_TRANSACTIONS_FULL_PAGE_FILE),
                        CreditCardTransactionsResponse.class);

        // when
        Optional<Boolean> canFetchMore = response.canFetchMore();

        // then
        assertThat(canFetchMore).hasValue(true);
    }

    @Test
    public void shouldReturnCanNotFetchMoreIfPageIsNotFull() {
        // given
        CreditCardTransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        new File(CreditCardTestData.CREDIT_CARD_TRANSACTIONS_NOT_FULL_PAGE_FILE),
                        CreditCardTransactionsResponse.class);

        // when
        Optional<Boolean> canFetchMore = response.canFetchMore();

        // then
        assertThat(canFetchMore).hasValue(false);
    }

    @Test
    public void shouldReturnNextPageNumberAsNextPaginationKey() {
        // given
        CreditCardTransactionsResponse response =
                SerializationUtils.deserializeFromString(
                        new File(CreditCardTestData.CREDIT_CARD_TRANSACTIONS_NOT_FULL_PAGE_FILE),
                        CreditCardTransactionsResponse.class);

        // when
        Integer nextKey = response.nextKey();

        // then
        assertThat(nextKey).isEqualTo(2);
    }
}
