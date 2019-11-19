package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.Secrets;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail.TimeStampProvider;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HeaderEntity {
    @JsonProperty("Time")
    private String time = TimeStampProvider.getTimestamp();

    @JsonProperty("Cellular")
    private boolean cellular = false;

    @JsonProperty("Origem")
    private int origem = 0;

    @JsonProperty("ApiKey")
    private String apiKey = Secrets.API_KEY;

    @JsonProperty("DeviceId")
    private String deviceId = FieldValues.DEFAULT_DEVICE_ID;

    @JsonProperty("RequestId")
    private String requestId = FieldValues.REQUEST_ID;

    @JsonProperty("Aid")
    @JsonInclude(NON_NULL)
    private String aid;

    @JsonProperty("Sid")
    @JsonInclude(NON_NULL)
    private String sid;

    @JsonProperty("Contexto")
    @JsonInclude(NON_NULL)
    private String context;

    @JsonProperty("IdServico")
    @JsonInclude(NON_NULL)
    private Integer serviceId;

    @JsonProperty("OpToken")
    private String opToken;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public void setOpToken(String opToken) {
        this.opToken = opToken;
    }

    public static class HeaderEntityBuilder {
        private HeaderEntity header = new HeaderEntity();

        public HeaderEntityBuilder withDeviceId(String deviceId) {
            header.setDeviceId(deviceId);
            return this;
        }

        public HeaderEntityBuilder withAuthId(String aid) {
            header.setAid(aid);
            return this;
        }

        public HeaderEntityBuilder withSessionId(String sessionId) {
            header.setSid(sessionId);
            return this;
        }

        public HeaderEntityBuilder withContext(String context) {
            header.setContext(context);
            return this;
        }

        public HeaderEntityBuilder withServiceId(Integer serviceId) {
            header.setServiceId(serviceId);
            return this;
        }

        public HeaderEntityBuilder withOpToken(String opToken) {
            header.setOpToken(opToken);
            return this;
        }

        public HeaderEntity build() {
            return header;
        }
    }
}
