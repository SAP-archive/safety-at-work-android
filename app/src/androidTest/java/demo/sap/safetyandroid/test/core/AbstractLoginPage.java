package demo.sap.safetyandroid.test.core;

import androidx.test.uiautomator.UiDevice;


public abstract class AbstractLoginPage {
    protected final int WAIT_TIMEOUT = 10000;
    protected UiDevice uiDevice;

    public abstract void authenticate();

}
