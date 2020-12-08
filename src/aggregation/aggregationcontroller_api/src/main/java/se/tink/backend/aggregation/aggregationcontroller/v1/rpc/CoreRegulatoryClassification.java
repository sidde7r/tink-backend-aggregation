package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreRegulatoryClassification {
    @JsonProperty private CorePsd2Classification psd2;

    @JsonIgnore
    public Optional<CorePsd2Classification> getPsd2() {
        return Optional.ofNullable(psd2);
    }

    public void setPsd2(CorePsd2Classification psd2) {
        Preconditions.checkNotNull(psd2);
        this.psd2 = psd2;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("psd2", psd2).toString();
    }
}
