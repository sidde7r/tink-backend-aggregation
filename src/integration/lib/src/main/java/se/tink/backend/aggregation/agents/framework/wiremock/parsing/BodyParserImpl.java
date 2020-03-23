package se.tink.backend.aggregation.agents.framework.wiremock.parsing;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public final class BodyParserImpl implements BodyParser {

    private static String URL_ENCODING = StandardCharsets.UTF_8.toString();

    @Override
    public ImmutableList<StringValuePattern> getStringValuePatterns(
            final String body, final String mediaType) {

        if (MediaType.APPLICATION_JSON.equalsIgnoreCase(mediaType)) {
            return asJsonPattern(body);
        }

        if (MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(mediaType)) {
            return asFormPattern(body);
        }

        if (MediaType.TEXT_PLAIN.equalsIgnoreCase(mediaType)) {
            return asPlainText(body);
        }

        throw new UnsupportedOperationException(
                String.format("No implemented parsing for Content-Type: %s", mediaType));
    }

    private ImmutableList<StringValuePattern> asJsonPattern(final String body) {
        return ImmutableList.of(WireMock.equalToJson(body, true, true));
    }

    private ImmutableList<StringValuePattern> asFormPattern(final String body) {

        List<NameValuePair> queryList = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);

        Preconditions.checkArgument(
                !queryList.isEmpty(), String.format("%s is not a valid form body.", body));

        return queryList.stream()
                .map(BodyParserImpl::toUrlSafeQueryPair)
                .map(WireMock::containing)
                .collect(ImmutableList.toImmutableList());
    }

    private ImmutableList<StringValuePattern> asPlainText(final String body) {
        return ImmutableList.of(WireMock.equalTo(body));
    }

    private static String toUrlSafeQueryPair(final NameValuePair pair) {

        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(pair.getName()), "The provided form body is invalid.");
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(pair.getValue()), "The provided form body is invalid.");

        try {
            return URLEncoder.encode(pair.getName(), URL_ENCODING)
                    + "="
                    + URLEncoder.encode(pair.getValue(), URL_ENCODING);

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
