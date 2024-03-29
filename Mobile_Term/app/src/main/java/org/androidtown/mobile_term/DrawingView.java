package org.androidtown.mobile_term;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class DrawingView extends View {
    private final Paint mPaintSrcIn = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint mPaintDstIn = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint mPaintColor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPaintEraser = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Matrix mMatrix = new Matrix();
    private final Canvas mLayerCanvas = new Canvas();

    private Bitmap mInnerShape;
    private Bitmap mOuterShape;
    private Bitmap mLayerBitmap;

    private ArrayList<DrawOp> mDrawOps = new ArrayList<DrawOp>();
    private DrawOp mCurrentOp = new DrawOp();

    private ArrayList<DrawOp> mUndoneOps = new ArrayList<DrawOp>();


    public DrawingView(Context context) {
        this(context, null, 0);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaintSrcIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mPaintDstIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        Bitmap bitmap = PDFActivity.bitmap;
        mPaintColor.setStyle(Paint.Style.STROKE);
        mPaintColor.setStrokeJoin(Paint.Join.ROUND);
        mPaintColor.setStrokeCap(Paint.Cap.ROUND);

        mPaintEraser.set(mPaintColor);
        mPaintEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaintEraser.setMaskFilter(new BlurMaskFilter(getResources()
                .getDisplayMetrics().density * 4, BlurMaskFilter.Blur.NORMAL));
    }

    public void setShape(int inner, int outer) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        setShape(BitmapFactory.decodeResource(getResources(), inner, options),
                BitmapFactory.decodeResource(getResources(), outer, options));
    }

    public void setShape(Bitmap inner, Bitmap outer) {
        mInnerShape = inner;
        mOuterShape = outer;
        requestLayout();
        invalidate();
    }

    public void setDrawingColor(int color) {
        mCurrentOp.reset();
        mCurrentOp.type = DrawOp.Type.PAINT;
        mCurrentOp.color = color;
    }

    public void setDrawingStroke(int stroke) {
        mCurrentOp.reset();
        mCurrentOp.type = DrawOp.Type.PAINT;
        mCurrentOp.stroke = stroke;
    }

    public void enableEraser() {
        mCurrentOp.reset();
        mCurrentOp.type = DrawOp.Type.ERASE;
    }

    public void clearDrawing() {
        mDrawOps.clear();
        mUndoneOps.clear();
        mCurrentOp.reset();
        invalidate();
    }

    public void undoOperation() {
        if (mDrawOps.size() > 0) {
            DrawOp last = mDrawOps.remove(mDrawOps.size() - 1);
            mUndoneOps.add(last);
            invalidate();
        }
    }

    public void redoOperation() {
        if (mUndoneOps.size() > 0) {
            DrawOp redo = mUndoneOps.remove(mUndoneOps.size() - 1);
            mDrawOps.add(redo);
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mLayerBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //if (PDFActivity.num == 0)
        mLayerBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mLayerCanvas.setBitmap(mLayerBitmap);

        if (mOuterShape != null) {
            int dx = w;
            int dy = h;
            mMatrix.setTranslate(dx, dy);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }

        // Clear software canvas
        mLayerCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // Draw picture from ops
        for (DrawOp op : mDrawOps) {
            drawOp(mLayerCanvas, op);
        }
        drawOp(mLayerCanvas, mCurrentOp);

        // Mask the drawing to the inner surface area of the shape
        mLayerCanvas.drawBitmap(mInnerShape, mMatrix, mPaintDstIn);

        // Draw orignal shape to view
        canvas.drawBitmap(mOuterShape, mMatrix, null);

        // Draw masked image to view
        if (PDFActivity.num == 0) {
            canvas.drawBitmap(mLayerBitmap, 0, 0, null);
        } else {
            canvas.drawBitmap(PDFActivity.bitmap, 0, 0, null);
            canvas.drawBitmap(mLayerBitmap, 0, 0, null);
        }

    }

    private void drawOp(Canvas canvas, DrawOp op) {

        if (op.path.isEmpty()) {
            return;
        }

        final Paint paint;

        if (op.type == DrawOp.Type.PAINT) {
            paint = mPaintColor;
            paint.setColor(op.color);
            paint.setStrokeWidth(op.stroke);
        } else {
            paint = mPaintEraser;
            paint.setStrokeWidth(op.stroke);
        }

        mLayerCanvas.drawPath(op.path, paint);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mUndoneOps.clear();
                mCurrentOp.path.moveTo(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getHistorySize(); i++) {
                    mCurrentOp.path.lineTo(event.getHistoricalX(i), event.getHistoricalY(i));
                }
                mCurrentOp.path.lineTo(x, y);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mCurrentOp.path.lineTo(x, y);
                mDrawOps.add(new DrawOp(mCurrentOp));
                mCurrentOp.path.reset();
                break;
        }

        invalidate();

        return true;
    }

    private static class DrawOp {
        public final Path path = new Path();
        public Type type;
        public int color;
        public int stroke;


        public DrawOp() {
            //
        }

        public void reset() {
            this.path.reset();
        }

        public DrawOp(DrawOp op) {
            this.path.set(op.path);
            this.type = op.type;
            this.color = op.color;
            this.stroke = op.stroke;
        }

        public static enum Type {
            PAINT, ERASE;
        }
    }
}