package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ApplicationSummary {
    @Tag(1)
    @ApiModelProperty(name = "description", value = "Description of the application.", example = "1.35% interest rate")
    private String description;
    @Tag(2)
    @ApiModelProperty(name = "id", value = "The application ID.", required = true, example = "89cfe91ac88341e2b9f00e6f85280392")
    private String id;
    @Tag(3)
    @ApiModelProperty(name = "imageUrl", value = "URL to an (icon) image, representing the product.", example = "http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/tink.png")
    private String imageUrl;
    @Tag(4)
    @ApiModelProperty(name = "progress", value = "The overall progress (0 to 1) of the application.", example = "0.60")
    private double progress;
    @Tag(5)
    @ApiModelProperty(name = "provider", value = "The name of the bank/service provider that provides the product/service, which the application is for.", example = "banco-tink")
    private String provider;
    @Tag(6)
    @ApiModelProperty(name = "statusKey", value = "Application status described by a set of pre-defined keys.", example = "SIGNED", allowableValues = ApplicationStatusKey.DOCUMENTED)
    private String statusKey;
    @Tag(7)
    @ApiModelProperty(name = "statusBody", value = "Status body text.", example = "Banco Tink is currently evaluating your application...")
    private String statusBody;
    @Tag(10)
    @ApiModelProperty(name = "statusPayload", value = "Status payload with i.e. links.", example = "[{\"type\":\"link\",\"text\":\"www.tink.se\",\"url\":\"https://www.tink.se\"},{\"type\":\"button\",\"text\":\"Call Tink\",\"url\":\"tel:/0046850908900/\"}]")
    private String statusPayload;
    @Tag(11)
    @ApiModelProperty(name = "statusTitle", value = "Status title.", example = "The application is being processed")
    private String statusTitle;
    @Tag(8)
    @ApiModelProperty(name = "title", value = "A title of the application.", example = "Move mortgage to Banco Tink")
    private String title;
    @Tag(9)
    @ApiModelProperty(name = "type", value = "The type of this application.", required = true, example = "mortgage/switch-provider", allowableValues = ApplicationType.DOCUMENTED)
    private String type;
    
    public String getDescription() {
        return description;
    }
    
    public String getId() {
        return id;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public double getProgress() {
        return progress;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public ApplicationStatusKey getStatusKey() {
        if (statusKey == null) {
            return null;
        } else {
            return ApplicationStatusKey.valueOf(statusKey);
        }
    }
    
    public String getStatusBody() {
        return statusBody;
    }
    
    public String getStatusPayload() {
        return statusPayload;
    }
    
    public String getStatusTitle() {
        return statusTitle;
    }
    
    public String getTitle() {
        return title;
    }
    
    public ApplicationType getType() {
        if (type == null) {
            return null;
        } else {
            return ApplicationType.fromScheme(type);
        }
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setProgress(double progress) {
        this.progress = progress;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public void setStatusKey(ApplicationStatusKey key) {
        if (key == null) {
            this.statusKey = null;
        } else {
            this.statusKey = key.toString();
        }
    }
    
    public void setStatusBody(String statusBody) {
        this.statusBody = statusBody;
    }
    
    public void setStatusPayload(String statusPayload) {
        this.statusPayload = statusPayload;
    }
    
    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setType(ApplicationType type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }
}
