package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.MembershipTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.ResponseValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Characteristics;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Response;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.SubscribeTypeItemsItem;
import se.tink.libraries.streamutils.StreamUtils;

@Getter
public class IdentificationRoutingResponse {

    @JsonProperty("characteristics")
    private Characteristics characteristics;

    @JsonProperty("response")
    private Response response;

    @JsonIgnore
    public boolean isValid() {
        return Objects.nonNull(response)
                && ResponseValues.RESPONSE_STATUS_SUCCESS.equals(response.getCode())
                && Objects.nonNull(characteristics)
                && !Strings.isNullOrEmpty(characteristics.getBankId())
                && (Objects.nonNull(characteristics.getSubscribeTypeItems())
                        && characteristics.getSubscribeTypeItems().size() == 1);
    }

    @JsonIgnore
    public MembershipTypes getMembershipType() {
        String code =
                Optional.ofNullable(
                                characteristics.getSubscribeTypeItems().stream()
                                        .collect(StreamUtils.toSingleton()))
                        .orElse(new SubscribeTypeItemsItem())
                        .getCode();
        return MembershipTypes.fromString(code);
    }

    @JsonIgnore
    public String getMembershipTypeLabel() {
        return Optional.ofNullable(
                        characteristics.getSubscribeTypeItems().stream()
                                .collect(StreamUtils.toSingleton()))
                .orElse(new SubscribeTypeItemsItem())
                .getLabel();
    }

    @JsonIgnore
    public String getMembershipTypeCode() {
        return Optional.ofNullable(
                        characteristics.getSubscribeTypeItems().stream()
                                .collect(StreamUtils.toSingleton()))
                .orElse(new SubscribeTypeItemsItem())
                .getCode();
    }

    @JsonIgnore
    public String getMembershipTypeValue() {
        return CaisseEpargneConstants.MEMBERSHIP_TYPES_TO_VALUE_MAP.get(getMembershipType());
    }

    @JsonIgnore
    public String getBankId() {
        if (Objects.isNull(characteristics)) {
            return "";
        }
        return characteristics.getBankId();
    }
}
