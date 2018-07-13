package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoPopularCustom {
    private int masOperaciones;
    private int noccursdatostab;
    private List<BancoPopularCuenta> customRr001014;


    public int getMasOperaciones() {
        return masOperaciones;
    }

    public void setMasOperaciones(int masOperaciones) {
        this.masOperaciones = masOperaciones;
    }

    public int getNoccursdatostab() {
        return noccursdatostab;
    }

    public void setNoccursdatostab(int noccursdatostab) {
        this.noccursdatostab = noccursdatostab;
    }

    public List<BancoPopularCuenta> getCustomRr001014() {
        return customRr001014;
    }

    public void setCustomRr001014(
            List<BancoPopularCuenta> customRr001014) {
        this.customRr001014 = customRr001014;
    }
}
