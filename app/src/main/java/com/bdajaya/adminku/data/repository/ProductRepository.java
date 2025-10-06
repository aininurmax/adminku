package com.bdajaya.adminku.data.repository;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.CategoryDao;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.dao.ProductImageDao;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.entity.ProductImage;
import com.bdajaya.adminku.data.model.ProductWithDetails;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProductRepository {
    private final ProductDao productDao;
    private final ProductImageDao productImageDao;
    private final CategoryDao categoryDao;

    public ProductRepository(ProductDao productDao, ProductImageDao productImageDao, CategoryDao categoryDao) {
        this.productDao = productDao;
        this.productImageDao = productImageDao;
        this.categoryDao = categoryDao;
    }

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


    public String insertProduct(Product product, List<String> imageBase64List) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }

        // Generate barcode if not provided
        if (product.getBarcode() == null || product.getBarcode().isEmpty()) {
            product.setBarcode(generateBarcode());
        }

        // Set timestamps
        long now = System.currentTimeMillis();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // Insert product
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.insert(product);

            // Insert images if provided
            if (imageBase64List != null && !imageBase64List.isEmpty()) {
                productImageDao.deleteAllForProduct(product.getId());

                for (int i = 0; i < imageBase64List.size(); i++) {
                    ProductImage image = new ProductImage(
                            UUID.randomUUID().toString(),
                            product.getId(),
                            imageBase64List.get(i),
                            i,
                            now
                    );
                    productImageDao.insert(image);
                }
            }
        });

        return product.getId();
    }

    public void updateProduct(Product product, List<String> imageBase64List) {
        // Update timestamp
        product.setUpdatedAt(System.currentTimeMillis());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.update(product);

            // Update images if provided
            if (imageBase64List != null) {
                productImageDao.deleteAllForProduct(product.getId());

                for (int i = 0; i < imageBase64List.size(); i++) {
                    ProductImage image = new ProductImage(
                            UUID.randomUUID().toString(),
                            product.getId(),
                            imageBase64List.get(i),
                            i,
                            System.currentTimeMillis()
                    );
                    productImageDao.insert(image);
                }
            }
        });
    }

    public void deleteProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.delete(product);
        });
    }

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

    public LiveData<List<ProductImage>> getProductImages(String productId) {
        return productImageDao.getByProductId(productId);
    }

    public List<ProductImage> getProductImagesSync(String productId) {
        return productImageDao.getByProductIdSync(productId);
    }

    public void updateImageOrder(String imageId, int newIndex) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productImageDao.updateOrderIndex(imageId, newIndex);
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

