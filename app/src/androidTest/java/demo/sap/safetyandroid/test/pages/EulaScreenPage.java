package demo.sap.safetyandroid.test.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import demo.sap.safetyandroid.test.core.UIElements;

public class EulaScreenPage {

    public void clickDeny() {
        // Close the soft keyboard first, since it might be covering the button.
        onView(withId(UIElements.EulaScreen.eulaDisagreeBtn)).perform(closeSoftKeyboard(), click());
    }

    public void clickAllow() {
        // Close the soft keyboard first, since it might be covering the button.
        onView(withId(UIElements.EulaScreen.eulaAgreeBtn)).perform(closeSoftKeyboard(), click());
    }
}
