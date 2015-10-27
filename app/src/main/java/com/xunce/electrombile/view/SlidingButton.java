package com.xunce.electrombile.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by lybvinci on 2015/10/27.
 */
public class SlidingButton extends Button {

    private boolean isMe = false;

    public SlidingButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SlidingButton(Context context) {
        super(context);
    }

    public SlidingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isMe = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isMe = false;
        }
        return false;
    }

    public boolean handleActivityEvent(MotionEvent activityEvent) {
        boolean result = false;
        if (isMe()) {
            if (activityEvent.getAction() == MotionEvent.ACTION_UP) {
                if (this.getLeft() + this.getWidth() / 2 > ((FrameLayout) this.getParent().getParent()).getWidth() - this.getWidth()) {
                    //用户完成了选择动作
                    //停留在右边
                    this.setGravity(Gravity.RIGHT);
                    this.setMe(false);
                    result = true;
                } else {
                    TranslateAnimation trans =
                            new TranslateAnimation(
                                    Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, -this.getLeft(), Animation.RELATIVE_TO_SELF, 0,
                                    Animation.RELATIVE_TO_SELF, 0);

                    trans.setDuration(600);
                    trans.setInterpolator(new AccelerateInterpolator());
                    trans.setInterpolator(new Interpolator() {

                        @Override
                        public float getInterpolation(float input) {
                            // TODO Auto-generated method stub
                            return 0;
                        }
                    });
                    trans.setAnimationListener(new SlidingAnimationListener(this));
                    startAnimation(trans);
                }
            } else {
                // 还在拖动
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) getLayoutParams();
                lp.leftMargin = (int) (activityEvent.getX() - ((FrameLayout) this.getParent().getParent()).getLeft()) - this.getWidth() / 2;
                if (lp.leftMargin > 0 && lp.leftMargin < ((FrameLayout) this.getParent().getParent()).getWidth() - this.getWidth()) {
                    setLayoutParams(lp);

                }
            }
        }
        return result;
    }

    private class SlidingAnimationListener implements AnimationListener {

        private SlidingButton but;

        public SlidingAnimationListener(SlidingButton button) {
            this.but = button;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            rePosition();
            but.setMe(false);
            but.clearAnimation();
        }

        private void rePosition() {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) but
                    .getLayoutParams();
            lp.leftMargin = 0;
            but.setLayoutParams(lp);

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

    }

}
