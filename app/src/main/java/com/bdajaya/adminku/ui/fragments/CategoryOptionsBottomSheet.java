package com.bdajaya.adminku.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CategoryOptionsBottomSheet extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return com.bdajaya.adminku.R.style.BottomSheetDialogTheme;
    }

    public interface OnCategoryOptionSelectedListener {
        void onAddSubcategory();
        void onUpdateCategory();
        void onDeleteCategory();
        void onCancel();
    }

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private String categoryId;
    private String categoryName;
    private OnCategoryOptionSelectedListener listener;

    public static CategoryOptionsBottomSheet newInstance(String categoryId, String categoryName) {
        CategoryOptionsBottomSheet fragment = new CategoryOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnCategoryOptionSelectedListener(OnCategoryOptionSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(
            com.bdajaya.adminku.R.layout.fragment_category_options_bottom_sheet, container, false);

        Button addSubButton = view.findViewById(com.bdajaya.adminku.R.id.addsub_button);
        Button updateButton = view.findViewById(com.bdajaya.adminku.R.id.update_button);
        Button deleteButton = view.findViewById(com.bdajaya.adminku.R.id.delete_button);
        Button cancelButton = view.findViewById(com.bdajaya.adminku.R.id.cancel_button);

        addSubButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddSubcategory();
            }
            dismissBottomSheet();
        });

        updateButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateCategory();
            }
            dismissBottomSheet();
        });

        deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteCategory();
            }
            dismissBottomSheet();
        });

        cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismissBottomSheet();
        });

        return view;
    }

    private void dismissBottomSheet() {
        // Dismiss the BottomSheetDialogFragment
        dismiss();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
