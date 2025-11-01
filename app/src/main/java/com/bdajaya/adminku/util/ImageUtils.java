package com.bdajaya.adminku.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageUtils {

    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int JPEG_QUALITY = 85;

    public static String encodeImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                return null;
            }

            // Resize bitmap if needed
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_DIMENSION);

            // Convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }

        float scale;
        if (width > height) {
            scale = (float) maxDimension / width;
        } else {
            scale = (float) maxDimension / height;
        }

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    public static List<File> exportBase64ImagesToFiles(Context context, List<String> base64Images) {
        List<File> files = new ArrayList<>();
        File cacheDir = new File(context.getCacheDir(), "shared_images");

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        for (String base64Image : base64Images) {
            try {
                Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                if (bitmap != null) {
                    File file = new File(cacheDir, UUID.randomUUID().toString() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
                    fos.flush();
                    fos.close();
                    files.add(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return files;
    }

    public static void cleanupExportedImages(List<File> files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}

