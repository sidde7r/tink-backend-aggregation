package se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.ActivatedMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivatedMethodsRequest extends ArrayList<ActivatedMethodEntity> {}
