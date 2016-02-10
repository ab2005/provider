// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// random, stable content

import android.net.Uri;
import android.util.SparseArray;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.seagate.alto.provider.Provider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PlaceholderContent {

    private static Random random = new Random();

    public static String INDEX = "index";

    private static SparseArray<Uri> uris = new SparseArray<>();
    private static List<Provider.Metadata> sMetadataItems = new ArrayList<>();

    public static int getCount() {
        return sMetadataItems.size();
    }

    private static Uri dumbUri() {
        int height = 300 + random.nextInt(10);
        int width = 300 + random.nextInt(10);
        String fun = "http://fillmurray.com/" + width + "/" + height;
        return Uri.parse(fun);
    }

    public static Uri getUri(int position) {
        if (position > getCount() - 1) {
            return dumbUri();
        }
        Provider.Metadata md = sMetadataItems.get(position);
        Uri uri = ((Provider.FileMetadata)md).imageUri();
        return uri;
    }

    public static Uri getThumbnailUri(int position) {
        return getThumbnailUri(position, 640);
    }

    public static Uri getThumbnailUri(int position, int size) {
        if (sMetadataItems == null) return dumbUri();

        if (position > getCount() - 1) {
            return dumbUri();
        }

        Provider.Metadata md = sMetadataItems.get(position);

        String s = "w" + size + "h" + size;
        if (size >= 1024) {
            s = "w1024h780";
        } else if (size >= 640) {
            s = "w640h480";
        }
        Uri uri = ((Provider.FileMetadata)md).thumbnailUri("jpeg", s);
        if (!UriUtil.isNetworkUri(uri)) {
            uri = ((Provider.FileMetadata)md).imageUri();
        }
        return uri;
    }

    public static void setContent(List<Provider.Metadata> list) {
        Fresco.getImagePipeline().clearCaches();
        sMetadataItems = list;
    }


    public static long getTimestamp(int position) {
        if (position > getCount() - 1) {
            long offset = Timestamp.valueOf("2015-01-01 00:00:00").getTime();
            long end = Timestamp.valueOf("2016-01-01 00:00:00").getTime();
            long diff = end - offset + 1;
            return offset + (long)(diff * (position + 1) / 100);      // TODO: 1/14/16 getTimeStamp here
        }

        Provider.FileMetadata md = null;
        try {
            md = (Provider.FileMetadata) sMetadataItems.get(position);
            Date t = md.mediaInfo().metadata().timeTaken();
            return t.getTime();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
