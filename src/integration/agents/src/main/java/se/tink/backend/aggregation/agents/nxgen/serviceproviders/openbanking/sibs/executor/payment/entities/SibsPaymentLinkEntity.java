package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsPaymentLinkEntity {

    private String redirect;
    private String updatePsuIdentification;
    private String updatePsuAuthentication;
    private String selectAuthenticationMethod;
    private String authoriseTransaction;
    private String self;
    private String status;

    public String getRedirect() {
        return redirect;
    }

    public String getUpdatePsuIdentification() {
        return updatePsuIdentification;
    }

    public String getUpdatePsuAuthentication() {
        return updatePsuAuthentication;
    }

    public String getSelectAuthenticationMethod() {
        return selectAuthenticationMethod;
    }

    public void setSelectAuthenticationMethod(String selectAuthenticationMethod) {
        this.selectAuthenticationMethod = selectAuthenticationMethod;
    }

    public String getAuthoriseTransaction() {
        return authoriseTransaction;
    }

    public void setAuthoriseTransaction(String authoriseTransaction) {
        this.authoriseTransaction = authoriseTransaction;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
