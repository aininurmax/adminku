package com.bdajaya.adminku.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bdajaya.adminku.R;
import com.bdajaya.adminku.ui.activities.AddEditProductActivity;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup click listener for products card
        setupProductsCard(view);

        return view;
    }

    private void setupProductsCard(View view) {
        View cardProducts = view.findViewById(R.id.card_products);
        if (cardProducts != null) {
            cardProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToAddEditProduct();
                }
            });
        }
    }

    private void navigateToAddEditProduct() {
        Intent intent = new Intent(getActivity(), AddEditProductActivity.class);
        startActivity(intent);

        // Optional: Add animation
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}