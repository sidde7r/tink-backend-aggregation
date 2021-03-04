package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.utils;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity.AccountDetailsResultEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.identitydata.IdentityData;

public class SoapHelperTest {

    private static final String XML_DATA =
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
        IdentityData data = SoapHelper.getIdentityData(XML_DATA);
        assertThat(data.getFullName()).isEqualTo("FIRSTNAME SURNAME");
    }

    private static final String ACCOUNTS_DATA =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "  <soap:Body>\n"
                    + "    <GetSyntheseCpteAbonnementResponse xmlns=\"http://caisse-epargne.fr/webservices/\">\n"
                    + "      <GetSyntheseCpteAbonnementResult>\n"
                    + "        <CodeRetour>0000</CodeRetour>\n"
                    + "        <LibelleRetour>La requête s'est bien déroulée (0000).</LibelleRetour>\n"
                    + "        <Resultat xsi:type=\"SyntheseCompteInterne\">\n"
                    + "          <Resultat>0000</Resultat>\n"
                    + "          <Message/>\n"
                    + "          <NumeroAbonne>999999999</NumeroAbonne>\n"
                    + "          <lstComptesInternesTit>\n"
                    + "            <CompteInterneSynt>\n"
                    + "              <NumeroRib>12312312312312312312312</NumeroRib>\n"
                    + "              <NumeroCompteReduit>12312312312</NumeroCompteReduit>\n"
                    + "              <LibelleTypeProduit>CPT DEPOT PART.</LibelleTypeProduit>\n"
                    + "              <MontantSoldeCompte>5923.00</MontantSoldeCompte>\n"
                    + "              <CodeDevise>EUR</CodeDevise>\n"
                    + "              <IntituleProduit>MLLE SURNAME NAME</IntituleProduit>\n"
                    + "              <LibelleAbregeTypeProduit>C.CHEQUE</LibelleAbregeTypeProduit>\n"
                    + "              <IsClicable>true</IsClicable>\n"
                    + "              <CodeSens>D</CodeSens>\n"
                    + "              <MontantDecouvert>80000</MontantDecouvert>\n"
                    + "              <CodeDeviseDecouvert>EUR</CodeDeviseDecouvert>\n"
                    + "              <CodeSensDecouvert>C</CodeSensDecouvert>\n"
                    + "              <CodeProduit>04</CodeProduit>\n"
                    + "              <CodeCategorieProduit>A</CodeCategorieProduit>\n"
                    + "              <NumeroRibCompteLie/>\n"
                    + "              <IndicateurChequierRice>R</IndicateurChequierRice>\n"
                    + "              <EncoursM>\n"
                    + "                <NumeroRib>12312312312312312312312</NumeroRib>\n"
                    + "                <NumeroCompteReduit>12312312312</NumeroCompteReduit>\n"
                    + "                <DateImputation>2020-08-04T00:00:00</DateImputation>\n"
                    + "                <MontantSoldeEnCours>55091.00</MontantSoldeEnCours>\n"
                    + "                <CodeDevise>EUR</CodeDevise>\n"
                    + "                <CodeSens>D</CodeSens>\n"
                    + "                <Personnalise>false</Personnalise>\n"
                    + "                <SeuilMin>0</SeuilMin>\n"
                    + "                <SeuilMax>0</SeuilMax>\n"
                    + "              </EncoursM>\n"
                    + "              <Personnalise>false</Personnalise>\n"
                    + "              <SeuilMin>0</SeuilMin>\n"
                    + "              <SeuilMax>0</SeuilMax>\n"
                    + "              <NvAutoCpt/>\n"
                    + "            </CompteInterneSynt>\n"
                    + "            <CompteInterneSynt>\n"
                    + "              <NumeroRib>32132132132132132132132</NumeroRib>\n"
                    + "              <NumeroCompteReduit>32132132132</NumeroCompteReduit>\n"
                    + "              <LibelleTypeProduit>LIVRET JEUNE</LibelleTypeProduit>\n"
                    + "              <MontantSoldeCompte>1000</MontantSoldeCompte>\n"
                    + "              <CodeDevise>EUR</CodeDevise>\n"
                    + "              <IntituleProduit>MLLE SURNAME NAME</IntituleProduit>\n"
                    + "              <LibelleAbregeTypeProduit>L. JEUNE</LibelleAbregeTypeProduit>\n"
                    + "              <IsClicable>true</IsClicable>\n"
                    + "              <CodeSens>C</CodeSens>\n"
                    + "              <MontantDecouvert>0</MontantDecouvert>\n"
                    + "              <CodeDeviseDecouvert>EUR</CodeDeviseDecouvert>\n"
                    + "              <CodeSensDecouvert>C</CodeSensDecouvert>\n"
                    + "              <CodeProduit>10</CodeProduit>\n"
                    + "              <CodeCategorieProduit>B</CodeCategorieProduit>\n"
                    + "              <NumeroRibCompteLie/>\n"
                    + "              <IndicateurChequierRice>N</IndicateurChequierRice>\n"
                    + "              <Personnalise>false</Personnalise>\n"
                    + "              <SeuilMin>0</SeuilMin>\n"
                    + "              <SeuilMax>0</SeuilMax>\n"
                    + "              <NvAutoCpt/>\n"
                    + "            </CompteInterneSynt>\n"
                    + "          </lstComptesInternesTit>\n"
                    + "          <lstComptesInternesAutre/>\n"
                    + "        </Resultat>\n"
                    + "      </GetSyntheseCpteAbonnementResult>\n"
                    + "    </GetSyntheseCpteAbonnementResponse>\n"
                    + "  </soap:Body>\n"
                    + "</soap:Envelope>";

    private static final String ACCOUNT_DETAILS_DATA =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "  <soap:Body>\n"
                    + "    <GetRiceResponse xmlns=\"http://caisse-epargne.fr/webservices/\">\n"
                    + "      <GetRiceResult>\n"
                    + "        <CodeRetour>0000</CodeRetour>\n"
                    + "        <LibelleRetour>La requête s'est bien déroulée (0000).</LibelleRetour>\n"
                    + "        <Resultat xsi:type=\"Rice\">\n"
                    + "          <CodeInseePaysAgence/>\n"
                    + "          <CodeInseePaysTitulaire/>\n"
                    + "          <AdresseTitulaire>\n"
                    + "            <string>32 RUE LA QUINTINIE</string>\n"
                    + "            <string/>\n"
                    + "            <string>75015 PARIS</string>\n"
                    + "            <string xsi:nil=\"true\"/>\n"
                    + "            <string xsi:nil=\"true\"/>\n"
                    + "          </AdresseTitulaire>\n"
                    + "          <AdresseAgence>\n"
                    + "            <string>LA DEFENSE</string>\n"
                    + "            <string>14 PLACE DE LA DEFENSE</string>\n"
                    + "            <string>92400 COURBEVOIE</string>\n"
                    + "            <string xsi:nil=\"true\"/>\n"
                    + "            <string xsi:nil=\"true\"/>\n"
                    + "            <string>TEL : 01.71.09.61.14</string>\n"
                    + "          </AdresseAgence>\n"
                    + "          <CleRib>58</CleRib>\n"
                    + "          <CodeBic>CEPAFRPP751</CodeBic>\n"
                    + "          <CodeGuicIntb>90000</CodeGuicIntb>\n"
                    + "          <CodeIban>FR1231231231231231231231231</CodeIban>\n"
                    + "          <IdntEtabGce>17515</IdntEtabGce>\n"
                    + "          <InttlCpte/>\n"
                    + "          <LiblGuicIntb/>\n"
                    + "          <NumrPrdt>04134777368</NumrPrdt>\n"
                    + "          <NumTelAgence>TEL : 01.71.09.61.14</NumTelAgence>\n"
                    + "          <LibelleCaisse>CE CEIDF</LibelleCaisse>\n"
                    + "        </Resultat>\n"
                    + "      </GetRiceResult>\n"
                    + "    </GetRiceResponse>\n"
                    + "  </soap:Body>\n"
                    + "</soap:Envelope>";

    private static final String TRANSACTIONS_DATA =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "  <soap:Body>\n"
                    + "    <GetHistoriqueOperationsByCompteResponse xmlns=\"http://caisse-epargne.fr/webservices/\">\n"
                    + "      <GetHistoriqueOperationsByCompteResult>\n"
                    + "        <CodeRetour>0000</CodeRetour>\n"
                    + "        <LibelleRetour>La requête s'est bien déroulée (0000).</LibelleRetour>\n"
                    + "        <Resultat xsi:type=\"Histo\">\n"
                    + "          <IndicateurNav>S</IndicateurNav>\n"
                    + "          <BufferSuite>050520202020-05-05-09.42.17.585414</BufferSuite>\n"
                    + "          <Nb_Op>50</Nb_Op>\n"
                    + "          <Nb_Op_Tot>130</Nb_Op_Tot>\n"
                    + "          <IndiceExaustif>P</IndiceExaustif>\n"
                    + "          <ListHistoCpt>\n"
                    + "            <HistoCpt>\n"
                    + "              <DateOprt>2020-06-19T00:00:00</DateOprt>\n"
                    + "              <LiblOprt>VERY COOL TRANSACTION</LiblOprt>\n"
                    + "              <MtOprt>133700</MtOprt>\n"
                    + "              <CodeDevise>EUR</CodeDevise>\n"
                    + "              <SensOprt>D</SensOprt>\n"
                    + "              <RefrOprt>1906202020200619-15.01.54.758945</RefrOprt>\n"
                    + "              <CodeTypeEcrit>D</CodeTypeEcrit>\n"
                    + "              <CodeTypeOp>01</CodeTypeOp>\n"
                    + "            </HistoCpt>\n"
                    + "            <HistoCpt>\n"
                    + "              <DateOprt>2020-05-05T00:00:00</DateOprt>\n"
                    + "              <LiblOprt>NOT SO COOL TRANSACTION</LiblOprt>\n"
                    + "              <MtOprt>9900</MtOprt>\n"
                    + "              <CodeDevise>EUR</CodeDevise>\n"
                    + "              <SensOprt>C</SensOprt>\n"
                    + "              <RefrOprt>0505202020200505-18.24.59.555648</RefrOprt>\n"
                    + "              <CodeTypeEcrit>D</CodeTypeEcrit>\n"
                    + "              <CodeTypeOp>02</CodeTypeOp>\n"
                    + "            </HistoCpt>\n"
                    + "          </ListHistoCpt>\n"
                    + "          <MttSoldeCompte>99999</MttSoldeCompte>\n"
                    + "          <SensSoldeCompte>D</SensSoldeCompte>\n"
                    + "        </Resultat>\n"
                    + "      </GetHistoriqueOperationsByCompteResult>\n"
                    + "    </GetHistoriqueOperationsByCompteResponse>\n"
                    + "  </soap:Body>\n"
                    + "</soap:Envelope>\n";

    private static final String NULL_ACCOUNT_BALANCE_ACCOUNTS_DATA =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "  <soap:Body>\n"
                    + "    <GetSyntheseCpteAbonnementResponse xmlns=\"http://caisse-epargne.fr/webservices/\">\n"
                    + "      <GetSyntheseCpteAbonnementResult>\n"
                    + "        <CodeRetour>0000</CodeRetour>\n"
                    + "        <LibelleRetour>La requ?te s'est bien d?roul?e (0000).</LibelleRetour>\n"
                    + "        <Resultat xsi:type=\"SyntheseCompteInterne\">\n"
                    + "          <Resultat>0000</Resultat><Message/>\n"
                    + "          <NumeroAbonne>750825454</NumeroAbonne>\n"
                    + "          <lstComptesInternesTit>\n"
                    + "            <CompteInterneSynt>\n"
                    + "              <NumeroRib>12312312312312312312312</NumeroRib>\n"
                    + "              <NumeroCompteReduit>12312312312</NumeroCompteReduit>\n"
                    + "              <LibelleTypeProduit>NUANCES 3D</LibelleTypeProduit><MontantSoldeCompte xsi:nil=\"true\"/><IntituleProduit/>\n"
                    + "              <LibelleAbregeTypeProduit>NUAN. 3D</LibelleAbregeTypeProduit>\n"
                    + "              <IsClicable>true</IsClicable>\n"
                    + "              <MontantDecouvert>0</MontantDecouvert>\n"
                    + "              <CodeSensDecouvert>C</CodeSensDecouvert>\n"
                    + "              <CodeProduit>AS</CodeProduit>\n"
                    + "              <CodeCategorieProduit>C</CodeCategorieProduit><NumeroRibCompteLie/>\n"
                    + "              <IndicateurChequierRice>N</IndicateurChequierRice>\n"
                    + "              <Personnalise>false</Personnalise>\n"
                    + "              <SeuilMin>0</SeuilMin>\n"
                    + "              <SeuilMax>0</SeuilMax><NvAutoCpt/></CompteInterneSynt>\n"
                    + "            <CompteInterneSynt>\n"
                    + "              <NumeroRib>12312312312312312312312</NumeroRib>\n"
                    + "              <NumeroCompteReduit>12312312312</NumeroCompteReduit>\n"
                    + "              <LibelleTypeProduit>CPT DEPOT PART.</LibelleTypeProduit>\n"
                    + "              <MontantSoldeCompte>64.00</MontantSoldeCompte>\n"
                    + "              <CodeDevise>EUR</CodeDevise>\n"
                    + "              <IntituleProduit>MLLE SURNAME NAME</IntituleProduit>\n"
                    + "              <LibelleAbregeTypeProduit>C.CHEQUE</LibelleAbregeTypeProduit>\n"
                    + "              <IsClicable>true</IsClicable>\n"
                    + "              <CodeSens>C</CodeSens>\n"
                    + "              <MontantDecouvert>20000</MontantDecouvert>\n"
                    + "              <CodeDeviseDecouvert>EUR</CodeDeviseDecouvert>\n"
                    + "              <CodeSensDecouvert>C</CodeSensDecouvert>\n"
                    + "              <CodeProduit>04</CodeProduit>\n"
                    + "              <CodeCategorieProduit>A</CodeCategorieProduit><NumeroRibCompteLie/>\n"
                    + "              <IndicateurChequierRice>R</IndicateurChequierRice>\n"
                    + "              <Personnalise>false</Personnalise>\n"
                    + "              <SeuilMin>0</SeuilMin>\n"
                    + "              <SeuilMax>0</SeuilMax><NvAutoCpt/></CompteInterneSynt>\n"
                    + "          </lstComptesInternesTit><lstComptesInternesAutre/></Resultat>\n"
                    + "      </GetSyntheseCpteAbonnementResult>\n"
                    + "    </GetSyntheseCpteAbonnementResponse>\n"
                    + "  </soap:Body>\n"
                    + "</soap:Envelope>";

    private static final String IBAN = "FR4820041010050014391645720";
    private static final String IBAN_B = "FR6720041010050008697430710";
    private static final String SHORT_ACC_NR = "12312312312";
    private static final String LONG_ACC_NR = "12312312312312312312312";
    private static final String LONG_ACC_NR_B = "32132132132132132132132";
    private static final String FAKE_NAME = "Mlle Surname Name";

    @Test
    public void testMoreAccounts() {
        AccountsResponse resp = SoapHelper.getAccounts(NULL_ACCOUNT_BALANCE_ACCOUNTS_DATA);
        resp.stream()
                .forEach(
                        accountEntity ->
                                accountEntity.setIban(
                                        getIban(accountEntity.getFullAccountNumber())));
        Collection<TransactionalAccount> accounts =
                resp.stream()
                        .map(AccountEntity::toTinkAccount)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        Optional<TransactionalAccount> checking =
                accounts.stream()
                        .filter(account -> account.getType().equals(AccountTypes.CHECKING))
                        .findFirst();
        assertThat(checking.isPresent()).isTrue();
        assertThat(checking.get().getAccountNumber()).isEqualTo(SHORT_ACC_NR);
        assertThat(checking.get().getIdentifiers().get(0).getIdentifier()).isEqualTo(IBAN);
        assertThat(checking.get().getHolderName().toString()).isEqualTo(FAKE_NAME);
        assertThat(
                        checking.get()
                                .getExactBalance()
                                .getExactValue()
                                .compareTo(BigDecimal.valueOf(0.64)))
                .isEqualTo(0);
    }

    @Test
    public void getAccounts() {
        AccountsResponse response = SoapHelper.getAccounts(ACCOUNTS_DATA);
        response.stream()
                .forEach(
                        accountEntity ->
                                accountEntity.setIban(
                                        getIban(accountEntity.getFullAccountNumber())));
        Collection<TransactionalAccount> accounts =
                response.stream()
                        .map(AccountEntity::toTinkAccount)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        assertThat(accounts.size()).isEqualTo(2);
        Optional<TransactionalAccount> checking =
                accounts.stream()
                        .filter(account -> account.getType().equals(AccountTypes.CHECKING))
                        .findFirst();
        assertThat(checking.isPresent()).isTrue();
        assertThat(checking.get().getAccountNumber()).isEqualTo(SHORT_ACC_NR);
        assertThat(checking.get().getIdentifiers().get(0).getIdentifier()).isEqualTo(IBAN);
        assertThat(checking.get().getHolderName().toString()).isEqualTo(FAKE_NAME);
        assertThat(
                        checking.get()
                                .getExactBalance()
                                .getExactValue()
                                .compareTo(BigDecimal.valueOf(-59.23)))
                .isEqualTo(0);
        Optional<TransactionalAccount> savings =
                accounts.stream()
                        .filter(account -> account.getType().equals(AccountTypes.SAVINGS))
                        .findFirst();
        assertThat(savings.isPresent()).isTrue();
        assertThat(savings.get().getHolderName().toString()).isEqualTo(FAKE_NAME);
        assertThat(savings.get().getIdentifiers().get(0).getIdentifier()).isEqualTo(IBAN_B);
        assertThat(
                        savings.get()
                                .getExactBalance()
                                .getExactValue()
                                .compareTo(BigDecimal.valueOf(10)))
                .isEqualTo(0);
    }

    private String getIban(String fullAccountNumber) {
        if (fullAccountNumber.equals(LONG_ACC_NR)) {
            return IBAN;
        } else if (fullAccountNumber.equals(LONG_ACC_NR_B)) {
            return IBAN_B;
        }
        return "";
    }

    @Test
    public void getAccountDetails() {
        AccountDetailsResponse response2 = SoapHelper.getAccountDetails(ACCOUNT_DETAILS_DATA);
        AccountDetailsResultEntity a = response2.getResult();
        assertThat(a.getIban()).isEqualTo("FR1231231231231231231231231");
    }

    @Test
    public void getTransactions() {
        TransactionsResponse response = SoapHelper.getTransactions(TRANSACTIONS_DATA);
        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        assertThat(transactions.size()).isEqualTo(2);
        Optional<? extends Transaction> negativeTransaction =
                transactions.stream()
                        .filter(t -> t.getExactAmount().getExactValue().signum() == -1)
                        .findFirst();
        assertThat(negativeTransaction.isPresent()).isTrue();
        Transaction negTransaction = negativeTransaction.get();
        assertThat(
                        negTransaction
                                .getExactAmount()
                                .getExactValue()
                                .compareTo(BigDecimal.valueOf(-1337)))
                .isEqualTo(0);
        Optional<? extends Transaction> positiveTransaction =
                transactions.stream()
                        .filter(t -> t.getExactAmount().getExactValue().signum() != -1)
                        .findFirst();
        assertThat(positiveTransaction.isPresent()).isTrue();
        Transaction posTransaction = positiveTransaction.get();
        assertThat(
                        posTransaction
                                .getExactAmount()
                                .getExactValue()
                                .compareTo(BigDecimal.valueOf(99)))
                .isEqualTo(0);
    }
}
