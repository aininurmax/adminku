package com.bdajaya.adminku.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.ui.components.CardInputView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UpdateCategoryBottomSheet extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    public interface OnCategoryUpdateListener {
        void onCategoryUpdated(String categoryId, String newName);
        void onCancel();
    }

    private static final String ARG_CATEGORY = "category";

    private Category category;
    private OnCategoryUpdateListener listener;

    public static UpdateCategoryBottomSheet newInstance(Category category) {
        UpdateCategoryBottomSheet fragment = new UpdateCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnCategoryUpdateListener(OnCategoryUpdateListener listener) {
        this.listener = listener;
    }

    @Deprecated
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                category = getArguments().getParcelable(ARG_CATEGORY, Category.class);
            } else {
                category = getArguments().getParcelable(ARG_CATEGORY);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_update_category, container, false);

        // Setup views
        setupDialogViews(view);

        // Setup buttons
        setupDialogButtons(view);

        return view;
    }

    private void setupDialogViews(View view) {
        if (category != null) {
            // Set current category name in the input field
            CardInputView categoryNameEditText = view.findViewById(R.id.category_name_edit_text);
            if (categoryNameEditText != null) {
                categoryNameEditText.setText(category.getName());
            }
        }
    }

    private void setupDialogButtons(View view) {
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnUpdate = view.findViewById(R.id.btn_update);
        CardInputView categoryNameEditText = view.findViewById(R.id.category_name_edit_text);

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismissBottomSheet();
        });

        btnUpdate.setOnClickListener(v -> {
            String newCategoryName = categoryNameEditText != null ? categoryNameEditText.getText().toString().trim() : "";

            if (!newCategoryName.isEmpty() && category != null) {
                if (listener != null) {
                    listener.onCategoryUpdated(category.getId(), newCategoryName);
                }
                dismissBottomSheet();
            } else {
                if (categoryNameEditText != null) {
                    categoryNameEditText.setError("Nama kategori tidak boleh kosong");
                }
            }
        });
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

    public Category getCategory() {
        return category;
    }
}
