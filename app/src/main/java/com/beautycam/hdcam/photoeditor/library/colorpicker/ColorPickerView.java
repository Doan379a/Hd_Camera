
package com.beautycam.hdcam.photoeditor.library.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * This ColorPickerView extends View class.
 * Here, I have used two Shader objects. One for horizontal gradient and one for vertical gradient.
 *
 * @author Mrudul Tora (mrudultora@gmail.com)
 * @since 6 May, 2021
 */
public class ColorPickerView extends View {
    Paint paint;
    Shader verticalShader;
    Shader horizontalShader;
    float[] hsv = new float[]{1f, 1f, 1f};  // hue (0-360), saturation (0-1), value (0-1)

    public ColorPickerView(Context context) {
        super(context);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * x0=0, y0=0, x1=0, y1=height (these are coordinates, assume similar to graph).
     * Color.WHITE is the start color (at (x0,y0)).
     * Color.BLACK is the end color (at (x1,y1)).
     * In between there is a vertical linear gradient.
     * (Vertical gradient is not needed to be initialized again as its value is same for every rgb)
     * <p>
     * x0=0, y0=0, x1=width, y1=0 (these are coordinates, assume similar to graph).
     * Color.WHITE is the start color (at (x0,y0)).
     * rgbValue is the end color (at (x1,y1)).
     * In between there is a horizontal linear gradient.
     *
     * @param canvas (canvas)
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (paint == null) {
            paint = new Paint();
        }

        // Tính toán màu từ HSV
        int rgbValue = Color.HSVToColor(hsv);

        // Khởi tạo các Shader
        verticalShader = new LinearGradient(0f, 0f, 0f, this.getMeasuredHeight(), Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
        horizontalShader = new LinearGradient(0f, 0f, this.getMeasuredWidth(), 0f, Color.WHITE, rgbValue, Shader.TileMode.CLAMP);

        // Kết hợp Shader
        ComposeShader composeShader = new ComposeShader(verticalShader, horizontalShader, PorterDuff.Mode.MULTIPLY);
        paint.setShader(composeShader);

        // Tạo RectF để vẽ hình chữ nhật với các góc bo tròn
        float radius = 15f;  // Độ bo góc (có thể thay đổi tùy thuộc vào nhu cầu)
        RectF rectF = new RectF(0f, 0f, this.getMeasuredWidth(), this.getMeasuredHeight());

        // Vẽ hình chữ nhật bo tròn
        canvas.drawRoundRect(rectF, radius, radius, paint);

        // Cài đặt chế độ phần cứng để tắt tăng tốc phần cứng
        setLayerType(LAYER_TYPE_SOFTWARE, paint);
    }


    public void setHue(float hue) {
        this.hsv[0] = hue;
        invalidate();
    }
}
