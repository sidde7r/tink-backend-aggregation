package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.LinkDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.TransactionsLinksEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class GetTransactionsResponseTest {

    @Test
    public void shouldReturnUrlOnNextKey() throws NoSuchFieldException, IllegalAccessException {
        // given
        TransactionsLinksEntity links = mock(TransactionsLinksEntity.class);
        LinkDetailsEntity next = mock(LinkDetailsEntity.class);
        String href = "href";

        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();
        Field linksField = GetTransactionsResponse.class.getDeclaredField("links");
        linksField.setAccessible(true);
        linksField.set(getTransactionsResponse, links);

        when(links.getNext()).thenReturn(next);
        when(next.getHref()).thenReturn(href);

        // when
        URL resp = getTransactionsResponse.nextKey();

        // then
        assertNotNull(resp);
        assertEquals(href, resp.get());
    }

    @Test
    public void shouldReturnNullOnNextKey() {
        // given
        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();

        // when
        URL resp = getTransactionsResponse.nextKey();

        // then
        assertNull(resp);
    }

    @Test
    public void shouldReturnTrueOnCanFetchMore()
            throws NoSuchFieldException, IllegalAccessException {
        // given
        TransactionsLinksEntity links = mock(TransactionsLinksEntity.class);
        LinkDetailsEntity next = mock(LinkDetailsEntity.class);
        LinkDetailsEntity self = mock(LinkDetailsEntity.class);
        String nextHref = "href";
        String selfHref = "self";

        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();
        Field linksField = GetTransactionsResponse.class.getDeclaredField("links");
        linksField.setAccessible(true);
        linksField.set(getTransactionsResponse, links);

        when(links.getNext()).thenReturn(next);
        when(links.getSelf()).thenReturn(self);
        when(next.getHref()).thenReturn(nextHref);
        when(self.getHref()).thenReturn(selfHref);

        // when
        Optional<Boolean> resp = getTransactionsResponse.canFetchMore();

        // then
        assertNotNull(resp);
        assertTrue(resp.isPresent());
        assertTrue(resp.get());
    }

    @Test
    public void shouldReturnFalseOnCanFetchMoreWhenHrefsAreEqual()
            throws NoSuchFieldException, IllegalAccessException {
        // given
        TransactionsLinksEntity links = mock(TransactionsLinksEntity.class);
        LinkDetailsEntity next = mock(LinkDetailsEntity.class);
        LinkDetailsEntity self = mock(LinkDetailsEntity.class);
        String nextHref = "href";
        String selfHref = "href";

        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();
        Field linksField = GetTransactionsResponse.class.getDeclaredField("links");
        linksField.setAccessible(true);
        linksField.set(getTransactionsResponse, links);

        when(links.getNext()).thenReturn(next);
        when(links.getSelf()).thenReturn(self);
        when(next.getHref()).thenReturn(nextHref);
        when(self.getHref()).thenReturn(selfHref);

        // when
        Optional<Boolean> resp = getTransactionsResponse.canFetchMore();

        // then
        assertNotNull(resp);
        assertTrue(resp.isPresent());
        assertFalse(resp.get());
    }

    @Test
    public void shouldReturnFalseOnCanFetchMoreWhenSelfIsNull()
            throws NoSuchFieldException, IllegalAccessException {
        // given
        TransactionsLinksEntity links = mock(TransactionsLinksEntity.class);
        LinkDetailsEntity self = mock(LinkDetailsEntity.class);
        String selfHref = "self";

        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();
        Field linksField = GetTransactionsResponse.class.getDeclaredField("links");
        linksField.setAccessible(true);
        linksField.set(getTransactionsResponse, links);

        when(links.getNext()).thenReturn(null);
        when(links.getSelf()).thenReturn(self);
        when(self.getHref()).thenReturn(selfHref);

        // when
        Optional<Boolean> resp = getTransactionsResponse.canFetchMore();

        // then
        assertNotNull(resp);
        assertTrue(resp.isPresent());
        assertFalse(resp.get());
    }

    @Test
    public void shouldReturnFalseOnCanFetchMoreWhenNextIsNull()
            throws NoSuchFieldException, IllegalAccessException {
        // given
        TransactionsLinksEntity links = mock(TransactionsLinksEntity.class);
        LinkDetailsEntity next = mock(LinkDetailsEntity.class);
        String nextHref = "href";

        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();
        Field linksField = GetTransactionsResponse.class.getDeclaredField("links");
        linksField.setAccessible(true);
        linksField.set(getTransactionsResponse, links);

        when(links.getNext()).thenReturn(next);
        when(links.getSelf()).thenReturn(null);
        when(next.getHref()).thenReturn(nextHref);

        // when
        Optional<Boolean> resp = getTransactionsResponse.canFetchMore();

        // then
        assertNotNull(resp);
        assertTrue(resp.isPresent());
        assertFalse(resp.get());
    }

    @Test
    public void shouldReturnEmptyCollectionWhenNoTransactions() {
        // given
        GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();

        // when
        Collection<? extends Transaction> resp = getTransactionsResponse.getTinkTransactions();

        // then
        assertNotNull(resp);
        assertEquals(0, resp.size());
    }
}
