/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.seagate.alto.provider.LyveCloudProvider;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;
import com.seagate.alto.provider.lyve.request.Client;
import com.seagate.alto.provider.lyve.request.ListFolderRequest;
import com.seagate.alto.provider.lyve.request.LoginRequest;
import com.seagate.alto.provider.lyve.request.SearchRequest;
import com.seagate.alto.provider.lyve.response.FileMetadata;
import com.seagate.alto.provider.lyve.response.ListFolderResponse;
import com.seagate.alto.provider.lyve.response.Match;
import com.seagate.alto.provider.lyve.response.SearchResponse;
import com.seagate.alto.provider.lyve.response.Token;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceGeneratorTest {
    public static final Client TEST_CLIENT = new Client()
            .withClientId("AAEAA765-0AA9-40B6-B414-CE723B70F07F")
            .withDisplayName("alto-test-lyve")
            .withClientPlatform("android")
            .withClientType("phone")
            .withClientVersion("0.0.1");
    public final static LoginRequest TEST_LOGIN = new LoginRequest()
            .withEmail("demo.zzz@seagate.com")
            .withPassword("demozzz")
            .withClient(TEST_CLIENT);

    private LyveCloudClient mLyveCloudJsonClient;
    private Token mToken;
    private LyveCloudClient mLyveCloudClient;


    @Before
    public void setUp() throws Exception {
        mLyveCloudJsonClient = ServiceGenerator.createJsonService(LyveCloudClient.class);
        Response<Token> responce = mLyveCloudJsonClient.login(TEST_LOGIN).execute();

        Assert.assertTrue(responce.isSuccess());

        mToken = responce.body();

        Assert.assertNotNull(mToken);
        mLyveCloudJsonClient = ServiceGenerator.createJsonService(LyveCloudClient.class, mToken.token);
        mLyveCloudClient = ServiceGenerator.createService(LyveCloudClient.class, mToken.token);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateLyveService() throws Exception {
        LyveCloudClient client = ServiceGenerator.createJsonService(LyveCloudClient.class);
        Assert.assertNotNull(client);
    }

    @Test(timeout = 5000)
    public void testLogin() throws Exception {
        LyveCloudClient client = ServiceGenerator.createJsonService(LyveCloudClient.class);
        Response<Token> responce = client.login(TEST_LOGIN).execute();
        Assert.assertTrue(responce.isSuccess());
        mToken = responce.body();
        Assert.assertNotNull(mToken);
    }

    @Test
    public void testCreateProvider() throws Exception {
    }

    @Test(timeout = 5000)
    public void testListRootFolder() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("")
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        Response<ListFolderResponse> response = mLyveCloudJsonClient.listFolder(req).execute();
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
    public void testListFolderDemo1() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test")
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        Response<ListFolderResponse> response = mLyveCloudJsonClient.listFolder(req).execute();
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

    //@Test(timeout = 15000)
    public void testImageSearch() throws Exception {
        SearchRequest req = new SearchRequest().withPath("").withQuery(".jpg");
        Response<SearchResponse> response = mLyveCloudJsonClient.search(req).execute();
        Assert.assertTrue(response.isSuccess());

        SearchResponse sr = response.body();

        Assert.assertNotNull(sr);

        List<Match> items = sr.matches;
        for (Match item : items) {
            Assert.assertTrue(item.metadata instanceof Provider.FileMetadata);
            System.out.println(item);
        }

        List<Provider.Metadata> fileItems = sr.matches();
        for (Provider.Metadata item : fileItems) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);
            Provider.FileMetadata md = (Provider.FileMetadata) item;
            System.out.println(md.thumbnailUri("jpg", "w32h32"));
            System.out.println(md.imageUri());
        }
    }

    @Test(timeout = 15000)
    public void testLyveCloudProvider() throws Exception {
        LyveCloudProvider provider = (LyveCloudProvider) Providers.SEAGATE.provider;
        provider.setAccessToken(mToken.token);
    }

    @Test()
    public void testLoadFilesFromFolderDemo1_test() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test")
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        Response<ListFolderResponse> response = mLyveCloudJsonClient.listFolder(req).execute();
        Assert.assertTrue(response.isSuccess());

        ListFolderResponse lfr = response.body();

        Assert.assertNotNull(lfr);

        List<FileMetadata> items = lfr.entries;

        List<Provider.Metadata> fileItems = lfr.entries();
        for (final Provider.Metadata item : items) {
            String path = item.pathLower();
            System.out.println(path);
            Callback<ResponseBody> cb = new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    System.out.println(response);
                    InputStream in = null;
                    ByteArrayOutputStream out = null;
                    long t = System.currentTimeMillis();
                    try {
                        in = new BufferedInputStream(response.body().byteStream());
                        out = new ByteArrayOutputStream();
                        byte[] buff = new byte[1024];
                        int count = 0;
                        while((count = in.read(buff, 0, buff.length)) != -1) {
                            out.write(buff, 0, count);
                        }
                        out.flush();
                        System.out.println(out.size() + " bytes, " + (System.currentTimeMillis() - t) + "ms");
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
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            };

            Call<ResponseBody> call = mLyveCloudClient.download(new DownloadRequest(path));
//            call.enqueue(cb);
            long t = System.currentTimeMillis();
            Response<ResponseBody> r = call.execute();
            System.out.println("POST Executed in " + (System.currentTimeMillis() - t) + "ms");
            if (r.isSuccess()) {
                cb.onResponse(call, r);
            } else {
                cb.onFailure(call, new Exception("code = " + r.code()));
            }

            Call<ResponseBody> call1 = mLyveCloudClient.checkout(path);
            long t1 = System.currentTimeMillis();
            Response<ResponseBody> r1 = call1.execute();
            System.out.println("GET Executed in " + (System.currentTimeMillis() - t) + "ms");
            if (r1.isSuccess()) {
                cb.onResponse(call, r1);
            } else {
                System.out.println("message " + r.message());
                System.out.println("body " + r.body());
                System.out.println("err body " + r.errorBody());
                System.out.println("raw  " + r.raw());
                System.out.println("raw  message" + r.raw().message());
                System.out.println("type " + r.raw().body().contentType());
                System.out.println("length " + r.raw().body().contentLength());
            }
        }
    }

}