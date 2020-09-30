package demo.sap.safetyandroid.test.pages;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.test.core.AbstractMasterDetailPage;
import demo.sap.safetyandroid.test.core.UIElements;
import demo.sap.safetyandroid.test.core.WizardDevice;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

public class MasterPage extends AbstractMasterDetailPage {

    public MasterPage() {
        super(UIElements.MasterScreen.refreshButton);
    }

    public MasterPage(int resourceID) {
        super(resourceID);
    }

    @Override
    public void clickFirstElement() {
        // If the current ui is the itemlist page
        onView(withId(R.id.item_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    @Override
    public void clickBack() {

        if (WizardDevice.fromBackground) {
            try {
                new UiObject(new UiSelector().descriptionContains(UIElements.MasterScreen.toolBarBackButton)).click();
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            onView(withContentDescription(UIElements.MasterScreen.toolBarBackButton)).perform(click());
        }
    }

    public void clickMaster(int i) {
        onData(anything())
                .inAdapterView(withId(R.id.item_list))
                .atPosition(i)
                .perform(click());

    }
}
