
package com.surfaceviewdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ly
 */
public class TurnTableSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private Context mContext;
    private float mScale = 1;

    private float mInitScale = 0.75f;
    // surfaceview 操作对象
    private SurfaceHolder mSurfaceHolder;
    // 画布
    private Canvas mCanvas;
    // 画笔
    private Paint mPaint;
    //
    private Matrix mMatrixBottom;
    private Matrix mMatrixTop;
    // 图片对象
    private Bitmap mBottomBitmap;
    private Bitmap mTopBitmap;

    // 窗口宽高 像素
    private float mWindowX;
    private float mWindowY;

    // 图片宽高 像素
    private float mBottomX;
    private float mBottomY;
    private float mTopX;
    private float mTopY;

    // 偏移量
    private float mBottomDeviationX;
    private float mBottomDeviationY;
    private float mTopDeviationX;
    private float mTopDeviationY;

    private int mMode = 0;
    private float mX;
    private float mY;
    private static final int TOP = 1;
    private static final int BOTTOM = 2;
    private static final int NONE = 0;
    // 选中的哪一个
    private int mWhich;
    // 图中心坐标
    private float mPointX;
    private float mPointY;

    private float mDownDegree;
    private float mUpDegree;

    private float mOldDist;

    // 线程池，保证线程以队列方式一个一个执行，不出现内存溢出
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    public TurnTableSurfaceView(Context context) {
        super(context);
        mContext = context;
        // setBackgroundColor(getResources().getColor(R.color.white));
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mPaint = new Paint();
        mMatrixBottom = new Matrix();
        mMatrixTop = new Matrix();

        mBottomBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jktz_2);
        mBottomX = mBottomBitmap.getWidth();
        mBottomY = mBottomBitmap.getHeight();

        mTopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jktz_1);
        mTopX = mTopBitmap.getWidth();
        mTopY = mTopBitmap.getHeight();

        // 屏幕宽高及中心坐标
        mWindowX = mContext.getResources().getDisplayMetrics().widthPixels;
        mPointX = mWindowX / 2;
        mWindowY = mContext.getResources().getDisplayMetrics().heightPixels;
        mPointY = mWindowY / 2;
        // 将图片置于屏幕中间，支持竖屏
        // 若底部屏幕大于图片宽度，则图片显示屏幕3/4宽度大小
        if (mWindowX < mBottomX) {
            float tempScale = (mBottomX - mWindowX) / mBottomX;
            if (tempScale > (1 - mInitScale)) {
                mScale = 1 - tempScale;
            }
            mBottomDeviationX = (mWindowX - (mBottomX * mScale)) / 2;
            mBottomDeviationY = (mWindowY - (mBottomY * mScale)) / 2;

            // 缩放倍数
            mMatrixBottom.postScale(mScale, mScale);
            mMatrixTop.postScale(mScale, mScale);
        } else {
            mBottomDeviationX = (mWindowX - mBottomX) / 2;
            mBottomDeviationY = (mWindowY - mBottomY) / 2;
        }

        // 移动
        mMatrixBottom.postTranslate(mBottomDeviationX, mBottomDeviationY);
        // 若顶上屏幕大于图片宽度，则图片显示屏幕3/4宽度大小
        if (mWindowX < mTopX) {

            mTopDeviationX = (mWindowX - (mTopX * mScale)) / 2;
            mTopDeviationY = (mWindowY - (mTopY * mScale)) / 2;
        } else {
            mTopDeviationX = (mWindowX - mTopX) / 2;
            mTopDeviationY = (mWindowY - mTopY) / 2;
        }
        // 移动
        mMatrixTop.postTranslate(mTopDeviationX, mTopDeviationY);
    }

    /**
     * surfaceview 创建
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // 像素密度系数
        // float density = mContext.getResources().getDisplayMetrics().density;
        // 像素密度
        // float densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;

        resetDraw();
    }

    /**
     * surfaceview 改变
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * surfaceview 销毁
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        mCanvas = mSurfaceHolder.lockCanvas();
        mCanvas.drawColor(Color.WHITE);
        // 画底部图片
        mCanvas.drawBitmap(mBottomBitmap, mMatrixBottom, mPaint);
        // 画顶部图片
        mCanvas.drawBitmap(mTopBitmap, mMatrixTop, mPaint);
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mMode = 1;
                mX = event.getX();
                mY = event.getY();
                Log.d("TurnTableSurfaceView", "mDownX=" + mX + ";mDownY=" + mY);
                float distance = getDistance(mX, mY);
                if (distance <= mTopX * mScale / 2) {
                    mWhich = TOP;
                } else if (distance > mTopX * mScale / 2 && distance <= mBottomX * mScale / 2) {
                    mWhich = BOTTOM;

                } else {
                    mWhich = NONE;
                }
                mDownDegree = caculateDegree(mX, mY);
                break;
            case MotionEvent.ACTION_UP:
                mMode = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mMode -= 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDist = spacing(event);// 两点按下时的距离
                mMode += 1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMode >= 2) {
                    float newDist = spacing(event);
                    float newScale = newDist / mOldDist;
                    newScale = (float) Math.sqrt(newScale);
                    mScale = mScale * newScale;
                    mMatrixBottom.postScale(newScale, newScale, mPointX, mPointY);
                    mMatrixTop.postScale(newScale, newScale, mPointX, mPointY);

                    resetDraw();

                    mOldDist = spacing(event);// 两点按下时的距离
                } else {
                    Log.d("TurnTableSurfaceView",
                            "mMoveX=" + event.getX() + ";mMoveY=" + event.getY());
                    Log.d("TurnTableSurfaceView", "mPointX=" + mPointX + ";mPointY=" + mPointY);
                    if (Math.abs(mX - mPointX) < 20
                            && Math.abs(mY - mPointY) < 20) {
                        // 若触摸中间则移动

                        mBottomDeviationX = event.getX() - mX;
                        mBottomDeviationY = event.getY() - mY;
                        mTopDeviationX = mBottomDeviationX;
                        mTopDeviationY = mBottomDeviationY;

                        mMatrixBottom.postTranslate(mBottomDeviationX, mBottomDeviationY);
                        mMatrixTop.postTranslate(mTopDeviationX, mTopDeviationY);

                        resetDraw();

                        // 重置中心坐标
                        mPointX += mBottomDeviationX;
                        mPointY += mBottomDeviationY;

                        // 记录上一次触摸坐标
                        mX = event.getX();
                        mY = event.getY();
                    } else {
                        // 旋转，根据圆心坐标计算角度
                        Log.d("TurnTableSurfaceView",
                                "mMoveX=" + event.getX() + ";mMoveY=" + event.getY());
                        mUpDegree = caculateDegree(event.getX(), event.getY());

                        rotataion();

                        mDownDegree = caculateDegree(event.getX(), event.getY());
                    }
                }

                break;
        }
        return true;
    }

    private void rotataion() {

        float devilizationDegree = mUpDegree - mDownDegree;
        switch (mWhich) {
            case TOP:
                // 顶部旋转
                mMatrixTop.postRotate(devilizationDegree, mPointX, mPointY);
                // 画顶部图片
                break;
            case BOTTOM:
                // 底部旋转
                mMatrixBottom.postRotate(devilizationDegree, mPointX, mPointY);
                // 画底部图片
                break;
            case NONE:
                break;
            default:
                break;
        }

        // 重新画
        resetDraw();
    }

    /**
     * 重新画
     */
    private void resetDraw() {
        mExecutorService.execute(this);
    }

    /**
     * 计算角度
     * 
     * @param x
     * @param y
     */
    private float caculateDegree(float x, float y) {
        float degree = (float) Math.toDegrees(Math.atan2(y - mPointY, x - mPointX));
        return degree;
    }

    // 获取距离圆心的距离
    private float getDistance(float x, float y) {
        // 根据圆心坐标计算角度
        float distance = (float) Math
                .sqrt(((x - mPointX) * (x - mPointX) + (y - mPointY)
                        * (y - mPointY)));
        return distance;
    }

    // 触碰两点间距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
