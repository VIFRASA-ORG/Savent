package Helper;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import com.vitandreasorino.savent.R;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class AnimationHelper {

    public static final int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

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

    public static final void fadeOut(View view, int duration){
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);

        view.animate().alpha(0f)
                .setDuration(duration)
                .setListener(null);
    }

    public static final void fadeIn(View view, int duration){
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate().alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

}
