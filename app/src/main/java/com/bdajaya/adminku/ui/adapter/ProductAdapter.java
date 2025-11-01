package com.bdajaya.adminku.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.model.ProductWithDetails;
import com.bdajaya.adminku.util.CurrencyFormatter;
import com.bdajaya.adminku.data.manager.ImageStorageManager;
import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductWithDetails> products;
    private final OnProductClickListener clickListener;
    private final OnProductLongClickListener longClickListener;
    private final ImageStorageManager imageStorage;

    public ProductAdapter(List<ProductWithDetails> products,
                          OnProductClickListener clickListener,
                          OnProductLongClickListener longClickListener,
                          ImageStorageManager imageStorage) {
        this.products = products;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.imageStorage = imageStorage;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductWithDetails product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateData(List<ProductWithDetails> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public interface OnProductClickListener {
        void onProductClick(ProductWithDetails product);
    }

    public interface OnProductLongClickListener {
        void onProductLongClick(ProductWithDetails product);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;
        private final TextView nameTextView;
        private final TextView barcodeTextView;
        private final TextView priceTextView;
        private final TextView stockTextView;
        private final TextView unitTextView;
        private final TextView categoryTextView;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.product_image_view);
            nameTextView = itemView.findViewById(R.id.product_name_text_view);
            barcodeTextView = itemView.findViewById(R.id.product_barcode_text_view);
            priceTextView = itemView.findViewById(R.id.product_price_text_view);
            stockTextView = itemView.findViewById(R.id.product_stock_text_view);
            unitTextView = itemView.findViewById(R.id.product_unit_text_view);
            categoryTextView = itemView.findViewById(R.id.product_category_text_view);
        }

        void bind(ProductWithDetails product) {
            nameTextView.setText(product.product.getName());
            barcodeTextView.setText(product.product.getBarcode());
            priceTextView.setText(CurrencyFormatter.formatCurrency(product.product.getSellPrice()));

            // Display stock with proper unit conversion
            String stockText = formatStockWithUnit(product);
            stockTextView.setText(stockText);

            // Display unit name separately for better readability
            unitTextView.setText(product.getUnitName());

            categoryTextView.setText(product.getCategoryName());

            // Load image dari file storage
            String firstImagePath = product.getFirstImagePath();
            if (firstImagePath != null && !firstImagePath.isEmpty()) {
                Uri imageUri = imageStorage.getImageUri(firstImagePath);

                if (imageUri != null) {
                    Glide.with(itemView.getContext())
                            .load(imageUri)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .centerCrop()
                            .into(productImageView);
                } else {
                    Glide.with(itemView.getContext())
                            .load(R.drawable.ic_image_placeholder)
                            .centerCrop()
                            .into(productImageView);
                }
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .into(productImageView);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onProductClick(product);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onProductLongClick(product);
                    return true;
                }
                return false;
            });
        }

        /**
         * Format stock with unit conversion for display
         */
        private String formatStockWithUnit(ProductWithDetails product) {
            if (product.unit == null) {
                return String.valueOf(product.product.getStock());
            }

            // Stock is stored in base unit, convert to display unit
            long baseStock = product.product.getStock();
            long displayStock = product.unit.fromBaseUnit(baseStock);

            // Format with thousands separator if needed
            if (displayStock >= 1000) {
                return String.format("%,d", displayStock);
            } else {
                return String.valueOf(displayStock);
            }
        }
    }
}