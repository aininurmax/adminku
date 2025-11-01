package com.bdajaya.adminku.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.engine.CropFileEngine;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;

import java.util.ArrayList;

/**
 * PictureSelector crop engine implementation backed by uCrop.
 */
public class UCropEngine implements CropFileEngine {

    private static final UCropImageEngine IMAGE_ENGINE = new UCropImageEngine() {
        @Override
        public void loadImage(Context context, String url, ImageView imageView) {
            Glide.with(context).load(url).into(imageView);
        }

        @Override
        public void loadImage(Context context,
                              Uri url,
                              int maxWidth,
                              int maxHeight,
                              OnCallbackListener<Bitmap> call) {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .override(maxWidth, maxHeight)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource,
                                                     Transition<? super Bitmap> transition) {
                            if (call != null) {
                                call.onCall(resource);
                            }
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // no-op
                        }
                    });
        }
    };

    @Override
    public void onStartCrop(Fragment fragment,
                            Uri srcUri,
                            Uri destinationUri,
                            ArrayList<String> dataSource,
                            int requestCode) {
        Context context = fragment.requireContext();

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);

        UCrop uCrop;
        if (dataSource != null && dataSource.size() > 1) {
            ArrayList<String> sources = new ArrayList<>(dataSource);
            uCrop = UCrop.of(srcUri, destinationUri, sources);
            uCrop.setImageEngine(IMAGE_ENGINE);
        } else {
            uCrop = UCrop.of(srcUri, destinationUri);
        }

        uCrop.withOptions(options)
                .start(context, fragment, requestCode);
    }
}
