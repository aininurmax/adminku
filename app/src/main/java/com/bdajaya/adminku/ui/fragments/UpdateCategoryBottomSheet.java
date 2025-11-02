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
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.ui.components.CardInputView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet fragment for updating an existing category.
 * Pre-populates the form with existing category data for editing.
 *
 * @author Adminku Development Team
 * @version 2.0.0
 */
public class UpdateCategoryBottomSheet extends BottomSheetDialogFragment {

    public interface OnCategoryUpdateListener {
        void onCategoryUpdated(String categoryId, String newName);
        void onCancel();
    }

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private OnCategoryUpdateListener listener;
    private CardInputView categoryNameEditText;
    private String categoryId;
    private String originalCategoryName;

    public static UpdateCategoryBottomSheet newInstance(Category category) {
        UpdateCategoryBottomSheet fragment = new UpdateCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, category.getId());
        args.putString(ARG_CATEGORY_NAME, category.getName());
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnCategoryUpdateListener(OnCategoryUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            originalCategoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_category, container, false);

        categoryNameEditText = view.findViewById(R.id.category_name_edit_text);
        Button updateButton = view.findViewById(R.id.btn_update);
        Button cancelButton = view.findViewById(R.id.btn_cancel);

        // Pre-populate with existing category name
        if (originalCategoryName != null) {
            categoryNameEditText.setText(originalCategoryName);
        }

        // Setup click listeners
        updateButton.setOnClickListener(v -> {
            String newCategoryName = categoryNameEditText != null ? categoryNameEditText.getText().toString().trim() : "";
            if (validateInput(newCategoryName)) {
                if (listener != null) {
                    listener.onCategoryUpdated(categoryId, newCategoryName);
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
        categoryNameEditText.requestFocus();
        categoryNameEditText.selectAll();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private boolean validateInput(String categoryName) {
        if (categoryName.isEmpty()) {
            Toast.makeText(getContext(), "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (categoryName.length() < 2) {
            Toast.makeText(getContext(), "Nama kategori minimal 2 karakter", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (categoryName.length() > 100) {
            Toast.makeText(getContext(), "Nama kategori maksimal 100 karakter", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if name actually changed
        if (originalCategoryName != null && originalCategoryName.equals(categoryName)) {
            Toast.makeText(getContext(), "Nama kategori tidak berubah", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
