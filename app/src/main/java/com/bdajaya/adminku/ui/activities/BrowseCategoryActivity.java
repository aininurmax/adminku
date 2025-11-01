package com.bdajaya.adminku.ui.activities;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bdajaya.adminku.AdminkuApplication;
import com.bdajaya.adminku.R;
import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.core.ValidationUtils;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.Breadcrumb;
import com.bdajaya.adminku.data.model.CategoryWithPath;
import com.bdajaya.adminku.data.repository.CategoryRepository;
import com.bdajaya.adminku.databinding.ActivityBrowseCategoryBinding;
import com.bdajaya.adminku.ui.adapter.BreadcrumbAdapter;
import com.bdajaya.adminku.ui.adapter.CategoryAdapter;
import com.bdajaya.adminku.ui.adapter.SearchCategoryAdapter;
import com.bdajaya.adminku.ui.fragments.AddCategoryBottomSheet;
import com.bdajaya.adminku.ui.fragments.CategoryOptionsBottomSheet;
import com.bdajaya.adminku.ui.fragments.ConfirmationBottomSheet;
import com.bdajaya.adminku.ui.fragments.UpdateCategoryBottomSheet;
import com.bdajaya.adminku.ui.viewmodel.BrowseCategoryViewModel;
import com.bdajaya.adminku.ui.viewmodel.FactoryViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for browsing and managing categories with improved architecture and error handling.
 * This activity focuses on UI coordination and delegates business logic to ViewModel and Use Cases.
 *
 * @author Adminku Development Team
 * @version 2.1.0
 */
public class BrowseCategoryActivity extends AppCompatActivity {

    private ActivityBrowseCategoryBinding binding;
    private BrowseCategoryViewModel viewModel;

    private CategoryAdapter categoryAdapter;
    private SearchCategoryAdapter searchAdapter;
    private BreadcrumbAdapter breadcrumbAdapter;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private boolean isUpdatingBreadcrumb = false;
    private Category currentSelectedCategory; // Untuk menyimpan kategori yang dipilih

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize activity and view binding
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.browse_categories);
        }

        // Enable back button in toolbar
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Tambahkan callback untuk back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                List<Breadcrumb> breadcrumbs = viewModel.getBreadcrumb().getValue();

                if (viewModel.isSearching().getValue() != null && viewModel.isSearching().getValue()) {
                    // If searching, clear search first
                    hideKeyboard();
                    binding.searchEditText.setText("");
                    viewModel.clearSearch();
                } else if (breadcrumbs != null && !breadcrumbs.isEmpty()) {
                    // If we're not at the root, go up one level
                    if (breadcrumbs.size() > 1) {
                        viewModel.jumpToBreadcrumb(breadcrumbs.size() - 2);
                    } else {
                        viewModel.loadRoot();
                    }
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

        // Load root categories
        viewModel.loadRoot();
    }

    private void setupViewModel() {
        AdminkuApplication application = (AdminkuApplication) getApplication();
        FactoryViewModel factory = new FactoryViewModel(application.getCategoryRepository());
        viewModel = new ViewModelProvider(this, factory).get(BrowseCategoryViewModel.class);
    }

    private void setupRecyclerViews() {
        // Category list
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), new CategoryAdapter.CategoryClickListener() {
            @Override
            public void onCategoryClick(Category category, boolean hasChildren) {
                if (hasChildren) {
                    viewModel.openParent(category);
                } else {
                    // Langsung return kategori yang dipilih tanpa dialog konfirmasi
                    returnSelectedCategory(category);
                }
            }

            @Override
            public void onAddSubcategoryClick(Category category) {
                showAddCategoryDialog(category.getId());
            }

            @Override
            public void onCategoryLongClick(Category category) {
                showCategoryOptionsDialog(category);
            }
        });

        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);

        // Search results
        searchAdapter = new SearchCategoryAdapter(new ArrayList<>(), categoryWithPath -> {
            // When a search result is clicked, select the category and return it
            Category category = categoryWithPath.getCategory();

            // Hide keyboard for better UX
            hideKeyboard();

            // Clear search first
            binding.searchEditText.setText("");
            viewModel.clearSearch();

            // Add small delay for smoother transition before returning result
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                returnSelectedCategoryWithPath(category, categoryWithPath);
            }, 150);
        });

        binding.recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSearchResults.setAdapter(searchAdapter);

        // Breadcrumb tabs
        breadcrumbAdapter = new BreadcrumbAdapter(new ArrayList<>(), position -> {
            viewModel.jumpToBreadcrumb(position);
        });

        binding.breadcrumbTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // Root tab - load root categories
                    if (!isUpdatingBreadcrumb) {
                        viewModel.loadRoot();
                    }
                } else {
                    // Breadcrumb tabs - navigate to specific level (position - 1 because of root tab)
                    if (!isUpdatingBreadcrumb) {
                        viewModel.jumpToBreadcrumb(position - 1);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Don't do anything on reselection to avoid infinite loops
            }
        });
    }

    /**
     * Sets up the search view with debouncing and proper error handling.
     */
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
                    // Use Constants for search delay
                    searchHandler.postDelayed(() -> {
                        try {
                            viewModel.search(s.toString());
                        } catch (Exception e) {
                            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error during search", e);
                            showErrorMessage(Constants.ERROR_UNEXPECTED);
                        }
                    }, Constants.SEARCH_DEBOUNCE_DELAY_MS);
                } else {
                    viewModel.clearSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        binding.clearSearchButton.setOnClickListener(v -> {
            try {
                // Hide keyboard for better UX
                hideKeyboard();
                binding.searchEditText.setText("");
                viewModel.clearSearch();
            } catch (Exception e) {
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error clearing search", e);
            }
        });
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_category, menu);
        return true;
    }

    // Update method getAddCategoryMenuTitle untuk menggunakan depth
    private String getAddCategoryMenuTitle(int currentDepth) {
        switch (currentDepth) {
            case 0:
                return getString(R.string.add_main_category);
            case 1:
                return getString(R.string.add_subcategory);
            case 2:
                return getString(R.string.add_sub_subcategory);
            case 3:
                return getString(R.string.add_sub_sub_subcategory);
            default:
                return getString(R.string.add_category);
        }
    }

    // PERBAIKI BARIS 262 - Extract common logic
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_category) {
            // Gunakan method yang sudah diperbaiki
            if (viewModel.isMaxDepthReached()) {
                Toast.makeText(this, R.string.max_depth_reached, Toast.LENGTH_SHORT).show();
                return true;
            } else {
                showAddCategoryDialog(viewModel.getCurrentParentId());
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void observeViewModel() {
        viewModel.getBreadcrumb().observe(this, this::updateBreadcrumb);
        viewModel.getCurrentLevelItems().observe(this, this::updateCategoryList);
        viewModel.getSearchResults().observe(this, this::updateSearchResults);
        viewModel.isSearching().observe(this, this::updateSearchState);
        viewModel.isLoading().observe(this, this::updateLoadingState);
        viewModel.getErrorMessage().observe(this, this::showErrorMessage);
    }

    private void showErrorMessage(String s) {
        if (s != null && !s.isEmpty()) {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLoadingState(Boolean aBoolean) {
        binding.progressBar.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
    }

    private void updateSearchResults(List<CategoryWithPath> categoryWithPaths) {
        searchAdapter.updateData(categoryWithPaths);
        updateEmptyViewVisibility(true);
    }

    private void updateCategoryList(List<Category> categories) {
        categoryAdapter.updateData(categories);
        updateEmptyViewVisibility(false);
        invalidateOptionsMenu(); // Update menu items based on current level
    }

    private void updateSearchState(boolean isSearching) {
        binding.recyclerViewCategories.setVisibility(isSearching ? View.GONE : View.VISIBLE);
        binding.recyclerViewSearchResults.setVisibility(isSearching ? View.VISIBLE : View.GONE);
        updateEmptyViewVisibility(isSearching);
        binding.clearSearchButton.setVisibility(isSearching ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyViewVisibility(boolean isSearching) {
        if (isSearching) {
            binding.emptyView.setVisibility(View.GONE);
        } else {
            boolean shouldShowEmpty = categoryAdapter.getItemCount() == 0;
            binding.emptyView.setVisibility(shouldShowEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void updateBreadcrumb(List<Breadcrumb> breadcrumbs) {
        if (isUpdatingBreadcrumb) return;
        isUpdatingBreadcrumb = true;

        try {
            breadcrumbAdapter.updateData(breadcrumbs);
            binding.breadcrumbTabLayout.removeAllTabs();

            // Always show root tab
            TabLayout.Tab rootTab = binding.breadcrumbTabLayout.newTab();
            rootTab.setText(R.string.browse_categories);
            binding.breadcrumbTabLayout.addTab(rootTab);

            // Add breadcrumb tabs
            for (Breadcrumb breadcrumb : breadcrumbs) {
                TabLayout.Tab tab = binding.breadcrumbTabLayout.newTab();
                tab.setText(breadcrumb.getName());
                binding.breadcrumbTabLayout.addTab(tab);
            }

            // Select appropriate tab
            if (breadcrumbs.isEmpty()) {
                binding.breadcrumbTabLayout.selectTab(rootTab);
            } else {
                TabLayout.Tab lastTab = binding.breadcrumbTabLayout.getTabAt(
                        binding.breadcrumbTabLayout.getTabCount() - 1
                );
                if (lastTab != null) {
                    binding.breadcrumbTabLayout.selectTab(lastTab);
                }
            }
        } finally {
            isUpdatingBreadcrumb = false;
        }
    }

    /**
     * Shows the add category bottom sheet with validation and error handling.
     *
     * @param parentId The parent category ID (null for root categories)
     */
    private void showAddCategoryDialog(String parentId) {
        try {
            // Get current level from viewModel
            int currentLevel = viewModel.getCurrentCategoryLevel();

            // Create and show bottom sheet
            AddCategoryBottomSheet bottomSheet = AddCategoryBottomSheet.newInstance(parentId, currentLevel);
            bottomSheet.setOnCategoryActionListener(new AddCategoryBottomSheet.OnCategoryActionListener() {
                @Override
                public void onCategoryAdded(String categoryName) {
                    try {
                        // Validate input
                        ValidationUtils.ValidationResult validation = ValidationUtils.validateCategoryName(categoryName);
                        if (validation.isFailure()) {
                            Toast.makeText(BrowseCategoryActivity.this, validation.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Let ViewModel/CategoryRepository handle level calculation and validation
                        viewModel.addCategory(categoryName, parentId);
                    } catch (Exception e) {
                        ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error adding category", e);
                        Toast.makeText(BrowseCategoryActivity.this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancel() {
                    // Nothing to do, bottom sheet will dismiss automatically
                }
            });

            bottomSheet.show(getSupportFragmentManager(), "AddCategoryBottomSheet");

        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error showing add category bottom sheet", e);
            Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
        }
    }

    private void returnSelectedCategory(Category category) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("categoryId", category.getId());
        resultIntent.putExtra("categoryName", category.getName());

        // Build path string dari breadcrumb
        List<Breadcrumb> breadcrumbs = viewModel.getBreadcrumb().getValue();
        StringBuilder pathString = new StringBuilder();

        if (breadcrumbs != null && !breadcrumbs.isEmpty()) {
            for (int i = 0; i < breadcrumbs.size(); i++) {
                pathString.append(breadcrumbs.get(i).getName());
                if (i < breadcrumbs.size() - 1) {
                    pathString.append(" > ");
                }
            }
            pathString.append(" > ");
        }
        pathString.append(category.getName());

        resultIntent.putExtra("pathString", pathString.toString());
        resultIntent.putExtra("categoryLevel", category.getLevel());

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void returnSelectedCategoryWithPath(Category category, CategoryWithPath categoryWithPath) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("categoryId", category.getId());
        resultIntent.putExtra("categoryName", category.getName());

        // Use the full path from CategoryWithPath instead of current breadcrumb
        String pathString = categoryWithPath.getPathString();
        resultIntent.putExtra("pathString", pathString);
        resultIntent.putExtra("categoryLevel", category.getLevel());

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Modern, clean, dan robust implementation untuk showCategoryOptionsDialog
     */
    private void showCategoryOptionsDialog(@NonNull Category category) {
        try {
            // Validasi input
            if (category == null || category.getId() == null) {
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_VALIDATION,
                        "Invalid category for options dialog", null);
                return;
            }

            // Use the Bottom Sheet fragment instead of AlertDialog
            CategoryOptionsBottomSheet bottomSheet = CategoryOptionsBottomSheet.newInstance(
                    category.getId(),
                    category.getName()
            );

            bottomSheet.setOnCategoryOptionSelectedListener(new CategoryOptionsBottomSheet.OnCategoryOptionSelectedListener() {
                @Override
                public void onAddSubcategory() {
                    if (viewModel.canAddSubcategoryTo(category)) {
                        showAddCategoryDialog(category.getId());
                    } else {
                        Toast.makeText(BrowseCategoryActivity.this,
                                R.string.max_depth_reached, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onUpdateCategory() {
                    showUpdateCategoryDialog(category);
                }

                @Override
                public void onDeleteCategory() {
                    showDeleteCategoryConfirmation(category);
                }

                @Override
                public void onCancel() {
                    // Clean up resources if needed
                    currentSelectedCategory = null;
                }
            });

            bottomSheet.show(getSupportFragmentManager(), "CategoryOptionsBottomSheet");

        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN,
                    "Error showing category options dialog", e);
            Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows the update category bottom sheet with validation and error handling.
     *
     * @param category The category to update
     */
    private void showUpdateCategoryDialog(Category category) {
        if (category == null) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_VALIDATION, "Cannot update null category", null);
            Toast.makeText(this, Constants.ERROR_INVALID_INPUT, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create and show bottom sheet
            UpdateCategoryBottomSheet bottomSheet = UpdateCategoryBottomSheet.newInstance(category);
            bottomSheet.setOnCategoryUpdateListener(new UpdateCategoryBottomSheet.OnCategoryUpdateListener() {
                @Override
                public void onCategoryUpdated(String categoryId, String newName) {
                    try {
                        // Validate input
                        ValidationUtils.ValidationResult validation = ValidationUtils.validateCategoryName(newName);
                        if (validation.isFailure()) {
                            Toast.makeText(BrowseCategoryActivity.this, validation.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check if name actually changed
                        if (newName.equals(category.getName())) {
                            Toast.makeText(BrowseCategoryActivity.this, "Nama kategori tidak berubah", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        viewModel.updateCategory(categoryId, newName);
                    } catch (Exception e) {
                        ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error updating category", e);
                        Toast.makeText(BrowseCategoryActivity.this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancel() {
                    // Nothing to do, bottom sheet will dismiss automatically
                }
            });

            bottomSheet.show(getSupportFragmentManager(), "UpdateCategoryBottomSheet");

        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error showing update category bottom sheet", e);
            Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Professional implementation untuk showDeleteCategoryConfirmation
     * Menggunakan ViewModel pattern dengan benar
     */
    private void showDeleteCategoryConfirmation(@NonNull Category category) {
        if (category == null || category.getId() == null) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_VALIDATION,
                    "Invalid category for deletion confirmation", null);
            return;
        }

        currentSelectedCategory = category;
        showCustomDeleteCategoryConfirmation(category);
    }

    /**
     * Robust implementation untuk showCustomDeleteCategoryConfirmation
     * Menggunakan ViewModel untuk data fetching
     */
    private void showCustomDeleteCategoryConfirmation(@NonNull Category category) {
        // Show loading state
        binding.progressBar.setVisibility(View.VISIBLE);

        // Use ViewModel to get deletion info
        viewModel.getCategoryDeletionInfo(category.getId()).observe(this, deletionInfo -> {
            binding.progressBar.setVisibility(View.GONE);

            if (deletionInfo != null) {
                showCustomConfirmationDialog(category, deletionInfo);
            } else {
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN,
                        "Failed to get category deletion info", null);
                Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
            }

            // Remove observer to prevent memory leaks
            viewModel.getCategoryDeletionInfo(category.getId()).removeObservers(this);
        });
    }

    /**
     * Modern confirmation dialog dengan structured data
     */
    private void showCustomConfirmationDialog(@NonNull Category category,
                                              @NonNull BrowseCategoryViewModel.CategoryDeletionInfo deletionInfo) {
        try {
            // Create and show bottom sheet dengan semua informasi yang diperlukan
            ConfirmationBottomSheet bottomSheet = ConfirmationBottomSheet.newInstance(
                    category,
                    deletionInfo.hasChildren(),
                    deletionInfo.hasProducts(),
                    deletionInfo.getProductCount()
            );

            bottomSheet.setOnConfirmationActionListener(new ConfirmationBottomSheet.OnConfirmationActionListener() {
                @Override
                public void onConfirmDelete(Object itemToDelete) {
                    if (itemToDelete instanceof Category) {
                        Category categoryToDelete = (Category) itemToDelete;
                        deleteCategory(categoryToDelete);
                    }
                }

                @Override
                public void onCancel() {
                    // Clean up
                    currentSelectedCategory = null;
                }
            });

            bottomSheet.show(getSupportFragmentManager(), "ConfirmationBottomSheet");

        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN,
                    "Error showing confirmation bottom sheet", e);
            Toast.makeText(this, Constants.ERROR_UNEXPECTED, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Efficient deleteCategory method dengan proper error handling
     */
    private void deleteCategory(@NonNull Category category) {
        if (category == null || category.getId() == null) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_VALIDATION,
                    "Invalid category for deletion", null);
            return;
        }

        // Show loading state
        binding.progressBar.setVisibility(View.VISIBLE);

        // Observe deletion result
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            binding.progressBar.setVisibility(View.GONE);

            if (errorMessage == null) {
                // Success case
                Toast.makeText(BrowseCategoryActivity.this,
                        R.string.category_deleted_successfully, Toast.LENGTH_SHORT).show();

                // Auto-refresh handled by ViewModel observers
            } else {
                // Error case - already shown by ViewModel observer
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_DELETION,
                        "Category deletion failed: " + errorMessage, null);
            }

            // Clean up
            currentSelectedCategory = null;
            // Remove observer after one-time use
            viewModel.getErrorMessage().removeObservers(this);
        });

        // Execute deletion
        viewModel.deleteCategory(category.getId(), (AdminkuApplication) getApplication());
    }

    /**
     * Hides the soft keyboard for better user experience.
     */
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

    // Tambahkan method cleanup di onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any pending operations
        currentSelectedCategory = null;
        searchHandler.removeCallbacksAndMessages(null);
    }
}