package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;

import se.tink.backend.core.Market;

public class MarketListResponse {
  
    @Tag(1)
    private List<Market> markets;

    public List<Market> getMarkets() {
        return markets;
    }

    public void setMarkets(List<Market> markets) {
        this.markets = markets;
    }
}
