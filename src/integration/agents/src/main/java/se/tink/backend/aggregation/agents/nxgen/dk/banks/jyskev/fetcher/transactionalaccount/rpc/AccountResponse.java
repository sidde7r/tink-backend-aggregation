package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountResponse extends ArrayList<AccountsEntity> {}
