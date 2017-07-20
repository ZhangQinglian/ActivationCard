# 从未见过如此美丽动人的CardView

故事还得从看到那张动图说起。

像往常一样，休息时间我都会打开[uplabs](https://www.uplabs.com)浏览一下国外大佬们的UI设计。

有个设计十分吸引眼球，就是下图。

![](http://7xprgn.com1.z0.glb.clouddn.com/preview.gif)

仔细看每张图片，在加载出来的时候背景都会有一个偏移的动效，简约而不简单。

这个能实现吗？如果公司UI团队给了这么一个效果图，你该咋办？

## 思路
首先说说思路，既然要做，显得有个载体吧，可能很多同学一下子就想到了`ImageView`这个东西。但现在是设么年代了？Material Design的呀，所以再用ImageView是不是有点low了。所以自然想到就应该是`CardView`嘛。

但CardView有个蛋疼的设定，不能设置背景图片，不知道小伙伴们发现了没有？

stackoverflow上的答案过于简单粗暴，不是我的菜。

![](http://7xprgn.com1.z0.glb.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202017-07-20%20%E4%B8%8B%E5%8D%884.17.27.png)

既然不用这种方法，那我们只能使用我们的神器`onDraw`了，从根本上解决问题。

## 代码

```java
@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((tobePaint!=null&&!tobePaint.isRecycled())) {
            canvas.drawBitmap(tobePaint, backgroundSubRec, backgroundRec, paint);
        }
    }
```
onDraw方法很简单，就是当有可绘制背景的时候就去绘制。

这里有四个变量要关注一下：

- tobePaint: 需要被绘制的Bitmap对象。
- backgroundSubRec：Rect对象，表示Bitmap中需要被绘制的区域。后续就是通过改变这个变量来达到动画效果。
- backgroundRec：Rect对象，表示绘制区域的大小，大小同CardView的大小。


最开始我们自定义的这个视图与普通的CardView没有差异，当调用完`public void enableActivation(Bitmap activationBg, String key)`这个方法后，背景就被绘制上去了，如下图。

![](http://7xprgn.com1.z0.glb.clouddn.com/device-2017-07-20-165223.png)


来看看代码

```java
    public void enableActivation(Bitmap activationBg, String key) {
        currentKey = key;
        isActivation = false;
        init(activationBg,key);
    }
```
具体看init这个方法：

```java
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
```

这个方法做了这么几件事：

- 确定CardView的长宽。
- 根据CardView的实际大小对传入的Bitmap进行适当放大，为后续动画做准备。
- 确定backgroundRec，backgroundSubRec这两个对象的值。
- 给tobePaint对象赋值。
- 调用invalidate()绘制背景。

为了方便理解，我画了如下这张图。

![](http://7xprgn.com1.z0.glb.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202017-07-20%20%E4%B8%8B%E5%8D%885.06.40.png)


下面是动画部分，这部分。

先来说说原理，上面绘制的图像是靠backgroundSubRec对tobePaint进行截取而来的，一开始backgroundSubRec截取的是放大后tobePaint的中间部分，其大小和CardView一致，接着通过不断的改变backgroundSubRec的值，让其慢慢向右移动。来截取tobePaint的右边部分。

![](http://7xprgn.com1.z0.glb.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202017-07-20%20%E4%B8%8B%E5%8D%885.59.29.png)

![](http://7xprgn.com1.z0.glb.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202017-07-20%20%E4%B8%8B%E5%8D%885.59.37.png)

下面是代码：

```java
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
```

最后是效果图。
![](http://7xprgn.com1.z0.glb.clouddn.com/activation.gif)

## 最后

虽然上面讲的比较简单，其实在这过程中有一些细节还是需要注意的，比如bitmap的格式最好使用RGB_565来减少内存占用，使用LruCahce来缓存Bitmap增加背景切换速度，还有就是背景放大的比例也需要根据实际需求做调整。

下面给出代码，

[github](https://github.com/ZhangQinglian/ActivationCard)