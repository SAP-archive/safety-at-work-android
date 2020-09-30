package demo.sap.safetyandroid.test.pages;

import com.pgssoft.espressodoppio.idlingresources.ViewIdlingResource;
import demo.sap.safetyandroid.test.core.Credentials;
import demo.sap.safetyandroid.test.core.UIElements;
import demo.sap.safetyandroid.test.core.WizardDevice;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class ActivationPage {

    public ActivationPage() {
        ViewIdlingResource viewIdlingResource = (ViewIdlingResource) new ViewIdlingResource(
                withId(UIElements.ActivationPage.startButton)).register();
    }

    public void enterEmailAddress() {
        WizardDevice.fillInputField(UIElements.ActivationPage.emailText, Credentials.EMAIL_ADDRESS);
    }

    public void clickStart() {
        onView(withId(UIElements.ActivationPage.startButton)).perform(closeSoftKeyboard(), click());
    }
}
