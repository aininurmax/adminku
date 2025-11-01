package com.bdajaya.adminku.data.manager;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Centralized image storage management.
 *
 * Storage Strategy:
 * - Internal storage untuk keamanan dan kontrol penuh
 * - Struktur: /data/data/com.bdajaya.adminku/files/products/{productId}/{imageId}.jpg
 * - Kompresi otomatis untuk efisiensi
 * - Cache directory untuk sharing temporary files
 */
public class ImageStorageManager {

    private static final String TAG = "ImageStorageManager";

    // Konstanta
    private static final String PRODUCTS_DIR = "products";
    private static final String CACHE_SHARE_DIR = "share_cache";
    private static final int MAX_DIMENSION = 1920; // HD quality
    private static final int THUMBNAIL_SIZE = 512;
    private static final int JPEG_QUALITY = 90;

    private final Context context;

    public ImageStorageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // ================================
    // SAVE OPERATIONS
    // ================================

    /**
     * Simpan gambar dari Uri dan return path relatif.
     *
     * @param productId ID produk
     * @param imageUri Uri gambar dari picker
     * @return Path relatif (e.g., "products/prod_123/img_456.jpg")
     */
    @Nullable
    public String saveProductImage(@NonNull String productId, @NonNull Uri imageUri) {
        try {
            // Baca gambar
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            // Resize untuk efisiensi
            bitmap = resizeIfNeeded(bitmap, MAX_DIMENSION);

            // Generate ID unik
            String imageId = UUID.randomUUID().toString();
            String fileName = imageId + ".jpg";

            // Simpan ke internal storage
            File productDir = getProductDirectory(productId);
            if (!productDir.exists()) {
                productDir.mkdirs();
            }

            File imageFile = new File(productDir, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();

            // Return path relatif
            return PRODUCTS_DIR + "/" + productId + "/" + fileName;

        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }

    /**
     * Batch save multiple images.
     */
    @NonNull
    public List<String> saveProductImages(@NonNull String productId, @NonNull List<Uri> imageUris) {
        List<String> paths = new ArrayList<>();
        for (Uri uri : imageUris) {
            String path = saveProductImage(productId, uri);
            if (path != null) {
                paths.add(path);
            }
        }
        return paths;
    }

    // ================================
    // READ OPERATIONS
    // ================================

    /**
     * Load gambar dari path relatif.
     *
     * @param relativePath Path relatif dari database
     * @return File object atau null
     */
    @Nullable
    public File getImageFile(@NonNull String relativePath) {
        File file = new File(context.getFilesDir(), relativePath);
        return file.exists() ? file : null;
    }

    /**
     * Load sebagai Uri untuk Glide.
     */
    @Nullable
    public Uri getImageUri(@NonNull String relativePath) {
        File file = getImageFile(relativePath);
        return file != null ? Uri.fromFile(file) : null;
    }

    /**
     * Load sebagai Bitmap.
     */
    @Nullable
    public Bitmap getImageBitmap(@NonNull String relativePath) {
        File file = getImageFile(relativePath);
        if (file == null) return null;

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    // ================================
    // SHARING OPERATIONS
    // ================================

    /**
     * Prepare images untuk sharing (copy ke cache directory).
     * FileProvider hanya bisa akses folder cache atau external.
     *
     * @param relativePaths List path relatif dari database
     * @return List File yang siap di-share via FileProvider
     */
    @NonNull
    public List<File> prepareImagesForSharing(@NonNull List<String> relativePaths) {
        List<File> shareFiles = new ArrayList<>();
        File shareCacheDir = new File(context.getCacheDir(), CACHE_SHARE_DIR);

        // Bersihkan cache lama
        if (shareCacheDir.exists()) {
            deleteRecursive(shareCacheDir);
        }
        shareCacheDir.mkdirs();

        // Copy files ke cache
        for (String relativePath : relativePaths) {
            File sourceFile = getImageFile(relativePath);
            if (sourceFile != null && sourceFile.exists()) {
                File destFile = new File(shareCacheDir, sourceFile.getName());
                try {
                    copyFile(sourceFile, destFile);
                    shareFiles.add(destFile);
                } catch (IOException e) {
                    Log.e(TAG, "Error copying file for sharing", e);
                }
            }
        }

        return shareFiles;
    }

    /**
     * Bersihkan cache sharing setelah selesai.
     */
    public void cleanupSharingCache() {
        File shareCacheDir = new File(context.getCacheDir(), CACHE_SHARE_DIR);
        if (shareCacheDir.exists()) {
            deleteRecursive(shareCacheDir);
        }
    }

    // ================================
    // DELETE OPERATIONS
    // ================================

    /**
     * Hapus gambar berdasarkan path relatif.
     */
    public boolean deleteImage(@NonNull String relativePath) {
        File file = getImageFile(relativePath);
        return file != null && file.delete();
    }

    /**
     * Hapus semua gambar untuk produk tertentu.
     */
    public boolean deleteProductImages(@NonNull String productId) {
        File productDir = getProductDirectory(productId);
        if (productDir.exists()) {
            return deleteRecursive(productDir);
        }
        return true;
    }

    // ================================
    // UTILITY METHODS
    // ================================

    private File getProductDirectory(String productId) {
        return new File(context.getFilesDir(), PRODUCTS_DIR + "/" + productId);
    }

    private Bitmap resizeIfNeeded(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }

        float scale = Math.min(
                (float) maxDimension / width,
                (float) maxDimension / height
        );

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        if (resized != bitmap) {
            bitmap.recycle();
        }

        return resized;
    }

    private void copyFile(File source, File dest) throws IOException {
        java.io.FileInputStream fis = new java.io.FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);

        byte[] buffer = new byte[8192];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        fis.close();
        fos.close();
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    /**
     * Get total size gambar untuk produk tertentu (untuk monitoring).
     */
    public long getProductImagesSize(@NonNull String productId) {
        File productDir = getProductDirectory(productId);
        return getDirectorySize(productDir);
    }

    private long getDirectorySize(File directory) {
        long size = 0;
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += file.length();
                }
            }
        }
        return size;
    }

    /**
     * Get file path from Uri.
     * Supports content and file Uri types.
     *
     * @param uri Uri of the image
     * @return Absolute file path or null if not found
     */
    @Nullable
    public String getPathFromUri(@NonNull Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = null;
        try {
            // Content Uri
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
            
            // File Uri
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting path from Uri", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
