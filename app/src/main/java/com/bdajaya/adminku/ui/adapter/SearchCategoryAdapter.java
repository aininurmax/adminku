package com.bdajaya.adminku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.model.CategoryWithPath;

import java.util.List;

public class SearchCategoryAdapter extends RecyclerView.Adapter<SearchCategoryAdapter.SearchCategoryViewHolder> {

    private List<CategoryWithPath> categories;
    private final SearchCategoryClickListener listener;

    public SearchCategoryAdapter(List<CategoryWithPath> categories, SearchCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_category, parent, false);
        return new SearchCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchCategoryViewHolder holder, int position) {
        CategoryWithPath category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateData(List<CategoryWithPath> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    public interface SearchCategoryClickListener {
        void onCategoryClick(CategoryWithPath category);
    }

    class SearchCategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView pathTextView;

        SearchCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.category_name_text_view);
            pathTextView = itemView.findViewById(R.id.category_path_text_view);
        }

        void bind(CategoryWithPath category) {
            nameTextView.setText(category.getCategory().getName());
            pathTextView.setText(category.getPathString());

            itemView.setOnClickListener(v -> {
                listener.onCategoryClick(category);
            });
        }
    }
}

