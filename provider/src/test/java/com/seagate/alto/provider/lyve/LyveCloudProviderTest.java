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

import java.util.List;

public class LyveCloudProviderTest {
    private static final String TEST_EMAIL = "demo.zzz@seagate.com";
    private static final String TEST_PWD = "demozzz";
    private static Provider mProvider;

    @Before
    public void setUp() throws Exception {
        String token = LyveCloudProvider.login(TEST_EMAIL, TEST_PWD);
        Assert.assertNotNull(token);

        mProvider = Providers.SEAGATE.provider;
        mProvider.setAccessToken(token);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test(timeout = 15000)
    public void testListRootFolder() throws Exception {
        listFolder("");
    }

    @Test(timeout = 15000)
    public void testListDemo1() throws Exception {
        listFolder("/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test");
    }

    private void listFolder(String path) throws Provider.ProviderException {
        Provider.ListFolderResult res = mProvider.listFolder(path);
        Assert.assertNotNull(res);

        List<Provider.Metadata> items = res.entries();
        for (Provider.Metadata item : items) {
            Assert.assertTrue(item instanceof Provider.FileMetadata);
            Provider.FileMetadata md = (Provider.FileMetadata)item;
            System.out.println(md.imageUri());
        }
    }
}