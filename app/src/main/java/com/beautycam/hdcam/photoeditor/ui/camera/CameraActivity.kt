package com.beautycam.hdcam.photoeditor.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.CamcorderProfile
import android.net.Uri
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.base.extensions.showToast
import com.beautycam.hdcam.photoeditor.data.DataApp
import com.beautycam.hdcam.photoeditor.databinding.ActivityCameraBinding
import com.beautycam.hdcam.photoeditor.model.FilterModel
import com.beautycam.hdcam.photoeditor.model.GridModel
import com.beautycam.hdcam.photoeditor.sharePreferent.PreferenceManager
import com.beautycam.hdcam.photoeditor.ui.camera.adapter.FilterAdapter
import com.beautycam.hdcam.photoeditor.ui.camera.adapter.GridAdapter
import com.beautycam.hdcam.photoeditor.ui.gallery.GalleryActivity
import com.beautycam.hdcam.photoeditor.ui.setting.SettingActivity
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.Foder_Key
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.invisible
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.tap2
import com.beautycam.hdcam.photoeditor.widget.visible
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class FlashMode {
    OFF, ON, AUTO
}

enum class AspectRatioType(val ratio: Float) {
    RATIO_1_1(1f / 1f),
    RATIO_3_4(3f / 4f),
    RATIO_9_16(9f / 16f)
}


class CameraActivity : BaseActivity<ActivityCameraBinding>() {
    private var captureDelaySeconds = 0
    private var currentFilter: FilterModel? = null
    private var currentGrid: GridModel? = null
    private var currentFlashMode = FlashMode.OFF
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var adapterFilter : FilterAdapter
    private lateinit var adapterGrid : GridAdapter
    private var aspectRatio : Float = 1f/1f
    private var isFilter = false
    private var isVideo = false
    private var isRecording = false
    private var isTaking = false
    private var isFlash = false
    private var isFPS60 = false
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var selectedAspectRatio: AspectRatioType = AspectRatioType.RATIO_1_1
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private var secondsElapsed = 0
    private var selectedVideoQuality: Quality = Quality.SD
    private lateinit var pref : PreferenceManager

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startCamera()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                finish()
            }
        }


    override fun setViewBinding(): ActivityCameraBinding {
        return ActivityCameraBinding.inflate(layoutInflater)
    }

    override fun initView() {
        pref = PreferenceManager(this)
        updateAspectRatio()
        currentFilter = DataApp.getFilterList(this)[0]
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        loadLastCapturedImage()
        setDataFilter()
        setDataGrid()
    }

    override fun viewListener() {
        // Button listeners
        binding.apply {
            btnBack.tap { finish() }
            btnChangeSize.tap { if (!isFilter && !isTaking) changeSize()}
            btnFlash.tap { if (!isRecording && !isTaking) chooseFlashMode() }
            btnRotateCamera.tap {if (!isTaking) switchCamera() }
            btnTimer.tap {if (!isTaking) chooseTimer() }
            btnFilter.tap {if (!isTaking) chooseFilter() }
            btnGrid.tap { if (!isRecording && !isTaking) chooseGrid() }
            btnCapture.tap {if (!isTaking) takePhoto() }
            btnVideoOrCamera.tap2 {
                if (!isTaking){
                    currentFilter = DataApp.getFilterList(this@CameraActivity)[0]
                    isVideo = !isVideo
                    setLayoutCameraOrVideo()
                }
            }
            btnRecord.tap {
                if (recording != null) {
                    stopRecording()
                } else {
                    startRecording()
                }
            }
            btnFPS.tap {
                if (!isRecording) chooseFPS()
            }
            btnVideoRung.tap {
                if (!isRecording) chooseVideoRung()
            }
            btnSetting.tap { if (!isTaking) showActivity(SettingActivity::class.java) }
            btnGallery.tap { if (!isTaking) showActivity(GalleryActivity::class.java) }
        }
    }


    override fun dataObservable() {
    }

    private fun chooseFlashMode() {
        setDefaultColorButton()
        binding.btnFlash.setColorFilter(ContextCompat.getColor(this, R.color.main))
        setDefaultVisibleLL()
        binding.llSplash.visible()

        binding.apply {
            btnFlashOff.setOnClickListener {
                setFlashModeDefault(FlashMode.OFF)
                btnFlash.setImageResource(R.drawable.ic_flash_off)
                btnFlashOff.setBackgroundResource(R.drawable.bg_choose)
                isFlash = false
            }
            btnFlashOn.setOnClickListener {
                setFlashModeDefault(FlashMode.ON)
                btnFlash.setImageResource(R.drawable.ic_flash_on)
                btnFlashOn.setBackgroundResource(R.drawable.bg_choose)
                isFlash = true
            }
            btnFlashA.setOnClickListener {
                setFlashModeDefault(FlashMode.AUTO)
                btnFlash.setImageResource(R.drawable.ic_flash_a)
                btnFlashA.setBackgroundResource(R.drawable.bg_choose)
                isFlash = true
            }
        }
    }

    private fun setLayoutCameraOrVideo(){
        isFilter = false
        binding.frameOverlay.gone()

        if (isVideo){
            binding.apply {
                btnChangeSize.gone()
                btnTimer.gone()
                btnFilter.gone()
                btnCapture.invisible()

                aspectRatio = 9f / 16f
                updateAspectRatio()
                setSizePreview(currentFilter!!,binding.frameCamera)

                tvFPS.visible()
                tvFPS.text = if (isFPS60) "60" else "30"
                btnVideoRung.visible()
                btnFPS.visible()
                btnRecord.visible()
                btnVideoOrCamera.setImageResource(R.drawable.ic_camera_small)
            }

            startVideoCamera()

        }else {
            binding.apply {
                btnChangeSize.visible()
                btnTimer.visible()
                btnFilter.visible()
                btnCapture.visible()

                tvFPS.gone()
                tvTimerRecord.gone()
                btnVideoRung.gone()
                btnFPS.gone()
                btnRecord.gone()
                btnVideoOrCamera.setImageResource(R.drawable.ic_video)

                aspectRatio = selectedAspectRatio.ratio
                updateAspectRatio()
                setSizePreview(DataApp.getFilterList(this@CameraActivity)[0],binding.frameCamera)

            }

            startCamera()

        }

    }


    private fun changeSize() {
        setDefaultColorButton().also { binding.btnChangeSize.setColorFilter(ContextCompat.getColor(this, R.color.main)) }
        setDefaultVisibleLL().also { binding.llChangeSize.visible() }

        binding.apply {
            btn11.tap2 {
                selectedAspectRatio = AspectRatioType.RATIO_1_1
                aspectRatio = 1f / 1f
                setChangeSizeDefault().also { btn11.setBackgroundResource(R.drawable.bg_choose) }
                updateAspectRatio()
                setSizePreview(currentFilter!!,frameCamera)
            }
            btn43.tap2 {
                selectedAspectRatio = AspectRatioType.RATIO_3_4
                aspectRatio = 3f / 4f
                setChangeSizeDefault().also { btn43.setBackgroundResource(R.drawable.bg_choose) }
                updateAspectRatio()
                setSizePreview(currentFilter!!,frameCamera)
            }
            btn169.tap2 {
                selectedAspectRatio = AspectRatioType.RATIO_9_16
                aspectRatio = 9f / 16f
                setChangeSizeDefault().also { btn169.setBackgroundResource(R.drawable.bg_choose) }
                updateAspectRatio()
                setSizePreview(currentFilter!!,frameCamera)
            }
        }
    }

    private fun chooseTimer() {
        setDefaultColorButton().also { binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.main)) }
        setDefaultVisibleLL().also { binding.llTimer.visible() }

        binding.apply {
            btnTimerOff.tap2 {
                setTimerDefault().also { btnTimerOff.setColorFilter(ContextCompat.getColor(this@CameraActivity, R.color.main)) }
                captureDelaySeconds = 0
            }
            btnTimer3s.tap2 {
                setTimerDefault().also { btnTimer3s.setColorFilter(ContextCompat.getColor(this@CameraActivity, R.color.main)) }
                captureDelaySeconds = 3
            }
            btnTimer5s.tap2 {
                setTimerDefault().also { btnTimer5s.setColorFilter(ContextCompat.getColor(this@CameraActivity, R.color.main)) }
                captureDelaySeconds = 5
            }
            btnTimer9s.tap2 {
                setTimerDefault().also { btnTimer9s.setColorFilter(ContextCompat.getColor(this@CameraActivity, R.color.main)) }
                captureDelaySeconds = 9
            }
        }
    }

    private fun chooseFilter() {
        setDefaultColorButton().also { binding.btnFilter.setColorFilter(ContextCompat.getColor(this, R.color.main)) }
        setDefaultVisibleLL().also { binding.llFilter.visible() }
    }

    private fun chooseGrid(){
        setDefaultColorButton().also { binding.btnGrid.setColorFilter(ContextCompat.getColor(this, R.color.main)) }
        setDefaultVisibleLL().also { binding.llGrid.visible() }
    }

    private fun chooseFPS(){
        setDefaultVisibleLL().also { binding.llFPS.visible() }
        setDefaultColorButton().also { binding.btnFPS.setColorFilter(ContextCompat.getColor(this, R.color.main)) }

        binding.apply {
            btn720p.tap2 {
                tvFPS.text = "30"
                isFPS60 = false
                setFPSDefault().also { btn720p.setBackgroundResource(R.drawable.bg_choose) }
                btnFPS.setImageResource(R.drawable.ic_720p)
                setVideoQuality(Quality.SD) // 720p ~ SD
            }
            btn1080p.tap2 {
                tvFPS.text = "30"
                isFPS60 = false
                setFPSDefault().also { btn1080p.setBackgroundResource(R.drawable.bg_choose) }
                btnFPS.setImageResource(R.drawable.ic_1080p)
                setVideoQuality(Quality.FHD) // 1080p ~ Full HD
            }
            btn4K.tap2 {
                if (check4KCamera()){
                    isFPS60 = true
                    tvFPS.text = "60"
                    setFPSDefault().also { btn4K.setBackgroundResource(R.drawable.bg_choose) }
                    btnFPS.setImageResource(R.drawable.ic_4k)
                    setVideoQuality(Quality.UHD) // 4K ~ UHD
                }else{
                    showToast(R.string.this_camera_have_not_4k)
                }

            }
        }
    }


    private fun chooseVideoRung(){
        setDefaultColorButton().also { binding.btnVideoRung.setColorFilter(ContextCompat.getColor(this, R.color.main)) }
        setDefaultVisibleLL().also { binding.llVideoRung.visible() }

        binding.apply {
            btnVideoRungOn.tap2 {
                setVideoRungDefault().also { btnVideoRungOn.setBackgroundResource(R.drawable.bg_choose) }
            }
            btnVideoRungOff.tap2 {
                setVideoRungDefault().also { btnVideoRungOff.setBackgroundResource(R.drawable.bg_choose) }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildImageCapture(): ImageCapture {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels

        val targetWidth = screenWidth
        val targetHeight = (screenWidth / aspectRatio).toInt()

        return ImageCapture.Builder()
            .setTargetResolution(Size(targetWidth, targetHeight))
            .setFlashMode(
                when (currentFlashMode) {
                    FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                    FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                    FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                }
            )
            .build()
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = buildImageCapture()

            try {
                cameraProvider.unbindAll()

                // ❗Lưu lại đối tượng Camera để điều khiển zoom
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo

                setupGestureControls()

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupGestureControls() {
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1f
                val scaleFactor = detector.scaleFactor
                cameraControl.setZoomRatio(currentZoomRatio * scaleFactor)
                return true
            }
        })

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val meteringPointFactory = binding.previewView.meteringPointFactory
                val point = meteringPointFactory.createPoint(e.x, e.y)

                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .disableAutoCancel() // Giữ focus cho đến khi có focus mới
                    .build()

                cameraControl.startFocusAndMetering(action)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val zoomState = cameraInfo.zoomState.value
                val currentZoomRatio = zoomState?.zoomRatio ?: 1f
                val newZoom = if (currentZoomRatio < 2f) 2f else 1f
                cameraControl.setZoomRatio(newZoom)
                return true
            }
        })

        binding.previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }



    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun setDefaultColorButton(){
        binding.btnFlash.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnChangeSize.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnTimer.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnFilter.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnGrid.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnVideoRung.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnFPS.setColorFilter(ContextCompat.getColor(this, R.color.white))
    }

    private fun setDefaultVisibleLL(){
        binding.apply {
            llSplash.gone()
            llChangeSize.gone()
            llTimer.gone()
            llFilter.gone()
            llGrid.gone()
            llFPS.gone()
            llVideoRung.gone()
        }
    }

    private fun setFlashModeDefault(mode: FlashMode){
        currentFlashMode = mode
        binding.btnFlashOff.setBackgroundColor(Color.TRANSPARENT)
        binding.btnFlashOn.setBackgroundColor(Color.TRANSPARENT)
        binding.btnFlashA.setBackgroundColor(Color.TRANSPARENT)

        startCamera()
    }

    private fun setChangeSizeDefault(){
        binding.btn11.setBackgroundColor(Color.TRANSPARENT)
        binding.btn43.setBackgroundColor(Color.TRANSPARENT)
        binding.btn169.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setFPSDefault(){
        binding.apply {
            btn720p.setBackgroundColor(Color.TRANSPARENT)
            btn1080p.setBackgroundColor(Color.TRANSPARENT)
            btn4K.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setVideoRungDefault(){
        binding.apply {
            btnVideoRungOn.setBackgroundColor(Color.TRANSPARENT)
            btnVideoRungOff.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setTimerDefault(){
        binding.btnTimerOff.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnTimer3s.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnTimer5s.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.btnTimer9s.setColorFilter(ContextCompat.getColor(this, R.color.white))
    }

    private fun setDataFilter(){
        adapterFilter = FilterAdapter(this) {
            binding.frameOverlay.visible()
            isFilter = true
            currentFilter = it
            binding.frameOverlay.setImageResource(currentFilter!!.img)
            setSizePreview(currentFilter!!,binding.frameOverlay)
            aspectRatio = 9f / 16f
            if (it.id == 0) {
                isFilter = false
                binding.frameOverlay.invisible()
                aspectRatio = selectedAspectRatio.ratio
                setSizePreview(it,binding.frameCamera)
            }
            updateAspectRatio()
            setDefaultColorButton()
            setDefaultVisibleLL()
            startCamera()
        }

        binding.rcvFilter.layoutManager = GridLayoutManager(this, 3)
        binding.rcvFilter.adapter = adapterFilter
        // Truyền danh sách vào adapter
        adapterFilter.addList(DataApp.getFilterList(this).toMutableList())
    }

    private fun setDataGrid(){
        adapterGrid = GridAdapter(this){
            pref.saveStyleGrid(it.id)
            adapterGrid.setCheck(it.id)
            if (it.id == 0){
                binding.imgGrid.gone()
                return@GridAdapter
            }
            binding.imgGrid.visible()
            binding.imgGrid.setImageResource(it.image)
        }

        binding.rcvGrid.layoutManager = GridLayoutManager(this, 4)
        binding.rcvGrid.adapter = adapterGrid
        // Truyền danh sách vào adapter
        adapterGrid.addList(DataApp.getGridList(this).toMutableList())

        currentGrid = DataApp.getGridList(this)[pref.getStyleGrid()]
        if (currentGrid != null && currentGrid!!.id != 0){
            binding.imgGrid.setImageResource(currentGrid!!.image)
            binding.imgGrid.visible()
        }
        adapterGrid.setCheck(currentGrid!!.id)

    }

    private fun setSizePreview(filter : FilterModel, view : View){
        // Lấy kích thước thực tế của frameOverlay sau khi nó render xong
        view.post {
            val containerWidth = view.width
            val containerHeight = view.height

            val previewWidth = (containerWidth * filter.previewRatioW).toInt()
            val previewHeight = (containerHeight * filter.previewRatioH).toInt()

            val params = binding.previewView.layoutParams
            params.width = previewWidth
            params.height = previewHeight
            binding.previewView.layoutParams = params
        }
    }

    private fun flashScreenEffect() {
        binding.flashOverlay.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction {
                    alpha = 0.8f
                    visibility = View.GONE
                }
                .start()
        }
    }

    private fun takePhoto() {
        if (captureDelaySeconds > 0) {
            startCountdown(captureDelaySeconds)
        } else {
            captureNow()
        }
    }

    private fun startCountdown(seconds: Int) {
        binding.tvCountdown.visibility = View.VISIBLE
        object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                binding.tvCountdown.text = sec.toString()
            }

            override fun onFinish() {
                binding.tvCountdown.visibility = View.GONE
                captureNow()
            }
        }.start()
    }

    private fun captureNow() {
        flashScreenEffect()
        isTaking = true
        if (isFlash) {
            Toast.makeText(this, R.string.keep_the_device_stable, Toast.LENGTH_SHORT).show()
        }
        if (currentFilter != null && currentFilter!!.id != 0) {
            // CASE: Có frame → capture in-memory
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val originalBitmap = rotateBitmapIfNeeded(imageProxyToBitmap(imageProxy), rotationDegrees)
                        imageProxy.close()

                        // Step 1: Crop ảnh đúng tỉ lệ vùng preview
                        val desiredRatio = (9f * currentFilter!!.previewRatioW) / (16f * currentFilter!!.previewRatioH)
                        val croppedBitmap = cropBitmapToAspectRatio(originalBitmap, desiredRatio)

// Step 2: Resize ảnh này xuống theo đúng phần % khung 9:16 mà ảnh chiếm
                        val previewWidthRatio = currentFilter!!.previewRatioW
                        val previewHeightRatio = currentFilter!!.previewRatioH

                        val fullWidth = binding.frameCamera.width
                        val fullHeight = binding.frameCamera.height

                        val finalWidth = (fullWidth * previewWidthRatio).toInt()
                        val finalHeight = (fullHeight * previewHeightRatio).toInt()

                        val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, finalWidth, finalHeight, true)

// Step 3: Resize lại frame thành full kích thước
                        val frameBitmap = BitmapFactory.decodeResource(resources, currentFilter!!.img)
                        val resizedFrame = Bitmap.createScaledBitmap(frameBitmap, fullWidth, fullHeight, true)

// Step 4: Ghép ảnh vào chính giữa frame
                        val combined = Bitmap.createBitmap(fullWidth, fullHeight, resizedBitmap.config)
                        val canvas = Canvas(combined)

                        val x = (fullWidth - resizedBitmap.width) / 2f
                        val y = (fullHeight - resizedBitmap.height) / 2f

                        canvas.drawBitmap(resizedBitmap, x, y, null)
                        canvas.drawBitmap(resizedFrame, 0f, 0f, null)

                        binding.btnGallery.setImageBitmap(combined)

                        // Save combined image
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/${Foder_Key}")
                        }

                        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        uri?.let {
                            contentResolver.openOutputStream(it)?.use { out ->
                                combined.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }
                            val uriString = uri.toString()
                            pref.saveUriPhoto(uriString)
//                            Toast.makeText(baseContext, "Đã lưu ảnh DCIM/${Foder_Key}", Toast.LENGTH_SHORT).show()
                            isTaking = false
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
//                        Toast.makeText(baseContext, "Lỗi chụp ảnh", Toast.LENGTH_SHORT).show()
                        Log.e("CameraX", "Capture error", exception)
                        isTaking = false
                    }
                }
            )
        } else {
            // CASE: Không có frame → lưu thẳng vào MediaStore
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val originalBitmap = rotateBitmapIfNeeded(imageProxyToBitmap(imageProxy), rotationDegrees)
                        imageProxy.close()

                        val croppedBitmap = cropBitmapToAspectRatio(originalBitmap, aspectRatio)
                        // Resize về đúng chiều ngang khung preview
                        val resizedBitmap = Bitmap.createScaledBitmap(
                            croppedBitmap,
                            binding.frameCamera.width,
                            binding.frameCamera.height,
                            true
                        )

                        binding.btnGallery.setImageBitmap(resizedBitmap)


                        // Lưu vào MediaStore
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/${Foder_Key}")
                        }

                        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        uri?.let {
                            contentResolver.openOutputStream(it)?.use { out ->
                                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }

                            val uriString = uri.toString()
                            pref.saveUriPhoto(uriString)
//                            Toast.makeText(baseContext, "Đã lưu ảnh vao DCIM/${Foder_Key}", Toast.LENGTH_SHORT).show()
                            isTaking = false
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
//                        Toast.makeText(baseContext, "Lỗi chụp ảnh", Toast.LENGTH_SHORT).show()
                        isTaking = false
                    }
                }
            )

        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val planeProxy = imageProxy.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun updateAspectRatio() {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val newHeight = (screenWidth / aspectRatio).toInt()
        val layoutParams = binding.frameCamera.layoutParams
        layoutParams.height = newHeight
        binding.frameCamera.layoutParams = layoutParams
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val viewsToCheck = listOf(
            binding.llTopBarCamera,
            binding.llSplash,
            binding.llChangeSize,
            binding.llTimer,
            binding.llFilter,
            binding.llGrid,
            binding.llFPS,
            binding.llVideoRung
        )

        if (ev.action == MotionEvent.ACTION_DOWN) {
            var touchedInsideAny = false

            for (view in viewsToCheck) {
                if (view.visibility == View.VISIBLE) {
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        touchedInsideAny = true
                        break
                    }
                }
            }

            if (!touchedInsideAny) {
                setDefaultVisibleLL() // Hàm bạn đã viết
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun startVideoCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(selectedVideoQuality))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, videoCapture
            )

            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
            setupGestureControls()

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun createPreviewWithStabilization(): Preview {
        val previewBuilder = Preview.Builder()

        val camera2Extender = Camera2Interop.Extender(previewBuilder)
        camera2Extender.setCaptureRequestOption(
            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
            CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON
        )

        return previewBuilder.build()
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun startRecording() {

        binding.llTopBarCamera.invisible()
        binding.llBottomBarCamera.invisible()
        setDefaultColorButton().also { binding.btnVideoRung.setColorFilter(ContextCompat.getColor(this, R.color.main)) }
        setVideoRungDefault().also { binding.btnVideoRungOn.setBackgroundResource(R.drawable.bg_choose) }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(selectedVideoQuality))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        val cameraProvider = ProcessCameraProvider.getInstance(this).get()
        cameraProvider.unbindAll()

        val previewBuilder = Preview.Builder()
        if (currentFlashMode == FlashMode.ON) {
            val camera2Extender = Camera2Interop.Extender(previewBuilder)
            camera2Extender.setCaptureRequestOption(
                CaptureRequest.FLASH_MODE,
                CaptureRequest.FLASH_MODE_TORCH
            )
        }

        val preview = createPreviewWithStabilization()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

//        val preview = previewBuilder.build()
//        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val camera = cameraProvider.bindToLifecycle(
            this, cameraSelector, preview, videoCapture
        )

        cameraControl = camera.cameraControl
        cameraInfo = camera.cameraInfo

        if (currentFlashMode == FlashMode.ON) {
            cameraControl.enableTorch(true)
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/${Foder_Key}")
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        recording = videoCapture!!.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(this)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        binding.tvTimerRecord.visible()
                        startTimer()
                    }

                    is VideoRecordEvent.Finalize -> {
                        isRecording = false
                        binding.tvTimerRecord.gone()
                        stopTimer()
                        cameraControl.enableTorch(false) // Tắt flash sau khi quay xong
                    }
                }
            }
    }

    private fun stopRecording() {
        isRecording = false
        binding.llBottomBarCamera.visible()
        binding.llTopBarCamera.visible()
        recording?.stop()
        recording = null
        stopTimer()
    }

    private fun startTimer() {
        secondsElapsed = 0
        binding.tvTimerRecord.visibility = View.VISIBLE

        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = object : Runnable {
            override fun run() {
                val minutes = secondsElapsed / 60
                val seconds = secondsElapsed % 60
                binding.tvTimerRecord.text = String.format("%02d:%02d", minutes, seconds)
                secondsElapsed++
                timerHandler?.postDelayed(this, 1000)
            }
        }
        timerHandler?.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerHandler?.removeCallbacks(timerRunnable!!)
        binding.tvTimerRecord.visibility = View.GONE
    }

    private fun setVideoQuality(quality: Quality) {
        selectedVideoQuality = quality

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo

            } catch (exc: Exception) {
                Log.e("CameraX", "Video binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadLastCapturedImage() {
        val uriString = pref.getUriPhoto()

        if (uriString != null) {
            val uri = Uri.parse(uriString)
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        binding.btnGallery.setImageBitmap(bitmap)
                        return  // Đã load ảnh thành công
                    }
                }
            } catch (e: FileNotFoundException) {
                binding.btnGallery.setImageResource(R.drawable.ic_gallery)
            }
        }

        binding.btnGallery.setImageResource(R.drawable.ic_gallery)
    }

    // hàm xoay anh
    private fun rotateBitmapIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    @SuppressLint("RestrictedApi")
    private fun check4KCamera() : Boolean{
        // Lấy cameraId (0 thường là camera sau, 1 là trước – tuỳ thiết bị)
        val cameraId = cameraSelector.lensFacing.let {
            if (it == CameraSelector.LENS_FACING_BACK) "0" else "1"
        }

        // Kiểm tra profile 2160p
        val supports4k = CamcorderProfile.hasProfile(cameraId.toInt(), CamcorderProfile.QUALITY_2160P)

        if (supports4k) {
            return true
        } else {
            return false
        }
    }

}