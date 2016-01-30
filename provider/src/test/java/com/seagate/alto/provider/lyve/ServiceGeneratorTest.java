/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceGeneratorTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateService() throws Exception {
        // Create a simple REST adapter which points the LyveCloud API endpoint.
        LyveCloudClient client = ServiceGenerator.createService(LyveCloudClient.class);

        // Fetch and print a list of the contributors to this library.
        Call<List<Contributor>> call =
                client.contributors("fs_opensource", "android-boilerplate");

        try {
            List<Contributor> contributors = call.execute().body();
        } catch (IOException e) {
            // handle errors
        }

        for (Contributor contributor : contributors) {
            System.out.println(
                    contributor.login + " (" + contributor.contributions + ")");
        }
    }

    @Test
    public void testMain() throws Exception {

    }
}