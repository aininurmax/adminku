package com.bdajaya.adminku.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.databinding.ViewCardSelectBinding;
import com.bdajaya.adminku.ui.components.RoundedBackground;

public class CardSelectView extends FrameLayout {

    private ViewCardSelectBinding b;
    private boolean showIcon = false;

    public CardSelectView(Context c) { this(c, null); }
    public CardSelectView(Context c, @Nullable AttributeSet a) { this(c, a, 0); }
    public CardSelectView(Context c, @Nullable AttributeSet a, int s) {
        super(c, a, s);
        b = ViewCardSelectBinding.inflate(LayoutInflater.from(c), this);

        TypedArray ta = c.obtainStyledAttributes(a, R.styleable.CardSelectView, s, 0);
        String title = ta.getString(R.styleable.CardSelectView_csv_title);
        String value = ta.getString(R.styleable.CardSelectView_csv_value);
        boolean required = ta.getBoolean(R.styleable.CardSelectView_csv_required, false);
        showIcon = ta.getBoolean(R.styleable.CardSelectView_csv_showIcon, false);
        int pos = ta.getInt(R.styleable.CardSelectView_csv_positionGroup, 3);
        int bg = ta.getColor(R.styleable.CardSelectView_csv_colorBackground,
                getResources().getColor(R.color.surface, getContext().getTheme()));
        int inputColor = ta.getColor(R.styleable.CardSelectView_csv_colorInput,
                getResources().getColor(R.color.primary_text, getContext().getTheme()));
        int iconColor = ta.getColor(R.styleable.CardSelectView_csv_colorIcon,
                getResources().getColor(R.color.primary_text, getContext().getTheme()));
        int titleColor = ta.getColor(R.styleable.CardSelectView_csv_colorTitle,
                getResources().getColor(R.color.secondary_text, getContext().getTheme()));
        int requiredColor = ta.getColor(R.styleable.CardSelectView_csv_colorRequired,
                getResources().getColor(R.color.primary, getContext().getTheme()));
        int colorStroke = ta.getColor(R.styleable.CardSelectView_csv_colorStroke,
                getResources().getColor(R.color.outline, getContext().getTheme()));

        int iconRes = ta.getResourceId(R.styleable.CardSelectView_csv_iconSrc, R.drawable.ic_category);
        ta.recycle();

        b.root.setBackground(RoundedBackground.build(getContext(),
                16f, bg,
                getResources().getColor(R.color.outline, getContext().getTheme()),
                RoundedBackground.dp(getContext(), 1), pos));

        b.csvTitle.setText(title != null ? title + (required ? " *" : "") : "");
        b.csvName.setText(value != null ? value : "");
        b.csvValue.setText(value != null ? value : "");
        b.icon.setImageResource(iconRes);

        b.icon.setVisibility(showIcon ? VISIBLE : GONE);
        if (showIcon && iconRes != 0) b.icon.setImageResource(iconRes);

        // Set colors
        b.root.setBackground(RoundedBackground.build(getContext(),16f, bg, colorStroke,
                RoundedBackground.dp(getContext(),1), pos));
        b.icon.setColorFilter(iconColor);
        b.csvTitle.setTextColor(titleColor);

        b.csvName.setTextColor(inputColor);
        b.csvValue.setTextColor(inputColor);
        if (required && title != null) {
            SpannableString ss = new SpannableString(title + " *");
            ss.setSpan(new ForegroundColorSpan(requiredColor), ss.length() - 2, ss.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            b.csvTitle.setText(ss);
        }
    }

    public void setPrimaryText(String text) {
        b.csvName.setText(text != null ? text : "");
    }

    public void setSecondaryText(String text) {
        b.csvValue.setText(text != null ? text : "");
    }

    public void setValue(String v) {
        b.csvValue.setText(v);
    }
        public String getValue() {
        return b.csvValue.getText().toString();
    }
    public void setOnRowClick(OnClickListener l) {
        b.row.setOnClickListener(l);
    }
    public void setTrailing(boolean visible) {
        b.trailing.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        b.row.setEnabled(enabled);
        b.row2.setEnabled(enabled);
        b.trailing.setAlpha(enabled ? 1f : 0.5f);
    }

}