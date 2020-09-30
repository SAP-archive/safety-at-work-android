package demo.sap.safetyandroid.test.core.matcher;

import android.view.View;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import androidx.appcompat.widget.AppCompatEditText;

public class SimpleInputCellMatcher extends TypeSafeMatcher<View> {
    private int simpleInputCell;

    public SimpleInputCellMatcher(int simpleInputCell) {
        this.simpleInputCell = simpleInputCell;
    }

    @Override
    protected boolean matchesSafely(View item) {
        if(item instanceof AppCompatEditText){
            ViewParent parent = item.getParent();
            boolean isItem = false;
            while(parent != null){
                if(parent instanceof View && ((View) parent).getId() == simpleInputCell){
                    isItem = true;
                    break;
                }
                parent = parent.getParent();
            }

            return isItem;
        }else {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Edit cell of the given SimplePropertyFormCell");
    }
}
