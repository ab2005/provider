/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * LyveCloud {@link Provider} API implementation.
 */
public class LyveCloudProvider implements Provider {
    @Override
    public FolderMetadata createFolder(@NonNull String path) throws ProviderException {
        return null;
    }

    @Override
    public ListFolderResult listFolder(@NonNull String path) throws ProviderException {
        return null;
    }

    @Override
    public ListFolderResult listFolderContinue(@NonNull String cursor) throws ProviderException {
        return null;
    }

    @Override
    public SearchResult search(@NonNull String path, @NonNull String query) throws ProviderException {
        return null;
    }

    @Override
    public SearchResult search(@NonNull String path, @NonNull String query, Long start, Long maxResults, SearchMode mode) throws ProviderException {
        return null;
    }

    @Override
    public Metadata getMetadata(@NonNull String path, boolean includeMediaInfo) throws ProviderException {
        return null;
    }

    @Override
    public Metadata delete(@NonNull String path) throws ProviderException {
        return null;
    }

    @Override
    public Uri getThumbnailUri(@NonNull String path, @NonNull String size, @Nullable String format) throws ProviderException {
        return null;
    }

    @Override
    public Uri getUri(@NonNull String path, @Nullable String rev) throws ProviderException {
        return null;
    }
}
