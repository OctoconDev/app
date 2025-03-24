package app.octocon.glance

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.octocon.app.Settings
import app.octocon.app.api.getFrontingAlters
import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.MyFrontItem
import app.octocon.app.utils.compressAsWebP
import app.octocon.app.utils.globalSerializer
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Duration

class FrontWidgetWorker(
  private val context: Context,
  workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
  companion object {
    private val uniqueWorkName = FrontWidgetWorker::class.java.simpleName

    fun enqueue(context: Context, settings: Settings, glanceId: GlanceId, force: Boolean = true) {
      val manager = WorkManager.getInstance(context)
      val requestBuilder = OneTimeWorkRequestBuilder<FrontWidgetWorker>().apply {
        addTag(glanceId.toString())
        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        setInputData(
          Data.Builder()
            .putString("token", settings.token!!)
            .putBoolean("force", force)
            .build()
        )
      }
      val workPolicy = if (force) {
        ExistingWorkPolicy.REPLACE
      } else {
        ExistingWorkPolicy.KEEP
      }

      manager.enqueueUniqueWork(
        uniqueWorkName + settings.hashCode(),
        workPolicy,
        requestBuilder.build()
      )

      // Temporary workaround to avoid WM provider to disable itself and trigger an
      // app widget update
      manager.enqueueUniqueWork(
        "$uniqueWorkName-workaround",
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<FrontWidgetWorker>().apply {
          setInitialDelay(Duration.ofDays(365))
        }.build()
      )
    }

    /**
     * Cancel any ongoing worker
     */
    fun cancel(context: Context, glanceId: GlanceId) {
      WorkManager.getInstance(context).cancelAllWorkByTag(glanceId.toString())
    }
  }

  override suspend fun doWork(): Result {
    return try {
      val token = inputData.getString("token")!!

      val currentlyFronting = getFrontingAlters(token).let {
        if (it.isError) {
          it
        } else {
          loadImages(it)
        }
      }
      updateFrontWidget(currentlyFronting)

      Result.success()
    } catch (e: Exception) {
      Log.e(uniqueWorkName, "Error while loading widget data!", e)
      if (runAttemptCount < 10) {
        // Exponential backoff strategy will avoid the request to repeat
        // too fast in case of failures.
        Result.retry()
      } else {
        Result.failure()
      }
    }
  }

  private suspend fun loadImages(frontingAlters: APIResponse<List<MyFrontItem>>): APIResponse<List<MyFrontItem>> = coroutineScope {
    val data = frontingAlters.ensureSuccess

    // Load all images in parallel and wait for all of them to finish
    val newItems =
      data.map { item ->
        async(Dispatchers.IO) {
          if(item.alter.avatarUrl == null) {
            item
          } else {
            try {
              val imageBitmapBase64 = getImageBitmap(item.alter.avatarUrl!!)
              item.copy(alter = item.alter.copy(avatarUrl = imageBitmapBase64))
            } catch(e: Exception) {
              Log.e("OCTOCON", "Error while loading image", e)
              item
            }
          }
        }
      }.awaitAll()

    APIResponse.success(newItems)
  }

  private suspend fun updateFrontWidget(frontingAlters: APIResponse<List<MyFrontItem>>) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(FrontWidget::class.java)
    glanceIds.forEach { glanceId ->
      updateAppWidgetState(context, glanceId) { prefs ->
        prefs[FrontWidget.resultKey] = globalSerializer.encodeToString(frontingAlters)
      }
    }
    FrontWidget().updateAll(context)
  }

  private suspend fun getImageBitmap(url: String, force: Boolean = false): String? {
    return try {
      val request = ImageRequest.Builder(context)
        .data(url)
        .build()

      // Request the image to be loaded and throw error if it failed
      with(context.imageLoader) {
        if (force) {
          diskCache?.remove(url)
          memoryCache?.remove(MemoryCache.Key(url))
        }
        val result = execute(request)
        if (result is ErrorResult) {
          throw result.throwable
        }
      }

      val bitmap = context.imageLoader.diskCache?.openSnapshot(url)?.use { snapshot ->
        val imageFile = snapshot.data.toFile()

        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val bytes = bitmap.compressAsWebP()

        Base64.encodeToString(bytes, Base64.DEFAULT)
      }
      requireNotNull(bitmap) {
        "Couldn't find cached file"
      }
    } catch (e: Exception) {
      Log.e("OCTOCON", "Error while loading widget image", e)
      null
    }
  }
}