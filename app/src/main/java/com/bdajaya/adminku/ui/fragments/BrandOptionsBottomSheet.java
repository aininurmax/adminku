package com.bdajaya.adminku.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdajaya.adminku.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet fragment for brand options (edit, delete).
 * Provides contextual actions for brand management.
 *
 * @author Adminku Development Team
 * @version 2.0.0
 */
public class BrandOptionsBottomSheet extends BottomSheetDialogFragment {

    public interface OnBrandOptionSelectedListener {
        void onUpdateBrand();
        void onDeleteBrand();
        void onCancel();
    }

    private static final String ARG_BRAND_ID = "brand_id";
    private static final String ARG_BRAND_NAME = "brand_name";

    private OnBrandOptionSelectedListener listener;
    private String brandId;
    private String brandName;

    public static BrandOptionsBottomSheet newInstance(String brandId, String brandName) {
        BrandOptionsBottomSheet fragment = new BrandOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_BRAND_ID, brandId);
        args.putString(ARG_BRAND_NAME, brandName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnBrandOptionSelectedListener(OnBrandOptionSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            brandId = getArguments().getString(ARG_BRAND_ID);
            brandName = getArguments().getString(ARG_BRAND_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_brand_options_bottom_sheet, container, false);

        LinearLayout updateOption = view.findViewById(R.id.update_option);
        LinearLayout deleteOption = view.findViewById(R.id.delete_option);

        // Setup click listeners
        updateOption.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateBrand();
            }
            dismiss();
        });

        deleteOption.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteBrand();
            }
            dismiss();
        });

        return view;
    }
}
