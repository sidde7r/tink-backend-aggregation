package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementListEntity {
    private String triDateOperation;
    private String triDateValeur;
    private String triCodeOperation;
    private String triMontantCrediteur;
    private String triMontantDebiteur;
    private String access;
    private String action;
    private String objet;
    private String identLib;
    private String identLo;
    private int identMa;
    private int identMi;
    private String lLonguer;
    private String maxAuto;
    private String lCourante;
    private String nbLPages;
    private String lRestantes;
    private String lTotale;
    private String pTotales;
    private String nom;
    private String module;
    private String courante;
    private String max;
    private String pointeur;
    private String type;
    private String dateOperationDebut;
    private String dateTraitementDebut;
    private String numMvtCompteDebut;
    private String libelleMvtDebut;
    private String codSensMvtDebut;
    private String mntMvtValeurAbsolueDebut;
    private String dateOperationFin;
    private String dateTraitementFin;
    private String numMvtCompteFin;
    private String libelleMvtFin;
    private String codSensMvtFin;
    private String mntMvtValeurAbsolueFin;
    private String topCompteEpargne;
    private String codeFormatProduit;
    private String exploiteStockDB2;
    private String mode;
    private int nbLignesVoulu;
    private boolean premierePageVoulue;

    public String getTriDateOperation() {
        return triDateOperation;
    }

    public String getTriDateValeur() {
        return triDateValeur;
    }

    public String getTriCodeOperation() {
        return triCodeOperation;
    }

    public String getTriMontantCrediteur() {
        return triMontantCrediteur;
    }

    public String getTriMontantDebiteur() {
        return triMontantDebiteur;
    }

    public String getAccess() {
        return access;
    }

    public String getAction() {
        return action;
    }

    public String getObjet() {
        return objet;
    }

    public String getIdentLib() {
        return identLib;
    }

    public String getIdentLo() {
        return identLo;
    }

    public int getIdentMa() {
        return identMa;
    }

    public int getIdentMi() {
        return identMi;
    }

    public String getlLonguer() {
        return lLonguer;
    }

    public String getMaxAuto() {
        return maxAuto;
    }

    public String getlCourante() {
        return lCourante;
    }

    public String getNbLPages() {
        return nbLPages;
    }

    public String getlRestantes() {
        return lRestantes;
    }

    public String getlTotale() {
        return lTotale;
    }

    public String getpTotales() {
        return pTotales;
    }

    public String getNom() {
        return nom;
    }

    public String getModule() {
        return module;
    }

    public String getCourante() {
        return courante;
    }

    public String getMax() {
        return max;
    }

    public String getPointeur() {
        return pointeur;
    }

    public String getType() {
        return type;
    }

    public String getDateOperationDebut() {
        return dateOperationDebut;
    }

    public String getDateTraitementDebut() {
        return dateTraitementDebut;
    }

    public String getNumMvtCompteDebut() {
        return numMvtCompteDebut;
    }

    public String getLibelleMvtDebut() {
        return libelleMvtDebut;
    }

    public String getCodSensMvtDebut() {
        return codSensMvtDebut;
    }

    public String getMntMvtValeurAbsolueDebut() {
        return mntMvtValeurAbsolueDebut;
    }

    public String getDateOperationFin() {
        return dateOperationFin;
    }

    public String getDateTraitementFin() {
        return dateTraitementFin;
    }

    public String getNumMvtCompteFin() {
        return numMvtCompteFin;
    }

    public String getLibelleMvtFin() {
        return libelleMvtFin;
    }

    public String getCodSensMvtFin() {
        return codSensMvtFin;
    }

    public String getMntMvtValeurAbsolueFin() {
        return mntMvtValeurAbsolueFin;
    }

    public String getTopCompteEpargne() {
        return topCompteEpargne;
    }

    public String getCodeFormatProduit() {
        return codeFormatProduit;
    }

    public String getExploiteStockDB2() {
        return exploiteStockDB2;
    }

    public String getMode() {
        return mode;
    }

    public int getNbLignesVoulu() {
        return nbLignesVoulu;
    }

    public boolean isPremierePageVoulue() {
        return premierePageVoulue;
    }
}
