package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.entities.PositionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PositionsListEntity extends ArrayList<PositionEntity> {
}
