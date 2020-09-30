package demo.sap.safetyandroid.mediaresource;

import androidx.annotation.NonNull;
import com.sap.cloud.mobile.odata.EntitySet;
import com.sap.cloud.mobile.odata.EntityValue;
import com.sap.cloud.mobile.odata.Property;
import com.sap.cloud.mobile.odata.StreamLink;

/*
 * Utility class to support the use of Glide to download media resources
 */
public class EntityMediaResource {

    /**
     * Determine if an entity set has media resource
     * @param entitySet
     * @return true if entity type is a Media Linked Entry (MLE) or it has stream properties
     */
    public static boolean hasMediaResources(@NonNull EntitySet entitySet) {
        if (entitySet.getEntityType().isMedia() || entitySet.getEntityType().getStreamProperties().length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Determine is the version within the metadata document is OData V4 or higher
     * Server of a V4 service usually do not return metadata with the query. As a result, one cannot construct
     * the download Url for media resources to use with Glide. This method is used to conditionally add parameter
     * to have server returns full metadata information during query so that we will always be able to construct
     * the download Url for Glide.
     * @param version - of OData service specified in metadata document.
     *                It is version multiplied by 100 i.e. 4.0 is 400, 4.0.1 is 401
     * @return true if version passed in is V4 or higher
     */
    public static boolean isV4(int version) {
        if (version > 399) {
            return true;
        }
        return false;
    }

    /**
     * Return download Url for one of the media resource associated with the entity parameter.
     * @param entityValue
     * @param rootUrl
     * @return If the entity type associated with the entity parameter is a Media Linked Entry,
     *         the MLE url will be returned. Otherwise, download url for one of the stream
     *         properties will be returned.
     */
    public static String getMediaResourceUrl(@NonNull EntityValue entityValue, @NonNull String rootUrl) {
        if (entityValue.getEntityType().isMedia()) {
            return mediaLinkedEntityUrl(entityValue, rootUrl);
        } else {
            if (entityValue.getEntityType().getStreamProperties().length() > 0) {
                return namedResourceUrl(entityValue, rootUrl);
            }
        }
        return null;
    }

    /**
     * Get the media linked entity url
     * @param entityValue - entity whose MLE url is to return
     * @param rootUrl - OData Service base url
     * @return the media linked entity url or null if one cannot be constructed from the entity
     */
    private static String mediaLinkedEntityUrl(@NonNull EntityValue entityValue, @NonNull String rootUrl) {
        String mediaLink = entityValue.getMediaStream().getReadLink();
        if (mediaLink != null) {
            return rootUrl + mediaLink;
        }
        return null;
    }

    /**
     * Get the named resource URL. If there are more than one named resources, only one will be returned
     * @param entityValue entity whose MLE URL is to return
     * @param rootUrl
     * @return The named resource URL
     */
    private static String namedResourceUrl(@NonNull EntityValue entityValue, @NonNull String rootUrl) {
        Property namedResourceProp = entityValue.getEntityType().getStreamProperties().first();
        StreamLink streamLink = namedResourceProp.getStreamLink(entityValue);
        String mediaLink = streamLink.getReadLink();
        if (mediaLink != null) {
            return rootUrl + mediaLink;
        } else {
            // This is to get around the problem that after we writeToParcel and read it back, we lost the url for stream link
            // To be removed when bug is fixed
            if (entityValue.getReadLink() != null) {
                mediaLink = entityValue.getReadLink() + '/' + namedResourceProp.getName();
                return rootUrl + mediaLink;
            }
        }
        return null;
    }
}