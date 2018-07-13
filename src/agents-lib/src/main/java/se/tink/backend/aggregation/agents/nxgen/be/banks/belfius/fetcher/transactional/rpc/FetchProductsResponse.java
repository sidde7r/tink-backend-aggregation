package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc;

import java.util.Map;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusProduct;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusProductMap;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchProductsResponse extends BelfiusResponse {

    public Stream<Map.Entry<String, BelfiusProduct>> stream() {
        return ScreenUpdateResponse.findWidgetOrElseThrow(this,
                BelfiusConstants.Widget.PRODUCT_LIST_REPEATER_DETAIL)
                .getProperties(BelfiusProductMap.class).getProducts().entrySet().stream();
    }
}
