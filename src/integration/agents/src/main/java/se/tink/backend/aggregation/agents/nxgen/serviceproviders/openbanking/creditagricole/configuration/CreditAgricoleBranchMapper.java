package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration;

import java.util.Arrays;

public class CreditAgricoleBranchMapper {

    private static final String BASE_URL_FORMAT = "https://psd2-api.%s.fr";
    private static final String AUTHORIZE_URL_FORMAT =
            "https://psd2-portal.credit-agricole.fr/%s/authorize";

    enum BranchValue {
        ALPES_PROVENCE("fr-creditagricolealpesprovence-ob", "ca-alpesprovence"),
        ALSACE_VOSGES("fr-creditagricolealsacevosges-ob", "ca-alsace-vosges"),
        ANJOU_MAINE("fr-creditagricolelanjouetdumaine-ob", "ca-anjou-maine"),
        ATLANTIQUE_VENDEE("fr-creditagricoleatlantiquevendee-ob", "ca-atlantique-vendee"),
        AQUITAINE("fr-creditagricoledaquitaine-ob", "ca-aquitaine"),
        BRIE_PICARDIE("fr-creditagricolebriepicardie-ob", "ca-briepicardie"),
        CENTRE_EST("fr-creditagricolecentreest-ob", "ca-centrest"),
        CENTRE_FRANCE("fr-creditagricolecentrefrance-ob", "ca-centrefrance"),
        CENTRE_LOIRE("fr-creditagricolecentreloire-ob", "ca-centreloire"),
        CENTRE_OUEST("fr-creditagricolecentreouest-ob", "ca-centreouest"),
        CHAMOAGNE_BOURGOGNE("fr-creditagricolechamoagnebourgogne-ob", "ca-cb"),
        CHARENTE_MERIYIME_DEUXSEVRES("fr-creditagricolecharentemeriyimedeuxsevres-ob", "ca-cmds"),
        CHARENTE_PERIGORD("fr-creditagricolecharenteperigord-ob", "ca-charente-perigord"),
        CORSE("fr-creditagricolelacorse-ob", "ca-corse"),
        COTES_DARMOR("fr-creditagricolecotesdarmor-ob", "ca-cotesdarmor"),
        DES_SAVOIE("fr-creditagricolesavoie-ob", "ca-des-savoie"),
        ILLE_ET_VILAINE("fr-creditagricoledilleetvilaine-ob", "ca-illeetvilaine"),
        FINISTERE("fr-creditagricolefinistere-ob", "ca-finistere"),
        FRANCHE_COMTE("fr-creditagricolefranchecomte-ob", "ca-franchecomte"),
        GUADELOUPE("fr-creditagricoleguadeloupe-ob", "ca-guadeloupe"),
        LANGUEDOC("fr-creditagricolelanguedoc-ob", "ca-languedoc"),
        LA_MARTINIQUE_ET_DE_LAGUYANE(
                "fr-creditagricolelamartiniqueetdelaguyane-ob", "ca-martinique"),
        REUNION("fr-creditagricolelareunion-ob", "ca-reunion"),
        TOURAINE_POITOU("fr-creditagricolelatourauneetdupoitou-ob", "ca-tourainepoitou"),
        LOIRE_HAUTE_LOIRE("fr-creditagricoleloirehauteloire-ob", "ca-loirehauteloire"),
        LORRAINE("fr-creditagricolelorraine-ob", "ca-lorraine"),
        MORBIHAN("fr-creditagricolemorbihan-ob", "ca-morbihan"),
        NORD_DE_FRANCE("fr-creditagricolenorddefrance-ob", "ca-norddefrance"),
        NORD_EST("fr-creditagricolenordest-ob", "ca-nord-est"),
        NORD_MIDI_PYRENEES("fr-creditagricolenordmidipyrenees-ob", "ca-nmp"),
        NORMANDIE("fr-creditagricolenormandie-ob", "ca-normandie"),
        NORMANDIE_SEINE("fr-creditagricolenormandieseine-ob", "ca-normandie-seine"),
        PARIS("fr-creditagricoleparisetdiledefrance-ob", "ca-paris"),
        PROVENCE_COTES_DAZUR("fr-creditagricoleprovencecotesdazur-ob", "ca-pca"),
        PYRENEES_GASCOGNE("fr-creditagricolepyreneesgascogne-ob", "ca-pyrenees-gascogne"),
        SUD_MEDITERRANEE("fr-creditagricolesudmediterranee-ob", "ca-sudmed"),
        SUD_RHONE_ALPES("fr-creditagricolesudrhonealpes-ob", "ca-sudrhonealpes"),
        TOULOUSE("fr-creditagricoletoulouse31-ob", "ca-toulouse31"),
        VAL_DE_FRANCE("fr-creditagricolevaldefrance-ob", "ca-valdefrance");

        private final String providerName;
        private final String urlBranchValue;

        BranchValue(String providerName, String urlBranchValue) {
            this.providerName = providerName;
            this.urlBranchValue = urlBranchValue;
        }

        public String getProviderName() {
            return providerName;
        }

        public String getUrlBranchValue() {
            return urlBranchValue;
        }
    }

    enum SpecificBranchConfiguration {
        BANQUE_CHALUS(
                "fr-creditagricolebanquechalus-ob",
                new CreditAgricoleBranchConfiguration(
                        "https://psd2-api.banque-chalus.fr",
                        "https://psd2-portal.banque-chalus.fr/banque-chalus/authorize"));

        private final String providerName;
        private final CreditAgricoleBranchConfiguration configuration;

        SpecificBranchConfiguration(
                String providerName, CreditAgricoleBranchConfiguration configuration) {
            this.providerName = providerName;
            this.configuration = configuration;
        }

        private String getProviderName() {
            return providerName;
        }

        private CreditAgricoleBranchConfiguration getConfiguration() {
            return configuration;
        }
    }

    public CreditAgricoleBranchConfiguration determineBranchConfiguration(String providerName) {
        return Arrays.stream(BranchValue.values())
                .filter(e -> e.getProviderName().equals(providerName))
                .findAny()
                .map(BranchValue::getUrlBranchValue)
                .map(
                        value ->
                                new CreditAgricoleBranchConfiguration(
                                        String.format(BASE_URL_FORMAT, value),
                                        String.format(AUTHORIZE_URL_FORMAT, value)))
                .orElseGet(() -> getSpecificConfiguration(providerName));
    }

    private CreditAgricoleBranchConfiguration getSpecificConfiguration(String providerName) {
        return Arrays.stream(SpecificBranchConfiguration.values())
                .filter(e -> e.getProviderName().equals(providerName))
                .findAny()
                .map(SpecificBranchConfiguration::getConfiguration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                "Could now find CreditAgricole branch configuration for name: %s",
                                                providerName)));
    }
}
