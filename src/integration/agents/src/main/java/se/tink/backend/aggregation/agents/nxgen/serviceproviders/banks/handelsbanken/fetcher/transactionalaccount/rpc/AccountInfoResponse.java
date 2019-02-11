package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.LabelValueEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoResponse extends BaseResponse {
    private String heading;
    private List<LabelValueEntity> items;

    @JsonIgnore
    public Map<String, String> getValuesByLabel() {
        return Optional.ofNullable(items).orElse(Collections.emptyList()).stream()
                .filter(lv -> !Strings.isNullOrEmpty(lv.getLabel()))
                .collect(Collectors.toMap(lv -> lv.getLabel().toLowerCase(), LabelValueEntity::getValue));
    }

    public String getHeading() {
        return heading;
    }

    public List<LabelValueEntity> getItems() {
        return items;
    }
}
