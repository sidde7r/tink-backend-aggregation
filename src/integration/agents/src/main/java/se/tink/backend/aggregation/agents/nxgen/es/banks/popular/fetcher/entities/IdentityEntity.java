package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class IdentityEntity {

    private String nif;

    @JsonProperty("codigoDoc")
    private String docCode;

    @JsonProperty("numeroDoc")
    private String docNumber;

    @JsonProperty("nombre")
    private String firstName;

    @JsonProperty("apellido1")
    private String lastname1;

    @JsonProperty("apellido2")
    private String surname2;

    private int identMod;
    private int identApell2;
    private int identVers;
    private int sector;

    @JsonProperty("colectivo")
    private int collective;

    @JsonProperty("sexo")
    private int sex;

    @JsonProperty("estadoCivil")
    private int civilStatus;

    @JsonProperty("numHijos")
    private int numChildren;

    private int prefTfno;

    @JsonProperty("numTfno")
    private int whetherTfno;

    @JsonProperty("indicadorTfno")
    private int tfnoIndicator;

    @JsonProperty("numIntNac")
    private int intWhetherNac;

    @JsonProperty("codOrigenDat")
    private int origenDat;

    @JsonProperty("tipoPer")
    private int typePer;

    @JsonProperty("nivelConf")
    private int confLevel;

    @JsonProperty("codConf")
    private int conf;

    @JsonProperty("bancoGrupo")
    private int groupBank;

    @JsonProperty("oficGrupo")
    private int officeGroup;

    @JsonProperty("diaNac")
    private int dayBorn;

    @JsonProperty("mesNac")
    private int monthNac;

    @JsonProperty("sinValor1")
    private int withoutValue1;

    @JsonProperty("annoNac")
    private int yearNac;

    @JsonProperty("diaFall")
    private int dayFall;

    @JsonProperty("mesFall")
    private int monthFall;

    @JsonProperty("sinValor2")
    private int withoutValue2;

    @JsonProperty("annoFall")
    private int yearFall;

    @JsonProperty("nivelEst")
    private int eastLevel;

    @JsonProperty("indCNO")
    private int indcno;

    @JsonProperty("codOcup")
    private String codeOcup;

    @JsonProperty("profesion")
    private String profession;

    private int indActEcon;
    private String actEcon;
    private String descripActEcon;

    @JsonProperty("indPlaza")
    private int intoThePlaza;

    @JsonProperty("numIntPlaza")
    private int intWhetherPlaza;

    @JsonProperty("codIdioma")
    private int idiomaCode;

    @JsonProperty("tratam")
    private int theyTreat;

    @JsonProperty("nomPlaza")
    private String namePlaza;

    @JsonProperty("numTerr")
    private int isTheEarth;

    @JsonProperty("codPost")
    private String postCode;

    private String version;

    @JsonProperty("noResidente")
    private int nonResident;

    @JsonProperty("numDocNoR")
    private String documentNumber;

    @JsonProperty("nombPlazaExpNoR")
    private String namePlazaExpNor;

    @JsonProperty("numPlazaExpNoR")
    private int numPlazaExpNor;

    @JsonProperty("pais")
    private String parents;

    private String certif;
    private int consul;

    @JsonProperty("nombConsul")
    private String nombConsul;

    @JsonProperty("inscrip")
    private String registra;

    @JsonProperty("archivo")
    private int archive;

    @JsonProperty("paisNac")
    private String birthParents;

    @JsonProperty("numNacionalid")
    private int inANational;

    @JsonProperty("ocupacion")
    private int occupation;

    @JsonProperty("moneda")
    private String currency;

    @JsonProperty("importeRenta")
    private int incomeAmount;

    private int tfno;
    private int indCambioFisc;

    @JsonProperty("numFisc")
    private int whetherFisch;

    private int activ93;

    @JsonProperty("descripActiv93")
    private String describeActive93;

    @JsonProperty("nacionalidad")
    private String nationality;

    @JsonProperty("proteccionDatos")
    private String protectionData;

    public String getNif() {
        return nif;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastname1() {
        return lastname1;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }
}
