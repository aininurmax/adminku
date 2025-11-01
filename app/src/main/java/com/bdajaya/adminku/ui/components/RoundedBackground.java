package com.bdajaya.adminku.ui.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import androidx.annotation.ColorInt;

public final class RoundedBackground {
    private RoundedBackground(){}

    public static GradientDrawable build(Context ctx,
                                         float corner,
                                         @ColorInt int bg,
                                         @ColorInt int stroke,
                                         int strokePx,
                                         int positionGroup /*0=first,1=middle,2=last,3=solo*/) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(bg);
        d.setStroke(strokePx, stroke);

        float[] r = new float[8]; // tl,tr,br,bl (each has x,y)
        switch (positionGroup) {
            case 0: // first
                r = new float[]{corner,corner, corner,corner, 0,0, 0,0}; break;
            case 1: // middle
                r = new float[]{0,0, 0,0, 0,0, 0,0}; break;
            case 2: // last
                r = new float[]{0,0, 0,0, corner,corner, corner,corner}; break;
            case 3: // solo
            default:
                r = new float[]{corner,corner, corner,corner, corner,corner, corner,corner}; break;
        }
        d.setCornerRadii(r);
        return d;
    }

    public static int dp(Context c, float dp){
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics()));
    }
}

