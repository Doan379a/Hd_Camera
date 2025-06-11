package com.beautycam.hdcam.photoeditor.ui.collage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.beautycam.hdcam.photoeditor.R
import com.beautycam.hdcam.photoeditor.base.BaseActivity
import com.beautycam.hdcam.photoeditor.base.extensions.getTagDebug
import com.beautycam.hdcam.photoeditor.base.extensions.showToast
import com.beautycam.hdcam.photoeditor.data.dataSource.PuzzleUtils
import com.beautycam.hdcam.photoeditor.data.repository.RepoPuzzleUtils
import com.beautycam.hdcam.photoeditor.databinding.ActivityCollageLayoutsBinding
import com.beautycam.hdcam.photoeditor.domain.UseCasePuzzleLayouts
import com.beautycam.hdcam.photoeditor.ui.main.MainActivity
import com.beautycam.hdcam.photoeditor.ui.save.SaveActivity
import com.beautycam.hdcam.photoeditor.utils.CameraUtils.Foder_Key
import com.beautycam.hdcam.photoeditor.viewmodels.ViewModelPuzzleLayouts
import com.beautycam.hdcam.photoeditor.viewmodels.ViewModelPuzzleLayoutsProvider
import com.beautycam.hdcam.photoeditor.widget.gone
import com.beautycam.hdcam.photoeditor.widget.tap
import com.beautycam.hdcam.photoeditor.widget.visible
import com.hypersoft.pzlayout.interfaces.PuzzleLayout
import com.hypersoft.pzlayout.utils.PuzzlePiece
import com.hypersoft.pzlayout.view.PuzzleView
import gun0912.tedimagepicker.util.ToastUtil.showToast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CollageLayoutsActivity :
    BaseActivity<ActivityCollageLayoutsBinding>(),
    PuzzleView.OnPieceClick, PuzzleView.OnPieceSelectedListener {

    private val puzzleLayout by lazy { PuzzleUtils() }
    private val repoPuzzleUtils by lazy { RepoPuzzleUtils(puzzleLayout) }
    private val useCasePuzzleLayouts by lazy { UseCasePuzzleLayouts(repoPuzzleUtils) }
    private val viewModelPuzzleLayouts by viewModels<ViewModelPuzzleLayouts> {
        ViewModelPuzzleLayoutsProvider(
            useCasePuzzleLayouts
        )
    }
    private val collageLayoutsAdapter by lazy { CollageLayoutsAdapter(itemClick) }

    private var theme: Int = 0
    private var mList: List<Uri> = emptyList()

    private val itemClick: ((PuzzleLayout, theme: Int) -> Unit) = { puzzleLayout, theme ->
        viewModelPuzzleLayouts.getPuzzleLayout(1, mList.size, theme)
    }

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                val path = uri.toString()
                binding.puzzleView.replace(bitmap, path)
                if (binding.puzzleView.handlingPiecePosition != -1) {
                    val position = binding.puzzleView.handlingPiecePosition
                    mList = mList.toMutableList().apply {
                        set(position, uri)
                    }
                }
            }
        }

    override fun setViewBinding(): ActivityCollageLayoutsBinding {
        return ActivityCollageLayoutsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        mList = intent.getParcelableArrayListExtra("LIST_IMAGE") ?: emptyList()
        theme = intent.getIntExtra("theme", 0)

        Log.d("DOAN_1", "$mList")
        initObservers()
        setupListeners()
        initRecyclerView()
        initListener()
    }

    override fun viewListener() {

    }

    override fun dataObservable() {

    }


    private fun initRecyclerView() {
        binding.rcvListPuzzleLayouts.adapter = collageLayoutsAdapter
    }

    private fun initObservers() {
        fetchLayouts(mList)
        checkImageSizeAndSetLayouts(mList)
        viewModelPuzzleLayouts.puzzleLayoutLiveData.observe(this) { list ->
            initView(list)
        }
        viewModelPuzzleLayouts.puzzleLayoutsLiveData.observe(this) { list ->
            collageLayoutsAdapter.setPuzzleLayouts(list)
        }
    }

    private fun setupListeners() = binding.apply {

        pmirror.setOnClickListener { mirror() }
        pflip.setOnClickListener { flip() }
        protate.setOnClickListener { rotate() }
        pzoomplus.setOnClickListener { zoomPlus() }
        pzoomminus.setOnClickListener { zoomMinus() }
        pleft.setOnClickListener { left() }
        pright.setOnClickListener { right() }
        pup.setOnClickListener { up() }
        pdown.setOnClickListener { down() }
        btnCorner.setOnClickListener { corner() }
        pchange.setOnClickListener { change() }
        imgDowload.setOnClickListener {
            var fileLink = savePuzzleViewToFile(binding.puzzleView)
            val intent = Intent(this@CollageLayoutsActivity,SaveActivity::class.java).apply {
                putExtra("keyActivity","CollageLayoutsActivity")
                putExtra("linkPath",fileLink)
            }
            startActivity(intent)
        }
        imgBack.tap {
            onBackPressed()
        }
    }

    private fun initListener() = binding.apply {
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.puzzleView.setPieceRadian(progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun fetchLayouts(it: List<Uri>) {
        viewModelPuzzleLayouts.getPuzzleLayouts(it.size)
    }

    private fun checkImageSizeAndSetLayouts(it: List<Uri>) {
        when (it.size) {
            1 -> {
                val selected = it[0]
                val imageList = listOf(selected, selected)
                viewModelPuzzleLayouts.getPuzzleLayout(1, imageList.size, 0)
            }

            2 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            3 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            4 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            5 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            6 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            7 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            8 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            9 -> {
                viewModelPuzzleLayouts.getPuzzleLayout(1, it.size, 0)
            }

            else -> {

            }
        }
    }

    //    private fun initView(list: PuzzleLayout) {
//        binding.puzzleView.apply {
//            setPuzzleLayout(list)
//
//            isTouchEnable = true
//            needDrawLine = false///Có vẽ đường phân cách mảnh không?
//            needDrawOuterLine = false//Có vẽ đường viền ngoài toàn bộ layout không?
//            lineSize = 10//	Độ dày đường viền các mảnh ghép (line stroke)
//            selectedLineColor = Color.RED//	Màu viền khi chọn mảnh
//            lineColor = Color.BLACK//Màu của đường viền các mảnh
//            selectedLineColor = ContextCompat.getColor(context, R.color.black)
//            setAnimateDuration(700)
//            piecePadding = 1f//	Khoảng cách giữa các mảnh
//            setOnPieceClickListener(this@FragmentLayoutWithImages)
//            setOnPieceSelectedListener(this@FragmentLayoutWithImages)
//
//            post {
//                loadPhotoFromRes(list)
//            }
//        }
//    }
    private fun initView(list: PuzzleLayout) {
        binding.puzzleView.apply {
            setPuzzleLayout(list)

            isTouchEnable = true
            needDrawLine = false
            needDrawOuterLine = false
            lineSize = 6
            lineColor = Color.RED
            selectedLineColor = Color.parseColor("#C5CFFF")
            setHandleBarColor(
                ContextCompat.getColor(
                    context,
                    gun0912.tedimagepicker.R.color.ted_image_picker_primary
                )
            )
            setAnimateDuration(700)
            piecePadding = 5f
            setOnPieceClickListener(this@CollageLayoutsActivity)
            setOnPieceSelectedListener(this@CollageLayoutsActivity)

            post {
                loadPhotoFromRes(list)
            }
        }
    }

    private fun loadPhotoFromRes(list: PuzzleLayout) {
        val pieces: MutableList<Bitmap> = ArrayList()
        val count = if (mList.size > list.areaCount) list.areaCount else mList.size
        for (i in 0 until count) {
            val target = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
                    pieces.add(bitmap)
                    if (pieces.size == count) {
                        if (mList.size < list.areaCount) {
                            for (q in 0 until list.areaCount) {
                                binding.puzzleView.addPiece(pieces[i % count])
                            }
                        } else {
                            binding.puzzleView.addPieces(pieces)
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {}
            }

            Glide.with(this).asBitmap().load(mList[i]).into(target)
        }
    }

    private fun mirror() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.mirrorPiece()
        else showToast(R.string.selectsingleimage)
    }

    private fun flip() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.flipPiece()
        else showToast(R.string.selectsingleimage)
    }

    private fun rotate() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.rotatePiece()
        else showToast(R.string.selectsingleimage)
    }

    private fun zoomPlus() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.zoomInPiece()
        else showToast(R.string.selectsingleimage)
    }

    private fun zoomMinus() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.zoomOutPiece()
        else showToast(R.string.selectsingleimage)
    }

    private fun left() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.moveLeft()
        else showToast(R.string.selectsingleimage)
    }

    private fun right() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.moveRight()
        else showToast(R.string.selectsingleimage)
    }

    private fun up() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.moveUp()
        else showToast(R.string.selectsingleimage)
    }

    private fun down() = binding.apply {
        seekbar.gone()
        if (puzzleView.handlingPiecePosition != -1) puzzleView.moveDown()
        else showToast(R.string.selectsingleimage)
    }

    private fun corner() = binding.apply {
        seekbar.visible()
        seekbar.max = 100
        seekbar.progress = puzzleView.getPieceRadian().toInt()
    }

    private fun change() = binding.apply {
        if (puzzleView.handlingPiecePosition != -1) {
            pickImages.launch("image/*")
        } else {
            showToast(R.string.selectsingleimage)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent=Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
    override fun onPieceClick() {}
    override fun onSwapGetPositions(pos1: Int, pos2: Int) {}
    override fun onPieceSelected(piece: PuzzlePiece?, position: Int) {}

    private fun savePuzzleViewToFile(puzzleView: PuzzleView): String? {
        puzzleView.clearHandlingPieces()

        val bitmap =
            Bitmap.createBitmap(puzzleView.width, puzzleView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        puzzleView.draw(canvas)

        val fileName = "collage_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "DCIM/$Foder_Key"
            )
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                showToast("Saved to HD_camera")
                return it.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("DEBUG", "Error saving image: ${e.message}")
            }
        }

        return null
    }


}
