package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement
@Getter
public class AccountDetailsResultEntity {
    @JacksonXmlProperty(localName = "CodeIban")
    private String iban;
}
