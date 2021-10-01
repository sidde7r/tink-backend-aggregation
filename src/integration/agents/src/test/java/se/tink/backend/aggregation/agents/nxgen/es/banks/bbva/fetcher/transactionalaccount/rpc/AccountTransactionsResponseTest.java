package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.PaginationEntity;

@RunWith(MockitoJUnitRunner.class)
public class AccountTransactionsResponseTest {

    @Mock private PaginationEntity paginationEntity;

    private AccountTransactionsResponse objectUnderTest = new AccountTransactionsResponse();

    @Test
    public void nextPageShouldBePossibleToFetch() {
        // given
        given(paginationEntity.getNumPages()).willReturn(2);
        given(paginationEntity.getPage()).willReturn(1);
        objectUnderTest.setPagination(paginationEntity);

        // when
        Optional<Boolean> canFetchMore = objectUnderTest.canFetchMore();
        String nextKey = objectUnderTest.nextKey();

        // then
        assertThat(canFetchMore).isPresent();
        assertThat(canFetchMore).containsSame(true);
        assertThat(nextKey).isEqualTo("2");
    }

    @Test
    public void nextPageShouldNotBePossibleToFetchWhenThereIsSinglePageOnly() {
        // given
        given(paginationEntity.getNumPages()).willReturn(1);
        given(paginationEntity.getPage()).willReturn(1);
        objectUnderTest.setPagination(paginationEntity);

        // when
        Optional<Boolean> canFetchMore = objectUnderTest.canFetchMore();

        // then
        assertThat(canFetchMore).isPresent();
        assertThat(canFetchMore).containsSame(false);
    }

    @Test
    public void nextPageShouldBeNotPossibleToFetchWhenPaginationEntityIsNull() {
        // given
        objectUnderTest.setPagination(null);

        // when
        Optional<Boolean> canFetchMore = objectUnderTest.canFetchMore();

        // then
        assertThat(canFetchMore).isPresent();
        assertThat(canFetchMore).containsSame(false);
    }

    @Test
    public void nextPageShouldBeUsedAsAKeyToFetchWhenNextPageLinkIsNotNull() {
        // given
        given(paginationEntity.getNextPage()).willReturn("dummyLink");
        objectUnderTest.setPagination(paginationEntity);

        // when
        String nextKey = objectUnderTest.nextKey();

        // then
        assertThat(nextKey).isEqualTo("dummyLink");
    }

    @Test
    public void defaultNextPageShouldBeUsedAsAKeyToFetchWhenNextPageLinkIsNull() {
        // given
        given(paginationEntity.getPage()).willReturn(1);
        objectUnderTest.setPagination(paginationEntity);

        // when
        String nextKey = objectUnderTest.nextKey();

        // then
        assertThat(nextKey).isEqualTo("2");
    }
}
