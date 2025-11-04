package com.bdajaya.adminku.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.bdajaya.adminku.data.AppDatabase;

/**
 * Utility to run EXPLAIN QUERY PLAN on a given SQL string and log the result.
 * Use this in debug builds only.
 */
public final class SqlInspector {
    private static final String TAG = "SqlInspector";

    private SqlInspector() {}

    public static void explain(@NonNull Context context, @NonNull String sql, Object[] bindArgs) {
        try {
            SupportSQLiteDatabase db = AppDatabase.getInstance(context).getOpenHelper().getReadableDatabase();
            // SQLite API expects a normal SQL — wrap it with EXPLAIN QUERY PLAN
            String explainSql = "EXPLAIN QUERY PLAN " + sql;
            Cursor c = db.query(explainSql, bindArgs == null ? new Object[]{} : bindArgs);
            try {
                while (c.moveToNext()) {
                    // Column 0 usually contains the explanation string
                    String row = c.getString(0);
                    Log.d(TAG, "EXPLAIN: " + row);
                }
            } finally {
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to run EXPLAIN QUERY PLAN", e);
        }
    }
}