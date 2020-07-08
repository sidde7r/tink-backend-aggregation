package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import java.util.stream.Stream;

@JsonIgnoreProperties({"type", "Resultat", "Message"})
@JacksonXmlRootElement
public class AccountsResultsEntity {

    @JacksonXmlProperty(localName = "NumeroAbonne")
    private String username;

    @JacksonXmlProperty(localName = "lstComptesInternesTit")
    private List<AccountEntity> accounts;

    @JacksonXmlProperty(localName = "lstComptesInternesAutre")
    private Object otherAccounts;

    public Stream<AccountEntity> stream() {
        return accounts.stream();
    }
}
