package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config;

import java.util.Arrays;

public enum ProviderMapping {
    STOREBRAND("9680", AuthenticationType.NETTBANK, "storebrand"),
    CULTURA_BANK("1254", AuthenticationType.NETTBANK, "cultura"),
    EASY_BANK("9791", AuthenticationType.NETTBANK, "easybank"),
    PERSONELLSERVICE_TRONDELAG("0010", AuthenticationType.NETTBANK, "www.personellservice"),
    AASEN("4484", AuthenticationType.PORTAL, "aasen-sparebank"),
    AFJORD("4345", AuthenticationType.PORTAL, "afjord-sparebank"),
    ANDEBU("2500", AuthenticationType.PORTAL, "andebu-sparebank"),
    ARENDAL("2895", AuthenticationType.PORTAL, "sparekassa"),
    ASKIM("1100", AuthenticationType.PORTAL, "asbank"),
    AURLAND("3745", AuthenticationType.PORTAL, "sognbank"),
    AURSKOG("1271", AuthenticationType.PORTAL, "aurskog-sparebank"),
    BANK2("9615", AuthenticationType.PORTAL, "bank2"),
    BERG("1105", AuthenticationType.PORTAL, "berg-sparebank"),
    BIEN("1720", AuthenticationType.PORTAL, "bien"),
    BIRKENES("2880", AuthenticationType.PORTAL, "birkenes-sparebank"),
    BJUGN("4295", AuthenticationType.PORTAL, "bjugn-sparebank"),
    BLAKER("1321", AuthenticationType.PORTAL, "blakersparebank"),
    BUD_FRAENA_OG_HUSTAD("4075", AuthenticationType.PORTAL, "romsdalsbanken"),
    DRANGEDAL("2635", AuthenticationType.PORTAL, "drangedalsparebank"),
    EIDSBERG("1020", AuthenticationType.PORTAL, "esbank"),
    ETNEDAL("2140", AuthenticationType.PORTAL, "etnedalsparebank"),
    EVJE_OG_HORNNES("2901", AuthenticationType.PORTAL, "eh-sparebank"),
    FORNEBUBANKEN("1450", AuthenticationType.PORTAL, "fornebusparebank"),
    GILDESKAL("4609", AuthenticationType.PORTAL, "gildeskaal-sparebank"),
    GJERSTAD("2907", AuthenticationType.PORTAL, "oasparebank"),
    GRONG("4448", AuthenticationType.PORTAL, "grong-sparebank"),
    GRUE("1830", AuthenticationType.PORTAL, "gruesparebank"),
    HALTDALEN("4355", AuthenticationType.PORTAL, "haltdalensparebank"),
    HARSTAD("4730", AuthenticationType.PORTAL, "68nord"),
    HEGRA("4465", AuthenticationType.PORTAL, "hegrasparebank"),
    HEMNE("4312", AuthenticationType.PORTAL, "hemnesparebank"),
    HJARTDAL_OG_GRANSHERAD("2699", AuthenticationType.PORTAL, "hjartdalbanken"),
    HJELMELAND("3353", AuthenticationType.PORTAL, "hjelmeland-sparebank"),
    HONEFOSS("2230", AuthenticationType.PORTAL, "honefossbank"),
    HOLAND_OG_SETSKOG("1280", AuthenticationType.PORTAL, "hsbank"),
    INDRE_SOGN("3730", AuthenticationType.PORTAL, "sognbank"),
    JAEREN("3290", AuthenticationType.PORTAL, "hsbank"),
    JERNBANEPERSONALETS_BANK_OG_FORSIKRING("1440", AuthenticationType.PORTAL, "jbf"),
    KLAEBU("4358", AuthenticationType.PORTAL, "nidaros-sparebank"),
    KVINESDAL("3080", AuthenticationType.PORTAL, "kvinesdalsparebank"),
    LARVIKBANKEN("2510", AuthenticationType.PORTAL, "larvikbanken"),
    LILLESTROMBANKEN("1286", AuthenticationType.PORTAL, "lillestrombanken"),
    LOFOTEN("4589", AuthenticationType.PORTAL, "68nord"),
    MARKER("1050", AuthenticationType.PORTAL, "marker-sparebank"),
    MELHUSBANKEN("4230", AuthenticationType.PORTAL, "melhusbanken"),
    NESSET("4106", AuthenticationType.PORTAL, "romsdalsbanken"),
    ODAL("1870", AuthenticationType.PORTAL, "odal-sparebank"),
    OFOTEN("4605", AuthenticationType.PORTAL, "ofotensparebank"),
    OPPDALSBANKEN("4266", AuthenticationType.PORTAL, "oppdalsbanken"),
    ORKLA("4270", AuthenticationType.PORTAL, "orklasparebank"),
    ORLAND("4290", AuthenticationType.PORTAL, "orland-sparebank"),
    ORSKOG("4060", AuthenticationType.PORTAL, "orskogsparebank"),
    RINDAL("4111", AuthenticationType.PORTAL, "rindalsbanken"),
    ROMSDALSBANKEN("4075", AuthenticationType.PORTAL, "romsdalsbanken"),
    ROROSBANKEN("4280", AuthenticationType.PORTAL, "rorosbanken"),
    SANDNES("3260", AuthenticationType.PORTAL, "sandnes-sparebank"),
    SELBU("4285", AuthenticationType.PORTAL, "selbusparebank"),
    SKAGERRAK("2601", AuthenticationType.PORTAL, "skagerraksparebank"),
    SKUE("2351", AuthenticationType.PORTAL, "skuesparebank"),
    SOKNEDAL("4333", AuthenticationType.PORTAL, "soknedal-sparebank"),
    SPAREBANKEN("2630", AuthenticationType.PORTAL, "sparebankendin"),
    SPAREBANKEN_NARVIK("4520", AuthenticationType.PORTAL, "sn"),
    STADSBYGD("4336", AuthenticationType.PORTAL, "stbank"),
    STROMMEN("1310", AuthenticationType.PORTAL, "strommensparebank"),
    SUNNDAL("4035", AuthenticationType.PORTAL, "sunndal-sparebank"),
    SURNADAL("4040", AuthenticationType.PORTAL, "bank"),
    TINN("2620", AuthenticationType.PORTAL, "tinnbank"),
    TOLGA_OS("1885", AuthenticationType.PORTAL, "tos"),
    TOTENS("2050", AuthenticationType.PORTAL, "totenbanken"),
    TROGSTAD("1140", AuthenticationType.PORTAL, "tsbank"),
    TYSNES("3525", AuthenticationType.PORTAL, "tysnes-sparebank"),
    VALDRESSPAREBANK("2153", AuthenticationType.PORTAL, "valdressparebank"),
    VALLE("2890", AuthenticationType.PORTAL, "valle-sparebank"),
    VANG("2146", AuthenticationType.PORTAL, "valdressparebank"),
    VEKSELBANKEN("9581", AuthenticationType.PORTAL, "vekselbanken"),
    VESTRE_SLIDRE("2153", AuthenticationType.PORTAL, "valdressparebank"),
    VIK("3800", AuthenticationType.PORTAL, "sognbank"),
    EIKA("1821", AuthenticationType.EIKA, "eika"),
    NORD68("4730", AuthenticationType.PORTAL, "68nord");

    private String bankCode;
    private AuthenticationType authenticationType;
    private String domain;

    ProviderMapping(String bankCode, AuthenticationType authenticationType, String domain) {
        this.bankCode = bankCode;
        this.authenticationType = authenticationType;
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public String getBankCode() {
        return bankCode;
    }

    public static ProviderMapping getProviderMappingTypeByBankCode(String bankCodeToCheck) {
        return Arrays.stream(values())
                .filter(e -> e.bankCode.equals(bankCodeToCheck))
                .findAny()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "Not found any provider enum with bank code %s",
                                                bankCodeToCheck)));
    }
}
