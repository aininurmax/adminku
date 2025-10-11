package com.bdajaya.adminku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bdajaya.adminku.data.entity.Config;

@Dao
public interface ConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Config config);

    @Update
    void update(Config config);

    @Query("SELECT * FROM Config WHERE `key` = :key")
    Config getByKey(String key);

    @Query("SELECT * FROM Config WHERE `key` = :key")
    LiveData<Config> getByKeyLive(String key);

    @Query("SELECT value FROM Config WHERE `key` = :key")
    String getValueByKey(String key);

    @Query("SELECT value FROM Config WHERE `key` = :key")
    LiveData<String> getValueByKeyLive(String key);
}
