package com.bdajaya.adminku.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bdajaya.adminku.R;
import com.bdajaya.adminku.databinding.ActivityBrowseUnitBinding;
import com.bdajaya.adminku.databinding.DialogAddEditUnitBinding;
import com.bdajaya.adminku.ui.adapter.UnitAdapter;
import com.bdajaya.adminku.ui.viewmodel.UnitViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BrowseUnitActivity extends AppCompatActivity {
    private ActivityBrowseUnitBinding binding;
    private UnitViewModel viewModel;
    private UnitAdapter adapter;
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseUnitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check unit_management preference for default mode
        // If unit_management=true: show management UI (selectionMode=false)
        // If unit_management=false: show selection UI (selectionMode=true)
        android.content.SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        boolean unitManagementEnabled = preferences.getBoolean("unit_management", false);

        // Default selectionMode based on preference (inverse of unit_management)
        isSelectionMode = !unitManagementEnabled;

        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupFab();
        setupSearch();
        observeData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isSelectionMode ? "Pilih Satuan" : "Kelola Satuan");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UnitViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new UnitAdapter(unit -> {
            if (isSelectionMode) {
                Intent result = new Intent();
                result.putExtra("unitId", unit.getId());
                result.putExtra("unitName", unit.getName());
                result.putExtra("conversionFactor", unit.getConversionFactor());
                result.putExtra("baseUnit", unit.getBaseUnit());
                setResult(RESULT_OK, result);
                finish();
            } else {
                showEditDialog(unit);
            }
        }, unit -> {
            showDeleteConfirmation(unit);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        if (isSelectionMode) {
            binding.fabAdd.setVisibility(View.GONE);
        } else {
            binding.fabAdd.setOnClickListener(v -> showAddDialog());
        }
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchUnits(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeData() {
        viewModel.getUnits().observe(this, units -> {
            if (units != null && !units.isEmpty()) {
                adapter.submitList(units);
                binding.emptyView.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            } else {
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showAddDialog() {
        DialogAddEditUnitBinding dialogBinding = DialogAddEditUnitBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Tambah Satuan Unit")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Simpan", null)
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String code = dialogBinding.codeInput.getText().toString().trim();
                String name = dialogBinding.nameInput.getText().toString().trim();
                String baseUnit = dialogBinding.baseUnitSpinner.getSelectedItem().toString();
                String quantityStr = dialogBinding.quantityInput.getText().toString().trim();

                if (code.isEmpty()) {
                    dialogBinding.codeInput.setError("Kode tidak boleh kosong");
                    return;
                }
                if (name.isEmpty()) {
                    dialogBinding.nameInput.setError("Nama tidak boleh kosong");
                    return;
                }
                if (quantityStr.isEmpty()) {
                    dialogBinding.quantityInput.setError("Kuantitas tidak boleh kosong");
                    return;
                }

                try {
                    long quantity = Long.parseLong(quantityStr);
                    if (quantity <= 0) {
                        dialogBinding.quantityInput.setError("Kuantitas harus lebih dari 0");
                        return;
                    }

                    viewModel.addUnit(code, name, baseUnit, quantity);
                    dialog.dismiss();
                    Snackbar.make(binding.getRoot(), "Satuan berhasil ditambahkan", Snackbar.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    dialogBinding.quantityInput.setError("Kuantitas tidak valid");
                }
            });
        });

        dialog.show();
    }

    private void showEditDialog(com.bdajaya.adminku.data.entity.Unit unit) {
        DialogAddEditUnitBinding dialogBinding = DialogAddEditUnitBinding.inflate(getLayoutInflater());

        dialogBinding.codeInput.setText(unit.getName());
        dialogBinding.codeInput.setEnabled(false);
        dialogBinding.nameInput.setText(unit.getName());
        dialogBinding.quantityInput.setText(String.valueOf(unit.getConversionFactor()));

        // Set base unit
        if (unit.getBaseUnit().equals("pcs")) {
            dialogBinding.baseUnitSpinner.setSelection(0);
        } else {
            dialogBinding.baseUnitSpinner.setSelection(1);
        }
        dialogBinding.baseUnitSpinner.setEnabled(false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Satuan Unit")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Update", null)
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.nameInput.getText().toString().trim();
                String quantityStr = dialogBinding.quantityInput.getText().toString().trim();

                if (name.isEmpty()) {
                    dialogBinding.nameInput.setError("Nama tidak boleh kosong");
                    return;
                }
                if (quantityStr.isEmpty()) {
                    dialogBinding.quantityInput.setError("Kuantitas tidak boleh kosong");
                    return;
                }

                try {
                    long quantity = Long.parseLong(quantityStr);
                    if (quantity <= 0) {
                        dialogBinding.quantityInput.setError("Kuantitas harus lebih dari 0");
                        return;
                    }

                    viewModel.updateUnit(unit.getId(), name, quantity);
                    dialog.dismiss();
                    Snackbar.make(binding.getRoot(), "Satuan berhasil diupdate", Snackbar.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    dialogBinding.quantityInput.setError("Kuantitas tidak valid");
                }
            });
        });

        dialog.show();
    }

    private void showDeleteConfirmation(com.bdajaya.adminku.data.entity.Unit unit) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Satuan")
                .setMessage("Yakin ingin menghapus satuan '" + unit.getName() + "'?")
                .setPositiveButton("Hapus", (d, w) -> {
                    viewModel.deleteUnit(unit.getId());
                    Snackbar.make(binding.getRoot(), "Satuan berhasil dihapus", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .show();
    }
}
