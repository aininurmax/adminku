package com.bdajaya.adminku.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.ui.components.CardInputView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet fragment for adding a new brand.
 * Provides a simple form interface for brand creation.
 *
 * @author Adminku Development Team
 * @version 2.0.0
 */
public class AddBrandBottomSheet extends BottomSheetDialogFragment {

    public interface OnBrandActionListener {
        void onBrandAdded(String brandName);
        void onCancel();
    }

    private OnBrandActionListener listener;
    private CardInputView brandNameEditText;

    public static AddBrandBottomSheet newInstance() {
        return new AddBrandBottomSheet();
    }

    public void setOnBrandActionListener(OnBrandActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_brand, container, false);

        brandNameEditText = view.findViewById(R.id.brand_name_edit_text);
        Button addButton = view.findViewById(R.id.add_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        // Setup click listeners
        addButton.setOnClickListener(v -> {
            String brandName = brandNameEditText.getText().toString().trim();
            if (validateInput(brandName)) {
                if (listener != null) {
                    listener.onBrandAdded(brandName);
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

        // Show keyboard automatically
        brandNameEditText.requestFocus();
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

        return true;
    }
}
