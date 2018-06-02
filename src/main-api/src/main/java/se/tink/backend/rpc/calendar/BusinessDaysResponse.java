package se.tink.backend.rpc.calendar;

import io.protostuff.Tag;
import java.util.List;
import java.util.Map;

public class BusinessDaysResponse {
    @Tag(1)
    private Map<String, Map<String, List<Integer>>> businessDays;

    public BusinessDaysResponse(Map<String, Map<String, List<Integer>>> businessDays) {
        this.businessDays = businessDays;
    }

    public Map<String, Map<String, List<Integer>>> getBusinessDays() {
        return businessDays;
    }
}
