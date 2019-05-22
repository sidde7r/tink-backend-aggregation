package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.entities.AliasEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class UserPaymentServiceProviderEntity {
    private int id;
    private String created;
    private String updated;
    private List<AliasEntity> alias;
    private AvatarEntity avatar;
    private String status;

    @JsonProperty("sub_status")
    private String subStatus;

    @JsonProperty("public_uuid")
    private String publicUuid;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("public_nick_name")
    private String publicNickName;

    private String language;
    private String region;

    @JsonProperty("session_timeout")
    private int sessionTimeout;

    @JsonProperty("daily_limit_without_confirmation_login")
    private DailyLimitWithoutConfirmationLoginEntity dailyLimitWithoutConfirmationLogin;

    @JsonProperty("notification_filters")
    private List<NotificationFiltersEntity> notificationFilters;

    @JsonProperty("certificate_distinguished_name")
    private String certificateDistinguishedName;

    public int getId() {
        return id;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }
}
