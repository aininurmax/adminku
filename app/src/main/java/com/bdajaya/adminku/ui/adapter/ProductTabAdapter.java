package com.bdajaya.adminku.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bdajaya.adminku.data.model.ProductWithDetails;
import com.bdajaya.adminku.ui.fragments.ProductListFragment;
import com.bdajaya.adminku.ui.activities.ProductManagementActivity;

import java.util.List;

public class ProductTabAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;
    private static final int TAB_LIVE = 0;
    private static final int TAB_OUT_OF_STOCK = 1;
    private static final int TAB_ARCHIVED = 2;

    private final ProductManagementActivity activity;
    private RecyclerView searchRecyclerView;
    private ProductAdapter searchAdapter;

    public ProductTabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.activity = (ProductManagementActivity) fragmentActivity;

        // Set up search results recycler view
        searchRecyclerView = activity.findViewById(com.bdajaya.adminku.R.id.recycler_view_search_results);
        searchAdapter = new ProductAdapter(null,
                product -> activity.onProductClick(product),
                product -> activity.onProductLongClick(product),
                activity.getImageStorageManager());
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        searchRecyclerView.setAdapter(searchAdapter);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_LIVE:
                return ProductListFragment.newInstance("LIVE");
            case TAB_OUT_OF_STOCK:
                return ProductListFragment.newInstance("OUT_OF_STOCK");
            case TAB_ARCHIVED:
                return ProductListFragment.newInstance("ARCHIVED");
            default:
                throw new IllegalArgumentException("Invalid tab position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }

    public void updateSearchResults(List<ProductWithDetails> products) {
        searchAdapter.updateData(products);
    }
}

