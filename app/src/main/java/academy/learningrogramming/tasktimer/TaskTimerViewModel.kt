package academy.learningrogramming.tasktimer

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.concurrent.thread

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel (application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange: called. uri is $uri")
            loadTasks()
        }
    }

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    init {
        Log.d(TAG, "TaskTimerViewModel: created")
        getApplication<Application>().contentResolver.registerContentObserver(TasksContract.CONTENT_URI,
            true, contentObserver)
        loadTasks()
    }

    private fun loadTasks() {
        val projection = arrayOf(TasksContract.Columns.ID,
                TasksContract.Columns.TASK_NAME,
                TasksContract.Columns.TASK_DESCRIPTION,
                TasksContract.Columns.TASK_SORT_ORDER)
        // <order by> Tasks.SortOrder, Tasks.Name
        val sortOrder = "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME}"

        thread {
            val cursor = getApplication<Application>().contentResolver.query(
                    TasksContract.CONTENT_URI,
                    projection, null, null,
                    sortOrder)
            databaseCursor.postValue(cursor)
        }
    }

    fun deleteTask(taskId: Long) {
        Log.d(TAG, "Deleting task")
        thread {
            getApplication<Application>().contentResolver?.delete(TasksContract.buildUriFromId(taskId), null, null)
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: called")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}