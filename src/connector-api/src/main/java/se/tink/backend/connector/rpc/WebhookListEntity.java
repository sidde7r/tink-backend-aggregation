package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class WebhookListEntity {

    @ApiModelProperty(value = "A list with the registered webhooks.")
    private List<WebhookEntity> webhooks;

    public WebhookListEntity(List<WebhookEntity> webhooks) {
        this.webhooks = webhooks;
    }
}
