package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankmappers;

import java.util.Arrays;

public enum ProviderMapping {
    CULTURA_BANK("1254", AuthenticationType.NETTBANK),
    EASY_BANK("9791", AuthenticationType.NETTBANK),
    PERSONELLSERVICE_TRONDELAG("0010", AuthenticationType.NETTBANK),
    AASEN("4484", AuthenticationType.PORTAL),
    AFJORD("4345", AuthenticationType.PORTAL),
    ANDEBU("2500", AuthenticationType.PORTAL),
    ARENDAL("2895", AuthenticationType.PORTAL),
    ASKIM("1100", AuthenticationType.PORTAL),
    AURLAND("3745", AuthenticationType.PORTAL),
    AURSKOG("1271", AuthenticationType.PORTAL),
    BANK2("9615", AuthenticationType.PORTAL),
    BERG("1105", AuthenticationType.PORTAL),
    BIEN("1720", AuthenticationType.PORTAL),
    BIRKENES("2880", AuthenticationType.PORTAL),
    BJUGN("4295", AuthenticationType.PORTAL),
    BLAKER("1321", AuthenticationType.PORTAL),
    BUD_FRÆNA_OG_HUSTAD("4075", AuthenticationType.PORTAL),
    DRANGEDAL("2635", AuthenticationType.PORTAL),
    EIDSBERG("1020", AuthenticationType.PORTAL),
    ETNEDAL("2140", AuthenticationType.PORTAL),
    EVJE_OG_HORNNES("2901", AuthenticationType.PORTAL),
    FORNEBUBANKEN("1450", AuthenticationType.PORTAL),
    GILDESKÅL("4609", AuthenticationType.PORTAL),
    GJERSTAD("2907", AuthenticationType.PORTAL),
    GRONG("4448", AuthenticationType.PORTAL),
    GRUE("1830", AuthenticationType.PORTAL),
    HALTDALEN("4355", AuthenticationType.PORTAL),
    HARSTAD("4730", AuthenticationType.PORTAL),
    HEGRA("4465", AuthenticationType.PORTAL),
    HEMNE("4312", AuthenticationType.PORTAL),
    HJARTDAL_OG_GRANSHERAD("2699", AuthenticationType.PORTAL),
    HJELMELAND("3353", AuthenticationType.PORTAL),
    HONEFOSS("2230", AuthenticationType.PORTAL),
    HOLAND_OG_SETSKOG("1280", AuthenticationType.PORTAL),
    INDRE_SOGN("3730", AuthenticationType.PORTAL),
    JAEREN("3290", AuthenticationType.PORTAL),
    JERNBANEPERSONALETS_BANK_OG_FORSIKRING("1440", AuthenticationType.PORTAL),
    KLAEBU("4358", AuthenticationType.PORTAL),
    KVINESDAL("3080", AuthenticationType.PORTAL),
    LARVIKBANKEN("2510", AuthenticationType.PORTAL),
    LILLESTROMBANKEN("1286", AuthenticationType.PORTAL),
    LOFOTEN("4589", AuthenticationType.PORTAL),
    MARKER("1050", AuthenticationType.PORTAL),
    MELHUSBANKEN("4230", AuthenticationType.PORTAL),
    NESSET("4106", AuthenticationType.PORTAL),
    ODAL("1870", AuthenticationType.PORTAL),
    OFOTEN("4605", AuthenticationType.PORTAL),
    OPPDALSBANKEN("4266", AuthenticationType.PORTAL),
    ORKLA("4270", AuthenticationType.PORTAL),
    ORLAND("4290", AuthenticationType.PORTAL),
    ORSKOG("4060", AuthenticationType.PORTAL),
    RINDAL("4111", AuthenticationType.PORTAL),
    ROROSBANKEN("4280", AuthenticationType.PORTAL),
    SANDNES("3260", AuthenticationType.PORTAL),
    SELBU("4285", AuthenticationType.PORTAL),
    SKAGERRAK("2601", AuthenticationType.PORTAL),
    SKUE("2351", AuthenticationType.PORTAL),
    SOKNEDAL("4333", AuthenticationType.PORTAL),
    SPAREBANKEN("2630", AuthenticationType.PORTAL),
    SPAREBANKEN_NARVIK("4520", AuthenticationType.PORTAL),
    STADSBYGD("4336", AuthenticationType.PORTAL),
    STROMMEN("1310", AuthenticationType.PORTAL),
    SUNNDAL("4035", AuthenticationType.PORTAL),
    SURNADAL("4040", AuthenticationType.PORTAL),
    TINN("2620", AuthenticationType.PORTAL),
    TOLGA_OS("1885", AuthenticationType.PORTAL),
    TOTENS("2050", AuthenticationType.PORTAL),
    TROGSTAD("1140", AuthenticationType.PORTAL),
    TYSNES("3525", AuthenticationType.PORTAL),
    VALLE("2890", AuthenticationType.PORTAL),
    VANG("2153", AuthenticationType.PORTAL),
    VEKSELBANKEN("9581", AuthenticationType.PORTAL),
    VESTRE_SLIDRE("2153", AuthenticationType.PORTAL),
    VIK("3800", AuthenticationType.PORTAL);

    private String bankCode;
    private AuthenticationType authenticationType;

    ProviderMapping(String bankCode, AuthenticationType authenticationType) {
        this.bankCode = bankCode;
        this.authenticationType = authenticationType;
    }

    public String getBankCode() {
        return bankCode;
    }

    public static AuthenticationType getAuthenticationTypeByBankCode(String bankCodeToCheck) {
        return Arrays.stream(values())
                .filter(e -> e.bankCode.equals(bankCodeToCheck))
                .findAny()
                .map(e -> e.authenticationType)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "Not found any provider enum with bank code %s",
                                                bankCodeToCheck)));
    }
}
