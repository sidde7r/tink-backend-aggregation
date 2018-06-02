package se.tink.backend.connector.rpc;

import com.google.common.collect.Sets;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.AssertTrue;
import se.tink.backend.core.oauth2.OAuth2WebHookEvent;
import se.tink.libraries.http.annotations.validation.ListNotNullOrEmpty;
import se.tink.libraries.http.annotations.validation.NoNullElements;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

public class WebhookEntity {

    @ApiModelProperty(value = "The internal Tink ID of the webhook.", example = "d3452eed13a0443997257ebe1042813c")
    private String id;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "A secret chosen by the partner. This secret can be used when getting the actual webhook executed back to verify it's a valid one.", example = "67abc1e08fb64c92b450a13e0876330b", required = true)
    private String secret;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "The URL that will receive the webhook. It needs to be over https, and Tink needs to have the domain registered in the database.", example = "https://www.clienturl.com/webhook", required = true)
    private String url;

    @ListNotNullOrEmpty
    @NoNullElements
    @ApiModelProperty(value = "A list of events to register webhooks for.", example = "[\"signable-operation:update, transaction:update\"]", allowableValues = OAuth2WebHookEvent.DOCUMENTED, required = true)
    private List<String> events;

    @AssertTrue(message = "Unknown event value. Allowed values are: " + OAuth2WebHookEvent.DOCUMENTED)
    private boolean isDocumentedEvents() {
        return events.stream().allMatch(OAuth2WebHookEvent.ALL::contains);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Set<String> getEvents() {
        return Sets.newHashSet(events);
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
