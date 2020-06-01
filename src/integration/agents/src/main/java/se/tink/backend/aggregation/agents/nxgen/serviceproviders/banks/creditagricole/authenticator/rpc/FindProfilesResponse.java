package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FindProfilesResponse extends DefaultResponse {

    private String idPartenaireCo;
    private List<ActiveUser> activeUsersList;
    private String numTelMobile;
    private Boolean numTelMobileFiabilise;

    public String getIdPartenaireCo() {
        return idPartenaireCo;
    }

    public void setIdPartenaireCo(String idPartenaireCo) {
        this.idPartenaireCo = idPartenaireCo;
    }

    public List<ActiveUser> getActiveUsersList() {
        return activeUsersList;
    }

    public void setActiveUsersList(List<ActiveUser> activeUsersList) {
        this.activeUsersList = activeUsersList;
    }

    public String getNumTelMobile() {
        return numTelMobile;
    }

    public void setNumTelMobile(String numTelMobile) {
        this.numTelMobile = numTelMobile;
    }

    public Boolean getNumTelMobileFiabilise() {
        return numTelMobileFiabilise;
    }

    public void setNumTelMobileFiabilise(Boolean numTelMobileFiabilise) {
        this.numTelMobileFiabilise = numTelMobileFiabilise;
    }

    @JsonObject
    public static class ActiveUser {
        private String userId;
        private String partnerId;
        private String userEmail;
        private String lastAccessDate;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(String partnerId) {
            this.partnerId = partnerId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getLastAccessDate() {
            return lastAccessDate;
        }

        public void setLastAccessDate(String lastAccessDate) {
            this.lastAccessDate = lastAccessDate;
        }
    }
}
