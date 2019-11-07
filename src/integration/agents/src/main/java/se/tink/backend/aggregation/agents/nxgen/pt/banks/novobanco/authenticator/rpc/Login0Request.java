package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.BodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues.DEFAULT_DEVICE_ID;

@JsonObject
public class Login0Request {
  @JsonProperty("Header")
  private HeaderEntity header;

  @JsonProperty("Body")
  private BodyEntity body;

  public Login0Request(){}

  public Login0Request(String userName, String secret) {
    header = new HeaderEntity.HeaderEntityBuilder().withDeviceId(DEFAULT_DEVICE_ID).build();
    body = new BodyEntity(userName, secret);
  }
}
