package com.bdajaya.adminku.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bdajaya.adminku.AdminkuApplication;
import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.Breadcrumb;
import com.bdajaya.adminku.data.model.CategoryWithPath;
import com.bdajaya.adminku.databinding.ActivityBrowseCategoryBinding;
import com.bdajaya.adminku.ui.adapter.BreadcrumbAdapter;
import com.bdajaya.adminku.ui.adapter.CategoryAdapter;
import com.bdajaya.adminku.ui.adapter.SearchCategoryAdapter;
import com.bdajaya.adminku.ui.viewmodel.BrowseCategoryViewModel;
import com.bdajaya.adminku.ui.viewmodel.FactoryViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class BrowseCategoryActivity extends AppCompatActivity {

    private ActivityBrowseCategoryBinding binding;
    private BrowseCategoryViewModel viewModel;

    private CategoryAdapter categoryAdapter;
    private SearchCategoryAdapter searchAdapter;
    private BreadcrumbAdapter breadcrumbAdapter;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final long SEARCH_DELAY_MS = 300;

    private boolean isUpdatingBreadcrumb = false;

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
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Tambahkan callback untuk back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                List<Breadcrumb> breadcrumbs = viewModel.getBreadcrumb().getValue();

                if (viewModel.isSearching().getValue() != null && viewModel.isSearching().getValue()) {
                    // If searching, clear search first
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
                    showSelectCategoryDialog(category);
                }
            }

            @Override
            public void onAddSubcategoryClick(Category category) {
                showAddCategoryDialog(category.getId());
            }
        });

        binding.recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCategories.setAdapter(categoryAdapter);

        // Search results
        searchAdapter = new SearchCategoryAdapter(new ArrayList<>(), categoryWithPath -> {
            // When a search result is clicked, navigate to that category
            Category category = categoryWithPath.getCategory();
            List<Category> path = categoryWithPath.getPathToRoot();

            // Clear search first
            binding.searchEditText.setText("");
            viewModel.clearSearch();

            // Navigate to the category by simulating breadcrumb clicks
            viewModel.loadRoot();

            // We need to navigate through the path in reverse order (from root to leaf)
            for (int i = path.size() - 1; i >= 0; i--) {
                final int index = i;
                // We need to delay each navigation to allow the UI to update
                new Handler().postDelayed(() -> {
                    viewModel.openParent(path.get(index));
                }, (path.size() - 1 - i) * 100L);
            }
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

    private void setupSearchView() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);

                if (s.length() > 0) {
                    searchHandler.postDelayed(() -> viewModel.search(s.toString()), SEARCH_DELAY_MS);
                } else {
                    viewModel.clearSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.clearSearchButton.setOnClickListener(v -> {
            binding.searchEditText.setText("");
            viewModel.clearSearch();
        });
    }

    // Update menu items based on current level
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem addCategoryItem = menu.findItem(R.id.action_add_category);
        if (addCategoryItem != null) {
            // Update visibility tombol add berdasarkan level saat ini
            addCategoryItem.setVisible(!viewModel.isMaxDepthReached());

            // Update judul menu sesuai level
            String menuTitle = getAddCategoryMenuTitle(viewModel.getCurrentCategoryLevel());
            addCategoryItem.setTitle(menuTitle);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_category, menu);
        return true;
    }

    private String getAddCategoryMenuTitle(int currentLevel) {
        switch (currentLevel) {
            case 0:
                return getString(R.string.add_main_category);
            case 1:
                return getString(R.string.add_subcategory);
            case 2:
                return getString(R.string.add_sub_subcategory);
            default:
                return getString(R.string.add_category);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_category) {
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
        if (isUpdatingBreadcrumb) {
            return; // Prevent infinite loops
        }

        isUpdatingBreadcrumb = true;

        try {
            // Update adapter data first
            breadcrumbAdapter.updateData(breadcrumbs);

            // Clear existing tabs
            binding.breadcrumbTabLayout.removeAllTabs();

            // Always show root tab first (always selected when no breadcrumbs)
            TabLayout.Tab rootTab = binding.breadcrumbTabLayout.newTab();
            rootTab.setText(R.string.browse_categories);
            binding.breadcrumbTabLayout.addTab(rootTab);

            // Add tabs for each breadcrumb level
            for (int i = 0; i < breadcrumbs.size(); i++) {
                Breadcrumb breadcrumb = breadcrumbs.get(i);
                TabLayout.Tab tab = binding.breadcrumbTabLayout.newTab();
                tab.setText(breadcrumb.getName());
                binding.breadcrumbTabLayout.addTab(tab);
            }

            // Select appropriate tab
            if (breadcrumbs.isEmpty()) {
                // At root level - select root tab
                binding.breadcrumbTabLayout.selectTab(rootTab);
            } else {
                // At deeper level - select last breadcrumb tab
                TabLayout.Tab lastTab = binding.breadcrumbTabLayout.getTabAt(binding.breadcrumbTabLayout.getTabCount() - 1);
                if (lastTab != null) {
                    binding.breadcrumbTabLayout.selectTab(lastTab);
                }
            }

            binding.breadcrumbTabLayout.setVisibility(View.VISIBLE);
        } finally {
            isUpdatingBreadcrumb = false;
        }
    }

    private void showAddCategoryDialog(String parentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Dapatkan level saat ini dari viewModel
        int currentLevel = viewModel.getCurrentCategoryLevel();

        // Set judul dialog sesuai level
        builder.setTitle(getAddCategoryMenuTitle(currentLevel));

        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText nameEditText = view.findViewById(R.id.category_name_edit_text);

        // Tambahkan hint sesuai level
        nameEditText.setHint(getAddCategoryHint(currentLevel));

        builder.setView(view);

        builder.setPositiveButton(R.string.add_category, (dialog, which) -> {
            String name = nameEditText.getText().toString().trim();
            if (!name.isEmpty()) {
                // Tambahkan level saat membuat kategori baru
                viewModel.addCategory(name, currentLevel, parentId);
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private String getAddCategoryHint(int level) {
        switch (level) {
            case 0:
                return getString(R.string.hint_main_category);
            case 1:
                return getString(R.string.hint_subcategory);
            case 2:
                return getString(R.string.hint_sub_subcategory);
            default:
                return getString(R.string.hint_category_name);
        }
    }

    private void showSelectCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_category);
        builder.setMessage(category.getName());

        builder.setPositiveButton(R.string.select, (dialog, which) -> {
            // Return the selected category to the caller
            Intent resultIntent = new Intent();
            resultIntent.putExtra("categoryId", category.getId());
            resultIntent.putExtra("categoryName", category.getName());

            // Get the full path
            List<Category> pathToRoot = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                pathToRoot = viewModel.getBreadcrumb().getValue().stream()
                        .map(breadcrumb -> new Category(
                                breadcrumb.getId(),
                                null,
                                breadcrumb.getLevel(),
                                breadcrumb.getName(),
                                null,
                                0,
                                0
                        ))
                        .toList();
            }

            StringBuilder pathString = new StringBuilder();
            for (int i = 0; i < pathToRoot.size(); i++) {
                pathString.append(pathToRoot.get(i).getName());
                if (i < pathToRoot.size() - 1) {
                    pathString.append(" > ");
                }
            }

            if (!pathToRoot.isEmpty()) {
                pathString.append(" > ");
            }
            pathString.append(category.getName());

            resultIntent.putExtra("pathString", pathString.toString());

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        if (!viewModel.isMaxDepthReached()) {
            builder.setNeutralButton(R.string.add_subcategory, (dialog, which) -> {
                showAddCategoryDialog(category.getId());
            });
        }

        builder.create().show();
    }
}
