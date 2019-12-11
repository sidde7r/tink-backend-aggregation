package se.tink.sa.agent.pt.ob.sibs.mapper;

import java.util.Arrays;
import se.tink.sa.framework.common.exceptions.StandaloneAgentException;

public enum SibsApspCodeMappinngs {
    ACTIVOBANK("pt-standaloneactivobank-oauth2", "ABPT"),
    ATLANTICOEUROPA("pt-standaloneatlanticoeuropa-oauth2", "ATLEU"),
    BANCOBPI("pt-standalonebancobpi-oauth2", "BBPI"),
    BANCOCTT("pt-standalonebancoctt-oauth2", "BCTT"),
    BANCOMONTEPIO("pt-standalonebancomontepio-oauth2", "CEMG"),
    BANKINTER("pt-standalonebankinter-oauth2", "BNKI"),
    BIG("pt-standalonebig-oauth2", "BIG"),
    BPG("pt-standalonebpg-oauth2", "BPG"),
    CAIXA("pt-standalonecaixa-ob", "CGDPT"),
    CAIXACRL("pt-standalonecaixacrl-oauth2", "CCAML"),
    CEMAH("pt-standalonecemah-oauth2", "CEMAH"),
    COFIDIS("pt-standalonecofidis-oauth2", "COF"),
    CREDITOAGRICOLA("pt-standalonecreditoagricola-oauth2", "GCA"),
    EUROBIC("pt-standaloneeurobic-oauth2", "BIC"),
    MILLENIUM("pt-standalonemillenniumbcp-oauth2", "BCPPT"),
    NOVOBANCO("pt-standalonenovobanco-oauth2", "NVB"),
    NOVOBANCOACORES("pt-standalonenovobancoacores-oauth2", "NVBA"),
    SANTANDER("pt-standalonesantander-oauth2", "BST"),
    UNICRE("pt-standaloneunicre-oauth2", "UNICR");

    private String providerName;
    private String aspsCode;

    SibsApspCodeMappinngs(String providerName, String aspsCode) {
        this.providerName = providerName;
        this.aspsCode = aspsCode;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getAspsCode() {
        return aspsCode;
    }

    public static SibsApspCodeMappinngs findByProviderName(String providerName) {
        return Arrays.asList(values()).stream()
                .filter(e -> e.getProviderName().equals(providerName))
                .findFirst()
                .orElseThrow(
                        () ->
                                new StandaloneAgentException(
                                        "Unable to find provider for given name '"
                                                + providerName
                                                + "'."));
    }

    public static String findCodeByProviderName(String providerName) {
        return findByProviderName(providerName).getAspsCode();
    }
}
