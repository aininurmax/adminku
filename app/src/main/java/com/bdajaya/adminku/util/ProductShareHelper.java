package com.bdajaya.adminku.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.bdajaya.adminku.data.entity.Product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

/**
 * Helper untuk sharing produk ke aplikasi lain.
 *
 * Menggunakan FileProvider untuk sharing file secara aman.
 * Support callback untuk monitoring hasil share dan background cleanup.
 */
public class ProductShareHelper {

    public interface ShareCallback {
        void onShareStarted();
        void onShareCompleted();
        void onShareFailed(String error);
    }

    private static final String AUTHORITY = "com.bdajaya.adminku.fileprovider";

    // Background cleanup after share completes using WorkManager
    private static final int SHARE_COMPLETION_CLEANUP_DELAY_MINUTES = 1; // 1 minute delay

    /**
     * Share produk dengan semua gambar ke aplikasi lain.
     *
     * @param context Context
     * @param product Product yang akan di-share
     * @param imageFiles List file gambar dari ImageStorageManager
     */
    public static void shareProduct(Context context, Product product, List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            shareProductTextOnly(context, product);
            return;
        }

        Intent shareIntent = new Intent();

        if (imageFiles.size() == 1) {
            // Single image
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");

            Uri imageUri = FileProvider.getUriForFile(context, AUTHORITY, imageFiles.get(0));
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

            // Explicitly grant URI permission for the target app
            context.grantUriPermission(String.valueOf(shareIntent.resolveActivity(context.getPackageManager())),
                    imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            // Multiple images
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/jpeg");

            ArrayList<Uri> imageUris = new ArrayList<>();
            for (File file : imageFiles) {
                Uri uri = FileProvider.getUriForFile(context, AUTHORITY, file);
                imageUris.add(uri);
            }
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);

            // Explicitly grant URI permissions for all images to the target app
            String targetPackage = String.valueOf(shareIntent.resolveActivity(context.getPackageManager()));
            if (targetPackage != null) {
                for (Uri uri : imageUris) {
                    context.grantUriPermission(targetPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
        }

        // Tambahkan teks deskripsi produk
        String shareText = buildProductShareText(product);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, product.getName());

        // Grant permission untuk apps lain
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Show chooser
        Intent chooser = Intent.createChooser(shareIntent, "Bagikan Produk");
        context.startActivity(chooser);
    }

    /**
     * Share ke WhatsApp secara spesifik.
     */
    public static void shareToWhatsApp(Context context, Product product, List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            shareProductTextOnlyToWhatsApp(context, product);
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setPackage("com.whatsapp");

        if (imageFiles.size() == 1) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");

            Uri imageUri = FileProvider.getUriForFile(context, AUTHORITY, imageFiles.get(0));
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

            // Explicitly grant URI permission to WhatsApp
            context.grantUriPermission("com.whatsapp", imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/jpeg");

            ArrayList<Uri> imageUris = new ArrayList<>();
            for (File file : imageFiles) {
                Uri uri = FileProvider.getUriForFile(context, AUTHORITY, file);
                imageUris.add(uri);
            }
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);

            // Explicitly grant URI permissions for all images to WhatsApp
            for (Uri uri : imageUris) {
                context.grantUriPermission("com.whatsapp", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        String shareText = buildProductShareText(product);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException e) {
            // WhatsApp not installed, fallback to regular share
            shareProduct(context, product, imageFiles);
        }
    }

    /**
     * Share text-only jika tidak ada gambar.
     */
    private static void shareProductTextOnly(Context context, Product product) {
        String shareText = buildProductShareText(product);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, product.getName());

        Intent chooser = Intent.createChooser(shareIntent, "Bagikan Produk");
        context.startActivity(chooser);
    }

    /**
     * Share text-only ke WhatsApp.
     */
    private static void shareProductTextOnlyToWhatsApp(Context context, Product product) {
        String shareText = buildProductShareText(product);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setPackage("com.whatsapp");
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            context.startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException e) {
            shareProductTextOnly(context, product);
        }
    }

    /**
     * Share produk dengan callback untuk monitoring hasil.
     * Akan otomatis cleanup cache setelah share selesai.
     */
    public static void shareProductWithCallback(Context context, Product product,
                                                List<File> imageFiles, ShareCallback callback) {
        if (callback != null) {
            callback.onShareStarted();
        }

        try {
            if (imageFiles == null || imageFiles.isEmpty()) {
                shareProductTextOnly(context, product);
            } else {
                shareProduct(context, product, imageFiles);
            }

            // Asumsi share berhasil jika startActivity berhasil
            if (callback != null) {
                callback.onShareCompleted();
            }

            // Schedule background cleanup setelah delay
            scheduleCleanup(context);

        } catch (Exception e) {
            if (callback != null) {
                callback.onShareFailed(e.getMessage());
            }
        }
    }

    /**
     * Share ke WhatsApp dengan callback.
     */
    public static void shareToWhatsAppWithCallback(Context context, Product product,
                                                   List<File> imageFiles, ShareCallback callback) {
        if (callback != null) {
            callback.onShareStarted();
        }

        try {
            if (imageFiles == null || imageFiles.isEmpty()) {
                shareProductTextOnlyToWhatsApp(context, product);
            } else {
                shareToWhatsApp(context, product, imageFiles);
            }

            if (callback != null) {
                callback.onShareCompleted();
            }

            // Schedule background cleanup
            scheduleCleanup(context);

        } catch (Exception e) {
            if (callback != null) {
                callback.onShareFailed(e.getMessage());
            }
        }
    }

    /**
     * Schedule background cleanup menggunakan WorkManager.
     * Cleanup akan terjadi secara otomatis setelah delay yang ditentukan.
     */
    private static void scheduleCleanup(Context context) {
        // Create one-time work request untuk cleanup cache
        WorkRequest cleanupWorkRequest = new OneTimeWorkRequest.Builder(ShareCacheCleanup.class)
                .setInitialDelay(SHARE_COMPLETION_CLEANUP_DELAY_MINUTES, TimeUnit.MINUTES)
                .build();

        // Enqueue work
        WorkManager.getInstance(context).enqueue(cleanupWorkRequest);
    }

    /**
     * Force immediate cleanup share cache jika diperlukan.
     */
    public static void forceCleanupSharingCache(Context context) {
        try {
            if (context.getApplicationContext() instanceof com.bdajaya.adminku.AdminkuApplication) {
                com.bdajaya.adminku.AdminkuApplication app =
                        (com.bdajaya.adminku.AdminkuApplication) context.getApplicationContext();
                app.getImageStorageManager().cleanupSharingCache();
            }
        } catch (Exception e) {
            android.util.Log.e("ProductShareHelper", "Error during force cleanup", e);
        }
    }

    /**
     * Build formatted text untuk sharing.
     */
    private static String buildProductShareText(Product product) {
        StringBuilder sb = new StringBuilder();

        sb.append("🛍️ *").append(product.getName()).append("*\n\n");

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            sb.append("📝 ").append(product.getDescription()).append("\n\n");
        }

        sb.append("💰 Harga: ").append(CurrencyFormatter.formatCurrency(product.getSellPrice())).append("\n");

        if (product.getStock() > 0) {
            sb.append("📦 Stok: ").append(product.getStock()).append("\n");
        } else {
            sb.append("⚠️ Stok: Habis\n");
        }

        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            sb.append("🔖 Kode: ").append(product.getBarcode()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Check apakah WhatsApp terinstall.
     */
    public static boolean isWhatsAppInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.whatsapp", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check apakah WhatsApp Business terinstall.
     */
    public static boolean isWhatsAppBusinessInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.whatsapp.w4b", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
