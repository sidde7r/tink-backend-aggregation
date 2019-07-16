package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;

public class PayPalUtil {

    public static <T> T fromJSON(final TypeReference<T> type, final String jsonPacket) {
        T data = null;

        try {
            data = new ObjectMapper().readValue(jsonPacket, type);
        } catch (Exception e) {
            // Handle the problem
        }
        return data;
    }

    public static Optional<LinkEntity> findByRelation(List<LinkEntity> links, String type) {
        return links.stream()
                .filter(linkEntity -> linkEntity.getRelation().equalsIgnoreCase(type))
                .findFirst();
    }
}
