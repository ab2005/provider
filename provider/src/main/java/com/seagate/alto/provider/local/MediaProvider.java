/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.local;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Local image provider.
 */
public class MediaProvider implements Provider {
    private static final String TAG = MediaProvider.class.getName();
    final private static Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    final private static Uri internalContentUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
    final public static String[] IMAGE_FIELDS = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DESCRIPTION,
            MediaStore.Images.Media.PICASA_ID,
            MediaStore.Images.Media.IS_PRIVATE,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.MINI_THUMB_MAGIC,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };

//    private static ContentObserver watcher = new ContentObserver(new Handler()) {
//        @Override
//        public void onChange(boolean selfChange, Uri uri) {
//            // TODO: notify
//            Log.d(TAG, "Content changed:" + uri);
//            super.onChange(selfChange, uri);
//        }
//    };
//    ;
//
//    static public void startWatchingMediaStore() {
//        Providers.getContext().getContentResolver()
//                .registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, watcher);
//    }
//
//    static public void stopWatchingMediaStore() {
//        Providers.getContext().getContentResolver().unregisterContentObserver(watcher);
//    }

    static public List<FileMetadata> getLocalMediaItems(Context ctx) {
        List<FileMetadata> items = new LinkedList<>();
        loadLocalUrls(items, ctx, externalContentUri, IMAGE_FIELDS);
        loadLocalUrls(items, ctx, internalContentUri, IMAGE_FIELDS);
        return items;
    }

    static private void loadLocalUrls(List<FileMetadata> items, Context ctx, Uri contentUri, String[] projection) {
        Cursor cursor = null;
        final int nFields = projection.length;
        try {
            cursor = ctx.getContentResolver().query(contentUri, projection, null, null, null);
            int[] idx = new int[nFields];
            for (int i = 0; i < nFields; i++) {
                idx[i] = cursor.getColumnIndexOrThrow(projection[i]);
                assert (i != idx[i]);
            }
            while (cursor != null && cursor.moveToNext()) {
                Object[] item = new Object[nFields];
                for (int i : idx) {
                    int type = cursor.getType(i);
                    switch (type) {
                        case Cursor.FIELD_TYPE_STRING:
                            item[i] = cursor.getString(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            item[i] = cursor.getLong(i);
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            item[i] = cursor.getDouble(i);
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                            item[i] = null;
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            item[i] = "blob";
                            break;
                        default:
                            item[i] = "Error: default";
                            break;
                    }
                }
                items.add(new FileMetadataImpl(item));
            }
        } catch (Throwable t) {
            Log.e(TAG, "" + t.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /*package*/
    public MediaProvider() {
        // nothing
    }

    @Override
    public void setAccessToken(String token) {
        // TODO: check permissions
    }

    @Override
    public FolderMetadata createFolder(@NonNull String path) throws ProviderException {
        return null;
    }

    @Override
    public ListFolderResult listFolder(@NonNull String path) throws ProviderException {
        final List items = getLocalMediaItems(Providers.getContext());

        return new ListFolderResult() {
            @Override
            public List<Metadata> entries() {
                return items;
            }

            @Override
            public String cursor() {
                return null;
            }

            @Override
            public boolean hasMore() {
                return false;
            }
        };
    }

    @Override
    public ListFolderResult listFolderContinue(@NonNull String cursor) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SearchResult search(@NonNull String path, @NonNull String query) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SearchResult search(@NonNull String path, @NonNull String query, Long start, Long maxResults, SearchMode mode) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Metadata getMetadata(@NonNull String path, boolean includeMediaInfo) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Metadata delete(@NonNull String path) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    /**
     * Download file at a given path.
     *
     * @param path
     */
    @Override
    public Call<ResponseBody> download(String path) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    /**
     * Download file at a given path. The callback will be invoked from  this provider executor thread.
     *
     * @param path
     * @param cb
     */
    @Override
    public void download(String path, NetworkFetcher.Callback cb) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri getThumbnailUri(@NonNull String path, @NonNull String size, @Nullable String format) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri getUri(@NonNull String path, @Nullable String rev) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAccessToken() {
        return "no token";
    }

    @Override
    public String getDomain() {
        return "local";
    }

    private static class FileMetadataImpl implements FileMetadata {
        public final MediaMetadata mMetadata;
        public final Size mSize;
        public final MediaInfo mMediaInfo;
        private final Date mTimeTaken;
        private final Uri thumbnailUri;
        private final Uri imageUri;
        private final String mId;
        private final Date mClientModified;
        private final Date mServerModified;
        private final String mRev;
        private final long mLength;
        private final String mName;
        private final String mPathLower;
        private final String mParentSharedFolderId;
        private final double mLatitude;
        private final double mLongtitude;

        public FileMetadataImpl(Object[] item) {
            try {
                mId = "" + item[0];
                mSize = item[8] != null && item[9] != null ? new Size(((Long) item[8]).intValue(), ((Long) item[9]).intValue()) : new Size (0, 0);
                mTimeTaken = item[15] != null ? new Date((Long) item[15]) : new Date();
                mLatitude = item[13] != null ? (double) item[13] : 0.;
                mLongtitude = item[14] != null ? (double) item[14] : 0.;
//                thumbnailUri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, mId);
                thumbnailUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mId);
                imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mId);
                mClientModified = new Date((Long) item[5]);
                mServerModified = new Date((Long) item[6]);
                mRev = "";
                mLength = (long) item[2];
                mName = (String) item[3];
                mPathLower = ("/" + item[19] + "/" + name()).toLowerCase();
                mParentSharedFolderId = "" + item[18];
                mMetadata = new MediaMetadata() {
                    public Size dimensions() {
                        return mSize;
                    }

                    public double latitude() {
                        return mLatitude;
                    }

                    public double longitude() {
                        return mLongtitude;
                    }

                    public Date timeTaken() {
                        return mTimeTaken;
                    }
                };
                mMediaInfo = new MediaInfo() {
                    public Tag tag() {
                        return Tag.metadata;
                    }

                    public MediaMetadata metadata() {
                        return mMetadata;
                    }
                };
            } catch (Throwable t) {
                Log.e(TAG, "Error constructing FileMetadata! " + t);
                throw t;
            }
        }

        @Override
        public String id() {
            return mId;
        }

        @Override
        public Date clientModified() {
            return mClientModified;
        }

        @Override
        public Date serverModified() {
            return mServerModified;
        }

        @Override
        public String rev() {
            return mRev;
        }

        @Override
        public long size() {
            return mLength;
        }

        @Override
        public MediaInfo mediaInfo() {
            return mMediaInfo;
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
            return mName;
        }

        @Override
        public String pathLower() {
            return mPathLower;
        }

        @Override
        public String parentSharedFolderId() {
            return mParentSharedFolderId;
        }
    }
}

