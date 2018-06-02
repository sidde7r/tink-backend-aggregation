package se.tink.backend.common.utils.giro;

import org.junit.Assert;
import org.junit.Test;

import se.tink.backend.common.utils.giro.GiroParser;
import se.tink.backend.core.Giro;

import com.google.common.base.Strings;

public class GiroParserTest {
    @Test
    public void test() {
        // BG

        parse("221-1555 KLARNA AB/N");
        parse("312-9897 COM HEM AB");
        parse("319-9973 3 (HI3G ACC");
        parse("319-9973 3 (HI3G ACCESS AB) mobilräkning");
        parse("322-3542 LANGLEY TRA");
        parse("375-4942 EUROSTAR RE");
        parse("356-5322 NEVA AB");
        parse("464-1056 FÖRSÄKRINGS");
        parse("5050-1055 SKATTEVERKET reavinstskatt hornsgatan");
        parse("5069-0486 BABYZ");
        parse("5051-6913 KÖRKORTSAV");
        parse(" 5051-6913 KÖRKORTSAV");
        parse("  5051-6913 KÖRKORTSAV");
        parse("BankGiro  5051-6913 KÖRKORTSAV");
        parse("5270-6009 IF SKADEFÖ");
        parse("5331-9901 GODEL I SV");
        parse("BG 5271-2320, Göteborg Energi, Ref 11300095385440");
        parse("BG 5195-2208, DENSIA AB, Ref Fakt.nr 35120, Knr 30");
        parse("BG 5427-2430, MENCKEL Design , Ref 054");
        parse("BG 5952-4066, FÖRENINGEN SALT, Ref Medlemsavgift");
        parse("BG 498-8077, OK-Q8 BANK AB, Ref 343348595");
        parse("BG 469-0962, Bonnier Tidskri, Ref 4687584352112366");
        parse("BG 170-3347, DAGENS NYHETER , Ref 1001868793933");
        parse("SPARBANKEN ÖRESUND Bg 59100594");
        parse("JUSEK Bg 6519540");
        parse("SKATTEVERKET Bg 50501055");

        // PG
        parse("PG 4151501-6, PBK PAY/BOKUS, Ref 35001077829346");
        parse("Pg 1389815-0 Praktikertjänst");
        parse("PG 932800-6, STHLM STADS PAR, Ref 079736200247158");
        parse("Pg 1497892-8 Antikvariat Lit");
        parse("PG 4872704-4, EBAY SWEDEN AB, Ref 351536495");
        parse("Pg 4894903-6 Discshop Svensk");
        parse("PG 4151501-6, PBK PAY/BOKUS, Ref 35001038848542");
        parse("Pg 875163-8 Nya Äppelvikens");
        parse("Pg 29751-5 P.a.a. Wilson Dan");
        parse("Pg 532165-8 Hemresursen I En");
        parse("PG 908504-4, VÄRLDSNATURFOND, Ref 8400024512232");
        parse("Pg 8392091-8 Magnus Klockare");
        parse("Pg 4984502-7 Fujicolor Sveri");
        parse("Pg 836603-1 Trafikförsäkring");
        parse("PG 4794801-3, BONNIER PUBLICA, Ref 237535762002186");
        parse("Pg 250182-3 W Sundström Fast");
        parse("Pg 9217346-7 Praktikertjänst");
        parse("PG 6134563-3, WEISS,PHILIPH J, Ref 3e Maj 09.00");
        parse("Pg 4869404-6 Alfa Romeo Förs");
        parse("Pg 4247669-7 Benon Helene");
        parse("PG 4158502-7, KLARNA AB /NORD, Ref 144650821100506");
        parse("PG 4791703-4, STH TRAFIKKONTO, Ref 4618000253331");
        parse("PG 820004-0, TELIASONERA SVE, Ref 25206697127");
        parse("Pg 676266-0 Eliasson Alf Åke");
        parse("Pg 964035-0 Roslagsbåtar");
        parse("Pg 970600-3 Hjemmet Mortense");
        parse("Pg 4293228-5 Econova Garden");
        parse("PG 936600-6, TELE2 SVERIGE A, Ref 21550322214");
        parse("PG 4857000-6 Skellefteå Kraf");
        parse("Pg 4134904-4 Arctic Seals");
        parse("PG 4896003-3, SVEA EKONOMI AB, Ref 1561795400");
        parse("Pg 4137604-7 Porträttstudion");
        parse("Pg 1244539-1 Brev & Kort Rol");
        parse("Pg 1651828-4 Kindahls Danssk");
        parse("PG 4857501-3, PBKPAY/ADLIBRIS, Ref 1415214806");
        parse("Pg 901902-7 Stockholms Stads");
        parse("Pg 957841-0 Patent- Och Regi");
        parse("PG 682591-3, VAXHOLMS SCOUTK, Ref Snåriga Skäggen");
        parse("Pg 752835-9 Henriksson Gilbe");
        parse("Pg 1683864-1 Edström Bengt");
        parse("PG 4145304-4, MEDMERA BANK AB, Ref 405091112455400");
        parse("PG 4770002-6, PBK PAY,SV ADRE, Ref 528527190029");
        parse("Pg 325009-9 Ia-produkter Med");
        parse("Pg 4800303-2 Ij Fakturaservi");
        parse("Pg 854001-5 Transportstyrels");
        parse("PG 974102-6, EGMONT KÄRNAN A, Ref 1170226036531");
        parse("Pg 1292561-6 Mattsson Jan Sv");
        parse("Pg 4800703-3 Huddinge Kyrkli");
        parse("PG 973801-4, RINKEBY-KISTA S, Ref 4817100287336");
        parse("PG 4811600-8, STOCKHOLMS STAD, Ref 0934239203");
        parse("Pg 4158502-7, Klarna");
        parse("Pg 1655878-5 Jörgen Schöldin");
        parse("Pg 265720-3 Salminen Jorma");
        parse("Pg 4143402-8 Västerviks Bost");
        parse("Pg 4414581-1 Föreningen Sörm");
        parse("Pg 806801-7 Ikano Bank Se Ik");
        parse("PG 653364-0, SKÖNDALS LAND- , Ref Julavslutning, i");
        parse("Pg 964060-8 Fhager Vilhelm");
        parse("Pg 4938903-4 Karlstads Kommu");
        parse("PG 932800-6, STHLM STADS PAR, Ref 079736199113056");
        parse("Pg 4938104-9 Gothia Financia");
        parse("Pg 152163-2 Föreningen Ekeby");
        parse("Pg 849600-2 Bonnierförlagen");
        parse("Pg 4161004-9 Magasinet Filte");
        parse("Pg 4821004-1 Svenskt Militär");
        parse("PG 4808602-9 Nordea MC Kredi");
        parse("Pg 1340-9 Hsb Stockholm Ek F");
        parse("PG 4163702-6, TRANSCOM CREDIT, Ref 5500102967639");
        parse("PG 4761801-2, NACKA ENERGI AB, Ref 321084097");
        parse("PG 880100-3, FÖRENINGSHUSET , Ref 73940050245");
        parse("PG 27311-0, HERMAN DONN R A, Ref Fakturanr 2170");
        parse("Pg 4912802-8 Järfälla Kommun");
        parse("Pg 4770193-3 Fotograf Lars-å");
        parse("Pg 54437-9 Sankt Johannes-lo");
        parse("PG 936600-6, TELE2 SVERIGE A, Ref 21986763510");
        parse("Pg 4864603-8 Srv Återvinning");
        parse("JUSEK MEDLEM Pg 8954018");
        parse("PERSSON,MILDRED Pg 64678568");
    }

    @Test
    public void testPlusGiro() {
        parse("584292-9");
    }
    
    @Test
    public void testFailures() {
        parseFailure("3257 22 61111");
        parseFailure("TEST 50501055");
        parseFailure("BLAH 8954018");
        parseFailure("750463-4563");
        parseFailure("BLACK HAT USA 084870008 206-443-5491");
        parseFailure("NEW YORK TIMES DIGITAL, 800-698-4637");
        parseFailure("BL 11-4808602-9");
        parseFailure("750463-4563");
    }

    private void parseFailure(String string) {
        GiroParser parser = new GiroParser();

        Assert.assertNull(parser.parse(string));
    }

    private void parse(String description) {
        GiroParser parser = new GiroParser();
        Giro giro = parser.parse(description);

        Assert.assertNotNull(giro);
        Assert.assertNotNull(giro.getAccountNumber());

        System.out.println("Parsed " + Strings.padStart(giro.getAccountNumber(), 12, ' ') + " from " + description);
    }
}
