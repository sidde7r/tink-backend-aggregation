package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PCBW2582 {
    @JsonProperty("ROW_ID")
    public String ROW_ID;

    @JsonProperty("KONTRAKTNR")
    public String KONTRAKTNR;

    @JsonProperty("KTOSLAG_TXT")
    public String KTOSLAG_TXT;

    @JsonProperty("SKULD")
    public String SKULD;

    @JsonProperty("RTE_SATS")
    public String RTE_SATS;

    @JsonProperty("DATRTEJUST")
    public String DATRTEJUST;

    @JsonProperty("LANTAGARE1")
    private String applicant1;

    @JsonProperty("LANTAGARE2")
    private String applicant2;

    public Account toAccount() {
        Account account = new Account();

        account.setBankId(KONTRAKTNR);
        account.setAccountNumber(KONTRAKTNR);
        account.setName(KTOSLAG_TXT);
        account.setBalance(-StringUtils.parseAmount(SKULD));
        account.setType(AccountTypes.LOAN);
        account.setCapabilities(SEBAgentUtils.getLoanAccountCapabilities());
        account.setSourceInfo(AccountSourceInfo.builder().bankProductName(KTOSLAG_TXT).build());

        // Due to this agent being legacy we have to work with the rpc Account model directly. Using
        // the same logic as we do in core Account model when we map to the rpc Account.
        AccountHolder accountHolder = SEBAgentUtils.getTinkAccountHolder(applicant1, applicant2);
        account.setAccountHolder(accountHolder);
        account.setHolderName(
                SEBAgentUtils.getFirstHolder(accountHolder.getIdentities()).orElse(null));

        return account;
    }

    public Loan toLoan() {
        Loan loan = new Loan();

        loan.setName(KTOSLAG_TXT);
        loan.setBalance(-StringUtils.parseAmount(SKULD));
        loan.setInterest(AgentParsingUtils.parsePercentageFormInterest(RTE_SATS));
        loan.setType(Loan.Type.BLANCO);
        try {
            loan.setNextDayOfTermsChange(ThreadSafeDateFormat.FORMATTER_DAILY.parse(DATRTEJUST));
        } catch (Exception e) {
            // Do nothing
        }

        return loan;
    }
}
