package se.tink.backend.aggregation.agents.summary.refresh;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RefreshSummaryTest {

    private final RefreshSummary summary = new RefreshSummary();

    @Test
    public void shouldResolveStatusToNotStarted() {
        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus()).isEqualTo(RefreshStatus.NOT_STARTED);
    }

    @Test
    public void shouldResolveStatusToCompleted1() {
        // given
        summary.addItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.addItemSummary(
                RefreshableItem.SAVING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.addItemSummary(
                RefreshableItem.CHECKING_TRANSACTIONS, RefreshableItemFetchingStatus.COMPLETED);
        summary.addItemSummary(
                RefreshableItem.SAVING_TRANSACTIONS, RefreshableItemFetchingStatus.COMPLETED);

        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus()).isEqualTo(RefreshStatus.FETCHING_COMPLETED);
    }

    @Test
    public void shouldResolveStatusToCompleted2() {
        // given
        summary.addItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.addItemSummary(
                RefreshableItem.CHECKING_TRANSACTIONS, RefreshableItemFetchingStatus.COMPLETED);
        summary.addItemSummary(
                RefreshableItem.IDENTITY_DATA, RefreshableItemFetchingStatus.RESTRICTED);

        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus()).isEqualTo(RefreshStatus.FETCHING_COMPLETED);
    }

    @Test
    public void shouldResolveStatusToCompletedPartially() {
        // given
        summary.addItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.addItemSummary(
                RefreshableItem.SAVING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.addItemSummary(
                RefreshableItem.CHECKING_TRANSACTIONS, RefreshableItemFetchingStatus.INTERRUPTED);
        summary.addItemSummary(
                RefreshableItem.SAVING_TRANSACTIONS, RefreshableItemFetchingStatus.COMPLETED);

        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus())
                .isEqualTo(RefreshStatus.FETCHING_COMPLETED_PARTIALLY);
    }

    @Test
    public void shouldResolveStatusToInterruptedByBank() {
        // given
        summary.addItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.updateStatus(RefreshStatus.INTERRUPTED_BY_BANK_SERVICE_EXCEPTION);

        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus())
                .isEqualTo(RefreshStatus.INTERRUPTED_BY_BANK_SERVICE_EXCEPTION);
    }

    @Test
    public void shouldResolveStatusToInterruptedBySessionException() {
        // given
        summary.addItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.updateStatus(RefreshStatus.INTERRUPTED_BY_SESSION_EXCEPTION);

        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus())
                .isEqualTo(RefreshStatus.INTERRUPTED_BY_SESSION_EXCEPTION);
    }

    @Test
    public void shouldResolveStatusToInterruptedByRuntimeException() {
        // given
        summary.addItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.updateStatus(RefreshStatus.INTERRUPTED_BY_RUNTIME_EXCEPTION);

        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus())
                .isEqualTo(RefreshStatus.INTERRUPTED_BY_RUNTIME_EXCEPTION);
    }

    @Test
    public void shouldResolveStatusToInterruptedByException() {
        // given
        summary.addItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS, RefreshableItemFetchingStatus.COMPLETED);
        summary.updateStatus(RefreshStatus.INTERRUPTED_BY_EXCEPTION);

        // when
        summary.toJson();

        // then
        Assertions.assertThat(summary.getStatus())
                .isEqualTo(RefreshStatus.INTERRUPTED_BY_EXCEPTION);
    }
}
