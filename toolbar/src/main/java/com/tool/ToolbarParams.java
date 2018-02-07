package com.tool;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

/**
 * Builder class containing all params to handle click and visibility in toolbar buttons.
 */

public class ToolbarParams {

    private ToolbarImageButton fontColorBtn;
    private ToolbarImageButton fillColorBtn;
    private ToolbarImageButton sizeBtn;
    private RadioGroup fillGroup;
    private RadioGroup fontGroup;
    private LinearLayout sizesLayout;
    private LinearLayout toolbarButtons;
    private ImageButton collapseToolbar;

    public static class Builder{

        private ToolbarImageButton fontColorBtn;
        private final ToolbarImageButton fillColorBtn;
        private final ToolbarImageButton sizeBtn;
        private final RadioGroup fillGroup;
        private final RadioGroup fontGroup;
        private final LinearLayout sizesLayout;
        private final LinearLayout toolbarButtons;
        private final ImageButton collapseToolbar;

        public Builder(ToolbarImageButton fontColorBtn, ToolbarImageButton fillColorBtn, ToolbarImageButton sizeBtn,
                       RadioGroup fillGroup, RadioGroup fontGroup, LinearLayout sizesLayout, LinearLayout toolbarButtons, ImageButton collapseToolbar){
            this.fontColorBtn= fontColorBtn;
            this.fillColorBtn=fillColorBtn;
            this.sizeBtn= sizeBtn;
            this.fillGroup= fillGroup;
            this.fontGroup=fontGroup;
            this.sizesLayout=sizesLayout;
            this.collapseToolbar=collapseToolbar;
            this.toolbarButtons=toolbarButtons;
        }
        public ToolbarParams build(){
            return new ToolbarParams(this);
        }

    }
    private ToolbarParams(Builder builder){
        fontColorBtn= builder.fontColorBtn;
        fillColorBtn= builder.fillColorBtn;
        sizeBtn= builder.sizeBtn;
        fillGroup= builder.fillGroup;
        fontGroup= builder.fontGroup;
        sizesLayout=builder.sizesLayout;
        collapseToolbar=builder.collapseToolbar;
        toolbarButtons=builder.toolbarButtons;
    }

    public ToolbarImageButton getFontColorBtn() {
        return fontColorBtn;
    }
    public ToolbarImageButton getFillColorBtn() {
        return fillColorBtn;
    }
    public ToolbarImageButton getSizeBtn() {
        return sizeBtn;
    }
    public RadioGroup getFillGroup() {
        return fillGroup;
    }
    public RadioGroup getFontGroup() {
        return fontGroup;
    }
    public LinearLayout getSizesLayout() {
        return sizesLayout;
    }
    public LinearLayout getToolbarButtons() {
        return toolbarButtons;
    }
    public ImageButton getCollapseToolbar() {
        return collapseToolbar;
    }
}