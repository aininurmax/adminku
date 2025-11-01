package com.bdajaya.adminku.ui.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.ui.fragments.AccountFragment;
import com.bdajaya.adminku.ui.fragments.ProductFragment;
import com.bdajaya.adminku.ui.fragments.HomeFragment;

import org.jetbrains.annotations.NotNull;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    AnimatedBottomBar animatedBottomBar;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Example 1");

        animatedBottomBar = findViewById(R.id.animatedBottomBar);

        if (savedInstanceState == null) {
            animatedBottomBar.selectTabById(R.id.home, true);
            fragmentManager = getSupportFragmentManager();
            HomeFragment homeFragment = new HomeFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, homeFragment)
                    .commit();
        }

        animatedBottomBar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            @Override
            public void onTabReselected(int i, @NotNull AnimatedBottomBar.Tab tab) {
                // No action on reselection
            }

            @Override
            public void onTabSelected(int lastIndex, @Nullable AnimatedBottomBar.Tab lastTab, int newIndex, @NotNull AnimatedBottomBar.Tab newTab) {
                Fragment fragment = null;
                int tabId = newTab.getId();
                if (tabId == R.id.home) {
                    fragment = new HomeFragment();
                } else if (tabId == R.id.products) {
                    fragment = new ProductFragment();
                } else if (tabId == R.id.account) {
                    fragment = new AccountFragment();
                }

                if (fragment != null) {
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                            .commit();
                } else {
                    Log.e(TAG, "Error in creating Fragment");
                }
            }
        });
    }

    /**
     * Called when products card is clicked (defined in XML with onClick attribute)
     */
    @Deprecated
    public void onProductsCardClick(View view) {
        Intent intent = new Intent(this, ProductManagementActivity.class);
        startActivity(intent);

        // Gunakan API baru untuk Android 14+ (API 34)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        } else {
            // Fix deprecated method for older versions
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    /**
     * Called when finance card is clicked (defined in XML with onClick attribute)
     */
    public void onFinanceCardClick(View view) {
        // TODO: Navigate to finance activity when implemented
        Log.d(TAG, "Finance card clicked - feature not yet implemented");
    }

    /**
     * Called when accounts card is clicked (defined in XML with onClick attribute)
     */
    public void onAccountsCardClick(View view) {
        // TODO: Navigate to accounts activity when implemented
        Log.d(TAG, "Accounts card clicked - feature not yet implemented");
    }
}
