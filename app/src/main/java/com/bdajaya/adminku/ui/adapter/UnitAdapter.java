package com.bdajaya.adminku.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bdajaya.adminku.data.entity.Unit;
import com.bdajaya.adminku.databinding.ItemUnitBinding;

public class UnitAdapter extends ListAdapter<Unit, UnitAdapter.UnitViewHolder> {
    private final OnUnitClickListener clickListener;
    private final OnUnitLongClickListener longClickListener;

    public interface OnUnitClickListener {
        void onUnitClick(Unit unit);
    }

    public interface OnUnitLongClickListener {
        void onUnitLongClick(Unit unit);
    }

    public UnitAdapter(OnUnitClickListener clickListener, OnUnitLongClickListener longClickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    private static final DiffUtil.ItemCallback<Unit> DIFF_CALLBACK = new DiffUtil.ItemCallback<Unit>() {
        @Override
        public boolean areItemsTheSame(@NonNull Unit oldItem, @NonNull Unit newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Unit oldItem, @NonNull Unit newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getConversionFactor() == newItem.getConversionFactor();
        }
    };

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUnitBinding binding = ItemUnitBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UnitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class UnitViewHolder extends RecyclerView.ViewHolder {
        private final ItemUnitBinding binding;

        UnitViewHolder(ItemUnitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Unit unit) {
            binding.unitName.setText(unit.getName());

            String conversionText;
            if (unit.isBaseUnit()) {
                conversionText = "Satuan dasar";
            } else {
                conversionText = String.format("1 %s = %d %s",
                        unit.getName(),
                        unit.getConversionFactor(),
                        unit.getBaseUnit());
            }
            binding.unitConversion.setText(conversionText);

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onUnitClick(unit);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onUnitLongClick(unit);
                    return true;
                }
                return false;
            });
        }
    }
}