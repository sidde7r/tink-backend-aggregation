package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Params {
    @JsonProperty("content_id")
    private String contentId;
    @JsonProperty("otml_context")
    private String otmlContext;
    @JsonProperty("otml_stack")
    private String otmlStack;
    @JsonProperty("popup_datasource")
    private String popupDatasource;
    @JsonProperty("userId.error")
    private String userIdError;
    @JsonProperty("password.error")
    private String passwordError;

    public String getUserIdError() {
        return userIdError;
    }

    public String getPopUpDataSource() {
        return popupDatasource;
    }

    public String getPasswordError() {
        return passwordError;
    }
}
