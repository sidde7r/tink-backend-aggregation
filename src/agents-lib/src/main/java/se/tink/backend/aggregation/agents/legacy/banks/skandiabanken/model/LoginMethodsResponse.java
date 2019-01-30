package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import java.util.List;

public class LoginMethodsResponse {
    private List<LoginMethod> loginMethods;
    private int defaultTimeToLive;
    private boolean maintenance;

    public List<LoginMethod> getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(List<LoginMethod> loginMethods) {
        this.loginMethods = loginMethods;
    }

    public int getDefaultTimeToLive() {
        return defaultTimeToLive;
    }

    public void setDefaultTimeToLive(int defaultTimeToLive) {
        this.defaultTimeToLive = defaultTimeToLive;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }
}
