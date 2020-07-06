package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.identitydata.IdentityData;

public class SoapHelperTest {

    private static final String xmlData =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "  <soap:Body>\n"
                    + "    <sso_BAPIResponse xmlns=\"http://caisse-epargne.fr/webservices/\">\n"
                    + "      <sso_BAPIResult>\n"
                    + "        <CodeRetour>0000</CodeRetour>\n"
                    + "        <LibelleRetour>La requÃªte s'est bien dÃ©roulÃ©e (0000).</LibelleRetour>\n"
                    + "        <Resultat xsi:type=\"AbonneSSO\">\n"
                    + "          <DateConnexion>2020-07-03T13:55:48.2411471+02:00</DateConnexion>\n"
                    + "          <TerminalHabituel>\n"
                    + "            <IdentifiantInterne>12341234</IdentifiantInterne>\n"
                    + "            <IdentifiantTerminal>DEADBEEF-DEAD-BEEF-DEAD-BEEFDEADBEEF</IdentifiantTerminal>\n"
                    + "            <IdentifiantClient>756273725</IdentifiantClient>\n"
                    + "            <IdentifiantUsager>000000</IdentifiantUsager>\n"
                    + "            <DateEnregistrement>2020-07-01T10:02:51.637</DateEnregistrement>\n"
                    + "            <ModeEnregistrement>0</ModeEnregistrement>\n"
                    + "            <Device>iPhone</Device>\n"
                    + "            <Status>1</Status>\n"
                    + "            <ModeVerif>0</ModeVerif>\n"
                    + "          </TerminalHabituel>\n"
                    + "          <FlagAnrTdcValide>true</FlagAnrTdcValide>\n"
                    + "          <CodeCaisse>17515</CodeCaisse>\n"
                    + "          <NumSession>12341234</NumSession>\n"
                    + "          <IdSession>aa1aa111aaa11aaa11aa1111</IdSession>\n"
                    + "          <NumeroAbonne>123456789</NumeroAbonne>\n"
                    + "          <NumeroAbonneCrypte>1111aaa1111aa11a11a1a111a11aa1a1</NumeroAbonneCrypte>\n"
                    + "          <IdentifiantPersonne>123456789</IdentifiantPersonne>\n"
                    + "          <IdentifiantPersonneH>1111aaa1111aa11a11a1a111a11aa1a1</IdentifiantPersonneH>\n"
                    + "          <DateDerniereConnexion>2020-07-03T13:38:46</DateDerniereConnexion>\n"
                    + "          <AbonnementValide>\n"
                    + "            <Civilite>MLLE</Civilite>\n"
                    + "            <Nom>SURNAME</Nom>\n"
                    + "            <Prenom>FIRSTNAME</Prenom>\n"
                    + "            <NumeroPersonne>123456789</NumeroPersonne>\n"
                    + "            <ListeServices>\n"
                    + "              <Service>\n"
                    + "                <Code>AUTHR\n"
                    + "                </Code>\n"
                    + "                <Etat>00</Etat>\n"
                    + "              </Service>\n"
                    + "              <Service>\n"
                    + "                <Code>RNUM\n"
                    + "                </Code>\n"
                    + "                <Etat>01</Etat>\n"
                    + "              </Service>\n"
                    + "              <Service>\n"
                    + "                <Code>SOL\n"
                    + "                </Code>\n"
                    + "                <Etat>01</Etat>\n"
                    + "              </Service>\n"
                    + "              <Service>\n"
                    + "                <Code>ENROL\n"
                    + "                </Code>\n"
                    + "                <Etat>10</Etat>\n"
                    + "              </Service>\n"
                    + "            </ListeServices>\n"
                    + "          </AbonnementValide>\n"
                    + "          <AuthentificationValide>\n"
                    + "            <ChangePwd>N</ChangePwd><CauseChangePwd/>\n"
                    + "            <NbEssais>3</NbEssais>\n"
                    + "            <AncienIdBad>000000000000</AncienIdBad><AncienPwd/></AuthentificationValide>\n"
                    + "          <NiveauServiceSelectionne>PAR</NiveauServiceSelectionne>\n"
                    + "          <NumeroUsager>0</NumeroUsager>\n"
                    + "          <IdAgPri>0000172</IdAgPri><ANR/>\n"
                    + "          <DateNaissance>0001-01-01T00:00:00</DateNaissance><NumCetelem xsi:nil=\"true\"/></Resultat>\n"
                    + "      </sso_BAPIResult>\n"
                    + "    </sso_BAPIResponse>\n"
                    + "  </soap:Body>\n"
                    + "</soap:Envelope>";

    @Test
    public void getIdentityData() {
        IdentityData data = SoapHelper.getIdentityData(xmlData);
        assertThat(data.getFullName()).isEqualTo("FIRSTNAME SURNAME");
    }
}
