package com.bdajaya.adminku.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.entity.Category;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeleteCategoryBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CATEGORY = "category";

    private Category category;
    private DeleteCategoryListener listener;

    public interface DeleteCategoryListener {
        void onDeleteCategory(Category category);
    }

    public static DeleteCategoryBottomSheet newInstance(Category category) {
        DeleteCategoryBottomSheet fragment = new DeleteCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = (Category) getArguments().getSerializable(ARG_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_delete_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView categoryNameTextView = view.findViewById(R.id.category_name_text_view);
        LinearLayout deleteButton = view.findViewById(R.id.delete_button);
        LinearLayout cancelButton = view.findViewById(R.id.cancel_button);

        if (categoryNameTextView != null && category != null) {
            categoryNameTextView.setText(category.getName());
        }

        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCategory(category);
                }
                dismiss();
            });
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dismiss());
        }
    }

    public void setDeleteCategoryListener(DeleteCategoryListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
