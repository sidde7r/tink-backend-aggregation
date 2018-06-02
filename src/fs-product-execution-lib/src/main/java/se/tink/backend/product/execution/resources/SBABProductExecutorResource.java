package se.tink.backend.product.execution.resources;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.product.execution.unit.agents.sbab.mortgage.SBABCreateProductExecutor;
import se.tink.backend.product.execution.api.SBABProductExecutorService;
import se.tink.backend.product.execution.api.dto.CreateProductRequest;
import se.tink.backend.product.execution.api.dto.ProductInformationRequest;
import se.tink.backend.product.execution.api.dto.RefreshApplicationRequest;
import se.tink.backend.product.execution.controller.SwitchMortgageExecutorController;
import se.tink.backend.product.execution.model.CredentialsUpdate;

public class SBABProductExecutorResource implements SBABProductExecutorService {

    private final ListenableThreadPoolExecutor<Runnable> worker;
    private final SBABCreateProductExecutor executor;
    private final SwitchMortgageExecutorController switchMortgageExecutorController;

    @Inject
    public SBABProductExecutorResource(SBABCreateProductExecutor executor,
            SwitchMortgageExecutorController switchMortgageExecutorController,
            @Named("product-executor") ListenableThreadPoolExecutor<Runnable> worker) {
        this.executor = executor;
        this.switchMortgageExecutorController = switchMortgageExecutorController;
        this.worker = worker;
    }

    @Override
    public void createProduct(CreateProductRequest request) throws Exception {
        worker.execute(() -> {
            try {
                switchMortgageExecutorController.create(executor, request.getApplication(),
                        request.getSignableOperation(), new CredentialsUpdate(request.getCredentials(), request.getUser().getDeviceId()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void fetchProductInformation(ProductInformationRequest request) throws Exception {
        worker.execute(() -> {
            try {
                switchMortgageExecutorController.fetchProductInformation(executor, request.getUser(), request.getProductInstanceId(), request.getParameters());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void refreshApplication(RefreshApplicationRequest request) throws Exception {
        worker.execute(() -> {
            try {
                switchMortgageExecutorController.refresh(executor, request.getUser(), request.getApplicationId(), request.getParameters());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
