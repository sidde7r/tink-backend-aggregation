package se.tink.backend.core;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "providers_tries")
public class ProviderTries {
    private Integer onetryfailure;
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String providername;
    private Integer threetriesfailure;
    private Integer tries;
    private Integer twotriesfailure;

    public Integer getOnetryfailure() {
        return onetryfailure;
    }

    public String getProvidername() {
        return providername;
    }

    public Integer getThreetriesfailure() {
        return threetriesfailure;
    }

    public Integer getTries() {
        return tries;
    }

    public Integer getTwotriesfailure() {
        return twotriesfailure;
    }

    public void setOnetryfailure(Integer onetryfailure) {
        this.onetryfailure = onetryfailure;
    }

    public void setProvidername(String providername) {
        this.providername = providername;
    }

    public void setThreetriesfailure(Integer threetriesfailure) {
        this.threetriesfailure = threetriesfailure;
    }

    public void setTries(Integer tries) {
        this.tries = tries;
    }

    public void setTwotriesfailure(Integer twotriesfailure) {
        this.twotriesfailure = twotriesfailure;
    }
}
