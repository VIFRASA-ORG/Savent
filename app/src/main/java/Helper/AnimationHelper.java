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
 * Classe Helper che fornisce dei metodi semplici per eseguire
 * delle animazioni su componenti View.
 */
public class AnimationHelper {

    /**
     * Metodo che converte una dimensione in dp a un valore in px.
     *
     * @param dp valore da convertire.
     * @param context contesto dell'activity chiamante.
     * @return il valore in px corrispondete al valore passato come paramentro.
     */
    public static final int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
    
    /**
     * Metodo che esegue uno spostamento lineare animato della vista passata come parametro.
     *
     * @param view la vista da spostare in maniera animata.
     * @param currentHeight l'altezza iniziale della vista.
     * @param newHeight l'altezza finale della vista.
     * @param duration la durata dell'animazione.
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
     * Metodo che esegue un animazione fade out della view passata come parametro.
     *
     * @param view la vista da animare.
     * @param duration la durata dell'animazione.
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
     * Metodo che esegue un animazione fade out della view data come parametro.
     * Lo stato finale di visibility della vista sarà View.GONE.
     *
     * @param view la vista da animare.
     * @param duration la durata dell'animazione.
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
     * Metodo che esegue un animazione fade in della view passata come parametro.
     *
     * @param view la vista da animare.
     * @param duration la durata dell'animazione.
     */
    public static final void fadeIn(View view, int duration){
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate().alpha(1f)
            .setDuration(duration)
            .setListener(null);
    }

    /**
     * Metodo che esegue un cambio animato dell'immagine all'interno dell'ImageView passata come parametro.
     * Lo switch avviene mediante una animazione fade in/out.
     *
     * @param imageView l'imageView di cui cambiare l'immagine
     * @param resId l'id della risorsa della nuova immagine da impostare nell'imageView passata come parametro.
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
     * Metodo che esegue un cambio animato di testo all'interno di una textView.
     * Lo switch avviene mediante una animazione fade in/out.
     * Se viene passato come paramentro anche un colore, anche il colore subisce la stessa
     * animazione del testo.
     *
     * @param textView la textView in cui cambiare il testo.
     * @param resId la risorsa id della stringa da mettere nella text view.
     * @param color la risorsa del nuovo colore di stile del testo da impostare nella textView.
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
