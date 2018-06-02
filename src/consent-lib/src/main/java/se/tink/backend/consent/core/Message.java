package se.tink.backend.consent.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Message {
    @ApiModelProperty(name = "message", value = "The message.", example = "I accept the Terms & Conditions")
    @Tag(1)
    private String message;

    @ApiModelProperty(name = "links", value = "Links to different parts of the message")
    @Tag(2)
    private List<Link> links;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
