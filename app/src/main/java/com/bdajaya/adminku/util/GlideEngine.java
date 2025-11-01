package com.bdajaya.adminku.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.luck.picture.lib.engine.ImageEngine;
import com.luck.picture.lib.utils.ActivityCompatHelper;

public class GlideEngine implements ImageEngine {

    private static GlideEngine instance;

    private GlideEngine() {
    }

    public static GlideEngine createGlideEngine() {
        if (instance == null) {
            synchronized (GlideEngine.class) {
                if (instance == null) {
                    instance = new GlideEngine();
                }
            }
        }
        return instance;
    }

    @Override
    public void loadImage(Context context, String url, ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, ImageView imageView, String url, int maxWidth, int maxHeight) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .override(maxWidth, maxHeight)
                .into(imageView);
    }

    @Override
    public void loadAlbumCover(Context context, String url, ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        // Gunakan MultiTransformation untuk menghindari varargs warning
        Glide.with(context)
                .load(url)
                .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners(8)))
                .into(imageView);
    }

    @Override
    public void loadGridImage(Context context, String url, ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        // Gunakan MultiTransformation untuk menghindari varargs warning
        Glide.with(context)
                .load(url)
                .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners(8)))
                .into(imageView);
    }

    @Override
    public void pauseRequests(Context context) {
        Glide.with(context).pauseRequests();
    }

    @Override
    public void resumeRequests(Context context) {
        Glide.with(context).resumeRequests();
    }
}