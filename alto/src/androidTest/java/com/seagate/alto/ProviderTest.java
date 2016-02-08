/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProviderTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    @Test
    public void showPhotos() {
        Provider p = Providers.LOCAL.provider;
        try {
            List<Provider.Metadata> list = p.listFolder("").entries();
            for (Provider.Metadata item : list) {
                Log.d("Test", item.pathLower());
                System.out.println(item.pathLower());
            }
        } catch (Provider.ProviderException e) {
            e.printStackTrace();
        }
//        onView(withId(R.id.provider_files_button)).perform(click());
//        onView(withId(R.id.editText)).perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());
//         onView(withId(R.id.editText)).perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());
//        onView(withText("Say hello!")).perform(click()); //line 2
//        String expectedText = "Hello, " + STRING_TO_BE_TYPED + "!";
//        onView(withId(R.id.textView)).check(matches(withText(expectedText))); //line 3
    }
}