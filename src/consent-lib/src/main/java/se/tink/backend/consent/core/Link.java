package se.tink.backend.consent.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Link {
    @ApiModelProperty(name = "destination", value = "Where the link should go, can for example be a reference to an attachment or an https link.", example = "PRIVACY_POLICY")
    @Tag(1)
    private String destination;

    @ApiModelProperty(name = "start", value = "The start position within the message that link should be created for.", example = "0")
    @Tag(2)
    private int start;

    @ApiModelProperty(name = "end", value = "The end position within the message that link should be created for.", example = "10")
    @Tag(3)
    private int end;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
