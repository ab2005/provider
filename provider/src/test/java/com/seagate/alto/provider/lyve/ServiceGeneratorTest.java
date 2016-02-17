/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.lyve.request.Client;
import com.seagate.alto.provider.lyve.request.DownloadRequest;
import com.seagate.alto.provider.lyve.request.ListFolderRequest;
import com.seagate.alto.provider.lyve.request.LoginRequest;
import com.seagate.alto.provider.lyve.request.SearchRequest;
import com.seagate.alto.provider.lyve.response.FileMetadata;
import com.seagate.alto.provider.lyve.response.ListFolderResponse;
import com.seagate.alto.provider.lyve.response.Match;
import com.seagate.alto.provider.lyve.response.SearchResponse;
import com.seagate.alto.provider.lyve.response.Token;
import com.seagate.alto.provider.network.ServiceGenerator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
            .withEmail("abarilov@geagate.com")
            .withPassword("pitkelevo")
            .withClient(TEST_CLIENT);

    private LyveCloudClient mLyveCloudClient;
    private Token mToken;
    private String mDeviceRoot;


    @Before
    public void setUp() throws Exception {
        mLyveCloudClient = ServiceGenerator.createLyveCloudService(null);
        Response<Token> responce = mLyveCloudClient.login(TEST_LOGIN).execute();
        Assert.assertTrue(responce.isSuccess());
        mToken = responce.body();
        Assert.assertNotNull(mToken);
        mLyveCloudClient = ServiceGenerator.createLyveCloudService(mToken.token);

        Response<ListFolderResponse> response = mLyveCloudClient.listFolder(
                new ListFolderRequest().withPath("").withIncludeMediaInfo(true)
        ).execute();

        Assert.assertTrue(response.isSuccess());
        List<Provider.Metadata> fileItems = response.body().entries();
        mDeviceRoot = fileItems.get(0).pathLower();
    }

    @After
    public void tearDown() throws Exception {
        // nothing
    }

    @Test()
    public void testListRootFolder() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath(mDeviceRoot)
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        Response<ListFolderResponse> response = mLyveCloudClient.listFolder(req).execute();
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
            FileMetadata md = (FileMetadata) item;
            System.out.println(item);
        }
    }

    @Test()
    public void testListPhotos() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath(mDeviceRoot + "/Photos/2015-12-03")
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        Response<ListFolderResponse> response = mLyveCloudClient.listFolder(req).execute();
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
        SearchRequest req = new SearchRequest().withPath(mDeviceRoot).withQuery(".jpg");
        Response<SearchResponse> response = mLyveCloudClient.search(req).execute();
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

    @Test()
    public void testLoadPhotos() throws Exception {
        final int MAX_CONNECTIONS = 3;
        ListFolderRequest req = new ListFolderRequest()
                .withPath(mDeviceRoot + "/Photos/2015-12-03")
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        Response<ListFolderResponse> response = mLyveCloudClient.listFolder(req).execute();
        Assert.assertTrue(response.isSuccess());
        ListFolderResponse lfr = response.body();
        Assert.assertNotNull(lfr);
        List<FileMetadata> items = lfr.entries;
        final int[] count = {0, 0};
        final long t0 = System.currentTimeMillis();
        for (int n = 0; n < 2/*items.size() / MAX_CONNECTIONS*/; n++) {
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                //        for (final Provider.Metadata item : items) {
                final String path = items.get(n * MAX_CONNECTIONS + i).pathLower();
                final long t = System.currentTimeMillis();
                System.out.println(count[0] + ": enqueue " + path);
                count[0] = count[0] + 1;
                Callback<ResponseBody> cb = new DownloadHandler(path, count);
                Call<ResponseBody> call = mLyveCloudClient.download(new DownloadRequest(path));
                call.enqueue(cb);
            }

            while (count[0] > 0) {
                Thread.sleep(100);
            }
        }
        long dt = (System.currentTimeMillis() - t0);
        double tp = (count[1] / (dt / 1000.)) / (1024. * 1024.);
        System.out.println("Downloaded " + count[1] + " bytes in " + dt + " ms, tp = " + tp + " Mb/sec");
    }
}