package demo.sap.safetyandroid.mdui;

import android.graphics.Color;

/*
 * Class containing all constants required by various UI activities/fragments
 */
public class UIConstants {

    /** Fragment tag to identify create fragment */
    public static final String CREATE_FRAGMENT_TAG = "CreateFragment";

    /** Fragment tag to identify update fragment */
    public static final String MODIFY_FRAGMENT_TAG = "UpdateFragment";

    /** Fragment tag to identify detail fragment */
    public static final String DETAIL_FRAGMENT_TAG = "DetailFragment";

    /** Fragment tag to identify entity list fragment */
    public static final String LIST_FRAGMENT_TAG = "ListFragment";

    /** Perform read/display operation */
    public static final String OP_READ = "OpRead";

    /** Perform create operation */
    public static final String OP_CREATE = "OpCreate";

    /** Perform update operation */
    public static final String OP_UPDATE = "OpUpdate";

    /** Fragment tag to identify the confirmation dialog fragment */
    public static final String CONFIRMATION_FRAGMENT_TAG = "ConfirmationFragment";

    /** Event types */
    public static final int EVENT_ITEM_CLICKED = 0;
    public static final int EVENT_CREATE_NEW_ITEM = 1;
    public static final int EVENT_DELETION_COMPLETED = 2;
    public static final int EVENT_EDIT_ITEM = 3;
    public static final int EVENT_BACK_NAVIGATION_CONFIRMED = 4;
    public static final int EVENT_ASK_DELETE_CONFIRMATION = 5;

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base' */
    public static final int FIORI_STANDARD_THEME_GLOBAL_LIGHT_BASE = Color.rgb(239, 244, 249);

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base' */
    public static final int FIORI_STANDARD_THEME_GLOBAL_DARK_BASE = Color.rgb(63, 81, 96);

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base' */
    public static final int FIORI_STANDARD_THEME_BRAND_HIGHLIGHT_LIGHT = Color.rgb(66, 124, 172);

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base' */
    public static final int FIORI_STANDARD_THEME_BRAND_HIGHLIGHT_DARK = Color.rgb(145, 200, 246);

    /** SAP Fiori Standard Theme Primary Color: 'Background' */
    public static final int FIORI_STANDARD_THEME_BACKGROUND = Color.rgb(250, 250, 250);
}