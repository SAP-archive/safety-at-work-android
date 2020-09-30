package demo.sap.safetyandroid.databinding.simplepropertyformcell;

import androidx.databinding.BindingAdapter;
import android.text.InputFilter;
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell;

/*
 * Binding adapter for Fiori SimplePropertyFormCell UI component.
 * Android data binding library invokes its methods to set value for the SimplePropertyFormCell
 * In one way databinding, layout file has binding expression to convert entity properties to string
 */
public class DataBindingAdapter {
    /*
     * For OData types: Edm.String
     * Getter of attribute bound returns String
     */
    @BindingAdapter("value")
    static public void setValue(SimplePropertyFormCell simplePropertyFormCell, String stringValue) {
        if (stringValue == null) {
            simplePropertyFormCell.setValue("");
        } else {
            simplePropertyFormCell.setValue(stringValue);
        }
    }

    @BindingAdapter("android:maxLength")
    public static void setMaxLength(SimplePropertyFormCell simplePropertyFormCell, int value) {
        InputFilter[] filters = simplePropertyFormCell.getValueView().getFilters();
        InputFilter[] newFilters = filters;
        if (filters == null) {
            newFilters = new InputFilter[]{new InputFilter.LengthFilter(value)};
        } else {
            boolean hasMaxLengthFilter = false;
            for (int index = 0; index < filters.length; index++) {
                InputFilter filter = filters[index];
                if (filter instanceof InputFilter.LengthFilter) {
                    hasMaxLengthFilter = true;
                    filters[index] = new InputFilter.LengthFilter(value);
                    break;
                }
            }
            if (!hasMaxLengthFilter) {
                int index = 0;
                newFilters = new InputFilter[filters.length + 1];
                for (InputFilter filter: filters) {
                    newFilters[index++] = filter;
                }
                newFilters[index] = new InputFilter.LengthFilter(value);
            }
        }
        simplePropertyFormCell.getValueView().setFilters(newFilters);
    }
}