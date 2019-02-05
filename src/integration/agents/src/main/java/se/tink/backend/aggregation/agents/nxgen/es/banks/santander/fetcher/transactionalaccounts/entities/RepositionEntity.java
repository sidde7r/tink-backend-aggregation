package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class RepositionEntity {
    @JsonProperty("fechaRepos")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateReposition;
    @JsonProperty("tipoMovimiento")
    private String transactionType;
    @JsonProperty("numeroDGO")
    private DgoNumberEntity dgoNumberEntity;
    @JsonProperty("numeroMovimiento")
    private int transactionNumber;
    @JsonProperty("diaMovimiento")
    private int transactionDay;

    public Date getDateReposition() {
        return dateReposition;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public DgoNumberEntity getDgoNumberEntity() {
        return dgoNumberEntity;
    }

    public int getTransactionNumber() {
        return transactionNumber;
    }

    public int getTransactionDay() {
        return transactionDay;
    }
}
