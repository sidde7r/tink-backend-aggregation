package se.tink.backend.aggregation.agents.framework.wiremock.entities.test;

import static org.hamcrest.core.IsCollectionContaining.hasItem;

import com.google.common.collect.ImmutableSet;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;

public final class HTTPRequestTest {

    @Test
    public void ensurePath_isParsed_butNotDecoded() {

        final String someEncodedUrl = "https://host.com/so%21meurl?k%21y=value&key2=value2";

        final HTTPRequest request =
                new HTTPRequest.Builder("", someEncodedUrl, ImmutableSet.of()).build();
        final String path = request.getPath();

        Assert.assertEquals("/so%21meurl", path);
    }

    @Test
    public void ensureQuery_isParsed_andDecoded() {

        final String someEncodedUrl = "https://host.com/so%21meurl?k%21y=value&key2=value2";

        final HTTPRequest request =
                new HTTPRequest.Builder("", someEncodedUrl, ImmutableSet.of()).build();
        final ImmutableSet<NameValuePair> query = request.getQuery();

        Assert.assertEquals(2, query.size());
        Assert.assertThat(query, hasItem(new BasicNameValuePair("k!y", "value")));
        Assert.assertThat(query, hasItem(new BasicNameValuePair("key2", "value2")));
    }
}
