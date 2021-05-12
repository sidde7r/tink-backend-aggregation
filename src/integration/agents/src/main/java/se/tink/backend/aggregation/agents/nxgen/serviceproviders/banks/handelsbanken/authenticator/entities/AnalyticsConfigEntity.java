package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AnalyticsConfigEntity {
    private String gaTrackingID;
    private boolean gaExceptions;
    private boolean gaScreenTracking;
    private String gaSampleRate;
    private String shbMarket;
    private String shbCustomerType;
    private String shbClearingNo;
    private String shbLoggedIn;
    private String gender;
    private String shbGroup;
}
