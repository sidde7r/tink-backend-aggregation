package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcLoanAccount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListLoanAccountsResponse extends ArrayList<SdcLoanAccount> {}
