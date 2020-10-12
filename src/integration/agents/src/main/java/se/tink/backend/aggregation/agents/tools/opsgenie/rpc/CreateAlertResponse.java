package se.tink.backend.aggregation.agents.tools.opsgenie.rpc;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CreateAlertResponse {
    private DataEntity data;
    private BigDecimal took;
    private String requestId;

    @Getter
    @JsonObject
    public class DataEntity {
        private boolean success;
        private String action;
        private String processedAt;
        private String integrationId;
        private boolean isSuccess;
        private String status;
        private String alertId;
        private String alias;
    }
}
