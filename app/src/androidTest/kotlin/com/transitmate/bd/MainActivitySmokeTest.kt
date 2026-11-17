package com.transitmate.bd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_navigatesToDataManagement_withoutActivityResultCrash() {
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("ছেড়ে যান").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Settings").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("সেটিংস").fetchSemanticsNodes().isNotEmpty()
        }

        if (composeRule.onAllNodesWithText("Skip").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onAllNodesWithText("Skip").onFirst().performClick()
        } else if (composeRule.onAllNodesWithText("ছেড়ে যান").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onAllNodesWithText("ছেড়ে যান").onFirst().performClick()
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Settings").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("সেটিংস").fetchSemanticsNodes().isNotEmpty()
        }

        if (composeRule.onAllNodesWithText("Settings").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onAllNodesWithText("Settings").onFirst().performClick()
            composeRule.onAllNodesWithText("Data Management").onFirst().performClick()
            composeRule.onAllNodesWithText("LOCAL STORAGE SUMMARY").onFirst().assertIsDisplayed()
            composeRule.onAllNodesWithText("IDm Privacy Reminder").onFirst().assertIsDisplayed()
        } else {
            composeRule.onAllNodesWithText("সেটিংস").onFirst().performClick()
            composeRule.onAllNodesWithText("ডেটা ব্যবস্থাপনা").onFirst().performClick()
            composeRule.onAllNodesWithText("স্থানীয় সংরক্ষণ সারাংশ").onFirst().assertIsDisplayed()
            composeRule.onAllNodesWithText("IDm গোপনীয়তা সতর্কতা").onFirst().assertIsDisplayed()
        }
    }
}
