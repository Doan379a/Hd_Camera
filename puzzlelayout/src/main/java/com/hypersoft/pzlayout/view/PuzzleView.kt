package com.hypersoft.pzlayout.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.hypersoft.pzlayout.R
import com.hypersoft.pzlayout.interfaces.Area
import com.hypersoft.pzlayout.interfaces.Line
import com.hypersoft.pzlayout.interfaces.PuzzleLayout
import com.hypersoft.pzlayout.utils.MatrixUtils.generateMatrix
import com.hypersoft.pzlayout.utils.PuzzleLayoutParser.parse
import com.hypersoft.pzlayout.utils.PuzzlePiece
import kotlin.math.abs
import kotlin.math.sqrt



@Suppress("unused")
open class PuzzleView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    // Enum representing different action modes the view can be in
    private enum class ActionMode {
        NONE, DRAG, ZOOM, MOVE, SWAP
    }

    // Size of the lines separating the puzzle pieces
    @JvmField
    var lineSize = 0

    // Boolean flag to indicate whether lines should be drawn inside the puzzle
    @JvmField
    var needDrawLine = false

    // Boolean flag to indicate whether the outer lines should be drawn
    @JvmField
    var needDrawOuterLine = false

    // Flag to enable or disable touch interactions
    @JvmField
    var isTouchEnable: Boolean = true

    // Color for the lines separating the puzzle pieces
    @JvmField
    var lineColor = 0

    // Color for the lines of the selected puzzle piece
    @JvmField
    var selectedLineColor = 0

    // Padding around each puzzle piece
    @JvmField
    var piecePadding = 0f

    // The current mode of the puzzle interaction (NONE, DRAG, ZOOM, MOVE, SWAP)
    private var currentMode = ActionMode.NONE

    // List of all puzzle pieces
    private val puzzlePieces: MutableList<PuzzlePiece> = ArrayList()

    // List of pieces that need to be changed when moving lines
    private val needChangePieces: MutableList<PuzzlePiece> = ArrayList()

    // Mapping between puzzle areas and their corresponding pieces
    private val areaPieceMap: MutableMap<Area, PuzzlePiece> = HashMap()

    // The current puzzle layout (holds the configuration of pieces)
    private var puzzleLayout: PuzzleLayout? = null

    // Initial information for restoring puzzle state (if applicable)
    private var initialInfo: PuzzleLayout.Info? = null

    // The bounds (rectangular area) of the puzzle
    private var bounds: RectF? = null

    // Animation duration for transitions like swaps and moves
    private var duration = 0

    // The currently selected line for movement
    private var handlingLine: Line? = null

    // The currently selected puzzle piece
    private var handlingPiece: PuzzlePiece? = null

    // The piece that is about to be swapped with the current piece
    private var replacePiece: PuzzlePiece? = null

    // The previously selected piece (used for comparison)
    private var previousHandlingPiece: PuzzlePiece? = null

    // Paint object for drawing lines between puzzle pieces
    private var linePaint: Paint? = null

    // Paint object for drawing the selected puzzle piece's area
    private var selectedAreaPaint: Paint? = null

    // Paint object for drawing the handle bars (for line movement)
    private var handleBarPaint: Paint? = null

    // Down X and Y coordinates when a touch event starts
    private var downX = 0f
    private var downY = 0f

    // Previous distance between two fingers for zoom gesture
    private var previousDistance = 0f

    // The mid-point between two fingers for zooming
    private var midPoint: PointF? = null

    // Color of the handle bars
    private var handleBarColor = 0

    // Radius of the corners for puzzle pieces
    private var pieceRadian = 0f

    // Flag to reset piece matrix when necessary
    private var needResetPieceMatrix = true

    // Flag for quick mode (optimizing performance)
    private var quickMode = false

    // Control flags for enabling/disabling drag, move, zoom, swap actions
    private var canDrag = true
    private var canMoveLine = true
    private var canZoom = true
    private var canSwap = true

    // The current puzzle piece being interacted with
    private var currentPiece: PuzzlePiece? = null

    // Matrices for handling transformations like zoom and drag
    private val matrix: Matrix? = null
    private val previousMatrix: Matrix? = null
    private val tempMatrix: Matrix? = null

    // Boolean to detect if swipe is activated
    private var swipeActivated: Boolean = false

    // Temporary drawable object for storing swap piece
    private var temp: Drawable? = null

    // Path for storing the image path of the piece being swapped
    private var tempPath: String? = null

    // Temporary reference to the piece being swapped
    private var swipeTempPiece: PuzzlePiece? = null

    // Listener for puzzle piece selection events
    private var onPieceSelectedListener: OnPieceSelectedListener? = null

    // Listener for puzzle piece click events
    private var onPieceClick: OnPieceClick? = null

    // Temporary position variables for handling swaps
    private var tempPathPos = -1
    private var tempPath2Pos = -1

    // List of pieces to move during line movement
    private val toMovePuzzlePieces: MutableList<PuzzlePiece?> = ArrayList()

    // Reference to the last piece that was handled
    private var lastHandlingPiece: PuzzlePiece? = null

    // Flag to ensure size is changed once
    private var sizeChangedOnce = false

    // Flag to reset the puzzle
    private var reset = true

    // List of pieces that have had editing applied
    private val pieceAppliedEditing: MutableList<PuzzlePiece?> = ArrayList()
    private lateinit var handleBarDrawable: Drawable
    private val HANDLE_BAR_WIDTH = 100
    private val HANDLE_BAR_HEIGHT =40

    // Switches the current mode to SWAP after checking if swapping is allowed
    private val switchToSwapAction = Runnable {
        if (!canSwap) return@Runnable
        currentMode = ActionMode.SWAP
        invalidate()  // Redraw the view to update the mode change
    }

    // Initialize with context and attributes
    init {
        context?.let { init(it, attrs) }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PuzzleView)

        // Get line size and colors from XML attributes, with default values if not set
        lineSize = ta.getInt(R.styleable.PuzzleView_line_size, 4)
        lineColor = ta.getColor(R.styleable.PuzzleView_line_color, Color.WHITE)
        selectedLineColor = ta.getColor(R.styleable.PuzzleView_selected_line_color, Color.parseColor("#99BBFB"))
        handleBarColor = ta.getColor(R.styleable.PuzzleView_handle_bar_color, Color.parseColor("#99BBFB"))

        // Set other view properties like piece padding, drawing lines, animation duration, etc.
        piecePadding = ta.getDimensionPixelSize(R.styleable.PuzzleView_piece_padding, 0).toFloat()
        needDrawLine = ta.getBoolean(R.styleable.PuzzleView_need_draw_line, false)
        needDrawOuterLine = ta.getBoolean(R.styleable.PuzzleView_need_draw_outer_line, false)
        duration = ta.getInt(R.styleable.PuzzleView_animation_duration, 300)
        pieceRadian = ta.getFloat(R.styleable.PuzzleView_radian, 0f)

        // Recycle TypedArray to free up resources
        ta.recycle()

        bounds = RectF()

        // Initialize paint objects for lines and handling bars
        linePaint = Paint().apply {
            isAntiAlias = true
            color = lineColor
            strokeWidth = lineSize.toFloat()
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.SQUARE
        }

        selectedAreaPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = selectedLineColor
            strokeWidth = lineSize.toFloat()
        }

        handleBarPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = handleBarColor
            strokeWidth = (lineSize * 3).toFloat()
        }
        handleBarDrawable = ContextCompat.getDrawable(context, R.drawable.ic_select_img)!!
        midPoint = PointF()  // PointF used for calculating midpoints for zooming
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d("onSizeChanged ", "Called $sizeChangedOnce")
        super.onSizeChanged(w, h, oldw, oldh)
        resetPuzzleBounds()  // Adjust the puzzle bounds based on the new dimensions
        areaPieceMap.clear()

        // Re-position puzzle pieces based on their areas if the layout contains pieces
        if (puzzlePieces.isNotEmpty()) {
            for (i in puzzlePieces.indices) {
                val piece = puzzlePieces[i]
                val area = puzzleLayout?.getArea(i)
                area?.let {
                    piece.area = it
                    areaPieceMap[it] = piece
                    if (needResetPieceMatrix) {
                        if (pieceAppliedEditing.contains(piece)) {
                            Log.d("SamePieceDetected", "onSizeChanged: ")
                        } else {
                            piece.set(generateMatrix(piece, 0f))  // Generate matrix for piece position
                        }
                    } else {
                        piece.fillArea(this, true)  // Fill the area with the puzzle piece
                    }
                }
            }
        }
        invalidate()  // Redraw the view after changes
        if (reset) {
            sizeChangedOnce = false
        }
    }


    fun doNotAutoResetFalse() {
        reset = false
    }


    fun resetAutoFalse() {
        reset = true
    }


    private fun resetPuzzleBounds() {
        Log.d("Debugging ", "resetPuzzleBounds")

        // Set the outer bounds of the puzzle based on padding and view dimensions
        bounds?.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat()
        )

        puzzleLayout?.let { layout ->
            layout.reset()  // Reset the layout
            bounds?.let { layout.setOuterBounds(it) }
            layout.layout()  // Layout the puzzle areas again
            layout.padding = piecePadding
            layout.radian = pieceRadian

            // Restore the positions of lines from saved info
            initialInfo?.lineInfos?.forEachIndexed { i, lineInfo ->
                layout.lines[i].apply {
                    startPoint().set(lineInfo.startX, lineInfo.startY)
                    endPoint().set(lineInfo.endX, lineInfo.endY)
                }
            }

            layout.sortAreas()  // Sort the areas based on the new layout
            layout.update()  // Update the layout with new changes
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d("Debugging ", "onDraw")

        if (puzzleLayout == null) {
            return  // Exit if no puzzle layout is set
        }

        // Set the stroke widths for drawing elements
        linePaint?.strokeWidth = lineSize.toFloat()
        selectedAreaPaint?.strokeWidth = lineSize.toFloat()
        handleBarPaint?.strokeWidth = (lineSize * 3).toFloat()

        // Draw each puzzle piece in the layout
        puzzleLayout?.areaCount?.let { areaCount ->
            for (i in 0 until areaCount) {
                if (i >= puzzlePieces.size) break
                val piece = puzzlePieces[i]

                // Skip drawing if the piece is being swapped
                if (piece == handlingPiece && currentMode == ActionMode.SWAP) continue

                // Draw the piece on the canvas
                piece.draw(canvas, quickMode)
            }
        }

        // Draw outer bounds if enabled
        if (needDrawOuterLine) {
            puzzleLayout?.outerLines?.forEach { outerLine ->
                drawLine(canvas, outerLine)  // Draw outer lines
            }
        }

        // Draw slant lines inside the puzzle layout if enabled
        if (needDrawLine) {
            puzzleLayout?.lines?.forEach { line ->
                drawLine(canvas, line)  // Draw inner lines
            }
        }

        // Draw the selected piece's area and handle bars when not in swap mode
        handlingPiece?.let {
            if (currentMode != ActionMode.SWAP) {
                drawSelectedArea(canvas, it)  // Highlight selected piece's area
            }
        }

        // Draw the piece being swapped and the area to be swapped with, if in swap mode
        handlingPiece?.let {
            if (currentMode == ActionMode.SWAP) {
                it.draw(canvas, 128, quickMode)  // Draw with lower opacity
                replacePiece?.let { rp -> drawSelectedArea(canvas, rp) }
            }
        }
    }


    private fun drawSelectedArea(canvas: Canvas, piece: PuzzlePiece) {
        Log.d("Debugging ", "drawSelectedArea")

        val area = piece.area

        // Draw the outline of the selected area
        selectedAreaPaint?.let { canvas.drawPath(area.areaPath, it) }

        // Draw handle bars for resizing/moving the area
        area.lines.forEach { line ->
            if (puzzleLayout?.lines?.contains(line) == true) {
                val handleBarPoints = area.getHandleBarPoints(line)

                // Draw handle bars at the ends of the lines
//                handleBarPaint?.let {
//                    canvas.drawLine(handleBarPoints[0].x, handleBarPoints[0].y, handleBarPoints[1].x, handleBarPoints[1].y, it)
//                    canvas.drawCircle(handleBarPoints[0].x, handleBarPoints[0].y, (lineSize * 3 / 2).toFloat(), it)
//                    canvas.drawCircle(handleBarPoints[1].x, handleBarPoints[1].y, (lineSize * 3 / 2).toFloat(), it)
//                }

                val startX = handleBarPoints[0].x
                val startY = handleBarPoints[0].y
                val endX = handleBarPoints[1].x
                val endY = handleBarPoints[1].y

                val centerX = (startX + endX) / 2
                val centerY = (startY + endY) / 2

                // Tính toán góc nghiêng
                val angle = Math.toDegrees(Math.atan2((endY - startY).toDouble(), (endX - startX).toDouble())).toFloat()

                // Lưu trạng thái Canvas
                canvas.save()

                // Di chuyển Canvas đến vị trí center
                canvas.translate(centerX, centerY)

                // Xoay Canvas theo góc
                canvas.rotate(angle)

                // Thiết lập kích thước Handle Bar (50x24)
                val left = -HANDLE_BAR_WIDTH / 2
                val top = -HANDLE_BAR_HEIGHT / 2
                val right = HANDLE_BAR_WIDTH / 2
                val bottom = HANDLE_BAR_HEIGHT / 2

                handleBarDrawable.setBounds(left, top, right, bottom)

                // Vẽ Handle Bar
                handleBarDrawable.draw(canvas)

                // Khôi phục Canvas về trạng thái ban đầu
                canvas.restore()
            }
        }
    }


    private fun drawLine(canvas: Canvas, line: Line) {
        Log.d("Debugging ", "drawLine")

        // Draw the line on the canvas
        linePaint?.let {
            canvas.drawLine(line.startPoint().x, line.startPoint().y, line.endPoint().x, line.endPoint().y, it)
        }
    }


    fun setPuzzleLayout(puzzleLayout: PuzzleLayout) {
        clearPieces() // Clear current pieces

        this.puzzleLayout = puzzleLayout

        bounds?.let { puzzleLayout.setOuterBounds(it) } // Set the outer bounds for the layout
        puzzleLayout.layout() // Layout the puzzle pieces

        invalidate() // Redraw the view
    }


    fun setPuzzleLayout(info: PuzzleLayout.Info) {
        this.initialInfo = info
        clearPieces() // Clear current pieces

        this.puzzleLayout = parse(info) // Parse the info into a new PuzzleLayout

        // Apply info settings
        this.piecePadding = info.padding
        this.pieceRadian = info.radian
        setBackgroundColor(info.color) // Set background color from info

        invalidate() // Redraw the view
    }


    fun getPuzzleLayout(): PuzzleLayout? {
        return puzzleLayout
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isTouchEnable) {
            return super.onTouchEvent(event)
        }
        gestureDetector.onTouchEvent(event)
        currentPiece = findHandlingPiece() // Find the piece being handled
        //
        pieceAppliedEditing.add(currentPiece) // Add piece to editing set
        //
        lastHandlingPiece = currentPiece
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Handle single-touch start
                downX = event.x
                downY = event.y

                decideActionMode(event) // Decide the action based on touch
                prepareAction() // Prepare the piece for action (drag, zoom, etc.)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                // Handle multi-touch start
                previousDistance = calculateDistance(event) // Calculate initial zoom distance
                calculateMidPoint(event, midPoint) // Calculate midpoint for zooming

                decideActionMode(event) // Decide action mode for multi-touch
            }

            MotionEvent.ACTION_MOVE -> {
                // Handle touch movement
                performAction(event) // Perform drag, zoom, etc.

                // Cancel swap mode if there is a significant movement
                if ((abs((event.x - downX).toDouble()) > 10 || abs((event.y - downY).toDouble()) > 10)
                    && currentMode != ActionMode.SWAP
                ) {
                    removeCallbacks(switchToSwapAction)
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                // Handle touch end
                finishAction(event) // Finalize the action performed
                currentMode = ActionMode.NONE // Reset the action mode
                removeCallbacks(switchToSwapAction) // Remove pending swap action
            }
        }
        invalidate() // Redraw the view
        return true
    }


    private fun decideActionMode(event: MotionEvent) {
        for (piece in puzzlePieces) {
            if (piece.isAnimateRunning) {
                currentMode = ActionMode.NONE // Disable actions if animation is running
                return
            }
        }

        if (event.pointerCount == 1) {
            // Single touch: Check if a piece or line is being handled
            handlingLine = findHandlingLine()
            if (handlingLine != null && canMoveLine) {
                currentMode = ActionMode.MOVE // Move a line
            } else {
                handlingPiece = findHandlingPiece() // Find the touched piece

                if (handlingPiece != null && canDrag) {
                    currentMode = ActionMode.DRAG // Drag a piece

                    postDelayed(switchToSwapAction, 500) // Switch to swap mode after delay
                }
            }
        } else if (event.pointerCount > 1) {
            // Multi-touch: Check if zooming is possible
            if (handlingPiece != null && handlingPiece?.contains(event.getX(1), event.getY(1)) == true && currentMode == ActionMode.DRAG && canZoom) {
                currentMode = ActionMode.ZOOM // Zoom a piece
            }
        }
    }


    private fun prepareAction() {
        when (currentMode) {
            ActionMode.NONE -> {}
            ActionMode.DRAG -> handlingPiece?.record() // Record initial state for dragging
            ActionMode.ZOOM -> handlingPiece?.record() // Record initial state for zooming
            ActionMode.MOVE -> {
                handlingLine?.prepareMove() // Prepare line for movement
                needChangePieces.clear() // Clear pieces that need changes
                needChangePieces.addAll(findNeedChangedPieces()) // Find pieces affected by the move
                for (piece in needChangePieces) {
                    piece.record() // Record their initial state
                    piece.setPreviousMoveX(downX)
                    piece.setPreviousMoveY(downY)
                }
            }

            else -> {}
        }
    }

    private fun performAction(event: MotionEvent) {
        when (currentMode) {
            ActionMode.NONE -> {}
            ActionMode.DRAG -> dragPiece(handlingPiece, event) // Drag a piece
            ActionMode.ZOOM -> zoomPiece(handlingPiece, event) // Zoom a piece
            ActionMode.SWAP -> {
                dragPiece(handlingPiece, event) // Drag a piece during swap mode
                replacePiece = findReplacePiece(event) // Find the piece to swap with
            }

            ActionMode.MOVE -> moveLine(handlingLine, event) // Move a line
        }
    }

    /**
     * Finishes the action (drag, zoom, swap) when the touch event ends, applying the final transformations
     * or swapping pieces, and triggering any piece selection listeners.
     */
    private fun finishAction(event: MotionEvent) {
        when (currentMode) {
            ActionMode.NONE -> {}
            ActionMode.DRAG -> {
                // Handle the end of a drag event
                if (previousHandlingPiece == handlingPiece && abs((downX - event.x).toDouble()) < 3 && abs((downY - event.y).toDouble()) < 3) {
                    handlingPiece = null // Deselect piece if the movement was minimal
                }
                previousHandlingPiece = handlingPiece
            }

            ActionMode.ZOOM -> {
                // Handle the end of a zoom event
                handlingPiece?.let {
                    if (!it.isFilledArea) {
                        if (it.canFilledArea()) {
                            it.moveToFillArea(this) // Fill the piece into its area
                        }
                    }
                    previousHandlingPiece = it
                }
            }

            ActionMode.MOVE -> {}
            ActionMode.SWAP -> if (handlingPiece != null && replacePiece != null) {
                swapPiece() // Swap the pieces
                handlingPiece = null
                replacePiece = null
                previousHandlingPiece = null
            }
        }

        // Trigger the piece selection listener, if available
        if (handlingPiece != null && onPieceSelectedListener != null) {
            onPieceSelectedListener?.onPieceSelected(
                handlingPiece,
                puzzlePieces.indexOf(handlingPiece)
            )
        }

        handlingLine = null // Reset handling line
        needChangePieces.clear() // Clear changed pieces list
    }

    private fun swapPiece() {
        handlingPiece?.let {
            val temp = it.getDrawable()
            val tempPath = it.path
            replacePiece?.let { rp ->
                rp.getDrawable().let { it1 -> it.setDrawable(it1) } // Swap drawable
                it.path = rp.path
            }
            replacePiece?.setDrawable(temp) // Set swapped drawable
            replacePiece?.path = tempPath

            it.fillArea(this, true) // Fill pieces into their areas
            replacePiece?.fillArea(this, true)

            tempPathPos = handlingPiecePosition
            onPieceClick?.onSwapGetPositions(tempPathPos, tempPath2Pos) // Trigger swap listener
        }
    }


    private fun moveLine(line: Line?, event: MotionEvent?) {
        if (line == null || event == null) return
        val needUpdate = if (line.direction() == Line.Direction.HORIZONTAL) {
            line.move(event.y - downY, 80f) // Move line vertically
        } else {
            line.move(event.x - downX, 80f) // Move line horizontally
        }
        if (needUpdate) {
            puzzleLayout?.update() // Update layout after line movement
            puzzleLayout?.sortAreas() // Sort the puzzle areas
            updatePiecesInArea(line, event) // Update pieces affected by the movement
        }
    }


    private fun updatePiecesInArea(line: Line, event: MotionEvent) {
        for (i in needChangePieces.indices) {
            needChangePieces[i].updateWith(event, line) // Update each piece with the new line position
        }
    }


    private fun zoomPiece(piece: PuzzlePiece?, event: MotionEvent?) {
        if (piece == null || event == null || event.pointerCount < 2) return
        val scale = calculateDistance(event) / previousDistance
        val matrixValues = FloatArray(9) // Matrix values array
        piece.matrix.getValues(matrixValues)
        var zoomMinMax = matrixValues[0] // Extract scaling factor
        if (abs(zoomMinMax.toDouble()).toInt() == 0) {
            zoomMinMax = matrixValues[1]
        }
        // Apply zoom with constraints
        if (abs(zoomMinMax.toDouble()) < 0.3) {
            if (scale > 1) {
                midPoint?.let { piece.zoomAndTranslate(scale, scale, it, event.x - downX, event.y - downY) }
            }
        } else if (abs(zoomMinMax.toDouble()) > 4.5) {
            if (scale < 1) {
                midPoint?.let { piece.zoomAndTranslate(scale, scale, it, event.x - downX, event.y - downY) }
            }
        } else {
            midPoint?.let { piece.zoomAndTranslate(scale, scale, it, event.x - downX, event.y - downY) }
        }
    }


    private fun dragPiece(piece: PuzzlePiece?, event: MotionEvent?) {
        if (piece == null || event == null) return
        piece.translate(event.x - downX, event.y - downY) // Translate piece
        toMovePuzzlePieces.add(handlingPiece) // Add to the list of moving pieces
    }


    fun replace(bitmap: Bitmap?, path: String?) {
        val bitmapDrawable = BitmapDrawable(resources, bitmap)
        bitmapDrawable.setAntiAlias(true)
        bitmapDrawable.isFilterBitmap = true

        replace(bitmapDrawable, path) // Replace the piece with new content
    }

    fun replace(bitmapDrawable: Drawable?, path: String?) {
        if (handlingPiece == null) {
            return
        }

        handlingPiece?.path = path ?: return
        handlingPiece?.setDrawable(bitmapDrawable ?: return)
        handlingPiece?.set(generateMatrix(handlingPiece ?: return, 0f))

        invalidate() // Redraw the view with the updated content
    }


    fun flipVertically() {
        if (handlingPiece == null) {
            return
        }
        handlingPiece?.postFlipVertically() // Apply vertical flip transformation
        handlingPiece?.record() // Record the piece's new state

        invalidate() // Redraw the view
    }

    fun flipHorizontally() {
        if (handlingPiece == null) {
            return
        }
        handlingPiece?.postFlipHorizontally() // Apply horizontal flip transformation
        handlingPiece?.record() // Record the piece's new state

        invalidate() // Redraw the view
    }


    fun rotate(degree: Float) {
        if (handlingPiece != null) {
            handlingPiece?.postRotate(degree) // Apply rotation transformation
            handlingPiece?.record() // Record the piece's new state
            invalidate() // Redraw the view
        } else {
            handlingPiece = puzzlePieces[0] // Select the first piece if none is selected
            handlingPiece?.postRotate(degree) // Apply rotation transformation
            handlingPiece?.record() // Record the piece's new state
            invalidate() // Redraw the view
        }
    }


    private fun findHandlingPiece(): PuzzlePiece? {
        for (piece in puzzlePieces) {
            if (piece.contains(downX, downY)) {
                return piece // Return the piece if the touch point is inside its bounds
            }
        }
        return null // No piece found at the touch location
    }


    private fun findHandlingLine(): Line? {
        puzzleLayout?.let {
            for (line in it.lines) {
                if (line.contains(downX, downY, 40f)) {
                    return line // Return the line if the touch point is near it
                }
            }
        }
        return null // No line found at the touch location
    }


    private fun findReplacePiece(event: MotionEvent): PuzzlePiece? {
        tempPath2Pos = handlingPiecePosition // Store the current handling piece's position
        for (piece in puzzlePieces) {
            if (piece.contains(event.x, event.y)) {
                return piece // Return the piece if the touch point is inside its bounds
            }
        }
        return null // No piece found at the touch location
    }


    private fun findNeedChangedPieces(): List<PuzzlePiece> {
        if (handlingLine == null) return ArrayList()

        val needChanged: MutableList<PuzzlePiece> = ArrayList()
        for (piece in puzzlePieces) {
            if (piece.contains(handlingLine)) {
                needChanged.add(piece) // Add the piece if it is affected by the line's movement
            }
        }

        return needChanged
    }


    private fun calculateDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1) // Difference in x coordinates
        val y = event.getY(0) - event.getY(1) // Difference in y coordinates

        return sqrt((x * x + y * y).toDouble()).toFloat() // Return the distance between the two points
    }


    private fun calculateMidPoint(event: MotionEvent, point: PointF?) {
        point?.x = (event.getX(0) + event.getX(1)) / 2 // Average of x coordinates
        point?.y = (event.getY(0) + event.getY(1)) / 2 // Average of y coordinates
    }


    fun reset() {
        clearPieces() // Clear all pieces
        if (puzzleLayout != null) {
            puzzleLayout?.reset() // Reset the layout to its initial state
        }
    }


    fun clearPieces() {
        clearHandlingPieces() // Clear the currently selected pieces and lines
        puzzlePieces.clear() // Remove all pieces from the list

        invalidate() // Redraw the view
    }


    fun clearHandlingPieces() {
        handlingLine = null
        handlingPiece = null
        replacePiece = null
        needChangePieces.clear() // Clear the list of pieces that need changes

        invalidate() // Redraw the view
    }
     fun addPieces(bitmaps: List<Bitmap?>) {
        for (bitmap in bitmaps) {
            addPiece(bitmap) // Add each bitmap as a piece
        }

        postInvalidate() // Redraw the view
    }


    fun addDrawablePieces(drawables: List<Drawable?>) {
        for (drawable in drawables) {
            addPiece(drawable) // Add each drawable as a piece
        }

        postInvalidate() // Redraw the view
    }


    fun addPiece(bitmap: Bitmap?) {
        val bitmapDrawable = BitmapDrawable(resources, bitmap)
        bitmapDrawable.setAntiAlias(true) // Enable anti-aliasing for smooth edges
        bitmapDrawable.isFilterBitmap = true // Enable filtering for scaled bitmaps

        addPiece(bitmapDrawable, null) // Add the bitmap as a drawable piece
    }


    @JvmOverloads
    fun addPiece(bitmap: Bitmap?, initialMatrix: Matrix?, path: String? = "") {
        val bitmapDrawable = BitmapDrawable(resources, bitmap)
        bitmapDrawable.setAntiAlias(true)
        bitmapDrawable.isFilterBitmap = true

        addPiece(bitmapDrawable, initialMatrix, path) // Add the bitmap as a drawable piece with the optional matrix and path
    }

    @JvmOverloads
    fun addPiece(drawable: Drawable?, initialMatrix: Matrix? = null, path: String? = "") {
        val position = puzzlePieces.size

        puzzleLayout?.let {
            if (position >= it.areaCount) {
                Log.e(
                    TAG, "addPiece: can not add more. the current puzzle layout can contains "
                            + it.areaCount
                            + " puzzle piece."
                )
                return
            }
        }
        val area = puzzleLayout?.getArea(position) // Get the area for the new piece
        area?.setPadding(piecePadding) // Set padding for the piece area

        // Create a new puzzle piece with the provided drawable, area, and an empty matrix
        val piece = PuzzlePiece(drawable ?: return, area ?: return, Matrix())

        // Set the initial matrix for the piece or generate a default one
        val matrix = if (initialMatrix != null) Matrix(initialMatrix) else generateMatrix(area, drawable, 0f)
        piece.set(matrix)

        piece.setAnimateDuration(duration) // Set the animation duration for the piece
        piece.path = path ?: return // Set the path for the piece

        puzzlePieces.add(piece) // Add the piece to the list of puzzle pieces
        areaPieceMap[area] = piece // Map the piece to its area

        setPiecePadding(piecePadding) // Apply the current piece padding
        setPieceRadian(pieceRadian) // Apply the current piece radian (corner radius)

        invalidate() // Redraw the view
    }

    /**
     * Selects the puzzle piece at the given position. If the position is valid,
     * it assigns the piece to `handlingPiece` and notifies the `onPieceSelectedListener`.
     */
    fun setSelected(position: Int) {
        post(Runnable {
            if (position >= puzzlePieces.size) return@Runnable // Return if position is out of bounds
            handlingPiece = puzzlePieces[position] // Set the selected piece
            previousHandlingPiece = handlingPiece // Store the previous piece

            // Notify the listener that a piece has been selected
            onPieceSelectedListener?.onPieceSelected(handlingPiece, position)
            invalidate() // Redraw the view
        })
    }

    /**
     * Returns the position of the currently selected puzzle piece. If no piece is selected, returns -1.
     */
    val handlingPiecePosition: Int
        get() {
            if (handlingPiece == null) {
                return -1
            }
            return puzzlePieces.indexOf(handlingPiece)
        }

    /**
     * Returns true if a piece is currently selected, false otherwise.
     */
    fun hasPieceSelected(): Boolean {
        return handlingPiece != null
    }

    /**
     * Sets the animation duration for all puzzle pieces.
     *
     * @param duration The duration for animations in milliseconds.
     */
    fun setAnimateDuration(duration: Int) {
        this.duration = duration
        for (piece in puzzlePieces) {
            piece.setAnimateDuration(duration)
        }
    }

    /**
     * Returns true if the puzzle view needs to draw lines between pieces, false otherwise.
     */
    fun isNeedDrawLine(): Boolean {
        return needDrawLine
    }

    /**
     * Sets whether the puzzle view should draw lines between pieces.
     * Clears the current selection and redraws the view.
     */
    fun setNeedDrawLine(needDrawLine: Boolean) {
        this.needDrawLine = needDrawLine
        handlingPiece = null
        previousHandlingPiece = null
        invalidate() // Redraw the view
    }

    /**
     * Returns true if the puzzle view needs to draw outer lines, false otherwise.
     */
    fun isNeedDrawOuterLine(): Boolean {
        return needDrawOuterLine
    }

    /**
     * Sets whether the puzzle view should draw outer lines and redraws the view.
     */
    fun setNeedDrawOuterLine(needDrawOuterLine: Boolean) {
        this.needDrawOuterLine = needDrawOuterLine
        invalidate() // Redraw the view
    }

    /**
     * Returns the current color of the lines between pieces.
     */
    fun getLineColor(): Int {
        return lineColor
    }

    /**
     * Sets the color of the lines between pieces and redraws the view.
     */
    fun setLineColor(lineColor: Int) {
        this.lineColor = lineColor
        linePaint?.color = lineColor
        invalidate() // Redraw the view
    }

    /**
     * Returns the current size of the lines between pieces.
     */
    fun getLineSize(): Int {
        return lineSize
    }

    /**
     * Sets the size of the lines between pieces and redraws the view.
     */
    fun setLineSize(lineSize: Int) {
        this.lineSize = lineSize
        invalidate() // Redraw the view
    }

    /**
     * Returns the color of the selected piece's outline.
     */
    fun getSelectedLineColor(): Int {
        return selectedLineColor
    }

    /**
     * Sets the color of the selected piece's outline and redraws the view.
     */
    fun setSelectedLineColor(selectedLineColor: Int) {
        this.selectedLineColor = selectedLineColor
        selectedAreaPaint?.color = selectedLineColor
        invalidate() // Redraw the view
    }

    /**
     * Returns the color of the handle bars used for resizing pieces.
     */
    fun getHandleBarColor(): Int {
        return handleBarColor
    }

    /**
     * Sets the color of the handle bars used for resizing pieces and redraws the view.
     */
    fun setHandleBarColor(handleBarColor: Int) {
        this.handleBarColor = handleBarColor
        handleBarPaint?.color = handleBarColor
        invalidate() // Redraw the view
    }

    /**
     * Clears the selection of the current puzzle piece and line,
     * as well as any pieces marked for changes.
     */
    fun clearHandling() {
        handlingPiece = null
        handlingLine = null
        replacePiece = null
        previousHandlingPiece = null
        needChangePieces.clear()
    }

    /**
     * Sets the padding for each puzzle piece and moves the pieces to fill their areas.
     * Redraws the view after updating.
     */
    fun setPiecePadding(padding: Float) {
        this.piecePadding = padding
        if (puzzleLayout != null) {
            puzzleLayout?.padding = padding
            val size = puzzlePieces.size
            for (i in 0 until size) {
                val puzzlePiece = puzzlePieces[i]
                if (puzzlePiece.canFilledArea()) {
                    puzzlePiece.moveToFillArea(null)
                } else {
                    //puzzlePiece.fillArea(this, true);
                }
            }
        }

        invalidate()
    }

    /**
     * Sets the corner radius (radian) of each puzzle piece and redraws the view.
     */
    fun setPieceRadian(radian: Float) {
        this.pieceRadian = radian
        if (puzzleLayout != null) {
            puzzleLayout?.radian = radian
        }

        invalidate()
    }

    /**
     * Enables or disables quick mode for puzzle piece movement. Redraws the view when updated.
     */
    fun setQuickMode(quickMode: Boolean) {
        this.quickMode = quickMode
        invalidate()
    }

    /**
     * Sets the background color of the puzzle layout.
     */
    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        if (puzzleLayout != null) {
            puzzleLayout?.color = color
        }
    }

    /**
     * Sets whether the puzzle pieces should reset their matrix when moved.
     */
    fun setNeedResetPieceMatrix(needResetPieceMatrix: Boolean) {
        this.needResetPieceMatrix = needResetPieceMatrix
    }

    /**
     * Returns the current padding for the puzzle pieces.
     */
    fun getPiecePadding(): Float {
        return piecePadding
    }

    /**
     * Returns the current corner radius for the puzzle pieces.
     */
    fun getPieceRadian(): Float {
        return pieceRadian
    }

    /**
     * Returns a list of all the puzzle pieces in the layout, sorted by their area.
     */
    fun getPuzzlePieces(): List<PuzzlePiece?> {
        val size = puzzlePieces.size
        val pieces: MutableList<PuzzlePiece?> = ArrayList(size)
        puzzleLayout?.sortAreas()
        for (i in 0 until size) {
            val area = puzzleLayout?.getArea(i)
            val piece = areaPieceMap[area]
            pieces.add(piece)
        }

        return pieces
    }

    /**
     * Similar to `setPiecePadding`, but calls `fillArea` on pieces that can be filled after moving.
     */
    fun setMyPiecePadding(padding: Float) {
        this.piecePadding = padding
        if (puzzleLayout != null) {
            puzzleLayout?.padding = padding
            val size = puzzlePieces.size
            for (i in 0 until size) {
                val puzzlePiece = puzzlePieces[i]
                if (puzzlePiece.canFilledArea()) {
                    puzzlePiece.moveToFillArea(null)
                } else {
                    puzzlePiece.fillArea(this, true)
                }
            }
        }

        invalidate()
    }

    /**
     * Returns true if dragging pieces is enabled, false otherwise.
     */
    fun canDrag(): Boolean {
        return canDrag
    }

    /**
     * Sets whether puzzle pieces can be dragged.
     */
    fun setCanDrag(canDrag: Boolean) {
        this.canDrag = canDrag
    }

    /**
     * Returns true if lines between pieces can be moved, false otherwise.
     */
    fun canMoveLine(): Boolean {
        return canMoveLine
    }

    /**
     * Sets whether lines between pieces can be moved.
     */
    fun setCanMoveLine(canMoveLine: Boolean) {
        this.canMoveLine = canMoveLine
    }

    /**
     * Returns true if zooming pieces is enabled, false otherwise.
     */
    fun canZoom(): Boolean {
        return canZoom
    }

    /**
     * Sets whether puzzle pieces can be zoomed in and out.
     */
    fun setCanZoom(canZoom: Boolean) {
        this.canZoom = canZoom
    }

    /**
     * Returns true if swapping pieces is enabled, false otherwise.
     */
    fun canSwap(): Boolean {
        return canSwap
    }

    /**
     * Sets whether puzzle pieces can be swapped with each other.
     */
    fun setCanSwap(canSwap: Boolean) {
        this.canSwap = canSwap
    }

    /**
     * Sets a listener for when a puzzle piece is selected.
     */
    fun setOnPieceSelectedListener(onPieceSelectedListener: OnPieceSelectedListener?) {
        this.onPieceSelectedListener = onPieceSelectedListener
    }

    /**
     * Sets a listener for when a puzzle piece is clicked.
     */
    fun setOnPieceClickListener(onPieceClick: OnPieceClick?) {
        this.onPieceClick = onPieceClick
    }

    interface OnPieceSelectedListener {
        fun onPieceSelected(piece: PuzzlePiece?, position: Int)
    }

    /**
     * Moves the currently selected piece to the left by 5 units.
     * If no piece is selected, the first piece is selected and moved.
     */
    fun moveLeft() {
        if (currentPiece != null) {
            currentPiece?.record()
            currentPiece?.translate(-5f, 0f)
            invalidate()
        } else {
            currentPiece = puzzlePieces[0]
            currentPiece?.record()
            currentPiece?.translate(-5f, 0f)
            invalidate()
        }
    }

    /**
     * Moves the currently selected piece to the right by 5 units.
     * If no piece is selected, the first piece is selected and moved.
     */
    fun moveRight() {
        if (currentPiece != null) {
            currentPiece?.record()
            currentPiece?.translate(5f, 0f)
            invalidate()
        } else {
            currentPiece = puzzlePieces[0]
            currentPiece?.record()
            currentPiece?.translate(5f, 0f)
            invalidate()
        }
    }

    /**
     * Moves the currently selected piece up by 5 units.
     * If no piece is selected, the first piece is selected and moved.
     */
    fun moveUp() {
        if (currentPiece != null) {
            currentPiece?.record()
            currentPiece?.translate(0f, -5f)
            invalidate()
        } else {
            currentPiece = puzzlePieces[0]
            currentPiece?.record()
            currentPiece?.translate(0f, -5f)
            invalidate()
        }
    }

    /**
     * Moves the currently selected piece down by 5 units.
     * If no piece is selected, the first piece is selected and moved.
     */
    fun moveDown() {
        if (currentPiece != null) {
            currentPiece?.record()
            currentPiece?.translate(0f, 5f)
            invalidate()
        } else {
            currentPiece = puzzlePieces[0]
            currentPiece?.record()
            currentPiece?.translate(0f, 5f)
            invalidate()
        }
    }

    /**
     * Rotates the currently selected piece by 90 degrees.
     * If no piece is selected, the first piece is selected and rotated.
     */
    fun rotatePiece() {
        if (currentPiece != null) {
            currentPiece?.record()
            rotate(90f)
            pieceAppliedEditing.add(currentPiece)
            invalidate()
        } else {
            currentPiece = puzzlePieces[0]
            currentPiece?.record()
            rotate(90f)
            pieceAppliedEditing.add(currentPiece)
            invalidate()
        }
    }

    /**
     * Zooms in the currently selected piece by 10%.
     * The zoom factor is limited to prevent excessive scaling.
     */
    fun zoomInPiece() {
        currentPiece?.let { currentPiece ->
            val matrixValues = FloatArray(9) // Matrix values array
            currentPiece.matrix.getValues(matrixValues)
            val zoomMinMax = matrixValues[0]
            if (abs(zoomMinMax.toDouble()) > 4.5) {
            } else {
                currentPiece.record()
                currentPiece.zoom(1.1f, 1.1f, currentPiece.areaCenterPoint)
                invalidate()
            }
        }
    }

    /**
     * Zooms out the currently selected piece by 10%.
     * The zoom factor is limited to prevent excessive scaling.
     */
    fun zoomOutPiece() {
        currentPiece?.let { currentPiece ->
            val matrixValues = FloatArray(9) // Matrix values array
            currentPiece.matrix.getValues(matrixValues)
            var zoomMinMax = matrixValues[0]
            if (abs(zoomMinMax.toDouble()).toInt() == 0) {
                zoomMinMax = matrixValues[1]
            }
            if (abs(zoomMinMax.toDouble()) < 0.3) {
            } else {
                currentPiece.record()
                currentPiece.zoom(0.9f, 0.9f, currentPiece.areaCenterPoint)
                invalidate()
            }
        }
    }

    /**
     * Mirrors the currently selected piece horizontally.
     */
    fun mirrorPiece() {
        currentPiece?.let { currentPiece ->
            currentPiece.record()
            currentPiece.postFlipHorizontally()
            pieceAppliedEditing.add(currentPiece)
            invalidate()
        }
    }

    /**
     * Flips the currently selected piece vertically.
     */
    fun flipPiece() {
        currentPiece?.let { currentPiece ->
            currentPiece.record()
            currentPiece.postFlipVertically()
            pieceAppliedEditing.add(currentPiece)
            invalidate()
        }
    }

    /**
     * Temporarily stores the currently selected piece for swiping functionality.
     */
    fun swipePieceFunction() {
        if (currentPiece != null) {
            swipeTempPiece = currentPiece
        }
    }

    /**
     * Performs the swap of two pieces during a swipe operation.
     * It exchanges the drawable and path properties between the current piece and the swiped piece.
     */
    fun swipePieceFunctionPerform() {
        if (currentPiece != null) {
            temp = currentPiece?.getDrawable()
            tempPath = currentPiece?.path
            currentPiece?.setDrawable(swipeTempPiece?.getDrawable() ?: return)
            currentPiece?.path = swipeTempPiece?.path ?: return
            swipeTempPiece?.setDrawable(temp ?: return)
            tempPath?.let { swipeTempPiece?.path = it }

            handlingPiece?.fillArea(this, true)
            swipeTempPiece?.fillArea(this, true)

            swipeActivated = false
        }
    }

    /**
     * Clears the list of pieces that have been edited.
     */
    fun clearPieceAppliedEditing() {
        pieceAppliedEditing.clear()
    }

    /**
     * Sets the sizeChangedOnce flag to indicate if the size has been changed.
     */
    fun setSizeChangedOnce(value: Boolean) {
        sizeChangedOnce = value
    }

    /**
     * Gesture detector for handling touch events on the pieces.
     * It triggers on a single tap event.
     */
    val gestureDetector: GestureDetector = GestureDetector(object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onPieceClick?.onPieceClick()
            return true
        }
    })

    /**
     * Interface for handling piece click events and position swapping.
     */
    interface OnPieceClick {
        fun onPieceClick()

        fun onSwapGetPositions(pos1: Int, pos2: Int)
    }

    // Tag for logging purposes
    companion object {
        private const val TAG = "PuzzleView"
    }
}
