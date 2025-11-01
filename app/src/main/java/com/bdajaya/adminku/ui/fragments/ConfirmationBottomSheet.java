package com.bdajaya.adminku.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdajaya.adminku.data.entity.Brand;
import com.bdajaya.adminku.data.entity.Category;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class ConfirmationBottomSheet extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return com.bdajaya.adminku.R.style.BottomSheetDialogTheme;
    }

    public interface OnConfirmationActionListener {
        void onConfirmDelete(Object itemToDelete);
        void onCancel();
    }

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_BRAND = "brand";
    private static final String ARG_HAS_CHILDREN = "has_children";
    private static final String ARG_HAS_PRODUCTS = "has_products";
    private static final String ARG_IS_BRAND = "is_brand";

    private Category category;
    private Brand brand;
    private boolean hasChildren;
    private boolean hasProducts;
    private boolean isBrand;
    private OnConfirmationActionListener listener;

    public static ConfirmationBottomSheet newInstance(Category category, boolean hasChildren, boolean hasProducts, int productCount) {
        ConfirmationBottomSheet fragment = new ConfirmationBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CATEGORY, category);
        args.putBoolean(ARG_HAS_CHILDREN, hasChildren);
        args.putBoolean(ARG_HAS_PRODUCTS, hasProducts);
        args.putBoolean(ARG_IS_BRAND, false);
        fragment.setArguments(args);
        return fragment;
    }

    public static ConfirmationBottomSheet newInstanceForBrand(Brand brand) {
        ConfirmationBottomSheet fragment = new ConfirmationBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_BRAND, brand);
        args.putBoolean(ARG_IS_BRAND, true);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnConfirmationActionListener(OnConfirmationActionListener listener) {
        this.listener = listener;
    }

    @Deprecated
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isBrand = getArguments().getBoolean(ARG_IS_BRAND, false);
            if (isBrand) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    brand = getArguments().getParcelable(ARG_BRAND, Brand.class);
                } else {
                    brand = getArguments().getParcelable(ARG_BRAND);
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    category = getArguments().getParcelable(ARG_CATEGORY, Category.class);
                } else {
                    category = getArguments().getParcelable(ARG_CATEGORY);
                }
                hasChildren = getArguments().getBoolean(ARG_HAS_CHILDREN);
                hasProducts = getArguments().getBoolean(ARG_HAS_PRODUCTS);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(
            com.bdajaya.adminku.R.layout.dialog_confirmation, container, false);

        // Setup dialog content based on category conditions
        setupDialogContent(view);

        // Setup buttons
        setupDialogButtons(view);

        return view;
    }

    private void setupDialogContent(View view) {
        MaterialTextView titleView = view.findViewById(com.bdajaya.adminku.R.id.dialog_title);
        MaterialTextView messageView = view.findViewById(com.bdajaya.adminku.R.id.dialog_message);

        if (titleView != null) {
            titleView.setText(com.bdajaya.adminku.R.string.confirm_delete);
        }

        if (messageView != null) {
            String message = buildConfirmationMessage();
            messageView.setText(message);
        }
    }

    private String buildConfirmationMessage() {
        StringBuilder message = new StringBuilder();

        if (isBrand) {
            if (hasProducts) {
                message.append("Brand memiliki produk terkait. Tidak dapat dihapus.");
            } else {
                message.append("Apakah Anda yakin ingin menghapus brand ini?");
            }
        } else {
            if (hasChildren) {
                // Prevent deletion of categories with children
                message.append(getString(com.bdajaya.adminku.R.string.category_has_children_cannot_delete));
            } else if (hasProducts) {
                message.append(getString(com.bdajaya.adminku.R.string.confirm_delete_category_with_products));
            } else {
                message.append(getString(com.bdajaya.adminku.R.string.confirm_delete_category_simple));
            }
        }

        return message.toString();
    }

    private void setupDialogButtons(View view) {
        MaterialButton btnCancel = view.findViewById(com.bdajaya.adminku.R.id.btn_cancel);
        MaterialButton btnConfirm = view.findViewById(com.bdajaya.adminku.R.id.btn_confirm);

        if (hasChildren) {
            // Hide confirm button and show only cancel button as OK
            btnConfirm.setVisibility(View.GONE);
            btnCancel.setText(com.bdajaya.adminku.R.string.confirm); // Change to "OK"

            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancel();
                }
                dismissBottomSheet();
            });
        } else {
            // Show both buttons for normal deletion confirmation
            btnCancel.setText(com.bdajaya.adminku.R.string.cancel); // Reset to "Cancel"

            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancel();
                }
                dismissBottomSheet();
            });

            btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    if (isBrand && brand != null) {
                        listener.onConfirmDelete(brand);
                    } else if (category != null) {
                        listener.onConfirmDelete(category);
                    }
                }
                dismissBottomSheet();
            });
        }
    }

    private void dismissBottomSheet() {
        dismiss();
    }

    public Category getCategory() {
        return category;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public boolean hasProducts() {
        return hasProducts;
    }
}
