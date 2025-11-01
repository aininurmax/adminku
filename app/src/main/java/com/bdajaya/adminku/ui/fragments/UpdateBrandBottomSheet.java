package com.bdajaya.adminku.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.entity.Brand;
import com.bdajaya.adminku.ui.components.CardInputView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet fragment for updating an existing brand.
 * Pre-populates the form with existing brand data for editing.
 *
 * @author Adminku Development Team
 * @version 2.0.0
 */
public class UpdateBrandBottomSheet extends BottomSheetDialogFragment {

    public interface OnBrandUpdateListener {
        void onBrandUpdated(String brandId, String newName);
        void onCancel();
    }

    private static final String ARG_BRAND_ID = "brand_id";
    private static final String ARG_BRAND_NAME = "brand_name";

    private OnBrandUpdateListener listener;
    private CardInputView brandNameEditText;
    private String brandId;
    private String originalBrandName;

    public static UpdateBrandBottomSheet newInstance(Brand brand) {
        UpdateBrandBottomSheet fragment = new UpdateBrandBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_BRAND_ID, brand.getId());
        args.putString(ARG_BRAND_NAME, brand.getName());
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnBrandUpdateListener(OnBrandUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            brandId = getArguments().getString(ARG_BRAND_ID);
            originalBrandName = getArguments().getString(ARG_BRAND_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_brand, container, false);

        brandNameEditText = view.findViewById(R.id.brand_name_edit_text);
        Button updateButton = view.findViewById(R.id.update_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        // Pre-populate with existing brand name
        if (originalBrandName != null) {
            brandNameEditText.setText(originalBrandName);
        }

        // Setup click listeners
        updateButton.setOnClickListener(v -> {
            String newBrandName = brandNameEditText  != null ? brandNameEditText.getText().toString().trim() : "";
            if (validateInput(newBrandName)) {
                if (listener != null) {
                    listener.onBrandUpdated(brandId, newBrandName);
                }
                dismissBottomSheet();
            }

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
        // Hide keyboard before dismissing
        hideKeyboard();
        dismiss();
    }

    private void hideKeyboard() {
        try {
            Activity activity = getActivity();
            if (activity != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    View currentFocus = activity.getCurrentFocus();
                    if (currentFocus != null) {
                        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    } else {
                        // If no focused view, try to hide keyboard from the dialog window
                        View view = getView();
                        if (view != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't crash the app
            android.util.Log.e("UpdateCategoryBottomSheet", "Error hiding keyboard", e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show keyboard automatically and select all text
        brandNameEditText.requestFocus();
        brandNameEditText.selectAll();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private boolean validateInput(String brandName) {
        if (brandName.isEmpty()) {
            Toast.makeText(getContext(), "Nama brand tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (brandName.length() < 2) {
            Toast.makeText(getContext(), "Nama brand minimal 2 karakter", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (brandName.length() > 100) {
            Toast.makeText(getContext(), "Nama brand maksimal 100 karakter", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if name actually changed
        if (originalBrandName != null && originalBrandName.equals(brandName)) {
            Toast.makeText(getContext(), "Nama brand tidak berubah", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
