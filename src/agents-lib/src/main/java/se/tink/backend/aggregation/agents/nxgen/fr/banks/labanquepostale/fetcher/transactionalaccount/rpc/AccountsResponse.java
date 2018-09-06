package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.AccountsGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.CristalLoansEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.PretsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.SecuritiesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    @JsonProperty("dateSysteme")
    private String dateSystem;
    @JsonProperty("heureSysteme")
    private String systemTime;
    @JsonProperty("intituleAbonneBAD")
    private String titledAbonnebad;
    @JsonProperty("codeCRSFProductionContrat")
    private String codecrsfProductionContract;
    @JsonProperty("demandeMajCollecteEmail")
    private boolean requestShiftEmailCollection;
    /**
     * Regular/checking accounts
     */
    private AccountsGroupEntity ccp;
    /**
     * Savings accounts
     */
    private AccountsGroupEntity cne;
    private Object assurances;
    private PretsEntity prets;
    @JsonProperty("pretsCristal")
    private CristalLoansEntity cristalLoans;
    @JsonProperty("titres")
    private SecuritiesEntity securities;

    public List<AccountEntity> getAccounts() {
        List<AccountEntity> retVal = new ArrayList<>();
        if (ccp != null) {
            retVal.addAll(ccp.getAccounts());
        }
        if (cne != null) {
            retVal.addAll(cne.getAccounts());
        }
        return retVal;
    }

}
