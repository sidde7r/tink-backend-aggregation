package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AbstractResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.LinkEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SHBUtilsTest {

    List<String> list = Lists.newArrayList("foo", "bar");

    static AbstractResponse createResponse(String rel, String url) {
        AbstractResponse r = new AbstractResponse();
        HashMap<String, LinkEntity> linksMap = new HashMap<>();
        LinkEntity link = new LinkEntity();
        link.setRel(rel);
        link.setHref(url);
        r.setLinks(Lists.newArrayList(link));
        linksMap.put(rel, link);
        r.setLinksMap(linksMap);
        return r;
    }

    @Test
    public void testGetEntitiesWithLinkPresentInList() throws Exception {
        Predicate<AbstractResponse> predicate = SHBUtils.getEntitiesWithLinkPresentInList("rel", list);

        assertFalse(predicate.apply(createResponse("rel", "troll")));
        assertFalse(predicate.apply(createResponse("rel", "troll?someQuery=value")));

        assertTrue(predicate.apply(createResponse("rel", "foo")));
        assertTrue(predicate.apply(createResponse("rel", "foo?someQuery=value")));
        assertTrue(predicate.apply(createResponse("rel", "bar")));
        assertTrue(predicate.apply(createResponse("rel", "bar?someQuery=value")));
    }

    @Test
    public void testGetEntitiesWithLinkPresentInListWithNegation() {
        Predicate<AbstractResponse> predicate = Predicates.not(SHBUtils.getEntitiesWithLinkPresentInList("rel", list));

        assertTrue(predicate.apply(createResponse("rel", "troll")));
        assertTrue(predicate.apply(createResponse("rel", "troll?someQuery=value")));

        assertFalse(predicate.apply(createResponse("rel", "foo")));
        assertFalse(predicate.apply(createResponse("rel", "foo?someQuery=value")));
        assertFalse(predicate.apply(createResponse("rel", "bar")));
        assertFalse(predicate.apply(createResponse("rel", "bar?someQuery=value")));
    }

    @Test
    public void testFilteringOutThoseInList() {
        Predicate<AbstractResponse> predicate = Predicates.not(SHBUtils.getEntitiesWithLinkPresentInList("rel", list));

        List<AbstractResponse> responses = Lists.newArrayList(
                createResponse("rel", "troll"),
                createResponse("rel", "troll?someQuery=value"),
                createResponse("rel", "foo"),
                createResponse("rel", "foo?someQuery=value"),
                createResponse("rel", "bar"),
                createResponse("rel", "bar?someQuery=value"));

        Iterable<AbstractResponse> filtered = Iterables.filter(responses, predicate);
        assertEquals(2, Iterables.size(filtered));
    }

    @Test
    public void testCleanHandelsbankenDescription() {
        String inputStringOne = "PRESSBYR$N";
        String inputStringTwo = "L{KARBES@K";
        String inputStringThree = "B@NOR OCH BLAD";

        assertEquals(
                "PRESSBYRÅN", SHBUtils.unescapeAndCleanTransactionDescription(inputStringOne));
        assertEquals(
                "LÄKARBESÖK", SHBUtils.unescapeAndCleanTransactionDescription(inputStringTwo));
        assertEquals(
                "BÖNOR OCH BLAD", SHBUtils.unescapeAndCleanTransactionDescription(inputStringThree));
    }
}
