package se.tink.backend.aggregation.agents.consent.suppliers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;

@RunWith(MockitoJUnitRunner.class)
public class ItemsSupplierTest {

    @Mock private ManualAuthenticateRequest mockedAuthenticationRequest;
    @Mock private RefreshInformationRequest mockedRefreshRequest;
    @Mock private RefreshScope mockedRefreshScope;

    @Test
    public void shouldFallbackToDefaultSetForAuthRequestWithNullRefreshScope() {
        // given
        given(mockedAuthenticationRequest.getRefreshScope()).willReturn(null);

        // when
        Set<RefreshableItem> items = ItemsSupplier.get(mockedAuthenticationRequest);

        // then
        assertThat(items)
                .containsExactlyInAnyOrderElementsOf(getDefaultItemsWithIdentityDataItem());
    }

    @Test
    public void shouldFallbackToDefaultSetForAuthRequestWithEmptyRefreshScope() {
        // given
        given(mockedAuthenticationRequest.getRefreshScope()).willReturn(mockedRefreshScope);
        given(mockedRefreshScope.getRefreshableItemsIn()).willReturn(Collections.emptySet());

        // when
        Set<RefreshableItem> items = ItemsSupplier.get(mockedAuthenticationRequest);

        // then
        assertThat(items)
                .containsExactlyInAnyOrderElementsOf(getDefaultItemsWithIdentityDataItem());
    }

    @Test
    public void shouldProvideItemsAsTheyAreForAuthRequest() {
        // given
        Set<RefreshableItem> expectedItems =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.CHECKING_TRANSACTIONS);
        given(mockedAuthenticationRequest.getRefreshScope()).willReturn(mockedRefreshScope);
        given(mockedRefreshScope.getRefreshableItemsIn()).willReturn(expectedItems);

        // when
        Set<RefreshableItem> items = ItemsSupplier.get(mockedAuthenticationRequest);

        // then
        assertThat(items).containsExactlyInAnyOrderElementsOf(expectedItems);
    }

    @Test
    public void shouldFallbackToDefaultISetForRefreshRequest() {
        // when
        Set<RefreshableItem> items = ItemsSupplier.get(mockedRefreshRequest);

        // then
        assertThat(items)
                .containsExactlyInAnyOrderElementsOf(getDefaultItemsWithIdentityDataItem());
    }

    private Set<RefreshableItem> getDefaultItemsWithIdentityDataItem() {
        Set<RefreshableItem> itemsExpectedToBeRefreshed =
                Sets.newHashSet(RefreshableItem.allRefreshableItemsAsArray());
        itemsExpectedToBeRefreshed.add(RefreshableItem.IDENTITY_DATA);
        return itemsExpectedToBeRefreshed;
    }
}
