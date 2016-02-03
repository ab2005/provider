/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.seagate.alto.provider.LyveCloudProvider;
import com.seagate.alto.provider.Provider;
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

import java.util.List;

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

    private LyveCloudClient mLyveCloudClient;
    private Token mToken;


    @Before
    public void setUp() throws Exception {
        mLyveCloudClient = ServiceGenerator.createService(LyveCloudClient.class);
        Response<Token> responce = mLyveCloudClient.login(TEST_LOGIN).execute();

        Assert.assertTrue(responce.isSuccess());

        mToken = responce.body();

        Assert.assertNotNull(mToken);
        mLyveCloudClient = ServiceGenerator.createService(LyveCloudClient.class, mToken.token);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateLyveService() throws Exception {
        LyveCloudClient client = ServiceGenerator.createService(LyveCloudClient.class);
        Assert.assertNotNull(client);
    }

    @Test(timeout = 5000)
    public void testLogin() throws Exception {
        LyveCloudClient client = ServiceGenerator.createService(LyveCloudClient.class);
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

    @Test(timeout = 5000)
    public void testListFolderDemo1() throws Exception {
        ListFolderRequest req = new ListFolderRequest()
                .withPath("/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test")
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

    //@Test(timeout = 15000)
    public void testImageSearch() throws Exception {
        SearchRequest req = new SearchRequest().withPath("").withQuery(".jpg");
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
            Provider.FileMetadata md = (Provider.FileMetadata)item;
            System.out.println(md.thumbnailUri("jpg", "w32h32"));
            System.out.println(md.imageUri());
        }
    }

    @Test(timeout = 15000)
    public void testLyveCloudProvider() throws Exception {
        LyveCloudProvider provider = new LyveCloudProvider(mToken.token);
    }

}