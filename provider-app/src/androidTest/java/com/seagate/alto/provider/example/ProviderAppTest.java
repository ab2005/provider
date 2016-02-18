/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProviderAppTest {
    @Rule
    public ActivityTestRule<ProviderUserActivity> mActivityRule = new ActivityTestRule<>(ProviderUserActivity.class);
    @Test
    public void showPhotos() {
        onView(withId(R.id.provider_files_button)).perform(click());
//        onView(withId(R.id.editText)).perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());
//         onView(withId(R.id.editText)).perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());
//        onView(withText("Say hello!")).perform(click()); //line 2
//        String expectedText = "Hello, " + STRING_TO_BE_TYPED + "!";
//        onView(withId(R.id.textView)).check(matches(withText(expectedText))); //line 3
    }
}