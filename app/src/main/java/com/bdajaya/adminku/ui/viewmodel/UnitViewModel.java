package com.bdajaya.adminku.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.data.entity.Unit;
import com.bdajaya.adminku.domain.service.UnitService;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class UnitViewModel extends ViewModel {
    private final UnitService unitService;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public UnitViewModel(UnitService unitService) {
        this.unitService = unitService;
    }

    public LiveData<List<Unit>> getUnits() {
        return Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return unitService.getAllUnits();
            } else {
                return unitService.searchUnitsLive(query);
            }
        });
    }

    public void searchUnits(String query) {
        searchQuery.setValue(query);
    }

    public void addUnit(String code, String name, String baseUnit, long quantity) {
        Result<String> result = unitService.addUnit(code, name, baseUnit, quantity);
        if (result.isFailure()) {
            errorMessage.setValue(result.getErrorMessage());
        }
    }

    public void updateUnit(String id, String name, long conversionFactor) {
        Result<Void> result = unitService.updateUnit(id, name, conversionFactor);
        if (result.isFailure()) {
            errorMessage.setValue(result.getErrorMessage());
        }
    }

    public void deleteUnit(String id) {
        Result<Void> result = unitService.deleteUnit(id);
        if (result.isFailure()) {
            errorMessage.setValue(result.getErrorMessage());
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}