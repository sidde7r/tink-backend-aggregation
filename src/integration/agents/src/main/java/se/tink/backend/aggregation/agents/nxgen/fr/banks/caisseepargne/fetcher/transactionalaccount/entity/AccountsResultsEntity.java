package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JacksonXmlRootElement
public class AccountsResultsEntity {

    @JacksonXmlProperty(localName = "NumeroAbonne")
    private String username;

    @JacksonXmlElementWrapper(localName = "lstComptesInternesTit")
    private List<AccountEntity> accounts;

    @JacksonXmlProperty(localName = "lstComptesInternesAutre")
    private Object otherAccounts;

    public Stream<AccountEntity> stream() {
        return accounts.stream();
    }
}
