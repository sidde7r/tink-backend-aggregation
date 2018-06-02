package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.core.Market;

import javax.annotation.Nullable;

public class CategorizationConfiguration {
    @JsonProperty
    private String executable = "../fasttext/fasttext";
    @JsonProperty
    private List<String> betaUserIdPrefixes = Lists.newArrayList();
    @JsonProperty
    private boolean betaForAllUsers = false;
    @JsonProperty
    private boolean betaForFeatureFlaggedUsers = false;
    @JsonProperty
    private List<FastTextConfiguration> shadowCategorizers = Lists.newArrayList();
    @JsonProperty
    private List<FastTextConfiguration> fastTextCategorizers = Lists.newArrayList(
            new FastTextConfiguration("minimal-model", "resource:///categorization-lib/minimal_model.bin", Market.Code.SE,
                    Collections.singletonList(new IdentityStringConverterFactory())));
    @JsonProperty
    private List<FastTextConfiguration> fastTextIncomeCategorizers = Lists.newArrayList();
    @JsonProperty
    private boolean enableGiroLookup = false;
    @JsonProperty
    private boolean usePatchedFastText = false;
    @JsonProperty
    private boolean microserviceEnabled = false;
    @JsonProperty
    private double minimumPercentage = 0.5;
    @JsonProperty
    private double topTwoPercentageDelta = 0.15;
    @JsonProperty
    private Map<Market.Code, Double> marketToUncategorizationProbability = Maps.newHashMap();
    @JsonProperty
    private double defaultUncategorizationProbability = 0.0;
    @JsonProperty
    private Market.Code defaultMarket = Market.Code.SE;
    @Nullable
    @JsonProperty
    private String merchantsFile;

    public String getExecutable() {
        return executable;
    }

    public List<String> getBetaUserIdPrefixes() {
        return betaUserIdPrefixes;
    }

    public boolean isBetaForAllUsers() {
        return betaForAllUsers;
    }

    public List<FastTextConfiguration> getShadowCategorizers() {
        return shadowCategorizers;
    }

    public List<FastTextConfiguration> getFastTextCategorizers() {
        return fastTextCategorizers;
    }

    public boolean isEnableGiroLookup() {
        return enableGiroLookup;
    }

    @VisibleForTesting
    public void setUsePatchedFastText(boolean usePatchedFastText) {
        this.usePatchedFastText = usePatchedFastText;
    }

    public boolean usePatchedFastText() {
        return usePatchedFastText;
    }

    public boolean isMicroserviceEnabled() {
        return microserviceEnabled;
    }

    public double getMinimumPercentage() {
        return minimumPercentage;
    }

    @VisibleForTesting
    public void setMinimumPercentage(double minimumPercentage) {
        this.minimumPercentage = minimumPercentage;
    }

    public double getTopTwoPercentageDelta() {
        return topTwoPercentageDelta;
    }

    @VisibleForTesting
    public void setTopTwoPercentageDelta(double topTwoPercentageDelta) {
        this.topTwoPercentageDelta = topTwoPercentageDelta;
    }

    public Map<Market.Code, Double> getMarketToUncategorizationProbability() {
        return marketToUncategorizationProbability;
    }

    public double getDefaultUncategorizationProbability() {
        return defaultUncategorizationProbability;
    }

    public boolean isBetaForFeatureFlaggedUsers() {
        return betaForFeatureFlaggedUsers;
    }

    public Market.Code getDefaultMarket() {
        return defaultMarket;
    }

    public List<FastTextConfiguration> getFastTextIncomeCategorizers() {
        return fastTextIncomeCategorizers;
    }

    public Optional<String> getMerchantsFile() {
        return Optional.ofNullable(merchantsFile);
    }

    public void setMerchants(String merchantsFile) {
        this.merchantsFile = merchantsFile;
    }
}
