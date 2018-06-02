package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.List;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.Category;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.grpc.v1.converter.category.CoreCategoriesToGrpcCategoryTreeConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.CategoryController;
import se.tink.grpc.v1.models.CategoryTree;
import se.tink.grpc.v1.rpc.ListCategoriesRequest;
import se.tink.grpc.v1.rpc.ListCategoriesResponse;
import se.tink.grpc.v1.services.CategoryServiceGrpc;

public class CategoryGrpcTransport extends CategoryServiceGrpc.CategoryServiceImplBase {
    private final CategoryController categoryController;

    @Inject
    public CategoryGrpcTransport(CategoryController categoryController) {
        this.categoryController = categoryController;
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.CATEGORIES_READ)
    public void listCategories(ListCategoriesRequest request, StreamObserver<ListCategoriesResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        List<Category> categories = categoryController.list(user.getLocale());
        CategoryTree categoryTree = new CoreCategoriesToGrpcCategoryTreeConverter().convertFrom(categories);

        streamObserver.onNext(ListCategoriesResponse.newBuilder().setCategories(categoryTree).build());
        streamObserver.onCompleted();
    }
}
