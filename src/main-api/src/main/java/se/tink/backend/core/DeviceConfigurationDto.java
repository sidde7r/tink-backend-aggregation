package se.tink.backend.core;

import io.protostuff.Tag;
import java.util.List;

public class DeviceConfigurationDto {
    @Tag(1)
    private List<String> flags;
    @Tag(2)
    private List<Market> markets;

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public List<Market> getMarkets() {
        return markets;
    }

    public void setMarkets(List<Market> markets) {
        this.markets = markets;
    }

}
