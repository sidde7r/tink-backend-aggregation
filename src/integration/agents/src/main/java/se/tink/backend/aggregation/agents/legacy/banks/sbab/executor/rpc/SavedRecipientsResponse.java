package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.banks.sbab.entities.SavedRecipientEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavedRecipientsResponse extends ArrayList<SavedRecipientEntity> {}
