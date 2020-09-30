package demo.sap.safetyandroid.test.pages;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import demo.sap.safetyandroid.test.core.AbstractMasterDetailPage;
import demo.sap.safetyandroid.test.core.UIElements;
import demo.sap.safetyandroid.test.core.WizardDevice;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

public class DetailPage extends AbstractMasterDetailPage {

    public DetailPage() {
        super(UIElements.DetailScreen.updateButton);
    }

    public DetailPage(int resourceID) {
        super(resourceID);
    }

    @Override
    public void clickFirstElement() {

    }

    @Override
    public void clickBack() {
        if (WizardDevice.fromBackground) {
            try {
                new UiObject(new UiSelector().descriptionContains(UIElements.DetailScreen.toolBarBackButton)).click();
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            onView(withContentDescription(UIElements.DetailScreen.toolBarBackButton)).perform(click());
        }
        // call leave page
        this.leavePage();
    }

    public void clickDelete() {
        onView(withId(UIElements.DetailScreen.deleteButton)).perform(click());

    }

    public void clickCancel() {

        onView(withId(UIElements.DetailScreen.cancelButton)).perform(click());

    }

    public void clickOk() {

        onView(withId(UIElements.DetailScreen.okButton)).perform(click());

    }

    public void clickUpdate() {

        onView(withId(UIElements.DetailScreen.updateButton)).perform(click());

    }

    public void clickSave() {

        onView(withId(UIElements.DetailScreen.saveButton)).perform(click());

    }

}
