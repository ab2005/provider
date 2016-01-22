package com.seagate.alto.provider;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Size;

/**
 * A cloud provider API
 */
public interface Provider {
    /**
     * Constructs a provider.
     *
     * @param accessToken
     *      authenticator token retrieved from the login or previous session
     * @param clientIdentifier
     *      An identifier for the API client, typically of the form "Name/Version".
     *      This is used to set the HTTP {@code User-Agent} header when making API requests.
     *      The exact format of the {@code User-Agent} header is described in
     *      <a href="http://tools.ietf.org/html/rfc2616#section-3.8">section 3.8 of the HTTP specification</a>.
     */
    Provider createProvider(@NonNull String accessToken, @NonNull String clientIdentifier);

    /**
     * Creates a folder at a given path.
     */
    FolderMetadata createFolder(@NonNull String path) throws ProviderException;

    /**
     * Returns the contents of a folder.
     */
    ListFolderResult listFolder(@NonNull String path) throws ProviderException;

    /**
     * Once a cursor has been retrieved from {@link Provider#listFolder(String)},
     * use this to paginate through all files and retrieve updates to the
     * folder.
     */
    ListFolderResult listFolderContinue(@NonNull String cursor) throws ProviderException;

    /**
     * Searches for files and folders.
     */
    public SearchResult search(@NonNull String path, @NonNull String query) throws ProviderException;

    /**
     * Searches for files and folders.
     */
    public SearchResult search(@NonNull String path, @NonNull String query, Long start, Long maxResults, SearchMode mode) throws ProviderException;

    /**
     * Returns the metadata for a file or folder.
     */
    Metadata getMetadata(@NonNull String path, boolean includeMediaInfo) throws ProviderException;

    /**
     * Delete the file or folder at a given path. If the path is a folder, all
     * its contents will be deleted too.
     */
    public Metadata delete(@NonNull String path) throws ProviderException;

    /**
     * Get a Uri to download an image thumbnail.
     * <p></>This method currently supports files with 'jpeg' and png formats.
     * Supported sizes are: "w32h32", "w64h64", "w128h128", "w640h480", "w1024h768".
     */
    Uri getThumbnailUri(@NonNull String path, @NonNull String size, @Nullable String format) throws ProviderException;

    /**
     * Get a Uri to download an image.
     * */
    Uri getUri(@NonNull String path, @Nullable String rev) throws ProviderException;

    /**
     * The base exception thrown by Provider API calls.
     */
    public class ProviderException extends Exception {
        public ProviderException(String message, Throwable e) {
            super(message, e);
        }
    }

    interface FolderMetadata extends Metadata {
        /**
         * A unique identifier for the folder.
         */
        @NonNull
        String id();

        /**
         * If this folder is a shared folder mount point, the ID of the shared
         * folder mounted at this location.
         */
        String sharedFolderId();
    }

    public enum SearchMode {
        /**
         * Search file and folder names.
         */
        filename,
        /**
         * Search file and folder names as well as file contents.
         */
        filenameAndContent,
        /**
         * Search for deleted file and folder names.
         */
        deletedFilename;
    }

    public interface SearchResult {
        /**
         * A list (possibly empty) of matches for the query.
         */
        java.util.ArrayList<Metadata> matches();
        /**
         * Used for paging. If true, indicates there is another page of results
         * available that can be fetched by calling {@linkProvider#search} again.
         */
        boolean hasMore();
        /**
         * Used for paging. Value to set the start argument to when calling
         * {@link Provider#search} to fetch the next page of results.
         */
        long start();
    }

    public interface ListFolderResult {
        /**
         * The files and (direct) subfolders in the folder.
         */
        java.util.ArrayList<Metadata> entries();
        /**
         * Pass the cursor into {@link Provider#listFolderContinue(String)} to
         * see what's changed in the folder since your previous query.
         */
        String cursor();
        /**
         * If true, then there are more entries available. Pass the cursor to
         * {@link Provider#listFolderContinue(String)} to retrieve the rest.
         */
        boolean hasMore();
    }

    public interface Metadata {
        /**
         * The last component of the path (including extension). This never
         * contains a slash.
         */
        String name();
        /**
         * The lowercased full path in the user's account. This always starts
         * with a slash.
         */
        String pathLower();
        /**
         * Set if this file or folder is contained in a shared folder.
         */
        String parentSharedFolderId();
    }

    public interface FileMetadata extends Metadata {
        /**
         * A unique identifier for the file.
         */
        String id();
        /**
         * For files, this is the modification time set by the desktop client
         * when the file was added to the App. Since this time is not verified
         * (the server stores whatever the desktop client sends up),
         * this should only be used for display purposes (such as sorting) and
         * not, for example, to determine if a file has changed or not.
         */
        java.util.Date clientModified();
        /**
         * The last time the file was modified on this account.
         */
        java.util.Date serverModified();
        /**
         * A unique identifier for the current revision of a file. This field is
         * the same rev as elsewhere in the API and can be used to detect
         * changes and avoid conflicts.
         */
        String rev();
        /**
         * The file size in bytes.
         */
        long size();
        /**
         * Additional information if the file is a photo or video.
         */
        MediaInfo mediaInfo();
        /**
         * A {@link Uri} for an image suitable for handling by the network fetcher
         * of this instance {@link Provider}.
         * <p></>See:{@link PicassoRequestHandler}, {@link FrescoNetworkFetcher}
         */
        Uri imageUri();
        /**
         * A {@link Uri} for an image thumbnail suitable for handling by the network fetcher
         * of this instance {@link Provider}.
         * <p></>See:{@link PicassoRequestHandler}, {@link FrescoNetworkFetcher}
         */
        Uri thumbnailUri(String type, String size);
    }

    public interface MediaInfo {
        /**
         * The discriminating tag type for {@link MediaInfo}.
         */
        public enum Tag {
            pending,
            metadata  // MediaMetadata
        }
        /**
         * The discriminating tag for this instance.
         */
        Tag tag();

        /**
         * A {@link Metadata} for a photo or video.
         */
        MediaMetadata metadata();
    }

    /**
     * Metadata for a photo or video.
     */
    public interface MediaMetadata {
        /**
         * Dimension of the photo/video.
         */
        Size dimensions();
        /**
         * Latitude of the GPS coordinates.
         */
        double latitude();
        /**
         * Longitude of the GPS coordinates.
         */
        double longitude();
        /**
         * The timestamp when the photo/video is taken.
         */
        java.util.Date timeTaken();
    }

}