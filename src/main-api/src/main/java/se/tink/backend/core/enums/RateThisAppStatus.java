package se.tink.backend.core.enums;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public enum RateThisAppStatus {
    SENT("sent"), NOT_SENT("not-sent"), USER_CLICKED_IGNORE("user-clicked-ignore"), USER_CLICKED_RATE_IN_STORE(
            "user-clicked-rate-in-store");

    private String status;

    RateThisAppStatus(String status) {
        this.status = status;
    }

    public static RateThisAppStatus valueOfOrNull(String status) {
        if (Strings.isNullOrEmpty(status)) {
            return null;
        }

        for(RateThisAppStatus value : values()) {
            if (Objects.equal(value.getStatus(), status)) {
                return value;
            }
        }

        return null;
    }

    public String getStatus() {
        return status;
    }
}
