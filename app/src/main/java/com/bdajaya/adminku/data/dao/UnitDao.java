package com.bdajaya.adminku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bdajaya.adminku.data.entity.Unit;

import java.util.List;

@Dao
public interface UnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Unit unit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Unit> units);

    @Update
    void update(Unit unit);

    @Delete
    void delete(Unit unit);

    @Query("SELECT * FROM Unit WHERE id = :id")
    Unit getById(String id);

    @Query("SELECT * FROM Unit WHERE id = :id")
    LiveData<Unit> getByIdLive(String id);

    @Query("SELECT * FROM Unit WHERE name = :name")
    Unit getByName(String name);

    @Query("SELECT * FROM Unit ORDER BY CASE WHEN isBaseUnit = 1 THEN 0 ELSE 1 END, name ASC")
    LiveData<List<Unit>> getAll();

    @Query("SELECT * FROM Unit ORDER BY CASE WHEN isBaseUnit = 1 THEN 0 ELSE 1 END, name ASC")
    List<Unit> getAllSync();

    @Query("SELECT * FROM Unit WHERE isBaseUnit = 1 ORDER BY name ASC")
    List<Unit> getBaseUnits();

    @Query("SELECT * FROM Unit WHERE baseUnit = :baseUnitName ORDER BY name ASC")
    List<Unit> getByBaseUnit(String baseUnitName);

    @Query("SELECT COUNT(*) FROM Unit WHERE name = :name")
    int countByName(String name);

    @Query("SELECT COUNT(*) FROM Unit WHERE name = :name AND id != :excludeId")
    int countByNameExcludingId(String name, String excludeId);

    @Query("SELECT * FROM Unit WHERE name LIKE '%' || :query || '%' ORDER BY CASE WHEN isBaseUnit = 1 THEN 0 ELSE 1 END, name ASC")
    List<Unit> search(String query);

    @Query("SELECT COUNT(*) FROM Product WHERE unitId = :unitId")
    int countProductsByUnitId(String unitId);

    @Query("DELETE FROM Unit WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Unit WHERE baseUnit = :baseUnit AND conversionFactor = :conversionFactor")
    Unit findByBaseUnitAndConversion(String baseUnit, long conversionFactor);
}