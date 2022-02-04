package se.tink.integration.webdriver.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchResult;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcher;

@RunWith(JUnitParamsRunner.class)
public class WebDriverServiceMockTest {

    /*
    Mocks
     */
    private WebDriverWrapper driverWrapper;
    private WebDriverBasicUtils basicUtils;
    private ElementsSearcher elementsSearcher;
    private ProxyManager proxyManager;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private WebDriverService driverService;

    @Before
    public void setup() {
        driverWrapper = mock(WebDriverWrapper.class);
        basicUtils = mock(WebDriverBasicUtils.class);
        elementsSearcher = mock(ElementsSearcher.class);
        proxyManager = mock(ProxyManager.class);

        mocksToVerifyInOrder = inOrder(driverWrapper, basicUtils, elementsSearcher, proxyManager);

        driverService =
                new WebDriverServiceImpl(driverWrapper, basicUtils, elementsSearcher, proxyManager);
    }

    @Test
    public void should_click_first_button_when_it_can_be_found() {
        // given
        ElementLocator buttonLocator = mock(ElementLocator.class);
        WebElement buttonElement1 = mock(WebElement.class);
        WebElement buttonElement2 = mock(WebElement.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(
                        ElementsSearchResult.of(
                                buttonLocator, asList(buttonElement1, buttonElement2)));

        // when
        driverService.clickButton(buttonLocator);

        // then
        verify(buttonElement1).click();
        verifyZeroInteractions(buttonElement2);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(buttonLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_illegal_state_exception_when_button_to_click_cannot_be_found() {
        // given
        ElementLocator buttonLocator = mock(ElementLocator.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.empty());

        // when
        Throwable throwable = catchThrowable(() -> driverService.clickButton(buttonLocator));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find button element by " + buttonLocator);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(buttonLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(value = {"value1", "value2"})
    public void should_set_value_to_first_found_element(String valueToSet) {
        // given
        ElementLocator elementLocator = mock(ElementLocator.class);
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.of(elementLocator, asList(element1, element2)));

        // when
        driverService.setValueToElement(valueToSet, elementLocator);

        // then
        verify(element1).sendKeys(valueToSet);
        verifyZeroInteractions(element2);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(elementLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_throw_illegal_state_exception_when_element_to_set_value_to_cannot_be_found() {
        // given
        ElementLocator elementLocator = mock(ElementLocator.class);

        when(elementsSearcher.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.empty());

        // when
        Throwable throwable =
                catchThrowable(() -> driverService.setValueToElement("some value", elementLocator));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find element by " + elementLocator);

        mocksToVerifyInOrder
                .verify(elementsSearcher)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(elementLocator)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_delegate_extended_interfaces_to_its_components() {
        verifyDelegation(driverService, WebDriverWrapper.class, driverWrapper);
        verifyDelegation(driverService, WebDriverBasicUtils.class, basicUtils);
        verifyDelegation(driverService, ElementsSearcher.class, elementsSearcher);
        verifyDelegation(driverService, ProxyManager.class, proxyManager);
    }

    @SneakyThrows
    private void verifyDelegation(
            Object wrapperObject, Class<?> delegatedClass, Object delegatedObject) {
        for (Method delegatedMethod : delegatedClass.getDeclaredMethods()) {

            Class<?>[] parameterTypes = delegatedMethod.getParameterTypes();
            Object[] arguments = Stream.of(parameterTypes).map(this::mockType).toArray();

            Method wrapperMethod =
                    wrapperObject.getClass().getMethod(delegatedMethod.getName(), parameterTypes);

            // invoke wrapper method
            wrapperMethod.invoke(wrapperObject, arguments);

            // ensure the same method was called on delegate exactly once with the correct arguments
            delegatedMethod.invoke(verify(delegatedObject), arguments);
        }
    }

    /** Mock primitives and final classes */
    private Object mockType(Class<?> objClass) {
        if (objClass == int.class) {
            return 0;
        }
        if (objClass == String.class) {
            return "";
        }
        return mock(objClass);
    }
}
