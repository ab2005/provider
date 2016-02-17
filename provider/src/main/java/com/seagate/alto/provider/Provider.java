/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.imagepipeline.PicassoRequestHandler;

import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * A cloud provider API
 */
public interface Provider {

    /**
     * Set a security token used by the network protocol handler when accessing provider API.
     */
    void setAccessToken(String token);

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
     * Download file at a given path.
     */
    public Call<ResponseBody> download(String path) throws ProviderException;

    /**
     * Download file at a given path. The callback will be invoked from  this provider executor thread.
     */
    public void download(String path, NetworkFetcher.Callback cb) throws ProviderException;

    /**
     * Get a Uri to download an image thumbnail.
     * <p></>This method currently supports files with 'jpeg' and png formats.
     * Supported sizes are: "w32h32", "w64h64", "w128h128", "w640h480", "w1024h768".
     */
    Uri getThumbnailUri(@NonNull String path, @NonNull String size, @Nullable String format) throws ProviderException;

    /**
     * Get a Uri to download an image.
     */
    Uri getUri(@NonNull String path, @Nullable String rev) throws ProviderException;

    /**
     * Get authentication token.
     */
    String getAccessToken();

    /**
     * Get provider's domain.
     */
    String getDomain();

    /**
     * The base exception thrown by Provider API calls.
     */
    public class ProviderException extends Exception {
        public ProviderException(String message, Throwable e) {
            super(message, e);
        }

        public ProviderException(Exception e) {
            super(e);
        }

        public ProviderException() {

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
        java.util.List<Metadata> entries();

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
     * MediaMetadata for a photo or video.
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

    /**
     * Immutable class for describing width and height dimensions in pixels.
     * Copied from {@linkplain android.util.Size}
     */
    public final class Size {
        /**
         * Create a new immutable Size instance.
         *
         * @param width The width of the size, in pixels
         * @param height The height of the size, in pixels
         */
        public Size(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        /**
         * Get the width of the size (in pixels).
         * @return width
         */
        public int getWidth() {
            return mWidth;
        }

        /**
         * Get the height of the size (in pixels).
         * @return height
         */
        public int getHeight() {
            return mHeight;
        }

        /**
         * Check if this size is equal to another size.
         * <p>
         * Two sizes are equal if and only if both their widths and heights are
         * equal.
         * </p>
         * <p>
         * A size object is never equal to any other type of object.
         * </p>
         *
         * @return {@code true} if the objects were equal, {@code false} otherwise
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj instanceof Size) {
                Size other = (Size) obj;
                return mWidth == other.mWidth && mHeight == other.mHeight;
            }
            return false;
        }

        /**
         * Return the size represented as a string with the format {@code "WxH"}
         *
         * @return string representation of the size
         */
        @Override
        public String toString() {
            return mWidth + "x" + mHeight;
        }

        private static NumberFormatException invalidSize(String s) {
            throw new NumberFormatException("Invalid Size: \"" + s + "\"");
        }

        /**
         * Parses the specified string as a size value.
         * <p>
         * The ASCII characters {@code \}{@code u002a} ('*') and
         * {@code \}{@code u0078} ('x') are recognized as separators between
         * the width and height.</p>
         * <p>
         * For any {@code Size s}: {@code Size.parseSize(s.toString()).equals(s)}.
         * However, the method also handles sizes expressed in the
         * following forms:</p>
         * <p>
         * "<i>width</i>{@code x}<i>height</i>" or
         * "<i>width</i>{@code *}<i>height</i>" {@code => new Size(width, height)},
         * where <i>width</i> and <i>height</i> are string integers potentially
         * containing a sign, such as "-10", "+7" or "5".</p>
         *
         * <pre>{@code
         * Size.parseSize("3*+6").equals(new Size(3, 6)) == true
         * Size.parseSize("-3x-6").equals(new Size(-3, -6)) == true
         * Size.parseSize("4 by 3") => throws NumberFormatException
         * }</pre>
         *
         * @param string the string representation of a size value.
         * @return the size value represented by {@code string}.
         *
         * @throws NumberFormatException if {@code string} cannot be parsed
         * as a size value.
         * @throws NullPointerException if {@code string} was {@code null}
         */
        public static Size parseSize(String string) throws NumberFormatException {
            if (string .startsWith("w")) {
                int ph = string.indexOf('h');
                if (ph > 1) {
                    return new Size(Integer.parseInt(string.substring(1, ph)),
                            Integer.parseInt(string.substring(ph + 1)));
                } else {
                    throw invalidSize(string);
                }
            }
            int sep_ix = string.indexOf('*');
            if (sep_ix < 0) {
                sep_ix = string.indexOf('x');
            }
            if (sep_ix < 0) {
                throw invalidSize(string);
            }
            try {
                return new Size(Integer.parseInt(string.substring(0, sep_ix)),
                        Integer.parseInt(string.substring(sep_ix + 1)));
            } catch (NumberFormatException e) {
                throw invalidSize(string);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
            return mHeight ^ ((mWidth << (Integer.SIZE / 2)) | (mWidth >>> (Integer.SIZE / 2)));
        }

        private final int mWidth;
        private final int mHeight;
    }


    public class DefaultFileMetadata implements FileMetadata {
        public final String id;
        final java.util.Date clientModified;
        final java.util.Date serverModified;
        final String rev;
        final long size;
        final MediaInfo mediaInfo;
        final Uri imageUri;
        final Uri thumbnailUri;
        final String pathLower;
        final String parentSharedFolderId;
        final String name;

        public DefaultFileMetadata(
                String id,
                String name,
                String pathLower,
                String parentSharedFolderId,
                Date clientModified,
                Date serverModified,
                String rev,
                long size,
                MediaInfo mediaInfo,
                Uri imageUri,
                Uri thumbnailUri) {
            this.id = id;
            this.clientModified = clientModified;
            this.serverModified = serverModified;
            this.rev = rev;
            this.size = size;
            this.mediaInfo = mediaInfo;
            this.imageUri = imageUri;
            this.pathLower = pathLower;
            this.parentSharedFolderId = parentSharedFolderId;
            this.thumbnailUri = thumbnailUri;
            this.name = name;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Date clientModified() {
            return clientModified;
        }

        @Override
        public Date serverModified() {
            return serverModified;
        }

        @Override
        public String rev() {
            return rev;
        }

        @Override
        public long size() {
            return size;
        }

        @Override
        public MediaInfo mediaInfo() {
            return mediaInfo;
        }

        @Override
        public Uri imageUri() {
            return imageUri;
        }

        @Override
        public Uri thumbnailUri(String type, String size) {
            return thumbnailUri;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String pathLower() {
            return pathLower;
        }

        @Override
        public String parentSharedFolderId() {
            return parentSharedFolderId;
        }
    }
}