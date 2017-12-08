package me.gavin.app.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import me.gavin.svg.model.SVG;
import me.gavin.util.DisplayUtil;

/**
 * Utils
 *
 * @author gavin.xiong 2017/12/5
 */
class Utils {

    static Path getKeyLines(int mSize) {
        Path mKeyLinesPath = new Path();

        mKeyLinesPath.addRect(0, 0, mSize, mSize, Path.Direction.CCW);

        float half = mSize / 2f;
        float hla = mSize * Icon.BG_L_RATIO / 2f;
        float hma = mSize * Icon.BG_M_RATIO / 2f;
        float hsa = mSize * Icon.BG_S_RATIO / 2f;
        float c = mSize * Icon.BG_C_RATIO;

        mKeyLinesPath.addRoundRect(half - hma, half - hma, half + hma, half + hma, c, c, Path.Direction.CCW);
        mKeyLinesPath.addCircle(half, half, hla, Path.Direction.CCW);
        mKeyLinesPath.addRoundRect(half - hsa, half - hla, half + hsa, half + hla, c, c, Path.Direction.CCW);
        mKeyLinesPath.addRoundRect(half - hla, half - hsa, half + hla, half + hsa, c, c, Path.Direction.CCW);

        mKeyLinesPath.moveTo(0, 0);
        mKeyLinesPath.rLineTo(mSize, mSize);
        mKeyLinesPath.rMoveTo(-mSize, 0);
        mKeyLinesPath.rLineTo(mSize, -mSize);

        mKeyLinesPath.moveTo(0, 68f / 192f * mSize);
        mKeyLinesPath.rLineTo(mSize, 0);
        mKeyLinesPath.rMoveTo(0, 28f / 192f * mSize);
        mKeyLinesPath.rLineTo(-mSize, 0);
        mKeyLinesPath.rMoveTo(0, 28f / 192f * mSize);
        mKeyLinesPath.rLineTo(mSize, 0);

        mKeyLinesPath.moveTo(68f / 192f * mSize, 0);
        mKeyLinesPath.rLineTo(0, mSize);
        mKeyLinesPath.rMoveTo(28f / 192f * mSize, 0);
        mKeyLinesPath.rLineTo(0, -mSize);
        mKeyLinesPath.rMoveTo(28f / 192f * mSize, 0);
        mKeyLinesPath.rLineTo(0, mSize);

        mKeyLinesPath.addCircle(half, half, 40f / 192f * mSize, Path.Direction.CCW);

        return mKeyLinesPath;
    }

    static Path getBgPath(int bgShape, int mSize, float bgCorner) {
        Path mBgPath = new Path();
        float half = mSize / 2f;
        float corner = mSize * bgCorner;
        if (bgShape == 0) {
            float hs = half * Icon.BG_M_RATIO;
            mBgPath.addRoundRect(half - hs, half - hs, half + hs, half + hs,
                    corner, corner, Path.Direction.CCW);
        } else if (bgShape == 1) {
            mBgPath.addCircle(half, half, half * Icon.BG_L_RATIO, Path.Direction.CCW);
        } else if (bgShape == 2) {
            float hhs = half * Icon.BG_S_RATIO;
            float hvs = half * Icon.BG_L_RATIO;
            mBgPath.addRoundRect(half - hhs, half - hvs, half + hhs, half + hvs,
                    corner, corner, Path.Direction.CCW);
        } else if (bgShape == 3) {
            float hhs = half * Icon.BG_L_RATIO;
            float hvs = half * Icon.BG_S_RATIO;
            mBgPath.addRoundRect(half - hhs, half - hvs, half + hhs, half + hvs,
                    corner, corner, Path.Direction.CCW);
        }
        return mBgPath;
    }

    static Path getBgLayerPath(Path mBgPath, int mSize) {
        Path path = new Path(mBgPath);
        Matrix matrix = new Matrix();
        matrix.postScale(1 - Icon.BG_SL_RATIO, 1 + Icon.BG_SL_RATIO / 4f, mSize / 2f, mSize / 2f);
        path.transform(matrix);
        return path;
    }

    static Bitmap getShadow(@NonNull Bitmap mIconBitmap, int mSize, @NonNull Path mBgPath, boolean preview) {
        Bitmap mShadowBitmap = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
        Canvas shadowCanvas = new Canvas(mShadowBitmap);
        shadowCanvas.clipPath(mBgPath);
        Paint mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mIconPaint.setStyle(Paint.Style.FILL);
        for (int i = 1; i <= mSize; i += preview ? 2 : 1) {
            shadowCanvas.drawBitmap(mIconBitmap, i, i, mIconPaint);
        }
        return mShadowBitmap;
    }

    static Path getScorePath(int mSize, @NonNull Path mBgPath) {
        Path mScorePath = new Path();
        mScorePath.addRect(0, 0, mSize, mSize / 2f, Path.Direction.CCW);
        mScorePath.op(mBgPath, Path.Op.INTERSECT);
        return mScorePath;
    }

    static Bitmap getBitmap(@NonNull SVG mSvg, int mSize, float iconScale, @NonNull Path mBgPath) {
        Bitmap mIconBitmap = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
        Canvas iconCanvas = new Canvas(mIconBitmap);
        iconCanvas.clipPath(mBgPath);

        Matrix mMatrix = new Matrix();
        float mInherentScale = mSvg.getInherentScale();
        // 比例同步
        mMatrix.postScale(mInherentScale, mInherentScale);
        mMatrix.postTranslate((mSize - mSvg.width) / 2f, (mSize - mSvg.height) / 2f);
        float scale = Math.min(mSize / mSvg.width, mSize / mSvg.height);
        mMatrix.postScale(scale, scale, mSize / 2f, mSize / 2f);
        // 当前缩放比
        mMatrix.postScale(iconScale, iconScale, mSize / 2f, mSize / 2f);
        iconCanvas.setMatrix(mMatrix);

        if (mSvg.width / mSvg.height != mSvg.viewBox.width / mSvg.viewBox.height) {
            iconCanvas.translate(
                    mSvg.width / mSvg.height > mSvg.viewBox.width / mSvg.viewBox.height
                            ? (mSvg.width - mSvg.viewBox.width / mSvg.viewBox.height * mSvg.height) / 2 / mInherentScale : 0,
                    mSvg.width / mSvg.height > mSvg.viewBox.width / mSvg.viewBox.height
                            ? 0 : (mSvg.height - mSvg.viewBox.height / mSvg.viewBox.width * mSvg.width) / 2 / mInherentScale);
        }

        for (int i = 0; i < mSvg.paths.size(); i++) {
            if (mSvg.drawables.get(i).getFillPaint().getColor() != 0) {
                iconCanvas.drawPath(mSvg.paths.get(i), mSvg.drawables.get(i).getFillPaint());
            }
        }
        return mIconBitmap;
    }

    static Bitmap getBitmap(Drawable drawable, int size, float scale, Path mBgPath) {
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.clipPath(mBgPath);
        int a = (int) (size * scale / 2f);
        drawable.setBounds(size / 2 - a, size / 2 - a, size / 2 + a, size / 2 + a);
        drawable.draw(canvas);
        return result;
    }

    static Bitmap getBitmap(Bitmap bitmap, int size, float iconScale, Path mBgPath) {
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.clipPath(mBgPath);
        Matrix matrix = new Matrix();
        matrix.postTranslate((size - bitmap.getWidth()) / 2f, (size - bitmap.getHeight()) / 2f);
        float scale = size * 1f / Math.min(bitmap.getWidth(), bitmap.getHeight());
        matrix.postScale(scale, scale, size / 2f, size / 2f);
        matrix.postScale(iconScale, iconScale, size / 2f, size / 2f);
        canvas.drawBitmap(bitmap, matrix, new Paint());
        return result;
    }

    static Bitmap getBitmap(String text, int size, float iconScale, Path mBgPath) {
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.clipPath(mBgPath);
        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        mTextPaint.setTextSize(size * iconScale / text.length());
        int baseY = (int) (size / 2 - mTextPaint.descent() / 2 - mTextPaint.ascent() / 2);
        canvas.drawText(text, size / 2, baseY, mTextPaint);
        return result;
    }

    static Bitmap getBitmap(SVG mSvg, Drawable mSrcDrawable, Bitmap mSrcBitmap, String mSrcText, Icon mIcon, int size,
                            Paint mBgPaint, Paint mBgLayerPaint, Paint mShadowPaint, Paint mIconPaint, Paint mScorePaint) {
        // 创建 bitmap
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 背景 Path
        Path mBgPath = Utils.getBgPath(mIcon.bgShape, size, mIcon.bgCorner);
        // 背景阴影 Path
        Path mBgLayerPath = Utils.getBgLayerPath(mBgPath, size);

        // 背景阴影画笔
        Paint mBgLayerPaint2 = new Paint(mBgLayerPaint);
        mBgLayerPaint2.setShadowLayer(size * Icon.BG_SL_RATIO, 0,
                DisplayUtil.dp2px(size * Icon.BG_SL_RATIO / 4f), 0x50000000);
        // 画背景阴影
        canvas.drawPath(mBgLayerPath, mBgLayerPaint2);
        // 画背景
        canvas.drawPath(mBgPath, mBgPaint);

        // 前景
        Bitmap mIconBitmap;
        if (mSvg != null) {
            mIconBitmap = Utils.getBitmap(mSvg, size, mIcon.iconScale, mBgPath);
        } else if (mSrcDrawable != null) {
            mIconBitmap = Utils.getBitmap(mSrcDrawable, size, mIcon.iconScale, mBgPath);
        } else if (mSrcBitmap != null) {
            mIconBitmap = Utils.getBitmap(mSrcBitmap, size, mIcon.iconScale, mBgPath);
        } else if (mSrcText != null) {
            mIconBitmap = Utils.getBitmap(mSrcText, size, mIcon.iconScale, mBgPath);
        } else {
            bitmap.recycle();
            return null;
        }

        // 前景阴影
        Bitmap mShadowBitmap = Utils.getShadow(mIconBitmap, size, mBgPath, false);
        // 画前景阴影
        canvas.drawBitmap(mShadowBitmap, 0, 0, mShadowPaint);
        // 画前景
        canvas.drawBitmap(mIconBitmap, 0, 0, mIconPaint);
        mIconBitmap.recycle();
        mShadowBitmap.recycle();

        // 画折痕
        if (mIcon.effectScore) {
            Path mScorePath = Utils.getScorePath(size, mBgPath);
            canvas.drawPath(mScorePath, mScorePaint);
        }

        return bitmap;
    }
}
