package se.tink.backend.aggregation.agents.framework.wiremock.parsing;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public final class BodyParserImpl implements BodyParser {

    private static final String URL_ENCODING = StandardCharsets.UTF_8.toString();
    private static final ObjectMapper MAPPER =
            new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    @Override
    public ImmutableList<StringValuePattern> getStringValuePatterns(
            final String body, String type) {
        MediaType mediaType;
        try {
            // Remove charset information from type if there is any
            if (type.contains("charset")) {
                type = type.split(";")[0].trim();
            }
            mediaType = MediaType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(
                    String.format("No implemented parsing for Content-Type: %s", type));
        }

        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
            return asJsonPattern(body);
        }

        if (MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(mediaType)) {
            return asFormPattern(body);
        }

        if (MediaType.TEXT_XML_TYPE.isCompatible(mediaType)
                || MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)) {
            return asXmlPattern(body);
        }

        if (MediaType.TEXT_PLAIN_TYPE.isCompatible(mediaType)) {
            return asPlainText(body);
        }

        throw new UnsupportedOperationException(
                String.format("No implemented parsing for Content-Type: %s", type));
    }

    private ImmutableList<StringValuePattern> asXmlPattern(final String body) {
        return ImmutableList.of(WireMock.equalToXml(body));
    }

    private ImmutableList<StringValuePattern> asJsonPattern(final String body) {
        // Check if the body is a valid JSON string
        try {
            MAPPER.readTree(body);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "The following JSON body could not be parsed, please check if it is a valid JSON string "
                            + body);
        }

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

        try {
            String encodedName = URLEncoder.encode(pair.getName(), URL_ENCODING) + "=";
            if (pair.getValue() == null) {
                return encodedName;
            } else {
                return encodedName + URLEncoder.encode(pair.getValue(), URL_ENCODING);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
