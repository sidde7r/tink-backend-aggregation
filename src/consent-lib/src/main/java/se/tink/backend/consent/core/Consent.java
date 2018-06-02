package se.tink.backend.consent.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Consent {
    @ApiModelProperty(name = "key", value = "The key of the consent.", example = "APP_TERMS_AND_CONDITIONS")
    @Tag(1)
    private String key;

    @ApiModelProperty(name = "version", value = "The semantic version of the consent.", example = "1.0.0")
    @Tag(2)
    private String version;

    @ApiModelProperty(name = "title", value = "The title of the consent.", example = "Terms and Conditions")
    @Tag(3)
    private String title;

    @ApiModelProperty(name = "body", value = "The body of the consent.", example = "<html>...</html>")
    @Tag(4)
    private String body;

    @ApiModelProperty(name = "messages", value = "Explicit messages that needs to be approved by the user.", example = "I accept the Terms & Conditions.")
    @Tag(5)
    private List<Message> messages;

    @ApiModelProperty(name = "checksum", value = "Checksum of the consent.", example = "7cb7d8321fa63ed7b7b5db8c7389c201e7970a647c862d975e407e09617a1156")
    @Tag(6)
    private String checksum;

    @ApiModelProperty(name = "attachments", value = "Different attachments connected to the consent. Each with defined with a key", example = "PRIVACY_POLICY: <html>...</html>")
    @Tag(7)
    private Map<String, String> attachments;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }
}
