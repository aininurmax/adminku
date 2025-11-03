package com.bdajaya.adminku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.data.entity.Brand;
import com.bdajaya.adminku.databinding.ActivityBrowseBrandBinding;
import com.bdajaya.adminku.ui.adapter.BrandAdapter;
import com.bdajaya.adminku.ui.fragments.AddBrandBottomSheet;
import com.bdajaya.adminku.ui.fragments.BrandOptionsBottomSheet;
import com.bdajaya.adminku.ui.fragments.ConfirmationBottomSheet;
import com.bdajaya.adminku.ui.fragments.UpdateBrandBottomSheet;
import com.bdajaya.adminku.ui.viewmodel.BrandViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for browsing and managing brands with improved architecture and error handling.
 * This activity focuses on UI coordination and delegates business logic to ViewModel and Services.
 *
 * @author Adminku Development Team
 * @version 2.0.0
 */
@AndroidEntryPoint
public class BrowseBrandActivity extends AppCompatActivity {

    private ActivityBrowseBrandBinding binding;
    private BrandViewModel viewModel;
    private BrandAdapter brandAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseBrandBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.browse_brands);
        }

        // Enable back button in toolbar
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Add callback for back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewModel.getIsLoading().getValue() != null && viewModel.getIsLoading().getValue()) {
                    // If loading, don't allow back press
                    return;
                }

                if (viewModel.getErrorMessage().getValue() != null && !viewModel.getErrorMessage().getValue().isEmpty()) {
                    // If there's an error, clear it first
                    viewModel.loadBrands();
                } else {
                    // Otherwise, just finish the activity
                    setEnabled(false);
                    finish();
                }
            }
        });

        // Initialize ViewModel and RecyclerViews
        setupViewModel();
        setupRecyclerViews();
        setupSearchView();

        // Observe ViewModel LiveData
        observeViewModel();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BrandViewModel.class);
    }

    private void setupRecyclerViews() {
        brandAdapter = new BrandAdapter(new ArrayList<>(), new BrandAdapter.BrandClickListener() {
            @Override
            public void onBrandClick(Brand brand) {
                returnSelectedBrand(brand);
            }

            @Override
            public void onBrandLongClick(Brand brand) {
                showBrandOptionsDialog(brand);
            }
        });

        binding.recyclerViewBrands.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBrands.setAdapter(brandAdapter);
    }

    private void setupSearchView() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);

                if (s != null && s.length() > 0) {
                    searchHandler.postDelayed(() -> {
                        try {
                            viewModel.searchBrands(s.toString());
                        } catch (Exception e) {
                            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error during brand search", e);
                            showErrorMessage(Constants.ERROR_UNEXPECTED);
                        }
                    }, Constants.SEARCH_DEBOUNCE_DELAY_MS);
                } else {
                    viewModel.loadBrands();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        binding.clearSearchButton.setOnClickListener(v -> {
            try {
                hideKeyboard();
                binding.searchEditText.setText("");
                viewModel.loadBrands();
            } catch (Exception e) {
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error clearing brand search", e);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_brand, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_brand) {
            showAddBrandDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void observeViewModel() {
        viewModel.getBrands().observe(this, this::updateBrandList);
        viewModel.getErrorMessage().observe(this, this::showErrorMessage);
        viewModel.getIsLoading().observe(this, this::updateLoadingState);
    }

    private void showErrorMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLoadingState(Boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void updateBrandList(List<Brand> brands) {
        brandAdapter.updateData(brands);
        updateEmptyViewVisibility();
    }

    private void updateEmptyViewVisibility() {
        boolean shouldShowEmpty = brandAdapter.getItemCount() == 0;
        binding.emptyView.setVisibility(shouldShowEmpty ? View.VISIBLE : View.GONE);
    }

    private void showAddBrandDialog() {
        try {
            AddBrandBottomSheet bottomSheet = new AddBrandBottomSheet();
            bottomSheet.setOnBrandActionListener(new AddBrandBottomSheet.OnBrandActionListener() {
                @Override
                public void onBrandAdded(String brandName) {
                    try {
                        viewModel.addBrand(brandName);
                    } catch (Exception e) {
                        ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error adding brand", e);
                        Toast.makeText(BrowseBrandActivity.this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancel() {
                    // Nothing to do, bottom sheet will dismiss automatically
                }
            });

            bottomSheet.show(getSupportFragmentManager(), "AddBrandBottomSheet");

        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error showing add brand bottom sheet", e);
            Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
        }
    }

    private void returnSelectedBrand(Brand brand) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("brandId", brand.getId());
        resultIntent.putExtra("brandName", brand.getName());

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showBrandOptionsDialog(Brand brand) {
        BrandOptionsBottomSheet bottomSheet = BrandOptionsBottomSheet.newInstance(
            brand.getId(),
            brand.getName()
        );

        bottomSheet.setOnBrandOptionSelectedListener(new BrandOptionsBottomSheet.OnBrandOptionSelectedListener() {
            @Override
            public void onUpdateBrand() {
                showUpdateBrandDialog(brand);
            }

            @Override
            public void onDeleteBrand() {
                showDeleteBrandConfirmation(brand);
            }

            @Override
            public void onCancel() {
                // Nothing to do, bottom sheet will dismiss automatically
            }
        });

        bottomSheet.show(getSupportFragmentManager(), "BrandOptionsBottomSheet");
    }

    private void showUpdateBrandDialog(Brand brand) {
        try {
            UpdateBrandBottomSheet bottomSheet = UpdateBrandBottomSheet.newInstance(brand);
            bottomSheet.setOnBrandUpdateListener(new UpdateBrandBottomSheet.OnBrandUpdateListener() {
                @Override
                public void onBrandUpdated(String brandId, String newName) {
                    try {
                        viewModel.updateBrand(brandId, newName);
                    } catch (Exception e) {
                        ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error updating brand", e);
                        Toast.makeText(BrowseBrandActivity.this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancel() {
                    // Nothing to do, bottom sheet will dismiss automatically
                }
            });

            bottomSheet.show(getSupportFragmentManager(), "UpdateBrandBottomSheet");

        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error showing update brand bottom sheet", e);
            Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteBrandConfirmation(Brand brand) {
        try {
            ConfirmationBottomSheet bottomSheet = ConfirmationBottomSheet.newInstanceForBrand(brand);
            bottomSheet.setOnConfirmationActionListener(new ConfirmationBottomSheet.OnConfirmationActionListener() {
                @Override
                public void onConfirmDelete(Object itemToDelete) {
                    if (itemToDelete instanceof Brand) {
                        try {
                            Brand brandToDelete = (Brand) itemToDelete;
                            viewModel.deleteBrand(brandToDelete.getId());
                        } catch (Exception e) {
                            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error deleting brand", e);
                            Toast.makeText(BrowseBrandActivity.this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancel() {
                    // Nothing to do, bottom sheet will dismiss automatically
                }
            });

            bottomSheet.show(getSupportFragmentManager(), "ConfirmationBottomSheet");

        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error showing confirmation bottom sheet", e);
            Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error hiding keyboard", e);
        }
    }
}
