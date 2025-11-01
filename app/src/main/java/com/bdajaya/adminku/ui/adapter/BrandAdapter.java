package com.bdajaya.adminku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.entity.Brand;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {

    private List<Brand> brands;
    private final BrandClickListener clickListener;

    public BrandAdapter(List<Brand> brands, BrandClickListener clickListener) {
        this.brands = brands;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand brand = brands.get(position);
        holder.bind(brand, clickListener);
    }

    @Override
    public int getItemCount() {
        return brands.size();
    }

    public void updateData(List<Brand> newBrands) {
        this.brands = newBrands;
        notifyDataSetChanged();
    }

    public static class BrandViewHolder extends RecyclerView.ViewHolder {
        private final TextView brandNameTextView;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            brandNameTextView = itemView.findViewById(R.id.brand_name_text_view);
        }

        public void bind(Brand brand, BrandClickListener clickListener) {
            brandNameTextView.setText(brand.getName());

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onBrandClick(brand);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onBrandLongClick(brand);
                    return true;
                }
                return false;
            });
        }
    }

    public interface BrandClickListener {
        void onBrandClick(Brand brand);
        void onBrandLongClick(Brand brand);
    }
}
