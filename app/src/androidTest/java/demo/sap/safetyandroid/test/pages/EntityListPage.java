package demo.sap.safetyandroid.test.pages;


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
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

public class EntityListPage extends AbstractMasterDetailPage {

    public EntityListPage(int resourceID) {
        super(resourceID);
    }

    public EntityListPage() {
        super(UIElements.EntityListScreen.entityList);
    }

    @Override
    public void clickFirstElement() {
        onData(anything()).inAdapterView(withId(R.id.entity_list)).atPosition(0).perform(click());
    }

    @Override
    public void clickBack() {
        // There is no back ui element on this screen

    }

    public void clickSettings() {
        try {
            UiObject settingsButton = new UiObject(new UiSelector().descriptionContains(UIElements.EntityListScreen.settingsToolBar));
            settingsButton.click();
            UiObject settingsText = new UiObject(new UiSelector().textContains("Settings"));
            settingsText.click();
        } catch (UiObjectNotFoundException e) {
        }
    }


    public void clickEntity(int i) {
        onData(anything())
                .inAdapterView(withId(R.id.entity_list))
                .atPosition(i)
                .perform(click());

    }


}
