package com.bdajaya.adminku.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.dao.ProductImageDao;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.entity.ProductImage;
import com.bdajaya.adminku.data.model.ProductWithDetails;
import com.bdajaya.adminku.data.manager.ImageStorageManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository untuk Product dengan file-based image management.
 *
 * Improvements:
 * - Gambar disimpan sebagai file, bukan Base64
 * - Transaksi atomic untuk konsistensi data
 * - Support untuk sharing ke apps lain
 */
public class ProductRepository {
    private final ProductDao productDao;
    private final ProductImageDao productImageDao;
    private final ImageStorageManager imageStorage;

    public ProductRepository(Context context, ProductDao productDao, ProductImageDao productImageDao) {
        this.productDao = productDao;
        this.productImageDao = productImageDao;
        this.imageStorage = new ImageStorageManager(context);
    }

    // ================================
    // BASIC CRUD
    // ================================

    public LiveData<Product> getProductById(String id) {
        return productDao.getById(id);
    }

    public Product getProductByIdSync(String id) {
        return productDao.getByIdSync(id);
    }

    public Product getProductByBarcode(String barcode) {
        return productDao.getByBarcode(barcode);
    }

    public LiveData<List<Product>> getProductsByStatus(String status) {
        return productDao.getByStatus(status);
    }

    public List<Product> getProductsByStatusSync(String status) {
        return productDao.getByStatusSync(status);
    }

    public List<Product> searchProducts(String query, int limit) {
        return productDao.search(query, limit);
    }

    public LiveData<ProductWithDetails> getProductWithDetails(String id) {
        return productDao.getProductWithDetails(id);
    }

    public LiveData<List<ProductWithDetails>> getProductsWithDetailsByStatus(String status) {
        return productDao.getProductsWithDetailsByStatus(status);
    }

    public List<ProductWithDetails> searchProductsWithDetails(String query, int limit) {
        return productDao.searchWithDetails(query, limit);
    }

    // ================================
    // INSERT WITH IMAGES
    // ================================

    /**
     * Insert product dengan gambar (file-based).
     *
     * @param product Product entity
     * @param imageUris List Uri dari PictureSelector
     * @return Product ID
     */
    public String insertProduct(Product product, List<Uri> imageUris) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }

        if (product.getBarcode() == null || product.getBarcode().isEmpty()) {
            product.setBarcode(generateBarcode());
        }

        long now = System.currentTimeMillis();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        final String productId = product.getId();

        // Execute dalam single transaction
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Insert product
                productDao.insert(product);

                // Save dan insert images
                if (imageUris != null && !imageUris.isEmpty()) {
                    List<String> imagePaths = imageStorage.saveProductImages(productId, imageUris);

                    for (int i = 0; i < imagePaths.size(); i++) {
                        ProductImage image = new ProductImage(
                                UUID.randomUUID().toString(),
                                productId,
                                imagePaths.get(i),
                                i,
                                now
                        );
                        productImageDao.insert(image);
                    }
                }
            } catch (Exception e) {
                // Rollback: hapus gambar jika insert gagal
                imageStorage.deleteProductImages(productId);
                throw new RuntimeException("Failed to insert product with images", e);
            }
        });

        return productId;
    }

    // ================================
    // UPDATE WITH IMAGES
    // ================================

    /**
     * Update product dengan gambar baru (menambahkan, bukan mengganti semua).
     *
     * Logika:
     * - Jika imageUris null: pertahankan semua gambar yang ada
     * - Jika imageUris tidak null: tambahkan gambar baru ke yang sudah ada
     */
    public void updateProduct(Product product, List<Uri> imageUris) {
        final long currentTimestamp = System.currentTimeMillis();
        final String productId = product.getId();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Ambil gambar yang sudah ada
                List<ProductImage> existingImages = productImageDao.getByProductIdSync(productId);

                // Update product
                product.setUpdatedAt(currentTimestamp);
                productDao.update(product);

                // Handle gambar hanya jika ada imageUris yang dikirim (perubahan eksplisit)
                if (imageUris != null) {
                    Log.d("ProductRepository", "Processing image changes. Existing: " +
                            existingImages.size() + ", New: " + imageUris.size());

                    // Jika imageUris kosong, ini berarti hapus semua gambar
                    if (imageUris.isEmpty()) {
                        Log.d("ProductRepository", "Removing all images");
                        // Hapus semua gambar lama
                        for (ProductImage img : existingImages) {
                            imageStorage.deleteImage(img.getImagePath());
                        }
                        productImageDao.deleteAllForProduct(productId);
                    } else {
                        // Jika ada imageUris, analisis perubahan
                        processImageChanges(productId, existingImages, imageUris, currentTimestamp);
                    }
                } else {
                    Log.d("ProductRepository", "No image changes. Keeping existing " +
                            existingImages.size() + " images.");
                }
            } catch (Exception e) {
                Log.e("ProductRepository", "Error updating product images", e);
                throw new RuntimeException("Failed to update product with images", e);
            }
        });
    }

    /**
     * Proses perubahan gambar: tambahkan yang baru, pertahankan yang lama
     */
    private void processImageChanges(String productId, List<ProductImage> existingImages,
                                     List<Uri> newUris, long timestamp) {

        // Identifikasi gambar yang sudah ada (berdasarkan filename)
        Set<String> existingFilenames = new HashSet<>();
        Map<String, ProductImage> existingImageMap = new HashMap<>();

        for (ProductImage existing : existingImages) {
            String filename = getFilenameFromPath(existing.getImagePath());
            existingFilenames.add(filename);
            existingImageMap.put(filename, existing);
        }

        // Identifikasi gambar baru vs yang sudah ada
        List<Uri> urisToAdd = new ArrayList<>();
        List<String> newFilenames = new ArrayList<>();

        for (Uri uri : newUris) {
            String filename = getFilenameFromUri(uri);
            newFilenames.add(filename);

            if (!existingFilenames.contains(filename)) {
                urisToAdd.add(uri);
                Log.d("ProductRepository", "New image to add: " + filename);
            }
        }

        // Identifikasi gambar yang dihapus (ada di existing tapi tidak di new)
        List<ProductImage> imagesToRemove = new ArrayList<>();
        for (ProductImage existing : existingImages) {
            String filename = getFilenameFromPath(existing.getImagePath());
            if (!newFilenames.contains(filename)) {
                imagesToRemove.add(existing);
                Log.d("ProductRepository", "Image to remove: " + filename);
            }
        }

        // Hapus gambar yang dihapus
        for (ProductImage imgToRemove : imagesToRemove) {
            imageStorage.deleteImage(imgToRemove.getImagePath());
            productImageDao.delete(imgToRemove);
            existingImages.remove(imgToRemove);
        }

        // Tambahkan gambar baru
        if (!urisToAdd.isEmpty()) {
            List<String> newPaths = imageStorage.saveProductImages(productId, urisToAdd);
            int startOrderIndex = existingImages.size();

            for (int i = 0; i < newPaths.size(); i++) {
                ProductImage newImage = new ProductImage(
                        UUID.randomUUID().toString(),
                        productId,
                        newPaths.get(i),
                        startOrderIndex + i,
                        timestamp
                );
                productImageDao.insert(newImage);
                existingImages.add(newImage);
            }
            Log.d("ProductRepository", "Added " + newPaths.size() + " new images");
        }

        // Update urutan gambar berdasarkan urutan di newUris
        updateImageOrder(productId, existingImages, newFilenames, timestamp);
    }

    /**
     * Deteksi perubahan gambar dengan membandingkan path yang ada.
     * Return true hanya jika ada perubahan eksplisit pada gambar.
     */
    private boolean detectImageChanges(List<ProductImage> existingImages, List<Uri> imageUris) {
        // Jika imageUris null, pertahankan gambar yang ada (tidak ada perubahan)
        if (imageUris == null) {
            return false;
        }

        // Jika imageUris kosong DAN sebelumnya ada gambar, ini adalah penghapusan eksplisit
        if (imageUris.isEmpty() && !existingImages.isEmpty()) {
            return true;
        }

        // Jika imageUris kosong DAN sebelumnya juga kosong, tidak ada perubahan
        if (imageUris.isEmpty() && existingImages.isEmpty()) {
            return false;
        }

        // Jika jumlah berbeda, ada perubahan
        if (existingImages.size() != imageUris.size()) {
            return true;
        }

        // Bandingkan path satu per satu untuk deteksi perubahan urutan atau gambar
        for (int i = 0; i < existingImages.size(); i++) {
            ProductImage existingImage = existingImages.get(i);
            Uri newUri = imageUris.get(i);

            // Jika ini adalah URI file internal (dari storage kita), bandingkan path
            if (newUri.getScheme() != null && newUri.getScheme().equals("file")) {
                String newPath = newUri.getPath();
                String existingPath = existingImage.getImagePath();

                // Extract filename dari path untuk perbandingan
                String newFilename = getFilenameFromPath(newPath);
                String existingFilename = getFilenameFromPath(existingPath);

                if (!newFilename.equals(existingFilename)) {
                    return true;
                }
            } else {
                // Untuk URI content atau lainnya, anggap ada perubahan
                return true;
            }
        }

        return false;
    }

    private String getFilenameFromPath(String path) {
        if (path == null) return "";
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    // ================================
    // DELETE
    // ================================

    /**
     * Delete product beserta gambarnya.
     */
    public void deleteProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Hapus gambar dari storage
            imageStorage.deleteProductImages(product.getId());

            // Hapus dari database (cascade akan hapus ProductImage)
            productDao.delete(product);
        });
    }

    // ================================
    // IMAGE OPERATIONS
    // ================================

    public LiveData<List<ProductImage>> getProductImages(String productId) {
        return productImageDao.getByProductId(productId);
    }

    public List<ProductImage> getProductImagesSync(String productId) {
        List<ProductImage> images = productImageDao.getByProductIdSync(productId);
        // Sort by orderIndex to ensure correct display order in product list
        images.sort((img1, img2) -> Integer.compare(img1.getOrderIndex(), img2.getOrderIndex()));
        return images;
    }

    /**
     * Get File objects untuk sharing.
     */
    public List<File> getProductImageFilesForSharing(String productId) {
        List<ProductImage> images = productImageDao.getByProductIdSync(productId);
        // Sort by orderIndex to ensure first image is the main display image
        images.sort((img1, img2) -> Integer.compare(img1.getOrderIndex(), img2.getOrderIndex()));
        List<String> paths = new ArrayList<>();

        for (ProductImage img : images) {
            paths.add(img.getImagePath());
        }

        return imageStorage.prepareImagesForSharing(paths);
    }

    /**
     * Cleanup sharing cache.
     */
    public void cleanupSharingCache() {
        imageStorage.cleanupSharingCache();
    }

    // ================================
    // UTILITY
    // ================================

    public void updateProductStatus(String id, String status) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.updateStatus(id, status);
        });
    }

    public void updateProductStock(String id, long quantity) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.updateStock(id, quantity);
        });
    }

    /**
     * Update urutan gambar berdasarkan urutan baru
     */
    private void updateImageOrder(String productId, List<ProductImage> existingImages,
                                  List<String> newFilenames, long timestamp) {

        Map<String, ProductImage> imageMap = new HashMap<>();
        for (ProductImage img : existingImages) {
            String filename = getFilenameFromPath(img.getImagePath());
            imageMap.put(filename, img);
        }

        // Update order index berdasarkan urutan di newFilenames
        for (int i = 0; i < newFilenames.size(); i++) {
            String filename = newFilenames.get(i);
            ProductImage image = imageMap.get(filename);
            if (image != null && image.getOrderIndex() != i) {
                image.setOrderIndex(i);
                image.setCreatedAt(timestamp); // Update timestamp
                productImageDao.update(image);
            }
        }

        Log.d("ProductRepository", "Updated image order for " + newFilenames.size() + " images");
    }

    private String getFilenameFromUri(Uri uri) {
        if (uri == null) return "";
        String path = uri.getPath();
        return getFilenameFromPath(path);
    }

    /**
     * Update order index untuk semua gambar produk.
     */
    public void updateProductImageOrder(String productId, List<String> imagePaths) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<ProductImage> currentImages = productImageDao.getByProductIdSync(productId);

                // Update order index berdasarkan urutan di imagePaths
                for (int i = 0; i < imagePaths.size(); i++) {
                    String path = imagePaths.get(i);
                    for (ProductImage img : currentImages) {
                        if (img.getImagePath().equals(path)) {
                            img.setOrderIndex(i);
                            productImageDao.updateOrderIndex(img.getId(), i);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to update image order", e);
            }
        });
    }

    private String generateBarcode() {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    int maxNumber = productDao.getMaxBarcodeNumber();
                    return maxNumber + 1;
                }
            });

            int nextNumber = future.get();
            return String.format("BE-%08d", nextNumber);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "BE-" + System.currentTimeMillis();
        }
    }
}
