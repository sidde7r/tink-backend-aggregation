package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.constants.EntitiesConstants;

@XmlRootElement(name = "Body")
public class Body {
    // TODO doesn't scale well as all but one of these fields are null
    private LoginRequestEntity loginRequestEntity;
    private LoginResponseEntity loginResponseEntity;
    private LogoutRequestEntity logoutRequestEntity;
    private LogoutResponseEntity logoutResponseEntity;
    private GetAccountInformationListRequestEntity getAccountInformationListRequestEntity;
    private GetAccountInformationListResponseEntity getAccountInformationListResponseEntity;
    private GetAccountStatementItemsRequestEntity getAccountStatementItemsRequestEntity;
    private GetAccountStatementItemsResponseEntity getAccountStatementItemsResponseEntity;
    private ServiceRequestEntity serviceRequestEntity;
    private ServiceResponseEntity serviceResponseEntity;
    private Fault fault;

    @XmlElement(name = "LoginRequest", namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setLoginRequestEntity(LoginRequestEntity loginRequestEntity) {
        this.loginRequestEntity = loginRequestEntity;
    }

    public LoginRequestEntity getLoginRequestEntity() {
        return loginRequestEntity;
    }

    @XmlElement(name = "LoginResponse", namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setLoginResponseEntity(LoginResponseEntity loginResponseEntity) {
        this.loginResponseEntity = loginResponseEntity;
    }

    public LoginResponseEntity getLoginResponseEntity() {
        return loginResponseEntity;
    }

    @XmlElement(name = "LogoutRequest", namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setLogoutRequestEntity(LogoutRequestEntity logoutRequestEntity) {
        this.logoutRequestEntity = logoutRequestEntity;
    }

    public LogoutRequestEntity getLogoutRequestEntity() {
        return logoutRequestEntity;
    }

    @XmlElement(name = "LogoutResponse", namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setLogoutResponseEntity(LogoutResponseEntity logoutResponseEntity) {
        this.logoutResponseEntity = logoutResponseEntity;
    }

    public LogoutResponseEntity getLogoutResponseEntity() {
        return logoutResponseEntity;
    }

    @XmlElement(
            name = "GetAccountInformationListRequest",
            namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setGetAccountInformationListRequestEntity(
            GetAccountInformationListRequestEntity getAccountInformationListRequestEntity) {
        this.getAccountInformationListRequestEntity = getAccountInformationListRequestEntity;
    }

    public GetAccountInformationListRequestEntity getGetAccountInformationListRequestEntity() {
        return getAccountInformationListRequestEntity;
    }

    @XmlElement(
            name = "GetAccountInformationListResponse",
            namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setGetAccountInformationListResponseEntity(
            GetAccountInformationListResponseEntity getAccountInformationListResponseEntity) {
        this.getAccountInformationListResponseEntity = getAccountInformationListResponseEntity;
    }

    public GetAccountInformationListResponseEntity getGetAccountInformationListResponseEntity() {
        return getAccountInformationListResponseEntity;
    }

    @XmlElement(
            name = "GetAccountStatementItemsRequest",
            namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setGetAccountStatementItemsRequestEntity(
            GetAccountStatementItemsRequestEntity getAccountStatementItemsRequestEntity) {
        this.getAccountStatementItemsRequestEntity = getAccountStatementItemsRequestEntity;
    }

    public GetAccountStatementItemsRequestEntity getGetAccountStatementItemsRequestEntity() {
        return getAccountStatementItemsRequestEntity;
    }

    @XmlElement(
            name = "GetAccountStatementItemsResponse",
            namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setGetAccountStatementItemsResponseEntity(
            GetAccountStatementItemsResponseEntity getAccountStatementItemsResponseEntity) {
        this.getAccountStatementItemsResponseEntity = getAccountStatementItemsResponseEntity;
    }

    public GetAccountStatementItemsResponseEntity getGetAccountStatementItemsResponseEntity() {
        return getAccountStatementItemsResponseEntity;
    }

    @XmlElement(name = "ServiceRequest", namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setServiceRequestEntity(ServiceRequestEntity serviceRequestEntity) {
        this.serviceRequestEntity = serviceRequestEntity;
    }

    public ServiceRequestEntity getServiceRequestEntity() {
        return serviceRequestEntity;
    }

    @XmlElement(name = "ServiceResponse", namespace = EntitiesConstants.Urls.SOAP_NAMESPACE)
    public void setServiceResponseEntity(ServiceResponseEntity serviceResponseEntity) {
        this.serviceResponseEntity = serviceResponseEntity;
    }

    public ServiceResponseEntity getServiceResponseEntity() {
        return serviceResponseEntity;
    }

    @XmlElement(name = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    public void setFault(Fault fault) {
        this.fault = fault;
    }

    public Fault getFault() {
        return fault;
    }
}
