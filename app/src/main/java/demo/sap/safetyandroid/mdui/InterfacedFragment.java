package demo.sap.safetyandroid.mdui;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import androidx.appcompat.widget.Toolbar;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentActivity;
import demo.sap.safetyandroid.R;
import com.sap.cloud.mobile.flowv2.core.DialogHelper;

public class InterfacedFragment<T> extends Fragment {

    /** Hold the current context */
    protected FragmentActivity currentActivity;

    /** Store the toolbar title of the actual fragment */
    protected String activityTitle;

    /** Store the toolbar menu resource of the actual fragment */
    protected int menu;

    /** Navigation parameter: name of the link */
    protected Parcelable parentEntityData;

    /** Navigation parameter: starting entity */
    protected String navigationPropertyName;

    /** The secondary tool bar */
    protected Toolbar secondaryToolbar;

    /** THe progress bar */
    protected ProgressBar progressBar;

    /** The fragment state listener */
    protected InterfacedFragmentListener<T> listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            currentActivity = getActivity();
            listener = (InterfacedFragmentListener<T>) currentActivity;
            progressBar = currentActivity.findViewById(R.id.indeterminateBar);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (secondaryToolbar != null) {
            secondaryToolbar.getMenu().clear();
            secondaryToolbar.inflateMenu(getMenu());
            secondaryToolbar.setOnMenuItemClickListener(item -> this.onOptionsItemSelected(item));
        } else {
            inflater.inflate(getMenu(), menu);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressBar = getActivity().findViewById(R.id.indeterminateBar);
        secondaryToolbar = getActivity().findViewById(R.id.secondaryToolbar);
    }

    /** Get the toolbar title of fragment */
    public String getTitle() {
        return activityTitle;
    }

    /** Get the toolbar menu resource of fragment */
    public int getMenu() {
        return menu;
    }

    /** An interface that the entity activity has to implement. */
    public interface InterfacedFragmentListener<T> {
        void onFragmentStateChange(int eventId, @NonNull T entity);
    }

    protected void showError(String errorMessage) {
        new DialogHelper(getActivity(),
                R.style.OnboardingDefaultTheme_Dialog_Alert)
                .showOKOnlyDialog(
                        getActivity().getSupportFragmentManager(),
                        errorMessage, null, null, null);
    }
}
