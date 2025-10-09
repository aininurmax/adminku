package com.bdajaya.adminku.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.bdajaya.adminku.AdminkuApplication;
import com.bdajaya.adminku.R;
import com.bdajaya.adminku.databinding.ActivityAddEditProductBinding;
import com.bdajaya.adminku.ui.viewmodel.FactoryViewModel;
import com.bdajaya.adminku.ui.viewmodel.AddEditProductViewModel;

public class AddEditProductActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT_CATEGORY = 1001;

    private ActivityAddEditProductBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.browse_categories);
        }

        // Enable back button in toolbar
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModel and other components here
        setupViewModel();
        setupCategorySelection();
    }
    private void setupViewModel() {
        try {
            if (!(getApplication() instanceof AdminkuApplication)) {
                throw new IllegalStateException("Application must be AdminkuApplication. Check AndroidManifest.xml");
            }
            AdminkuApplication application = (AdminkuApplication) getApplication();
            FactoryViewModel factory = new FactoryViewModel(
                    application.getProductRepository(),
                    application.getCategoryRepository()
            );
            AddEditProductViewModel viewModel = new ViewModelProvider(this, factory).get(AddEditProductViewModel.class);
        } catch (Exception e) {
            // Log the error and handle gracefully
            e.printStackTrace();
            finish(); // Close activity if setup fails
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            binding.toolbar.setTitle(R.string.app_name);
        }
    }
    private void setupCategorySelection() {
        binding.categorySelect.setOnClickListener(v -> {
            Intent intent = new Intent(this, BrowseCategoryActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_CATEGORY);
        });
    }
}