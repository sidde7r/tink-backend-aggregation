package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BodyEntity {
  @JsonProperty("Device")
  private DeviceEntity device = new DeviceEntity();

  @JsonProperty("Secret")
  private String secret;

  @JsonProperty("Env")
  private EnvEntity env = new EnvEntity();

  @JsonProperty("LoginWith")
  private int loginWith = FieldValues.LOGIN_MODE;

  @JsonProperty("RememberMe")
  private boolean rememberMe = true;

  @JsonProperty("UserName")
  private String userName;

  @JsonProperty("App")
  private AppEntity app = new AppEntity();

  public BodyEntity() {}

  public BodyEntity(String userName, String secret) {
    this.userName = userName;
    this.secret = secret;
  }
}
