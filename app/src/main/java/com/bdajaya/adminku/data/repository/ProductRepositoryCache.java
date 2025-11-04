package com.bdajaya.adminku.data.repository;

import android.util.LruCache;

import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.model.ProductWithDetails;
import com.bdajaya.adminku.data.entity.Product;

import java.util.List;

/**
 * Example wrapper demonstrating a simple read-through cache for Product objects.
 * Integrate into ProductRepository (inject ProductDao via Hilt as in RepositoryModule).
 *
 * Important:
 * - All write operations must call invalidate(id) or clear().
 * - Choose cache size based on device memory profile; this is just an example.
 */
public class ProductRepositoryCache {
    private final ProductDao productDao;
    private final LruCache<String, ProductWithDetails> productCache;

    public ProductRepositoryCache(ProductDao productDao) {
        this.productDao = productDao;
        // capacity in entries - tune as needed
        this.productCache = new LruCache<>(256);
    }

    public ProductWithDetails getProductWithDetails(String id) {
        ProductWithDetails cached = productCache.get(id);
        if (cached != null) return cached;

        // call DAO sync method from background thread (caller must ensure background thread)
        ProductWithDetails p = null;
        try {
            // If ProductDao had a sync method to return ProductWithDetails, use it. For example:
            // p = productDao.getProductWithDetailsSync(id);
            // If not available, you can fetch product and relations separately.
        } catch (Exception ignored) {}

        if (p != null) productCache.put(id, p);
        return p;
    }

    public void invalidate(String id) {
        productCache.remove(id);
    }

    public void clear() {
        productCache.evictAll();
    }

    // Example search caching: for search results you may cache by normalized query string
    // but be mindful of memory growth and TTL/expiry.
}