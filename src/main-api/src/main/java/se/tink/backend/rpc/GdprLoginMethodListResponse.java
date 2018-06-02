package se.tink.backend.rpc;

import java.util.List;
import se.tink.backend.core.GdprLoginMethod;

public class GdprLoginMethodListResponse {

    private List<GdprLoginMethod> gdprLoginMethods;

    public GdprLoginMethodListResponse() {
        super();
    }

    public GdprLoginMethodListResponse(List<GdprLoginMethod> gdprLoginMethods) {
        this.gdprLoginMethods = gdprLoginMethods;
    }

    public List<GdprLoginMethod> getGdprLoginMethods() {
        return gdprLoginMethods;
    }

    public void setGdprLoginMethods(List<GdprLoginMethod> gdprLoginMethods) {
        this.gdprLoginMethods = gdprLoginMethods;
    }
}
