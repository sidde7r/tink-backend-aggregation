package se.tink.backend.product.execution.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.product.execution.api.dto.CreateProductRequest;
import se.tink.backend.product.execution.api.dto.ProductInformationRequest;
import se.tink.backend.product.execution.api.dto.RefreshApplicationRequest;

public interface ProductExecutorService {
    @POST
    @Path("product")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void createProduct(CreateProductRequest request) throws Exception;

    @POST
    @Path("product/information")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void fetchProductInformation(ProductInformationRequest request) throws Exception;

    @POST
    @Path("application/refresh")
    @TeamOwnership(Team.FINANCIAL_SERVICES)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void refreshApplication(RefreshApplicationRequest request) throws Exception;
}
