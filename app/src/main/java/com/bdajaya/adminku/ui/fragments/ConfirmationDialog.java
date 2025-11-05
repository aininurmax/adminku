package com.bdajaya.adminku.ui.fragments;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.entity.Brand;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.entity.Unit;
import com.bdajaya.adminku.databinding.DialogConfirmationBinding;

public class ConfirmationDialog extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_POSITIVE_TEXT = "positive_text";
    private static final String ARG_NEGATIVE_TEXT = "negative_text";
    private static final String ARG_ENTITY_TYPE = "entity_type";
    private static final String ARG_HAS_CHILDREN = "has_children";
    private static final String ARG_HAS_PRODUCTS = "has_products";
    private static final String ARG_PRODUCT_COUNT = "product_count";

    // Separate keys for different data types
    private static final String ARG_CATEGORY_DATA = "category_data";
    private static final String ARG_BRAND_DATA = "brand_data";
    private static final String ARG_PRODUCT_DATA = "product_data";
    private static final String ARG_UNIT_DATA = "unit_data";

    // Entity types
    public static final int ENTITY_CATEGORY = 1;
    public static final int ENTITY_BRAND = 2;
    public static final int ENTITY_PRODUCT = 3;
    public static final int ENTITY_UNIT = 4;

    private OnConfirmationActionListener listener;
    private DialogConfirmationBinding binding;

    // Constructor untuk delete category
    public static ConfirmationDialog newInstance(Category category,
                                                boolean hasChildren,
                                                boolean hasProducts,
                                                int productCount) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_CATEGORY_DATA, category);
        args.putBoolean(ARG_HAS_CHILDREN, hasChildren);
        args.putBoolean(ARG_HAS_PRODUCTS, hasProducts);
        args.putInt(ARG_PRODUCT_COUNT, productCount);
        args.putInt(ARG_ENTITY_TYPE, ENTITY_CATEGORY);

        String message = buildCategoryDeleteMessage(hasChildren, hasProducts, productCount);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_TITLE, "Hapus Kategori");

        ConfirmationDialog fragment = new ConfirmationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    // Constructor untuk brand
    public static ConfirmationDialog newInstanceForBrand(Brand brand) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_BRAND_DATA, brand);
        args.putInt(ARG_ENTITY_TYPE, ENTITY_BRAND);
        args.putString(ARG_TITLE, "Hapus Brand");
        args.putString(ARG_MESSAGE, "Apakah Anda yakin ingin menghapus brand \"" + brand.getName() + "\"?");

        ConfirmationDialog fragment = new ConfirmationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    // Constructor untuk product - SIMPLE VERSION tanpa data
    public static ConfirmationDialog newInstanceForProduct(String productId, String productName) {
        Bundle args = new Bundle();
        args.putInt(ARG_ENTITY_TYPE, ENTITY_PRODUCT);
        args.putString(ARG_TITLE, "Hapus Produk");
        args.putString(ARG_MESSAGE, "Apakah Anda yakin ingin menghapus produk \"" + productName + "\"?");
        args.putString("product_id", productId);
        args.putString("product_name", productName);

        ConfirmationDialog fragment = new ConfirmationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    // Constructor untuk unit - SIMPLE VERSION tanpa data
    public static ConfirmationDialog newInstanceForUnit(String unitId, String unitName) {
        Bundle args = new Bundle();
        args.putInt(ARG_ENTITY_TYPE, ENTITY_UNIT);
        args.putString(ARG_TITLE, "Hapus Satuan");
        args.putString(ARG_MESSAGE, "Apakah Anda yakin ingin menghapus satuan \"" + unitName + "\"?");
        args.putString("unit_id", unitId);
        args.putString("unit_name", unitName);

        ConfirmationDialog fragment = new ConfirmationDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private static String buildCategoryDeleteMessage(boolean hasChildren, boolean hasProducts, int productCount) {
        if (hasChildren) {
            return "Kategori ini memiliki sub-kategori. Tidak dapat dihapus.";
        } else if (hasProducts) {
            return "Kategori ini memiliki " + productCount + " produk. Semua produk akan kehilangan kategori ini.";
        } else {
            return "Apakah Anda yakin ingin menghapus kategori ini?";
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Gunakan 90% lebar layar agar ada margin kanan–kiri
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_SlideUp;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogConfirmationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        Bundle args = getArguments();
        if (args == null) return;

        // Set title and message
        binding.dialogTitle.setText(args.getString(ARG_TITLE, "Konfirmasi"));
        binding.dialogMessage.setText(args.getString(ARG_MESSAGE, "Apakah Anda yakin?"));

        // Set button texts
        binding.btnConfirm.setText(args.getString(ARG_POSITIVE_TEXT, "Hapus"));
        binding.btnCancel.setText(args.getString(ARG_NEGATIVE_TEXT, "Batal"));

        // Handle special cases (like categories with children)
        boolean hasChildren = args.getBoolean(ARG_HAS_CHILDREN, false);
        if (hasChildren) {
            // For categories with children, show only OK button
            binding.btnConfirm.setVisibility(View.GONE);
            binding.btnCancel.setText("OK");
        }

        setupButtonListeners(args);
    }

    private void setupButtonListeners(Bundle args) {
        binding.btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });

        binding.btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                Object data = extractDataFromArgs(args);
                listener.onConfirm(data);
            }
            dismiss();
        });
    }

    private Object extractDataFromArgs(Bundle args) {
        int entityType = args.getInt(ARG_ENTITY_TYPE, 0);

        switch (entityType) {
            case ENTITY_CATEGORY:
                return getParcelableSafe(args, ARG_CATEGORY_DATA, Category.class);
            case ENTITY_BRAND:
                return getParcelableSafe(args, ARG_BRAND_DATA, Brand.class);
            case ENTITY_PRODUCT:
                // Return simple data for product
                return new ProductSimpleData(
                    args.getString("product_id"),
                    args.getString("product_name")
                );
            case ENTITY_UNIT:
                // Return simple data for unit
                return new UnitSimpleData(
                    args.getString("unit_id"),
                    args.getString("unit_name")
                );
            default:
                return null;
        }
    }

    /**
     * Safe method to get Parcelable without deprecation warnings
     * Supports both old and new Android versions
     */
    @SuppressWarnings("deprecation")
    private <T> T getParcelableSafe(Bundle args, String key, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return args.getParcelable(key, clazz);
        } else {
            return args.getParcelable(key);
        }
    }

    // Simple data classes untuk Product dan Unit
    public static class ProductSimpleData {
        public final String id;
        public final String name;

        public ProductSimpleData(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class UnitSimpleData {
        public final String id;
        public final String name;

        public UnitSimpleData(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public void setOnConfirmationActionListener(OnConfirmationActionListener listener) {
        this.listener = listener;
    }

    public interface OnConfirmationActionListener {
        void onConfirm(Object data);
        void onCancel();
    }
}