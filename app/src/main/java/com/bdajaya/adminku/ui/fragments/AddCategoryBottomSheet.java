package com.bdajaya.adminku.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.DialogFragment;
import com.bdajaya.adminku.R;
import com.bdajaya.adminku.ui.components.CardInputView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddCategoryBottomSheet extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    public interface OnCategoryActionListener {
        void onCategoryAdded(String categoryName);
        void onCancel();
    }

    private static final String ARG_PARENT_ID = "parent_id";
    private static final String ARG_CURRENT_LEVEL = "current_level";

    private String parentId;
    private int currentLevel;
    private OnCategoryActionListener listener;

    public static AddCategoryBottomSheet newInstance(String parentId, int currentLevel) {
        AddCategoryBottomSheet fragment = new AddCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PARENT_ID, parentId);
        args.putInt(ARG_CURRENT_LEVEL, currentLevel);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnCategoryActionListener(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
        if (getArguments() != null) {
            parentId = getArguments().getString(ARG_PARENT_ID);
            currentLevel = getArguments().getInt(ARG_CURRENT_LEVEL);
        }
    }

    @Deprecated
    @Override
    public void onStart() {
        super.onStart();

        // Set dialog position above keyboard
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();

            params.gravity = Gravity.BOTTOM;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;

            // Gunakan API baru untuk Android 11+ (API 30)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            } else {
                params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
            }

            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(
                R.layout.dialog_add_category, container, false);

        // Setup views
        setupDialogViews(view);

        // Setup buttons
        setupDialogButtons(view);

        return view;
    }

    private void setupDialogViews(View view) {
        // Dialog title and subtitle are already defined in the layout
        // No additional setup needed for basic views
    }

    private void setupDialogButtons(View view) {
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = view.findViewById(R.id.btn_save);
        CardInputView categoryNameEditText = view.findViewById(R.id.category_name_edit_text);

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismissBottomSheet();
        });

        btnSave.setOnClickListener(v -> {
            String categoryName = categoryNameEditText != null ? categoryNameEditText.getText().toString().trim() : "";

            if (!categoryName.isEmpty()) {
                if (listener != null) {
                    listener.onCategoryAdded(categoryName);
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
            android.util.Log.e("AddCategoryBottomSheet", "Error hiding keyboard", e);
        }
    }

    public String getParentId() {
        return parentId;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
}
