package com.abellimz.sgbusbuzz.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.ArrayMap;
import android.util.Property;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;

import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;

/**
 * Created by Abel on 9/20/2016.
 */

public class AnimUtils {

    public static final int MORPH_DURATION_FAB = 200;
    public static final int MORPH_DURATION_MAP = 200;
    public static final int CLOSE_DURATION_MAP = 150;
    private static final int RETURN_DEGREE = 85;
    private static final int OUT_DEGREE = 85;
    private static final float FAB_SCALE = 4f;
    private static int colorFabOriginal = Color.parseColor("#FF4081");
    private static int originalFabX;
    private static int originalFabY;
    private static Interpolator fastOutSlowIn;
    private static Interpolator fastOutLinearIn;
    private static Interpolator linearOutSlowIn;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void animateOpenMap(final View fab, final View mapContainer, View overlay, final Animator.AnimatorListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getDarkenOverlayAnim(overlay).start();
            mapContainer.setVisibility(View.VISIBLE);
            return;
        }
        originalFabX = fab.getLeft() + fab.getWidth() / 2;
        originalFabY = fab.getTop() + fab.getHeight() / 2 + (int) fab.getTranslationY();
        ViewCompat.setZ(fab, ViewUtils.dpToPx(21));
        ViewCompat.setZ(mapContainer, ViewUtils.dpToPx(21));
        ViewCompat.setZ(overlay, ViewUtils.dpToPx(20));
        ArcAnimator arcAnimator = ArcAnimator.createArcAnimator(fab,
                fab.getRootView(), OUT_DEGREE, Side.LEFT)
                .setDuration(MORPH_DURATION_FAB);

        colorFabOriginal = ((FloatingActionButton) fab).getColorNormal();
        Animator colorAnimator = getFabColorAnimator((FloatingActionButton) fab,
                colorFabOriginal, Color.WHITE);

        Interpolator upInterpolator = new DecelerateInterpolator();
        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(fab, "scaleY", FAB_SCALE);
        imgScaleUpYAnim.setDuration(MORPH_DURATION_FAB);
        imgScaleUpYAnim.setInterpolator(upInterpolator);
        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(fab, "scaleX", FAB_SCALE);
        imgScaleUpXAnim.setDuration(MORPH_DURATION_FAB);
        imgScaleUpXAnim.setInterpolator(upInterpolator);

        Animator overlayAnim = getDarkenOverlayAnim(overlay);

        AnimatorSet fabSet = new AnimatorSet();
        fabSet.play(imgScaleUpXAnim).with(imgScaleUpYAnim).with(colorAnimator).with(overlayAnim);
        fabSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Animator mapAnimator = getMapOpenAnimator(mapContainer, listener);
                mapAnimator.start();

            }
        });
        arcAnimator.start();
        fabSet.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void animateCloseMap(final View fab, final View mapContainer, final View overlay, final Animator.AnimatorListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getBrightenOverlayAnim(overlay).start();
            mapContainer.setVisibility(View.INVISIBLE);
            return;
        }
        final ArcAnimator arcAnimator = ArcAnimator.createArcAnimator(fab, originalFabX,
                originalFabY, RETURN_DEGREE, Side.LEFT);

        Animator colorAnimator = getFabColorAnimator((FloatingActionButton) fab,
                Color.WHITE, colorFabOriginal);

        Interpolator downInterpolator = new FastOutSlowInInterpolator();
        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(fab, "scaleY", 1f);
        imgScaleDownYAnim.setDuration(MORPH_DURATION_FAB);
        imgScaleDownYAnim.setInterpolator(downInterpolator);
        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(fab, "scaleX", 1f);
        imgScaleDownXAnim.setDuration(MORPH_DURATION_FAB);
        imgScaleDownXAnim.setInterpolator(downInterpolator);

        Animator overlayAnim = getBrightenOverlayAnim(overlay);

        final AnimatorSet fabSet = new AnimatorSet();
        fabSet.playTogether(imgScaleDownXAnim, imgScaleDownYAnim, colorAnimator, overlayAnim);
        if (listener != null) {
            fabSet.addListener(listener);
        }
        fabSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewCompat.setZ(fab, ViewUtils.dpToPx(6));
                ViewCompat.setZ(mapContainer, ViewUtils.dpToPx(1));
                ViewCompat.setZ(overlay, ViewUtils.dpToPx(20));
                fab.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        });
        Animator mapAnimator = getMapCloseAnimator(mapContainer, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                arcAnimator.start();
                fabSet.start();
                super.onAnimationEnd(animation);
            }
        });
        mapAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator getMapOpenAnimator(View mapContainer, Animator.AnimatorListener listener) {
        // get the center for the clipping circle
        final int initialRadius = ViewUtils.dpToPx((int) (FAB_SCALE * 28));
        final int finalRadius = (int) Math.hypot(mapContainer.getWidth(), mapContainer.getHeight());
        int cx = (int) mapContainer.getPivotX();
        int cy = (int) mapContainer.getPivotY();
        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(mapContainer, cx, cy, initialRadius, finalRadius);

        // make the view visible and start the animation
        anim.setDuration(MORPH_DURATION_MAP);
        mapContainer.setVisibility(View.VISIBLE);
        if (listener != null) {
            anim.addListener(listener);
        }
        return anim;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Animator getMapCloseAnimator(final View mapContainer, Animator.AnimatorListener listener) {
        // get the center for the clipping circle
        int cx = (int) mapContainer.getPivotX();
        int cy = (int) mapContainer.getPivotY();

        // get the initial radius for the clipping circle'
        final int finalRadius = ViewUtils.dpToPx((int) (FAB_SCALE * 28));
        int initialRadius = (int) Math.hypot(mapContainer.getWidth(), mapContainer.getHeight());

        // create the animation (the final radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(mapContainer, cx, cy, initialRadius, finalRadius);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mapContainer.setVisibility(View.INVISIBLE);
            }
        });
        anim.setInterpolator(new LinearInterpolator());
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.setDuration(CLOSE_DURATION_MAP);
        return anim;
    }

    public static Animator getFabColorAnimator(final FloatingActionButton fab, int colorFrom, int colorTo) {
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimator.setDuration(MORPH_DURATION_FAB); // milliseconds
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                fab.setColorNormal((int) animator.getAnimatedValue());
            }

        });
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        return colorAnimator;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void circularReveal(View startView, final View revealView, View filledView, int duration, Animator.AnimatorListener listener) {
        // get the center for the clipping circle
        int cx = startView.getLeft() + startView.getWidth() / 2;
        int cy = startView.getTop() + startView.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = (int) Math.hypot(filledView.getWidth(), filledView.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(revealView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        anim.setDuration(duration);
        revealView.setVisibility(View.VISIBLE);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void circularExit(final View exitView, View endView, int duration, Animator.AnimatorListener listener) {
        // get the center for the clipping circle
        int cx = endView.getLeft() + endView.getMeasuredWidth() / 2;
        int cy = endView.getTop() + endView.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = (int) Math.hypot(exitView.getWidth(), exitView.getHeight());

        // create the animation (the final radius is zero)
        Animator anim =
                null;
        anim = ViewAnimationUtils.createCircularReveal(exitView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                exitView.setVisibility(View.GONE);
            }
        });
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.setDuration(duration);

        // start the animation
        anim.start();
    }

    public static void animateTouchView(View view, AnimatorListenerAdapter listener) {
        AnimatorSet beforeAnimator = new AnimatorSet();
        Interpolator upInterpolator = new DecelerateInterpolator();
        ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1.2f);
        imgScaleUpYAnim.setDuration(200);
        imgScaleUpYAnim.setInterpolator(upInterpolator);
        ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1.2f);
        imgScaleUpXAnim.setDuration(200);
        imgScaleUpXAnim.setInterpolator(upInterpolator);
        beforeAnimator.play(imgScaleUpXAnim).with(imgScaleUpYAnim);

        AnimatorSet afterAnimator = new AnimatorSet();
        Interpolator downInterpolator = new AccelerateInterpolator();
        ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        imgScaleDownYAnim.setDuration(200);
        imgScaleDownYAnim.setInterpolator(downInterpolator);
        ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        imgScaleDownXAnim.setDuration(200);
        imgScaleDownXAnim.setInterpolator(downInterpolator);
        afterAnimator.play(imgScaleDownXAnim).with(imgScaleDownYAnim);

        AnimatorSet fullAnimator = new AnimatorSet();
        fullAnimator.play(afterAnimator).after(beforeAnimator);
        fullAnimator.addListener(listener);
        fullAnimator.start();
    }


    public static Animator getBrightenOverlayAnim(final View overlay) {
        int currentColor = ((ColorDrawable) overlay.getBackground()).getColor();
        ValueAnimator brightenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                currentColor,
                Color.TRANSPARENT);
        brightenAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                overlay.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        brightenAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }
        });

        brightenAnim.setDuration(MORPH_DURATION_FAB);
        return brightenAnim;
    }

    public static Animator getDarkenOverlayAnim(final View overlay) {
        overlay.setVisibility(View.VISIBLE);
        int currentColor = ((ColorDrawable) overlay.getBackground()).getColor();
        ValueAnimator darkenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                currentColor,
                Color.parseColor("#6f000000"));
        darkenAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                overlay.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        darkenAnim.setDuration(MORPH_DURATION_FAB);
        return darkenAnim;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Interpolator getFastOutSlowInInterpolator(Context context) {
        if (fastOutSlowIn == null) {
            fastOutSlowIn = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in);
        }
        return fastOutSlowIn;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Interpolator getFastOutLinearInInterpolator(Context context) {
        if (fastOutLinearIn == null) {
            fastOutLinearIn = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_linear_in);
        }
        return fastOutLinearIn;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Interpolator getLinearOutSlowInInterpolator(Context context) {
        if (linearOutSlowIn == null) {
            linearOutSlowIn = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.linear_out_slow_in);
        }
        return linearOutSlowIn;
    }

    /**
     * https://halfthought.wordpress.com/2014/11/07/reveal-transition/
     * <p/>
     * Interrupting Activity transitions can yield an OperationNotSupportedException when the
     * transition tries to pause the animator. Yikes! We can fix this by wrapping the Animator:
     */
    public static class NoPauseAnimator extends Animator {
        private final Animator mAnimator;
        private final ArrayMap<AnimatorListener, AnimatorListener> mListeners =
                new ArrayMap<AnimatorListener, AnimatorListener>();

        public NoPauseAnimator(Animator animator) {
            mAnimator = animator;
        }

        @Override
        public void addListener(AnimatorListener listener) {
            AnimatorListener wrapper = new AnimatorListenerWrapper(this, listener);
            if (!mListeners.containsKey(listener)) {
                mListeners.put(listener, wrapper);
                mAnimator.addListener(wrapper);
            }
        }

        @Override
        public void cancel() {
            mAnimator.cancel();
        }

        @Override
        public void end() {
            mAnimator.end();
        }

        @Override
        public long getDuration() {
            return mAnimator.getDuration();
        }

        @Override
        public TimeInterpolator getInterpolator() {
            return mAnimator.getInterpolator();
        }

        @Override
        public void setInterpolator(TimeInterpolator timeInterpolator) {
            mAnimator.setInterpolator(timeInterpolator);
        }

        @Override
        public ArrayList<AnimatorListener> getListeners() {
            return new ArrayList<AnimatorListener>(mListeners.keySet());
        }

        @Override
        public long getStartDelay() {
            return mAnimator.getStartDelay();
        }

        @Override
        public void setStartDelay(long delayMS) {
            mAnimator.setStartDelay(delayMS);
        }

        @Override
        public boolean isPaused() {
            return mAnimator.isPaused();
        }

        @Override
        public boolean isRunning() {
            return mAnimator.isRunning();
        }

        @Override
        public boolean isStarted() {
            return mAnimator.isStarted();
        }

        /* We don't want to override pause or resume methods because we don't want them
         * to affect mAnimator.
        public void pause();

        public void resume();

        public void addPauseListener(AnimatorPauseListener listener);

        public void removePauseListener(AnimatorPauseListener listener);
        */

        @Override
        public void removeAllListeners() {
            mListeners.clear();
            mAnimator.removeAllListeners();
        }

        @Override
        public void removeListener(AnimatorListener listener) {
            AnimatorListener wrapper = mListeners.get(listener);
            if (wrapper != null) {
                mListeners.remove(listener);
                mAnimator.removeListener(wrapper);
            }
        }

        @Override
        public Animator setDuration(long durationMS) {
            mAnimator.setDuration(durationMS);
            return this;
        }

        @Override
        public void setTarget(Object target) {
            mAnimator.setTarget(target);
        }

        @Override
        public void setupEndValues() {
            mAnimator.setupEndValues();
        }

        @Override
        public void setupStartValues() {
            mAnimator.setupStartValues();
        }

        @Override
        public void start() {
            mAnimator.start();
        }
    }

    static class AnimatorListenerWrapper implements Animator.AnimatorListener {
        private final Animator mAnimator;
        private final Animator.AnimatorListener mListener;

        public AnimatorListenerWrapper(Animator animator, Animator.AnimatorListener listener) {
            mAnimator = animator;
            mListener = listener;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            mListener.onAnimationStart(mAnimator);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mListener.onAnimationEnd(mAnimator);
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            mListener.onAnimationCancel(mAnimator);
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
            mListener.onAnimationRepeat(mAnimator);
        }
    }

    /**
     * An implementation of {@link android.util.Property} to be used specifically with fields of
     * type
     * <code>float</code>. This type-specific subclass enables performance benefit by allowing
     * calls to a {@link #set(Object, Float) set()} function that takes the primitive
     * <code>float</code> type and avoids autoboxing and other overhead associated with the
     * <code>Float</code> class.
     *
     * @param <T> The class on which the Property is declared.
     **/
    public static abstract class FloatProperty<T> extends Property<T, Float> {
        public FloatProperty(String name) {
            super(Float.class, name);
        }

        /**
         * A type-specific override of the {@link #set(Object, Float)} that is faster when dealing
         * with fields of type <code>float</code>.
         */
        public abstract void setValue(T object, float value);

        @Override
        final public void set(T object, Float value) {
            setValue(object, value);
        }
    }

    /**
     * An implementation of {@link android.util.Property} to be used specifically with fields of
     * type
     * <code>int</code>. This type-specific subclass enables performance benefit by allowing
     * calls to a {@link #set(Object, Integer) set()} function that takes the primitive
     * <code>int</code> type and avoids autoboxing and other overhead associated with the
     * <code>Integer</code> class.
     *
     * @param <T> The class on which the Property is declared.
     */
    public static abstract class IntProperty<T> extends Property<T, Integer> {

        public IntProperty(String name) {
            super(Integer.class, name);
        }

        /**
         * A type-specific override of the {@link #set(Object, Integer)} that is faster when dealing
         * with fields of type <code>int</code>.
         */
        public abstract void setValue(T object, int value);

        @Override
        final public void set(T object, Integer value) {
            setValue(object, value.intValue());
        }

    }

}
