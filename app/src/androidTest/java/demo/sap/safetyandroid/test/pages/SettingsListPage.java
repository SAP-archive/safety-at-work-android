package demo.sap.safetyandroid.test.pages;

import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.test.core.AbstractMasterDetailPage;
import demo.sap.safetyandroid.test.core.UIElements;
import demo.sap.safetyandroid.test.core.matcher.ToastMatcher;

import ch.qos.logback.classic.Level;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import demo.sap.safetyandroid.test.core.ClientPolicyManager;

public class SettingsListPage extends AbstractMasterDetailPage {

    public SettingsListPage(int resourceID) {
        super(resourceID);
    }

    public SettingsListPage() {
        super((R.id.recycler_view));
    }

    @Override
    public void clickFirstElement() {
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click());
    }

    @Override
    public void clickBack() {
        // There is no back ui element on this screen
        onView(withContentDescription(UIElements.MasterScreen.toolBarBackButton)).perform(click());
    }

    public void clickLogLevel() {
        onView(withText(R.string.log_level)).perform(click());
    }

    public void clickUploadLog() {
        onView(withText(R.string.upload_log)).perform(click());
    }

    public void clickManagePasscode() {
        onView(withText(R.string.manage_passcode)).perform(click());
    }

    public void clickResetApp() {
        onView(withText(R.string.reset_app)).perform(click());
    }

    public void clickYes() {
        onView(withId(UIElements.SettingsScreen.yesButtonResetApp))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
    }

    public void clickCancelOnDialog() {
        onView(withId(UIElements.SettingsScreen.noButtonResetApp))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /* Checkers */
    public void checkLoglevel(String expectedLoglevel) {
        onView(withText(expectedLoglevel)).check(matches(isDisplayed()));
    }

  public void checkLogUploadToast() {
      Level policyLevel = ClientPolicyManager.getInstance().getClientPolicy().getLogLevel();

      if (!policyLevel.levelStr.equals("OFF")) {
          onView(withText(R.string.log_upload_ok)).inRoot(new ToastMatcher())
                  .check(matches(withText(R.string.log_upload_ok)));
      }
  }

     public void checkConfirmationDialog() {
         onView(withText(R.string.reset_app_confirmation))
                .check(matches(isDisplayed()));
     }

}
