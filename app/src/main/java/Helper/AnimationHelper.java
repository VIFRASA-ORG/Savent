package Helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * Class used to perform some basic animation to Views.
 */
public class AnimationHelper {

    /**
     * Perform the conversion from a value in dp to a value in Px
     * @param dp value to convert
     * @param context context in which the value is to be converted
     * @return the value in Px
     */
    public static final int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Perform a slide animation of the view given as parameter
     *
     * @param view the view to be slided
     * @param currentHeight the starting height of the view
     * @param newHeight the final height of the view
     * @param duration the duration of the animation
     */
    public static final void slideView(View view, int currentHeight, int newHeight, int duration) {

        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(duration);

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */
        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
        });

        /*  We use an animationSet to play the animation  */
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    /**
     * Perform a fadeOut animation of the view given as parameter
     *
     * @param view the view to be animated
     * @param duration the duration of the animation
     */
    public static final void fadeOut(View view, int duration){
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);

        view.animate().alpha(0f)
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.setVisibility(View.INVISIBLE);
                }
            });
    }

    /**
     * Perform a fadeOut animation of the view given as parameter
     * The final visibility status of the given view is View.GONE
     *
     * @param view the view to be animated
     * @param duration the duration of the animation
     */
    public static final void fadeOutWithGone(View view, int duration){
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);

        view.animate().alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Perform a fadeIn animation to the view given as parameter.
     *
     * @param view the view to ba animated
     * @param duration the duration of the animation
     */
    public static final void fadeIn(View view, int duration){
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate().alpha(1f)
            .setDuration(duration)
            .setListener(null);
    }

    /**
     * Switch between the selected image into the imageView and the given image.
     * The switch takes place with a fade out/in animation.
     *
     * @param imageView the source imageView
     * @param resId the resource id of the new image.
     */
    public static final void switchImageWithFadeAnimations(ImageView imageView, @DrawableRes int resId){
        imageView.animate()
            .alpha(0f)
            .setDuration(100)
            .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) { }
                @Override
                public void onAnimationEnd(Animator animator) {
                    imageView.setImageResource(resId);
                    imageView.animate().alpha(1).setDuration(100);
                }
                @Override
                public void onAnimationCancel(Animator animator) { }
                @Override
                public void onAnimationRepeat(Animator animator) { }
            });
    }

    /**
     * Switch between the actual text into the textView and the given new text.
     * The switch takes places with a fade out/in animation.
     * If color is set, the animation include also a text color change.
     *
     * @param textView the source textView
     * @param resId the resource id of the new string.
     * @param color the resources of the new color.
     */
    public static final void switchTextWithFadeAnimation(TextView textView, @StringRes int resId, @ColorRes @Nullable Integer color){
        textView.animate()
            .alpha(0f)
            .setDuration(100)
            .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) { }
                @SuppressLint("ResourceAsColor")
                @Override
                public void onAnimationEnd(Animator animator) {
                    textView.setText(resId);
                    textView.animate().alpha(1).setDuration(100);
                    if(color != null) textView.setTextColor(color);
                }
                @Override
                public void onAnimationCancel(Animator animator) { }
                @Override
                public void onAnimationRepeat(Animator animator) { }
            });
    }

}
