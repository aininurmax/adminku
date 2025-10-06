package com.bdajaya.adminku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.model.Breadcrumb;

import java.util.List;

public class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbAdapter.BreadcrumbViewHolder> {

    private List<Breadcrumb> breadcrumbs;
    private final BreadcrumbClickListener listener;

    public BreadcrumbAdapter(List<Breadcrumb> breadcrumbs, BreadcrumbClickListener listener) {
        this.breadcrumbs = breadcrumbs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BreadcrumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_breadcrumb, parent, false);
        return new BreadcrumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreadcrumbViewHolder holder, int position) {
        Breadcrumb breadcrumb = breadcrumbs.get(position);
        holder.bind(breadcrumb, position);
    }

    @Override
    public int getItemCount() {
        return breadcrumbs.size();
    }

    public void updateData(List<Breadcrumb> newBreadcrumbs) {
        this.breadcrumbs = newBreadcrumbs;
        notifyDataSetChanged();
    }

    public interface BreadcrumbClickListener {
        void onBreadcrumbClick(int position);
    }

    class BreadcrumbViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;

        BreadcrumbViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.breadcrumb_name_text_view);
        }

        void bind(Breadcrumb breadcrumb, int position) {
            nameTextView.setText(breadcrumb.getName());

            itemView.setOnClickListener(v -> {
                listener.onBreadcrumbClick(position);
            });
        }
    }
}

