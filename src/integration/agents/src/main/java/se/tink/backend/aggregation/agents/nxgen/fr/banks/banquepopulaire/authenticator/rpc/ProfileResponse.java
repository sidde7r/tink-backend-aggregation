package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.InfosDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.IdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileResponse {
    @JsonProperty("idAbonne")
    private IdentifierEntity idSubscriber;

    private String codeEtablissement;
    private IdentifierEntity idClient;
    private TypeEntity natureClient;

    @JsonProperty("identitePersonnePhysique")
    private UserEntity userData;

    @JsonProperty("dateDerniereConnexion")
    private String dateLastLogin;

    private List<String> habilitations;
    private String typeAbonnement;
    private String typeAbonnementSegment;
    private String typeAbonnementSegmentInternet;
    private TypeEntity typeProfilAbonnement;
    private TypeEntity typeClient;
    private TypeEntity statutClient;
    private TypeEntity categorieJuridique;
    private InfosDetailEntity infosDetail;
    private List<TypeEntity> agregats;
}
