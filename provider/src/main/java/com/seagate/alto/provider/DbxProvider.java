/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider;

import android.net.Uri;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxFiles;
import com.dropbox.core.v2.DbxUsers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Dropbox provider implementation.
 */
public class DbxProvider implements Provider {

    private static final String DOMAIN = "dropbox";

    private final DbxClientV2 mDbxClient;

    /**
     * Constructs a Dropbox provider.
     *
     * @param accessToken
     *      authenticator token retrieved from the login or previous session
     * @param clientIdentifier
     *      An identifier for the API client, typically of the form "Name/Version".
     *      This is used to set the HTTP {@code User-Agent} header when making API requests.
     *      The exact format of the {@code User-Agent} header is described in
     *      <a href="http://tools.ietf.org/html/rfc2616#section-3.8">section 3.8 of the HTTP specification</a>.
     */
    public DbxProvider(String accessToken, String clientIdentifier) {
        String userLocale = Locale.getDefault().toString();
        DbxRequestConfig requestConfig = new DbxRequestConfig(clientIdentifier, userLocale, OkHttpRequestor.Instance);
        mDbxClient = new DbxClientV2(requestConfig, accessToken, DbxHost.Default);
    }

    @Override
    public FolderMetadata createFolder(String path) throws ProviderException {
        final DbxFiles.FolderMetadata fm;
        try {
            fm = mDbxClient.files.createFolder(path);
        } catch (DbxException e) {
            throw new ProviderException("Failed to createFolder " + path, e);
        }
        return new FolderMetadataImpl(fm);
    }

    @Override
    public ListFolderResult listFolder(String path) throws ProviderException {
        final DbxFiles.ListFolderResult lfr;
        try {
            lfr = mDbxClient.files.listFolder(path);
        } catch (DbxException e) {
            throw new ProviderException("Failed to listFolder " + path, e);
        }
        return new ListFolderResultImpl(lfr);
    }

    @Override
    public ListFolderResult listFolderContinue(String cursor) throws ProviderException {
        final DbxFiles.ListFolderResult lfr;
        try {
            lfr = mDbxClient.files.listFolderContinue(cursor);
        } catch (DbxException e) {
            throw new ProviderException("Failed to listFolderContinue " + cursor, e);
        }
        return new ListFolderResultImpl(lfr);
    }

    @Override
    public SearchResult search(String path, String query) throws ProviderException {
        final DbxFiles.SearchResult sr;
        try {
            sr = mDbxClient.files.search(path, query);
        } catch (DbxException e) {
            throw new ProviderException("Failed to search at " + path + " with query " + query, e);
        }
        return new SearchResultImpl(sr);
    }

    @Override
    public SearchResult search(String path, String query, Long start, Long maxResults, SearchMode mode) throws ProviderException {
        DbxFiles.SearchMode dsm = DbxFiles.SearchMode.valueOf(mode.toString());
        final DbxFiles.SearchResult sr;
        try {
            sr = mDbxClient.files.searchBuilder(path, query).maxResults(maxResults).mode(dsm).start();
        } catch (DbxException e) {
            throw new ProviderException("Failed to search at " + path + " with query " + query + ", maxresults = " + maxResults, e);
        }
        return new SearchResultImpl(sr);
    }

    @Override
    public Metadata getMetadata(String path, boolean includeMediaInfo) throws ProviderException {
        DbxFiles.Metadata md = null;
        try {
            md = mDbxClient.files.getMetadata(path);
        } catch (DbxException e) {
            throw new ProviderException("Failed to getMetadate for " + path, e);
        }
        return new MetadataImpl(md);
    }

    @Override
    public Metadata delete(String path) throws ProviderException {
        DbxFiles.Metadata md = null;
        try {
            md = mDbxClient.files.delete(path);
        } catch (DbxException e) {
            throw new ProviderException("Failed to delete " + path, e);
        }
        return new MetadataImpl(md);
    }

    @Override
    public Uri getThumbnailUri(String path, String size, String format) throws ProviderException {
        return getImageUri(path, format, size);
    }

    @Override
    public Uri getUri(String path, String rev) throws ProviderException {
        return getImageUri(path, null, null);
    }

    private static Uri getImageUri(String path, String format, String size) {
        Uri.Builder ub = new Uri.Builder()
                .scheme("http")
                .authority(DOMAIN)
                .appendPath(path);
        if (size != null) {
            ub.appendQueryParameter("size", size);
        }
        if (format != null){
            ub.appendQueryParameter("format", format);
        }
        return ub.build();
    }

    @Override
    public String getToken() {
        return mDbxClient.getAccessToken();
    }

    public DbxFiles getFilesClient() {
        return mDbxClient.files;
    }

    public DbxUsers getUsersClient() {
        return mDbxClient.users;
    }

    public static class SearchResultImpl implements SearchResult {
        final DbxFiles.SearchResult sr;

        public SearchResultImpl(DbxFiles.SearchResult sr) {
            this.sr = sr;
        }

        @Override
        public ArrayList<Metadata> matches() {
            ArrayList<Metadata> entries = new ArrayList<Metadata>();
            for (final DbxFiles.SearchMatch item : sr.matches) {
                entries.add(new FileMetadataImpl(item));
            }
            return entries;
        }

        @Override
        public boolean hasMore() {
            return sr.more;
        }

        @Override
        public long start() {
            return sr.start;
        }
    }

    public static class ListFolderResultImpl implements ListFolderResult {
        final DbxFiles.ListFolderResult lfr;

        public ListFolderResultImpl(DbxFiles.ListFolderResult lfr) {
            this.lfr = lfr;
        }

        @Override
        public ArrayList<Metadata> entries() {
            ArrayList<Metadata> entries = new ArrayList<Metadata>();
            for (final DbxFiles.Metadata item : lfr.entries) {
                if (item instanceof DbxFiles.FileMetadata) {
                    entries.add(new FileMetadataImpl((DbxFiles.FileMetadata)item));
                } else if (item instanceof DbxFiles.FolderMetadata) {
                    entries.add(new FolderMetadataImpl((DbxFiles.FolderMetadata)item));
                }
            }
            return entries;
        }

        @Override
        public String cursor() {
            return lfr.cursor;
        }

        @Override
        public boolean hasMore() {
            return lfr.hasMore;
        }

    }

    public static class MetadataImpl implements Metadata {
        final DbxFiles.Metadata item;

        public MetadataImpl(DbxFiles.Metadata item) {
            this.item = item;
        }

        MetadataImpl(DbxFiles.SearchMatch item) {
            this.item = item.metadata;
        }

        @Override
        public String name() {
            return item.name;
        }

        @Override
        public String pathLower() {
            return item.pathLower;
        }

        @Override
        public String parentSharedFolderId() {
            return item.parentSharedFolderId;
        }
    }

    public static class FileMetadataImpl extends MetadataImpl implements FileMetadata {
        private String id;
        private java.util.Date clientModified;
        private java.util.Date serverModified;
        private String rev;
        private long size;
        private MediaInfo mediaInfo;

        public FileMetadataImpl(DbxFiles.FileMetadata item) {
            super(item);
            this.id = item.id;
            this.clientModified = item.clientModified;
            this.serverModified = item.serverModified;
            this.rev = item.rev;
            this.size = item.size;

            this.mediaInfo = new MediaInfo() {
                public Tag tag() {return mediaInfo.tag();}
                public MediaMetadata metadata() {return mediaInfo.metadata();}
            };
        }

        public FileMetadataImpl(DbxFiles.SearchMatch item) {
            super(item);
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
            return getImageUri(pathLower(), null, null);
        }

        @Override
        public Uri thumbnailUri(String format, String size) {
            return getImageUri(pathLower(), format, size);
        }
    }

    public static class FolderMetadataImpl extends MetadataImpl implements FolderMetadata {
        final DbxFiles.FolderMetadata fm;

        public FolderMetadataImpl(DbxFiles.FolderMetadata fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public String id() {
            return fm.id;
        }

        @Override
        public String sharedFolderId() {
            return fm.sharedFolderId;
        }
    }
}
