package com.bdajaya.adminku.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * A TextWatcher that prevents recursive calls when programmatically changing text.
 */
public abstract class SafeTextWatcher implements TextWatcher {

    protected boolean isUpdating = false;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Default implementation does nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Default implementation does nothing
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }
}

