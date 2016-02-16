/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.lyve.request.ListFolderRequest;
import com.seagate.alto.provider.lyve.request.SearchRequest;
import com.seagate.alto.provider.lyve.response.FileMetadata;
import com.seagate.alto.provider.lyve.response.ListFolderResponse;
import com.seagate.alto.provider.lyve.response.SearchResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class DbxServiceGeneratorTest {
    private DbxCloudClient mCloudClient;
    private String mToken;


    @Before
    public void setUp() throws Exception {
        mToken = "c0SXSHbaqn4AAAAAAAAUPiaClemepM8ptLH9yTj7anMi9vC1n11M1VKMe6Bse1Bm";
        Assert.assertNotNull(mToken);
        mCloudClient = ServiceGenerator.createService(ServiceGenerator.DBX_API_BASE_URL, DbxCloudClient.class, mToken);
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test(timeout = 5000)
    public void testListRootFolder() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("")
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        Response<ListFolderResponse> response = mCloudClient.listFolder(req).execute();
        Assert.assertTrue(response.isSuccess());

        ListFolderResponse lfr = response.body();

        Assert.assertNotNull(lfr);

        List<FileMetadata> items = lfr.entries;
        for (FileMetadata item : items) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);
            System.out.println(item);
        }

        List<Provider.Metadata> fileItems = lfr.entries();
        for (Provider.Metadata item : items) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);

        }
    }

    @Test(timeout = 5000)
    public void testListFolderCameraUploads() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("/camera uploads")
                .withIncludeMediaInfo(true)
                .withIncludeDeleted(true)
// THESE DOES NOT WORK:
//                .withIncludeChildCount(true)
//                .withLimit(1000)
        ;

        Response<ListFolderResponse> response = mCloudClient.listFolder(req).execute();
        System.out.println(response.code() + ", " + response.message());
        Assert.assertTrue(response.isSuccess());

        ListFolderResponse lfr = response.body();

        Assert.assertNotNull(lfr);

        List<FileMetadata> items = lfr.entries;
        for (FileMetadata item : items) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);
            System.out.println(item);
        }

        List<Provider.Metadata> fileItems = lfr.entries();
        for (Provider.Metadata item : items) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);
        }
    }

    @Test(timeout = 15000)
    public void testImageSearch() throws Exception {
        SearchRequest req = new SearchRequest().withPath("").withQuery(".jpg");
        Response<SearchResponse> response = mCloudClient.search(req).execute();
        Assert.assertTrue(response.isSuccess());

        SearchResponse sr = response.body();

        Assert.assertNotNull(sr);

        List<Provider.Metadata> fileItems = sr.matches();
        for (Provider.Metadata item : fileItems) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);
            Provider.FileMetadata md = (Provider.FileMetadata) item;
            System.out.println(md);
        }
    }

    @Test()
    public void downloadCall() throws Exception {
        final String path = "/camera uploads/2015-12-12 17.11.33.jpg";
//        String params = "{\"path\":\"/camera uploads/2015-12-12 17.11.33.jpg\"}";
        String params = "{\"path\":\"" + path + "\"}";
        Request request = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .url(ServiceGenerator.DBX_API_BASE_URL + "/2/files/download")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer c0SXSHbaqn4AAAAAAAAUPiaClemepM8ptLH9yTj7anMi9vC1n11M1VKMe6Bse1Bm")
                .header("Dropbox-API-Arg", params)
                .method("POST", RequestBody.create(MediaType.parse(""), ""))
                .build();
        ;
        final okhttp3.Call httpCall = new okhttp3.OkHttpClient.Builder().build().newCall(request);

        System.out.println("enqueued " + path);
        httpCall.enqueue(
                new okhttp3.Callback() {
                    long submitTime = System.currentTimeMillis();
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(final okhttp3.Call call, final okhttp3.Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            System.out.println(response.message());
                            return;
                        }
                        final ResponseBody body = response.body();
                        InputStream in = null;
                        ByteArrayOutputStream out = null;
                        try {
                            in = new BufferedInputStream(response.body().byteStream());
                            out = new ByteArrayOutputStream();
                            byte[] buff = new byte[1024];
                            int count = 0;
                            while ((count = in.read(buff, 0, buff.length)) != -1) {
                                out.write(buff, 0, count);
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
//                        try {
//                            long contentLength = body.contentLength();
//                            if (contentLength < 0) {
//                                contentLength = 0;
//                            }
//                            long t = System.currentTimeMillis() - submitTime;
//                            System.out.println("response consumed in "+ t + "ms, " + path);
//                        } catch (Exception e) {
//                            handleException(call, e);
//                        } finally {
//                            try {
//                                body.close();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                    }
//
//                    void handleException(okhttp3.Call call, final Exception e) {
//                        long t = System.currentTimeMillis() - submitTime;
//                        if (call.isCanceled()) {
//                            System.out.println("response canceled in " + t + " ms, " + path);
//                        } else {
//                            System.out.println("response failed in " + t + " ms, " + path);
//                        }
//                    }
                });
        System.out.println("enqueued done");
    }

        @Test()
    public void testDowmloads() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("/camera uploads")
                .withIncludeMediaInfo(true)
                .withIncludeDeleted(true);

        Response<ListFolderResponse> response = mCloudClient.listFolder(req).execute();
        System.out.println(response.code() + ", " + response.message());
        Assert.assertTrue(response.isSuccess());

        ListFolderResponse lfr = response.body();

        Assert.assertNotNull(lfr);

        List<FileMetadata> items = lfr.entries;

        final int[] cnt = {0};
        cnt[0] = items.size();
        final long t = System.currentTimeMillis();
        for (final Provider.Metadata item : items) {
            final String path = "/camera uploads/2015-12-12 17.11.33.jpg";//item.pathLower();
            Call<ResponseBody> call = mCloudClient.download(new DownloadRequest(path));
            Response<ResponseBody> res = call.execute();
            if (!res.isSuccess()) {
                System.out.println(path + " failed! " + response.code() + ": " +  response.message());
                Headers hh = call.request().headers();
                String re = new DownloadRequest(path).toString();
                String repl = hh.value(0);
                Assert.assertEquals(re, repl);
                return;
            }

            System.out.println("responded " + (System.currentTimeMillis() - t) + "ms");

        }
    }
/*
    curl -X POST https://content.dropboxapi.com/2/files/download --header 'Authorization: Bearer c0SXSHbaqn4AAAAAAAAUPiaClemepM8ptLH9yTj7anMi9vC1n11M1VKMe6Bse1Bm' --header 'Dropbox-API-Arg: {"path":"/camera uploads/2015-12-12 17.11.33.jpg”}’ --header 'Content-Length: 0'

curl -X POST https://content.dropboxapi.com/2/files/download \
  --header 'Authorization: Bearer c0SXSHbaqn4AAAAAAAAUPiaClemepM8ptLH9yTj7anMi9vC1n11M1VKMe6Bse1Bm' \
  --header 'Dropbox-API-Arg: {"path":"/camera uploads/2015-12-12 17.11.33.jpg"}' \
  --header 'Content-Length: 0'

            Dropbox-API-Arg: {"path":"/camera uploads/2015-12-12 17.11.33.jpg"}
            Authorization: Bearer c0SXSHbaqn4AAAAAAAAUPiaClemepM8ptLH9yTj7anMi9vC1n11M1VKMe6Bse1Bm
            Content-Length: 0

 */
    @Test()
    public void testMultipleDowmloads() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("/camera uploads")
                .withIncludeMediaInfo(true)
                .withIncludeDeleted(true)
// THESE DOES NOT WORK:
//                .withIncludeChildCount(true)
//                .withLimit(1000)
                ;

        Response<ListFolderResponse> response = mCloudClient.listFolder(req).execute();
        System.out.println(response.code() + ", " + response.message());
        Assert.assertTrue(response.isSuccess());

        ListFolderResponse lfr = response.body();

        Assert.assertNotNull(lfr);

        List<FileMetadata> items = lfr.entries;

        final int[] cnt = {1};
        cnt[0] = items.size();
        final long t = System.currentTimeMillis();
        for (final Provider.Metadata item : items) {
           downloadCall();
            break;
            //            downloadCall(item.pathLower());
//            break;
//            final String path = "/camera uploads/2015-12-12 17.11.33.jpg";//item.pathLower();
//            final String p = "{\"path\":\"/camera uploads/2015-12-12 17.11.33.jpg\"}";
//
//            Call<ResponseBody> call = mCloudClient.download(new DownloadRequest(path));
//            call.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    if (!response.isSuccess()) {
//                        System.out.println(path + " failed! " + response.code() + ": " +  response.message());
//                        Headers hh = call.request().headers();
//                        String req = new DownloadRequest(path).toString();
//                        String repl = hh.value(0);
//                        Assert.assertEquals(req, repl);
//                        Assert.assertEquals(repl, p);
//
//                        return;
//                    }
//
//                    System.out.println("responded " + (System.currentTimeMillis() - t) + "ms");
//                    InputStream in = null;
//                    ByteArrayOutputStream out = null;
//                    //long t = System.currentTimeMillis();
//                    try {
//                        in = new BufferedInputStream(response.body().byteStream());
//                        out = new ByteArrayOutputStream();
//                        byte[] buff = new byte[1024];
//                        int count = 0;
//                        while ((count = in.read(buff, 0, buff.length)) != -1) {
//                            out.write(buff, 0, count);
//                        }
//                        out.flush();
//                        cnt[0]--;
//                        System.out.println(cnt[0] + ":" + path + ": " + out.size() + " bytes, " + (System.currentTimeMillis() - t) + "ms");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (in != null) {
//                            try {
//                                in.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    t.printStackTrace();
//                }
//            });
//            break;
        }
        System.out.println("Enqueue time = " + (System.currentTimeMillis() - t) + " ms");

        System.out.println("waiting...");
        while(cnt[0] > 0) {
            Thread.sleep(100);
        }
        System.out.println("Totoal time = " +  (System.currentTimeMillis() - t) + " ms");
    }

}