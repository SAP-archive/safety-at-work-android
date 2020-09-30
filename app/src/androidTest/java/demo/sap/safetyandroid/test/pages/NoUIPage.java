package demo.sap.safetyandroid.test.pages;

import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import demo.sap.safetyandroid.test.core.Constants;
import demo.sap.safetyandroid.test.core.UIElements;

import org.junit.Assert;

public class NoUIPage {

    public NoUIPage() {
        // Check whether the page appeared or not
        // We use uiautomator since the resource is not generated every case
        UiObject helloWorldText = new UiObject(new UiSelector().text(UIElements.NoUIScreen.helloWorldTextID));
        boolean textExists = helloWorldText.waitForExists(Constants.NETWORK_REQUEST_TIMEOUT);

        // Fail the test if the textview is not visible
        Assert.assertTrue(textExists);
    }
}
