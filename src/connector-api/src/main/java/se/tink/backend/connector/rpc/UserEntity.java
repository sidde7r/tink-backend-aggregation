package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.AssertTrue;
import se.tink.backend.core.Market;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;
import se.tink.libraries.i18n.Catalog;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "Persistent identifier for the user.", example = "2ce1f090a9304f13a15458d480f8a85d", required = true)
    private String externalId;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "Access token for the user. This is required later as authentication when fetching data for the user.", example = "9ac7f1611519afa1a66488ad11fda19a", required = true)
    private String token;

    @ApiModelProperty(value = "Market specific code for the user as a ISO 3166-1 country code.", example = "SE")
    private Market.Code market;

    @ApiModelProperty(value = "Set locale for the user. Defaults to default locale for the user's market.", example = "en_US")
    private String locale;

    @ApiModelProperty(value = "Set to either block or unblock the user.")
    private Boolean blocked;

    // Hidden. Re-add when we do something about the payload
    @ApiModelProperty(hidden = true, value = "The payload property can include arbitrary metadata provided by the financial institution in question that can be used either for deep-linking back to the app of the financial institution, for displaying additional information about the user, etc. The format is key-value, where key is a String and value any object.", example = "{}", required = false)
    private Map<String, Object> payload;

    @AssertTrue(message = "Locale must exist on the system.")
    @ApiModelProperty(hidden = true)
    public boolean isExistingLocale() {
        if (locale == null) {
            return true;
        }
        Optional<Catalog> catalogOptional = Catalog.getOptionalCatalog(locale);
        return catalogOptional.isPresent();
    }

    public String getExternalId() {
        return externalId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Market.Code getMarket() {
        return market;
    }

    public void setMarket(Market.Code market) {
        this.market = market;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }
}
