import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Общий набор тестов — запускает все сценарии
 */
@Suite
@SuiteDisplayName("BookStore API — Все сценарии")
@SelectClasses({
        Scenario1_FullBookLifecycleTest.class,
        Scenario2_StockAndReviewTest.class,
        Scenario3_FilterAndPaginationTest.class,
        ErrorHandlingTest.class
})
public class AllScenariosTest {
}
