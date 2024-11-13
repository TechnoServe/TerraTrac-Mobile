//package test.ui.composables
//
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import org.junit.Rule
//import org.junit.Test
//
//class MyComposableTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    @Test
//    fun myComposable_DisplaysCorrectText() {
//        // Set up the composable content to test
//        composeTestRule.setContent {
//            MyComposable(text = "Hello, Compose!")
//        }
//
//        // Check that the text is displayed correctly
//        composeTestRule.onNodeWithText("Hello, Compose!").assertIsDisplayed()
//    }
//}
//
//@Composable
//fun MyComposable(text: String) {
//    Text(text = text)
//}