package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.Account;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.CestaLista;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.CestaNegocio;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.ConceptoPcas;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.KreditLine;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.Saldo;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities.SubtipoProducto;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Accounts {

  @JsonProperty("tipoIntervencion")
  private String tipoIntervencion;

  @JsonProperty("subtipoProducto")
  private SubtipoProducto subtipoProducto;

  @JsonProperty("indOper")
  private Object indOper;

  @JsonProperty("aliasCta")
  private String aliasCta;

  @JsonProperty("indOpera")
  private String indOpera;

  @JsonProperty("formInter")
  private String formInter;

  @JsonProperty("fecBajaCto")
  private String fecBajaCto;

  @JsonProperty("monedaCta")
  private Object monedaCta;

  @JsonProperty("kreditLine")
  private KreditLine kreditLine;

  @JsonProperty("indPreferenca")
  private String indPreferenca;

  @JsonProperty("cestaLista")
  private CestaLista cestaLista;

  @JsonProperty("fecAltaCto")
  private String fecAltaCto;

  @JsonProperty("panTarjeta")
  private String panTarjeta;

  @JsonProperty("conceptoPcas")
  private ConceptoPcas conceptoPcas;

  @JsonProperty("saldo")
  private Saldo saldo;

  @JsonProperty("estadoCto")
  private String estadoCto;

  @JsonProperty("calidadParticipacion")
  private String calidadParticipacion;

  @JsonProperty("cestaNegocio")
  private CestaNegocio cestaNegocio;

  @JsonProperty("ordenRealcion")
  private int ordenRealcion;

  @JsonProperty("ordenPres")
  private int ordenPres;

  @JsonProperty("caducidadTarjeta")
  private int caducidadTarjeta;

  @JsonProperty("ordenPrioridad")
  private int ordenPrioridad;

  @JsonProperty("indCtasTerceros")
  private String indCtasTerceros;

  @JsonProperty("ordenInter")
  private int ordenInter;

  @JsonProperty("account")
  private Account account;

  public String getTipoIntervencion() {
    return tipoIntervencion;
  }

  public SubtipoProducto getSubtipoProducto() {
    return subtipoProducto;
  }

  public Object getIndOper() {
    return indOper;
  }

  public String getAliasCta() {
    return aliasCta;
  }

  public String getIndOpera() {
    return indOpera;
  }

  public String getFormInter() {
    return formInter;
  }

  public String getFecBajaCto() {
    return fecBajaCto;
  }

  public Object getMonedaCta() {
    return monedaCta;
  }

  public KreditLine getKreditLine() {
    return kreditLine;
  }

  public String getIndPreferenca() {
    return indPreferenca;
  }

  public CestaLista getCestaLista() {
    return cestaLista;
  }

  public String getFecAltaCto() {
    return fecAltaCto;
  }

  public String getPanTarjeta() {
    return panTarjeta;
  }

  public ConceptoPcas getConceptoPcas() {
    return conceptoPcas;
  }

  public Saldo getSaldo() {
    return saldo;
  }

  public String getEstadoCto() {
    return estadoCto;
  }

  public String getCalidadParticipacion() {
    return calidadParticipacion;
  }

  public CestaNegocio getCestaNegocio() {
    return cestaNegocio;
  }

  public int getOrdenRealcion() {
    return ordenRealcion;
  }

  public int getOrdenPres() {
    return ordenPres;
  }

  public int getCaducidadTarjeta() {
    return caducidadTarjeta;
  }

  public int getOrdenPrioridad() {
    return ordenPrioridad;
  }

  public String getIndCtasTerceros() {
    return indCtasTerceros;
  }

  public int getOrdenInter() {
    return ordenInter;
  }

  public Account getAccount() {
    return account;
  }
}
