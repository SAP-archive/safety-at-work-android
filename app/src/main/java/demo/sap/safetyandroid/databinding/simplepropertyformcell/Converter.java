package demo.sap.safetyandroid.databinding.simplepropertyformcell;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseMethod;
import demo.sap.safetyandroid.R;
import com.sap.cloud.mobile.fiori.formcell.FormCell.CellValueChangeListener;
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell;
import com.sap.cloud.mobile.odata.DayTimeDuration;
import com.sap.cloud.mobile.odata.GeographyCollection;
import com.sap.cloud.mobile.odata.GeographyLineString;
import com.sap.cloud.mobile.odata.GeographyMultiLineString;
import com.sap.cloud.mobile.odata.GeographyMultiPoint;
import com.sap.cloud.mobile.odata.GeographyMultiPolygon;
import com.sap.cloud.mobile.odata.GeographyPoint;
import com.sap.cloud.mobile.odata.GeographyPolygon;
import com.sap.cloud.mobile.odata.GeometryCollection;
import com.sap.cloud.mobile.odata.GeometryLineString;
import com.sap.cloud.mobile.odata.GeometryMultiLineString;
import com.sap.cloud.mobile.odata.GeometryMultiPoint;
import com.sap.cloud.mobile.odata.GeometryMultiPolygon;
import com.sap.cloud.mobile.odata.GeometryPoint;
import com.sap.cloud.mobile.odata.GeometryPolygon;
import com.sap.cloud.mobile.odata.GlobalDateTime;
import com.sap.cloud.mobile.odata.GuidValue;
import com.sap.cloud.mobile.odata.LocalDate;
import com.sap.cloud.mobile.odata.LocalDateTime;
import com.sap.cloud.mobile.odata.LocalTime;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;


/**
 * Supports Android two way data binding
 * For each data type returned by the getter of the bound attribute, a pair of methods is required
 * to set the value of SimplePropertyFormCell and convert user's input (string) to respective
 * data type.
 */
public class Converter {

    /*
     * Specifies method for retrieving the value of SimplePropertyFormCell
     */
    @InverseBindingAdapter(attribute = "value", event = "valueAttrChanged")
    public static String getValue(SimplePropertyFormCell cell) {
        return cell.getValue().toString();
    }

    /*
     * Associate inverse binding listener to the view's change listener
     */
    @BindingAdapter({"valueAttrChanged"})
    public static void setValueChanged(SimplePropertyFormCell cell, final InverseBindingListener listener) {
        cell.setCellValueChangeListener(new CellValueChangeListener<CharSequence>() {
            @Override
            protected void cellChangeHandler(final CharSequence charSequence) {
                listener.onChange();
            }
        });
    }

    /*
     * For OData types: Edm.Int32, Edm.Byte: (int and Integer), Edm.Decimal: (Integer)
     * Handles two way data binding to and from Integer
     */
    @InverseMethod("toInteger")
    public static String toString(SimplePropertyFormCell cell, Integer oldValue, Integer value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static Integer toInteger(SimplePropertyFormCell cell, Integer oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.Decimal (Scale 0)
     * Handles two way data binding to and from BigInteger
     */
    @InverseMethod("toBigInteger")
    public static String toString(SimplePropertyFormCell cell, BigInteger oldValue, BigInteger value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static BigInteger toBigInteger(SimplePropertyFormCell cell, BigInteger oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return BigInteger.valueOf(Long.parseLong(value));
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.Int64
     * Handles two way data binding to and from Long
     */
    @InverseMethod("toLong")
    public static String toString(SimplePropertyFormCell cell, Long oldValue, Long value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static Long toLong(SimplePropertyFormCell cell, Long oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.Int16
     * Handles two way data binding to and from Short
     */
    @InverseMethod("toShort")
    public static String toString(SimplePropertyFormCell cell, Short oldValue, Short value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static Short toShort(SimplePropertyFormCell cell, Short oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return Short.valueOf(value);
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.SByte
     * Handles two way data binding to and from Byte
     */
    @InverseMethod("toSByte")
    public static String toString(SimplePropertyFormCell cell, Byte oldValue, Byte value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static Byte toSByte(SimplePropertyFormCell cell, Byte oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return Byte.valueOf(value);
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.Decimal
     * Handles two way data binding to and from BigDecimal
     */
    @InverseMethod("toDecimal")
    public static String toString(SimplePropertyFormCell cell, BigDecimal oldValue, BigDecimal value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static BigDecimal toDecimal(SimplePropertyFormCell cell, BigDecimal oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            String viewText = cell.getValue().toString();
            return BigDecimal.valueOf(Double.valueOf(viewText));
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.Binary
     * Handles two way data binding to and from byte[]
     */
    @InverseMethod("toBinary")
    public static String toString(SimplePropertyFormCell cell, byte[] oldValue, byte[] value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public static byte[] toBinary(SimplePropertyFormCell cell, byte[] oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return value.getBytes(StandardCharsets.UTF_8);
        } catch (NumberFormatException ex) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.Double
     * Handles two way data binding to and from Double
     */
    @InverseMethod("toDouble")
    public static String toString(SimplePropertyFormCell cell, Double oldValue, Double value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static Double toDouble(SimplePropertyFormCell cell, Double oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            String viewText = cell.getValue().toString();
            return Double.valueOf(Double.valueOf(viewText));
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.Single
     * Handles two way data binding to and from Single
     */
    @InverseMethod("toSingle")
    public static String toString(SimplePropertyFormCell cell, Float oldValue, Float value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static Float toSingle(SimplePropertyFormCell cell, Float oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            String viewText = cell.getValue().toString();
            return Float.valueOf(Float.valueOf(viewText));
        } catch (NumberFormatException e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.DateTime (V2 only)
     * Handles two way data binding to and from LocalDateTime
     */
    @InverseMethod("toLocalDateTime")
    public static String toString(SimplePropertyFormCell cell, LocalDateTime oldValue, LocalDateTime value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static LocalDateTime toLocalDateTime(SimplePropertyFormCell cell, LocalDateTime oldValue, String value) {
        cell.setErrorEnabled(false);
        if (value.length() == 0) {
            return oldValue;
        }
        LocalDateTime newValue = LocalDateTime.parse(value);
        if (newValue == null) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
        return newValue;
    }

    /*
     * For OData types: Edm.DateTimeOffset
     * Handles two way data binding to and from GlobalDateTime
     */
    @InverseMethod("toGlobalDateTime")
    public static String toString(SimplePropertyFormCell cell, GlobalDateTime oldValue, GlobalDateTime value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static GlobalDateTime toGlobalDateTime(SimplePropertyFormCell cell, GlobalDateTime oldValue, String value) {
        cell.setErrorEnabled(false);
        if (value.length() == 0) {
            return oldValue;
        }
        GlobalDateTime newValue = GlobalDateTime.parse(value);
        if (newValue == null) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
        return newValue;
    }

    /*
     * For OData types: Edm.Date
     * Handles two way data binding to and from LocalDate
     */
    @InverseMethod("toLocalDate")
    public static String toString(SimplePropertyFormCell cell, LocalDate oldValue, LocalDate value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static LocalDate toLocalDate(SimplePropertyFormCell cell, LocalDate oldValue, String value) {
        cell.setErrorEnabled(false);
        if (value.length() == 0) {
            return oldValue;
        }
        LocalDate newValue = LocalDate.parse(value);
        if (newValue == null) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
        return newValue;
    }

    /*
     * For OData types: Edm.TimeOfDay
     * Handles two way data binding to and from LocalTime
     */
    @InverseMethod("toLocalTime")
    public static String toString(SimplePropertyFormCell cell, LocalTime oldValue, LocalTime value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static LocalTime toLocalTime(SimplePropertyFormCell cell, LocalTime oldValue, String value) {
        cell.setErrorEnabled(false);
        if (value.length() == 0) {
            return oldValue;
        }
        LocalTime newValue = LocalTime.parse(value);
        if (newValue == null) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
        return newValue;
    }

    /*
    * For OData types: Edm.TimeOfDay
    * Handles two way data binding to and from LocalTime
    */
    // Edm.Duration: (DayTimeDuration)
    @InverseMethod("toDuration")
    public static String toString(SimplePropertyFormCell cell, DayTimeDuration oldValue, DayTimeDuration value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static DayTimeDuration toDuration(SimplePropertyFormCell cell, DayTimeDuration oldValue, String value) {
        cell.setErrorEnabled(false);
        if (value.length() == 0) {
            return oldValue;
        }
        DayTimeDuration newValue = DayTimeDuration.parse(value);
        if (newValue == null) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
        return newValue;
    }

    /*
     * For OData types: Edm.Guid
     * Handles two way data binding to and from GuidValue
     */
    @InverseMethod("ToGuid")
    public static String toString(SimplePropertyFormCell cell, GuidValue oldValue, GuidValue value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return value.toString();
    }

    public static GuidValue ToGuid(SimplePropertyFormCell cell, GuidValue oldValue, String value) {
        cell.setErrorEnabled(false);
        if (value.length() == 0) {
            return oldValue;
        }
        GuidValue newValue = GuidValue.parse(value);
        if (newValue == null) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
        return newValue;
    }

    /*
     * For OData types: Edm.Boolean
     * Handles two way data binding to and from Boolean
     */
    @InverseMethod("toBoolean")
    public static String toString(SimplePropertyFormCell cell, Boolean oldValue, Boolean value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static Boolean toBoolean(SimplePropertyFormCell cell, Boolean oldValue, String value) {
        if (value.length() == 0) {
            return oldValue;
        }
        return Boolean.valueOf(value);
    }

    /*
     * For OData types: Edm.GeographyPoint
     * Handles two way data binding to and from GeographyPoint
     */
    @InverseMethod("toGeographyPoint")
    public static String toString(SimplePropertyFormCell cell, GeographyPoint oldValue, GeographyPoint value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeographyPoint toGeographyPoint(SimplePropertyFormCell cell, GeographyPoint oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeographyPoint.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeographyCollection
     * Handles two way data binding to and from GeographyCollection
     */
    @InverseMethod("toGeographyCollection")
    public static String toString(SimplePropertyFormCell cell, GeographyCollection oldValue, GeographyCollection value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeographyCollection toGeographyCollection(SimplePropertyFormCell cell, GeographyCollection oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeographyCollection.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeographyMultiPoint
     * Handles two way data binding to and from GeographyMultiPoint
     */
    @InverseMethod("toGeographyMultipoint")
    public static String toString(SimplePropertyFormCell cell, GeographyMultiPoint oldValue, GeographyMultiPoint value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeographyMultiPoint toGeographyMultipoint(SimplePropertyFormCell cell, GeographyMultiPoint oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeographyMultiPoint.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeographyMultiPoint
     * Handles two way data binding to and from GeographyMultiPoint
     */
    // Edm.GeographyLineString: (GeographyLineString)
    @InverseMethod("toGeographyLineString")
    public static String toString(SimplePropertyFormCell cell, GeographyLineString oldValue, GeographyLineString value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeographyLineString toGeographyLineString(SimplePropertyFormCell cell, GeographyLineString oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeographyLineString.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeographyMultiLineString
     * Handles two way data binding to and from GeographyMultiLineString
     */
    @InverseMethod("toGeographyMultiLineString")
    public static String toString(SimplePropertyFormCell cell, GeographyMultiLineString oldValue, GeographyMultiLineString value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeographyMultiLineString toGeographyMultiLineString(SimplePropertyFormCell cell, GeographyMultiLineString oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeographyMultiLineString.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeographyPolygon
     * Handles two way data binding to and from GeographyPolygon
     */
    @InverseMethod("toGeographyPolygon")
    public static String toString(SimplePropertyFormCell cell, GeographyPolygon oldValue, GeographyPolygon value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeographyPolygon toGeographyPolygon(SimplePropertyFormCell cell, GeographyPolygon oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeographyPolygon.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeographyMultiPolygon
     * Handles two way data binding to and from GeographyMultiPolygon
     */
    @InverseMethod("toGeographyMultiPolygon")
    public static String toString(SimplePropertyFormCell cell, GeographyMultiPolygon oldValue, GeographyMultiPolygon value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeographyMultiPolygon toGeographyMultiPolygon(SimplePropertyFormCell cell, GeographyMultiPolygon oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeographyMultiPolygon.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeometryPoint
     * Handles two way data binding to and from GeometryPoint
     */
    @InverseMethod("toGeometryPoint")
    public static String toString(SimplePropertyFormCell cell, GeometryPoint oldValue, GeometryPoint value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeometryPoint toGeometryPoint(SimplePropertyFormCell cell, GeometryPoint oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeometryPoint.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeometryCollection
     * Handles two way data binding to and from GeometryCollection
     */
    @InverseMethod("toGeometryCollection")
    public static String toString(SimplePropertyFormCell cell, GeometryCollection oldValue, GeometryCollection value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeometryCollection toGeometryCollection(SimplePropertyFormCell cell, GeometryCollection oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeometryCollection.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeometryMultiPoint
     * Handles two way data binding to and from GeometryMultiPoint
     */
    @InverseMethod("toGeometryMultipoint")
    public static String toString(SimplePropertyFormCell cell, GeometryMultiPoint oldValue, GeometryMultiPoint value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeometryMultiPoint toGeometryMultipoint(SimplePropertyFormCell cell, GeometryMultiPoint oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeometryMultiPoint.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeometryLineString
     * Handles two way data binding to and from GeometryLineString
     */
    @InverseMethod("toGeometryLineString")
    public static String toString(SimplePropertyFormCell cell, GeometryLineString oldValue, GeometryLineString value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeometryLineString toGeometryLineString(SimplePropertyFormCell cell, GeometryLineString oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeometryLineString.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeometryMultiLineString
     * Handles two way data binding to and from GeometryMultiLineString
     */
    @InverseMethod("toGeometryMultiLineString")
    public static String toString(SimplePropertyFormCell cell, GeometryMultiLineString oldValue, GeometryMultiLineString value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeometryMultiLineString toGeometryMultiLineString(SimplePropertyFormCell cell, GeometryMultiLineString oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeometryMultiLineString.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeometryPolygon
     * Handles two way data binding to and from GeometryPolygon
     */
    @InverseMethod("toGeometryPolygon")
    public static String toString(SimplePropertyFormCell cell, GeometryPolygon oldValue, GeometryPolygon value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeometryPolygon toGeometryPolygon(SimplePropertyFormCell cell, GeometryPolygon oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeometryPolygon.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

    /*
     * For OData types: Edm.GeometryMultiPolygon
     * Handles two way data binding to and from GeometryMultiPolygon
     */
    @InverseMethod("toGeometryMultiPolygon")
    public static String toString(SimplePropertyFormCell cell, GeometryMultiPolygon oldValue, GeometryMultiPolygon value) {
        if (value == null) {
            return cell.getValue().toString();
        }
        return String.valueOf(value);
    }

    public static GeometryMultiPolygon toGeometryMultiPolygon(SimplePropertyFormCell cell, GeometryMultiPolygon oldValue, String value) {
        try {
            cell.setErrorEnabled(false);
            if (value.length() == 0) {
                return oldValue;
            }
            return GeometryMultiPolygon.parseWKT(value);
        } catch (Exception e) {
            cell.setErrorEnabled(true);
            cell.setError(cell.getResources().getString(R.string.format_error));
            return oldValue;
        }
    }

}