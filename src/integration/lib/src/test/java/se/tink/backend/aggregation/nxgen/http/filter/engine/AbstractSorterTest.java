package se.tink.backend.aggregation.nxgen.http.filter.engine;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Ignore
public class AbstractSorterTest {

    private static final Random RANDOM = new Random();
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected void checkOrder(List sorted, List<Class> expected) {
        Iterator<Class> it = expected.iterator();
        for (Object srt : sorted) {
            while (it.hasNext()) {
                Class toCheck = it.next();
                if (toCheck.equals(srt.getClass())) {
                    break;
                }
                if (!it.hasNext()) {
                    log.error("Expected: {}", expected);
                    log.error("Given: {}", sorted);
                    fail("List is not sorted as expected");
                }
            }
        }
    }

    protected <T> List<T> createRandomOrderShuffledList(List<T> toShuffle) {
        List<T> result = new ArrayList<>();
        List<T> tmp = new ArrayList<>(toShuffle);

        while (!tmp.isEmpty()) {
            T elem = tmp.remove(RANDOM.nextInt(tmp.size()));
            result.add(elem);
        }

        return result;
    }

    @FilterOrder(category = FilterPhases.PRE_PROCESS, order = 1)
    protected class PreProcessFilter1 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_PROCESS, order = 2)
    protected class PreProcessFilter2 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_PROCESS, order = 3)
    protected class PreProcessFilter3 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_PROCESS, order = 4)
    protected class PreProcessFilter4 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_PROCESS, order = 5)
    protected class PreProcessFilter5 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.CUSTOM, order = 1)
    protected class CustomFilter1 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.CUSTOM, order = 2)
    protected class CustomFilter2 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.CUSTOM, order = 3)
    protected class CustomFilter3 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.CUSTOM, order = 4)
    protected class CustomFilter4 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.CUSTOM, order = 5)
    protected class CustomFilter5 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_SECURITY, order = 1)
    protected class PreSecurityFilter1 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_SECURITY, order = 2)
    protected class PreSecurityFilter2 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_SECURITY, order = 3)
    protected class PreSecurityFilter3 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_SECURITY, order = 4)
    protected class PreSecurityFilter4 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.PRE_SECURITY, order = 5)
    protected class PreSecurityFilter5 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SECURITY, order = 1)
    protected class SecurityFilter1 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SECURITY, order = 2)
    protected class SecurityFilter2 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SECURITY, order = 3)
    protected class SecurityFilter3 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SECURITY, order = 4)
    protected class SecurityFilter4 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SECURITY, order = 5)
    protected class SecurityFilter5 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.POST_SECURITY, order = 1)
    protected class PostSecurityFilter1 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.POST_SECURITY, order = 2)
    protected class PostSecurityFilter2 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.POST_SECURITY, order = 3)
    protected class PostSecurityFilter3 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.POST_SECURITY, order = 4)
    protected class PostSecurityFilter4 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.POST_SECURITY, order = 5)
    protected class PostSecurityFilter5 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SEND, order = 1)
    protected class SendFilter1 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SEND, order = 2)
    protected class SendFilter2 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SEND, order = 3)
    protected class SendFilter3 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SEND, order = 4)
    protected class SendFilter4 extends AbstractTestFilter {}

    @FilterOrder(category = FilterPhases.SEND, order = 5)
    protected class SendFilter5 extends AbstractTestFilter {}

    protected class NotAnnotated1 extends AbstractTestFilter {}

    protected abstract class AbstractTestFilter extends Filter {
        @Override
        public HttpResponse handle(HttpRequest httpRequest)
                throws HttpClientException, HttpResponseException {
            return null;
        }

        public String toString() {
            return this.getClass().getSimpleName();
        }
    }
}
