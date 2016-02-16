/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.seagate.alto.provider.LyveCloudProvider;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class LyveCloudProviderTest {
    private static final String TEST_EMAIL = "abarilov@geagate.com";
    private static final String TEST_PWD = "pitkelevo";
    private static Provider mProvider;
    private String mDeviceRoot;

    @Before
    public void setUp() throws Exception {
        mProvider = Providers.SEAGATE.provider;
        String token = LyveCloudProvider.login(TEST_EMAIL, TEST_PWD);
        Assert.assertNotNull(token);
        mProvider.setAccessToken(token);

        Provider.ListFolderResult res = mProvider.listFolder("");
        Assert.assertNotNull(res);
        mDeviceRoot = res.entries().get(0).pathLower();
        Assert.assertNotNull(mDeviceRoot);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test()
    public void testListPhotos() throws Exception {
        Provider.ListFolderResult res = mProvider.listFolder(mDeviceRoot + "/Photos/2015-12-03");
        Assert.assertNotNull(res);
        for (Provider.Metadata item : res.entries()) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);
        }
    }

    @Test()
    public void testLoadPhotosRetrofitCallback() throws Exception {
        int MAX_CONNECTIONS = 5;
        Provider.ListFolderResult res = mProvider.listFolder(mDeviceRoot + "/Photos/2015-12-03");
        Assert.assertNotNull(res);
        int[] count = {0, 0};

        final long t0 = System.currentTimeMillis();
        for (int n = 0; n < 2/*items.size() / MAX_CONNECTIONS*/; n++) {
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                final String path = res.entries().get(n * MAX_CONNECTIONS + i).pathLower();
                final long t = System.currentTimeMillis();
                System.out.println(count[0] + ": enqueue " + path);
                count[0] = count[0] + 1;
                Callback<ResponseBody> cb = new DownloadHandler(path, count);
                Call<ResponseBody> call = mProvider.download(path);
                call.enqueue(cb);
            }

            while (count[0] > 0) {
                Thread.sleep(100);
            }
        }

        long dt = (System.currentTimeMillis() - t0);
        double tp = (count[1] / (dt / 1000.)) / (1024. * 1024.);
        String st = String.format("%.2f Mb/sec", tp);
        System.out.println("Downloaded " + count[1] + " bytes in " + dt + " ms, tp = " + st);
    }

}