package se.tink.backend.rpc.webhook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.ApiModelProperty;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2WebHookEvent;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2WebHook {
    private static final TypeReference<Set<String>> SET_TYPE_REFERENCE = new TypeReference<Set<String>>() {
    };

    @ApiModelProperty(name = "secret", value = "A secret chosen by the consumer. This secret can be used when getting the actual web hook executed back to verify it is a valid one.", example = "67abc1e08fb64c92b450a13e0876330b", required = true)
    private String secret;
    @ApiModelProperty(name = "url", value = "The URL that will receive the web hook. Need to be over https, and Tink needs to have the domain registered in the database.", example = "https://www.clienturl.com/webhook/{userid}", required = true)
    private String url;
    private String eventsSerialized;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean validate(OAuth2Client client) {
        if (Strings.isNullOrEmpty(secret)) {
            return false;
        }

        Set<String> events = getEvents();
        if (events == null || events.size() == 0 || events.stream().anyMatch(Objects::isNull)) {
            return false;
        } else if (events.stream().anyMatch(e -> !OAuth2WebHookEvent.ALL.contains(e))) {
            return false;
        }

        try {
            URL parsedUrl = new URL(url);

            if (!Objects.equals(parsedUrl.getProtocol(), "https")) {
                return false;
            }

            Optional<String> domainsProperty = client.getPayloadValue(OAuth2Client.PayloadKey.WEBHOOK_DOMAINS);
            if (!domainsProperty.isPresent()) {
                return false;
            }

            Set<String> validDomains = ImmutableSet.copyOf(Splitter.on(',').trimResults().split(domainsProperty.get()));

            if (!isAmongValid(validDomains, parsedUrl)) {
                return false;
            }

        } catch (MalformedURLException e) {
            return false;
        }

        return true;
    }

    @ApiModelProperty(name = "events", value = "A list of events to register web hooks for.", example = "[\"signable-operation:update\"]", required = true)
    public Set<String> getEvents() {
        return SerializationUtils.deserializeFromString(this.eventsSerialized, SET_TYPE_REFERENCE);
    }

    public void setEvents(Set<String> events) {
        this.eventsSerialized = SerializationUtils.serializeToString(events);
    }

    @JsonIgnore
    public String getEventsSerialized() {
        return eventsSerialized;
    }

    @JsonIgnore
    public void setEventsSerialized(String eventsSerialized) {
        this.eventsSerialized = eventsSerialized;
    }

    private static boolean isAmongValid(Set<String> validDomains, final URL incoming) {
        return validDomains.stream().map(new Function<String, URL>() {
            @Nullable
            @Override
            public URL apply(String s) {
                try {
                    return new URL(s);
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        }).filter(Objects::nonNull)
                .anyMatch(url -> Objects.equals(url.getHost().toLowerCase(), incoming.getHost().toLowerCase()));
    }
}
