package ir.moeini.persiantodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.moeini.persiantodo.data.AppDatabase
import ir.moeini.persiantodo.data.TaskRepository
import ir.moeini.persiantodo.ui.PersianTodoApp
import ir.moeini.persiantodo.ui.theme.PersianTodoTheme
import ir.moeini.persiantodo.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getInstance(applicationContext)
        val repository = TaskRepository(db.taskDao(), db.taskListDao())

        setContent {
            PersianTodoTheme {
                // Force RTL layout regardless of system locale, since the app's
                // primary and only language is Persian.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val viewModel: TaskViewModel = viewModel(
                            factory = TaskViewModel.Factory(repository)
                        )
                        PersianTodoApp(viewModel)
                    }
                }
            }
        }
    }
}
