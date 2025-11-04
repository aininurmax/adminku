package com.bdajaya.adminku.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

/**
 * Minimal DAO to run FTS searches and to expose docid results if needed.
 */
@Dao
public interface ProductFtsDao {

    /**
     * Return docids (rowids) matching the FTS query. Repo can then map docid -> Product.id
     * (if Product.id stored as ROWID or use a join).
     */
    @Query("SELECT docid FROM ProductFts WHERE ProductFts MATCH :query LIMIT :limit")
    List<Long> searchDocIds(String query, int limit);
}