package com.android.zqlxtt.activationcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import java.util.concurrent.Executors;

/**
 * Created by scott on 2017/7/17.
 */

public class ActivationCard extends CardView {

    public static final int POSITION_CENTER = 0;
    public static final int POSITION_LEFT = 1;
    public static final int POSITION_TOP = 2;
    public static final int POSITION_RIGHT = 3;
    public static final int POSITION_BOTTOM = 4;
    public static final int POSITION_INVAL = -1;

    private static LruCache<String, Bitmap> sCache = new LruCache<String, Bitmap>(12 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private static boolean sIsEnableCache = false;

    private final int fps = 1000 / 60;

    private final int defaultDuration = 500;

    private boolean isActivation = false;

    private int w;

    private int h;

    private float bgScale = 1.09f;


    private Paint paint;

    private Rect backgroundSubRec;

    private Rect backgroundRec;

    private int defaultLeft;

    private int defaultTop;

    private int currentPosition;

    private Handler handler = new Handler();

    private int animationDuration = defaultDuration;

    private String currentKey;

    private Bitmap background;

    private Bitmap tobePaint;

    public ActivationCard(Context context) {
        super(context);
    }

    public ActivationCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActivationCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(final Bitmap originBitmap,final String key) {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        w = getWidth();
                        h = getHeight();

                        int scaledW = (int) (w * bgScale);
                        int scaledH = (int) (h * bgScale);

                        double preSH = 1.0 * originBitmap.getHeight() / scaledH;
                        double preSW = 1.0 * originBitmap.getWidth() / scaledW;

                        float smallPreS = (float) Math.min(preSH, preSW);

                        Matrix matrix = new Matrix();
                        float s = 1 / smallPreS;
                        matrix.postScale(s, s);
                        if(sIsEnableCache){
                            background = sCache.get(key);
                            if(background == null){
                                background = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, true);
                                sCache.put(key,background);
                            }
                        }else {
                            background = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, true);
                        }


                        defaultLeft = (background.getWidth() - w) / 2;
                        defaultTop = (background.getHeight() - h) / 2;
                        backgroundRec = new Rect(0, 0, w, h);
                        backgroundSubRec = new Rect(defaultLeft, defaultTop, w + defaultLeft, h + defaultTop);
                        currentPosition = POSITION_CENTER;
                        tobePaint = background;
                        Log.d("scott"," key = " + key + "    current key = " + currentKey);
                        if(key.equals(currentKey)){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    invalidate();
                                    isActivation = true;
                                }
                            });
                        }

                    }
                });

                return true;
            }
        });
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((tobePaint!=null&&!tobePaint.isRecycled())) {
            canvas.drawBitmap(tobePaint, backgroundSubRec, backgroundRec, paint);
        }
    }

    public void enableActivation(Bitmap activationBg, String key) {
        currentKey = key;
        isActivation = false;
        init(activationBg,key);
    }

    public void postRight() {

        if (!isActivation) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    postRight();
                }
            }, 1000 / 60);
            return;
        }

        if (currentPosition == POSITION_INVAL || currentPosition == POSITION_RIGHT) {
            Log.d("scott", "current position is already right");
            return;
        }

        currentPosition = POSITION_INVAL;
        final int delta = defaultLeft * 2 - backgroundSubRec.left;
        int tempStep = delta / animationDuration;
        if (tempStep == 0) tempStep = 1;
        final int step = tempStep;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isActivation) return;
                invalidate();
                if (backgroundSubRec.left < defaultLeft * 2) {
                    backgroundSubRec.left += step;
                    backgroundSubRec.right += step;
                    handler.postDelayed(this, fps);
                } else {
                    currentPosition = POSITION_RIGHT;
                }
            }
        }, fps);
    }

    public void postLeft() {
        if (!isActivation) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    postLeft();
                }
            }, 1000 / 60);
            return;
        }

        if (currentPosition == POSITION_INVAL || currentPosition == POSITION_LEFT) {
            Log.d("scott", "current position is already left");
            return;
        }

        currentPosition = POSITION_INVAL;
        final int delta = 0 - backgroundSubRec.left;
        int tempStep = delta / animationDuration;
        if (tempStep == 0) tempStep = -1;
        final int step = tempStep;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isActivation) return;
                invalidate();
                if (backgroundSubRec.left > 0) {
                    backgroundSubRec.left += step;
                    backgroundSubRec.right += step;
                    handler.postDelayed(this, fps);
                } else {
                    currentPosition = POSITION_LEFT;
                }
            }
        }, fps);
    }

    public void postCenter() {

        if (!isActivation) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    postCenter();
                }
            }, 1000 / 60);
            return;
        }

        if (currentPosition == POSITION_INVAL || currentPosition == POSITION_CENTER) {
            Log.d("scott", "current position is already center");
            return;
        }

        currentPosition = POSITION_INVAL;
        final int delta = defaultLeft - backgroundSubRec.left;
        int tempStep = delta / animationDuration;
        if (tempStep == 0) {
            if (delta > 0) {
                tempStep = 1;
            } else {
                tempStep = -1;
            }
        }
        final int step = tempStep;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isActivation) return;
                invalidate();
                if (step > 0) {
                    if (backgroundSubRec.left < defaultLeft) {
                        backgroundSubRec.left += step;
                        backgroundSubRec.right += step;
                        handler.postDelayed(this, fps);
                    } else {
                        currentPosition = POSITION_CENTER;
                    }
                } else {
                    if (backgroundSubRec.left > defaultLeft) {
                        backgroundSubRec.left += step;
                        backgroundSubRec.right += step;
                        handler.postDelayed(this, fps);
                    } else {
                        currentPosition = POSITION_CENTER;
                    }
                }

            }
        }, fps);
    }

    public static void enableCache() {
        sIsEnableCache = true;
    }

    public static void disableCache() {
        sIsEnableCache = false;
        releaseCache();
    }

    public static void releaseCache() {
        sCache.evictAll();
    }
}
