package com.tool;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.example.test.toolbarRichText.R;


public class ToolbarImageButton extends android.support.v7.widget.AppCompatImageButton {
    private static final int[] CHECKED_STATE_SET = {R.attr.state_checked};
    private boolean mChecked;

    public ToolbarImageButton(Context context) {
        this(context, null);
    }

    public ToolbarImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ToolbarButton);
    }

    public ToolbarImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToolbarImageButton, defStyle, 0);
        mChecked = a.getBoolean(R.styleable.ToolbarImageButton_checked, false);
        a.recycle();
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + CHECKED_STATE_SET.length);
        if (mChecked) mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }
}
