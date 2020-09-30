package demo.sap.safetyandroid.test.testcases.ui;

import androidx.test.rule.ActivityTestRule;

import demo.sap.safetyandroid.app.WelcomeActivity;
import demo.sap.safetyandroid.test.core.BaseTest;
import demo.sap.safetyandroid.test.core.UIElements;
import demo.sap.safetyandroid.test.core.Utils;
import demo.sap.safetyandroid.test.pages.DetailPage;
import demo.sap.safetyandroid.test.pages.EntityListPage;
import demo.sap.safetyandroid.test.pages.MasterPage;

import org.junit.Rule;
import org.junit.Test;

public class NavigationTests extends BaseTest {

    @Rule
    public ActivityTestRule<WelcomeActivity> activityTestRule = new ActivityTestRule<>(WelcomeActivity.class);

     @Test
    public void testBackButtons() {

        // First do the onboarding flow
        Utils.doOnboarding(activityTestRule.getActivity());

        // Click on the first element
        EntityListPage entityListPage = new EntityListPage();
        // Check the page directly
        entityListPage.checkPageVisible(UIElements.EntityListScreen.entityList);
        entityListPage.clickFirstElement();

        // We should arrive on the master page
        MasterPage masterPage = new MasterPage();
        // Check the page directly
        masterPage.checkPageVisible(UIElements.MasterScreen.listView);
        masterPage.checkFloatingButton(true);
        masterPage.clickFirstElement();

        // We should arrive on the detail page
        DetailPage detailPage = new DetailPage();
        // Unregister idling resource
        detailPage.leavePage();
        detailPage.checkPageVisible(UIElements.DetailScreen.updateButton);
        //masterPage.checkFloatingButton(false);
        detailPage.clickBack();

        // After clicking the back button we should arrive on the MasterPage
        masterPage = new MasterPage();
        // Check the page directly
        masterPage.checkPageVisible(UIElements.MasterScreen.listView);
        masterPage.checkFloatingButton(true);
        masterPage.clickBack();

        // After clicking the back button we should arrive on the EntityListPage
        entityListPage = new EntityListPage();
        // Check the page directly
        entityListPage.checkPageVisible(UIElements.EntityListScreen.entityList);

    }

}
