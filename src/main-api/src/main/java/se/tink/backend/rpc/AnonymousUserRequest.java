package se.tink.backend.rpc;

import com.google.common.collect.Lists;
import java.util.List;

public class AnonymousUserRequest {

    private String market;
    private String origin;
    private String locale;
    private List<String> flags;

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<String> getFlags() {
        return flags == null ? Lists.newArrayList() : flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }
}
