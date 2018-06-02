package se.tink.analytics.jobs.loandata;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class UserCoordinates implements Serializable {

    private static final long serialVersionUID = -1028184213887072561L;

    private UUID userId;
    private UUID areaId;

    public static Map<String, String> getColumnMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("userId", "userid");
        map.put("areaId", "areaid");

        return map;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getAreaId() {
        return areaId;
    }

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
    }
}