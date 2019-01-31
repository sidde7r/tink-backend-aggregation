package se.tink.backend.aggregation.agents.brokers.nordnet.model.Response;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AccountInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoResponse extends ArrayList<AccountInfoEntity> {

}
