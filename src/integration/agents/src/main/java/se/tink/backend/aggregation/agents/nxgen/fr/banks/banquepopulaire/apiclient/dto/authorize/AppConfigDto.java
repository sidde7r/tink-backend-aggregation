package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class AppConfigDto {

    private String version;

    private String currentDate;

    private String storeAppExterneConfigURL;

    private String cyberEditorialAnoURL;

    private String ssoStrategy;

    private String authBaseUrl;

    private String webSSOv3LogoutURL;

    private String webSSOv3CookieNamePrefix;

    private String webSSOv3LoginURL;

    private String webSSOv3LoginScreenURL;

    private String webSSOv3WebAPIBaseURL;

    private String webSSOv3WebAPIStepURL;

    private WebAPI2ResourceDto webAPI2;

    private KeepAliveResourceDto keepAlive;
}
