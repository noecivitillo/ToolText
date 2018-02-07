
/*The MIT License (MIT)

 This file contain parts of CustomEditor and is Copyright (c) by Phạm Huỳnh Minh Triết 2015

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 Modifications Copyright (c) 2018 Noelia Civitillo
 */

package com.tool;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ParagraphStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;


import com.example.test.toolbarRichText.R;
import com.tool.converter.HtmlHandler;
import com.tool.spans.AbsoluteSizeSpan;
import com.tool.spans.AgsUnderlineSpan;
import com.tool.spans.BackgroundColorSpan;
import com.tool.spans.BoldSpan;
import com.tool.spans.BulletSpan;

import com.tool.spans.ForegroundColorSpan;
import com.tool.spans.ItalicSpan;
import com.tool.spans.NumberSpan;
import com.tool.spans.RTFormat;
import com.tool.utils.Helper;
import com.tool.utils.Paragraph;
import com.tool.utils.RTLayout;
import com.tool.utils.Selection;

import java.util.List;






public class CustomEditText extends android.support.v7.widget.AppCompatEditText {

    // Log tag
    private static final String TAG = "CustomEditText";

    // Style constants
    private static final int STYLE_BOLD = 0;
    private static final int STYLE_ITALIC = 1;
    private static final int STYLE_UNDERLINED = 2;
    private static final int PARAGRAPH_BULLET = 3;
    private static final int PARAGRAPH_NUMBER = 4;

    // Optional styling button references
    ToolbarImageButton boldBtn;
    ToolbarImageButton italicBtn;
    ToolbarImageButton underlinedBtn;
    ToolbarImageButton bulletedBtn;
    ToolbarImageButton numberedBtn;
    SeekBar seekBarSizes;
    RadioGroup groupColors;

    boolean expanded=false;

    private boolean mLayoutChanged;
    private RTLayout mRTLayout;

    // Html image getter that handles the loading of inline images
    private Html.ImageGetter imageGetter;

    private boolean isDeleteCharaters = false;
    int nr=0;
    int removedNumber=0;
    boolean resetNumber =false;
    boolean hasPreviousLineNumber;

    // Color
    private int currentColor = -1;
    private int currentBGColor = -1;
    private int currentSize= Math.round(getTextSize());
    private EventBack eventBack;
    private int margin = Helper.getLeadingMarging();


    boolean hasNumber;
    boolean hasBullet;
    boolean cursorAtStart= false;


    int number = 0;
    //see this
    boolean addedNumber1withSetNumberButton = false;
    boolean numberButtonIsChecked=false;

    int lastFontColorChecked;
    int lastFillColorChecked;


    // interface
    public interface EventBack {
        public void close();

        public void show();
    }

    public EventBack getEventBack() {
        return eventBack;
    }

    public void setEventBack(EventBack eventBack) {
        this.eventBack = eventBack;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            eventBack.close();
        } else {
            eventBack.show();
        }
        return super.dispatchKeyEvent(event);
    }
    public CustomEditText(Context context) {
        super(context);
        initialize();
    }
    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }
    private void initialize() {
        // Add a default imageGetter
        imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                return null;
            }
        };

        // Add TextWatcher that reacts to text changes and applies the selected
        // styles
        /**
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (bulletedBtn.isChecked()) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            Log.i(TAG, "enter button pressed");
                            append("");
                            //insert(" ");
                            return true;
                        }
                    }
                }
                return false;
            }
        });
         */
        this.addTextChangedListener(new DWTextWatcher());
    }
    /**
     * When the user selects a section of the text, this method is used to
     * toggle the defined style on it. If the selected text already has the
     * style applied, we remove it, otherwise we apply it.
     *
     * @param style The styles that should be toggled on the selected text.
     */
    private void toggleStyle(int style) {
        int selectionStart = this.getSelectionStart();
        int selectionEnd = this.getSelectionEnd();

        // Reverse if the case is what's noted above
        if (selectionStart > selectionEnd) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }
        if (selectionEnd > selectionStart) {
            switch (style) {
                case STYLE_BOLD:
                    boldButtonClick(selectionStart, selectionEnd);
                    break;
                case STYLE_ITALIC:
                    italicButtonClick(selectionStart, selectionEnd);
                    break;
                case STYLE_UNDERLINED:
                    underlineButtonClick(selectionStart, selectionEnd);
                    break;
                case PARAGRAPH_BULLET:
                    bulletButtonClick(selectionStart, selectionEnd);
                    break;
                case PARAGRAPH_NUMBER:
                    //this work OK -- see afterTextChanged
                    //Effects.NUMBER.applyToSelection(this,true);
                    numberButtonClick(selectionStart, selectionEnd);
                //TODO add PARAGRAPH_NUMBER
            }
        }
    }
    private void changeSize(int size) {
        //size= Helper.convertPxToSp(size);
        int selectionStart = this.getSelectionStart();
        int selectionEnd = this.getSelectionEnd();
        // Reverse if the case is what's noted above
        if (selectionStart > selectionEnd) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }
        if (selectionEnd > selectionStart) {
            seekBarChanged(size, selectionStart, selectionEnd);
        }
    }
    public void setSizeSeekBar(SeekBar seekBar){
        //Log.i(TAG, "---TEXT SIZE IN PX is "+ currentSize);
        seekBarSizes = seekBar;
        seekBar.incrementProgressBy(50);
        seekBar.setMax(190);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==0){
                    progress=50;
                }
                changeSize(progress);
                currentSize=progress;
                if(currentSize==0){
                    currentSize=50;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.i(TAG, "---START --The progress is "+ seekBar.getProgress());
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
    }
    private void seekBarChanged(int size, int selectionStart, int selectionEnd) {
        boolean exists = false;
        Spannable str = this.getText();

        AbsoluteSizeSpan[] strikeSpan = str.getSpans(selectionStart, selectionEnd, AbsoluteSizeSpan.class);
        // If the selected text-part already has UNDERLINE style on it, then we need to disable it
        int underlineStart = -1;
        int underlineEnd = -1;

        for (AbsoluteSizeSpan styleSpan : strikeSpan) {

            if (str.getSpanStart(styleSpan) < selectionStart) {
                underlineStart = str.getSpanStart(styleSpan);
            }
            if (str.getSpanEnd(styleSpan) > selectionEnd) {
                underlineEnd = str.getSpanEnd(styleSpan);
            }
            str.removeSpan(styleSpan);
            //exists = true;
        }
        //TODO see this
        /**
         if (underlineStart > -1) {
         str.setSpan(new AbsoluteSizeSpan(size), underlineStart, selectionStart,
         Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
         if (underlineEnd > -1) {
         str.setSpan(new AbsoluteSizeSpan(size), selectionEnd, underlineEnd,
         Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
         */

        if (!exists) {
            str.setSpan(new AbsoluteSizeSpan(size), selectionStart, selectionEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        this.setSelection(selectionStart, selectionEnd);

    }
    private void numberButtonClick(int selectionStart, int selectionEnd) {
        boolean exists = false;
        Spannable str = this.getText();
        NumberSpan[] quoteSpan = str.getSpans(selectionStart - 1, selectionEnd, NumberSpan.class);
        BulletSpan[] bulletSpan = str.getSpans(selectionStart - 1, selectionEnd, BulletSpan.class);
        // If the selected text-part already has UNDERLINE style on it, then we need to disable it
        for (NumberSpan aQuoteSpan : quoteSpan) {
            str.removeSpan(aQuoteSpan);
            exists = true;
            Log.i(TAG, "Removing number in numberButtonClick");
        }
        for (BulletSpan bulletSpa : bulletSpan) {
            str.removeSpan(bulletSpa);
           // exists = true;
           // Log.i(TAG, "Removing number in numberButtonClick");
        }
        // Else we set UNDERLINE style on it
        if (!exists) {
            if (getSelectionStart() == 0 && getSelectionEnd() == getText().length()) {
                Log.i(TAG, "Selection end is " + getSelectionEnd() + "  Txt length is " + getText().length());

                int linecount = getLineCount();
                Log.i(TAG, "Amount of lines " + linecount);
                nr = 1;

                if (linecount >= 0) {
                    for (int i = 0; i < linecount; i++) {
                        int startPos = getLayout().getLineStart(i);
                        int endPos = getLayout().getLineEnd(i);


                        str.setSpan(new NumberSpan(nr++, margin, true, true, true), startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Log.i(TAG, " Adding number in position start " + startPos + " Position end " + endPos);
                    }
                }
            } else {
                nr=1;
                str.setSpan(new NumberSpan(nr++, margin,true, true, true), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Log.i(TAG, "Adding number in numberButtonClick");
            }
        }
        this.setSelection(selectionStart, selectionEnd);

    }
    private void bulletButtonClick(int selectionStart, int selectionEnd) {
        boolean exists = false;
        Spannable str = this.getText();
        NumberSpan[] quotSpan = str.getSpans(selectionStart - 1, selectionEnd, NumberSpan.class);
        BulletSpan[] quoteSpan = str.getSpans(selectionStart - 1, selectionEnd, BulletSpan.class);
        // If the selected text-part already has UNDERLINE style on it, then we need to disable it
        for (BulletSpan aQuoteSpan : quoteSpan) {
            str.removeSpan(aQuoteSpan);
            exists = true;
            Log.i(TAG, "Removing bullet in bulletButtonClick");
        }
        for (NumberSpan n : quotSpan) {
            str.removeSpan(n);
            // Else we set UNDERLINE style on it
        }
        if (!exists) {
            if (getSelectionStart() == 0 && getSelectionEnd() == getText().length()) {
                Log.i(TAG, "Selection end is " + getSelectionEnd() + "  Txt length is " + getText().length());

                int linecount = getLineCount();
                Log.i(TAG, "Amount of lines " + linecount);

                if (linecount >= 0) {
                    for (int i = 0; i < linecount; i++) {
                        int startPos = getLayout().getLineStart(i);
                        int endPos = getLayout().getLineEnd(i);

                        str.setSpan(new BulletSpan(margin, true, true, true), startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Log.i(TAG, " Adding bullet in position start " + startPos + " Position end " + endPos);
                    }
                }
            } else {
                str.setSpan(new BulletSpan(margin, true, true, true), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Log.i(TAG, "Adding bullet in bulletButtonClick");
            }
        }
        this.setSelection(selectionStart, selectionEnd);
    }
    private void underlineButtonClick(int selectionStart, int selectionEnd) {
        boolean exists = false;
        Spannable str = this.getText();
        UnderlineSpan[] underSpan = str.getSpans(selectionStart, selectionEnd, UnderlineSpan.class);
        // If the selected text-part already has UNDERLINE style on it, then we need to disable it
        int underlineStart = -1;
        int underlineEnd = -1;
        for (UnderlineSpan styleSpan : underSpan) {
            if (str.getSpanStart(styleSpan) < selectionStart) {
                underlineStart = str.getSpanStart(styleSpan);
            }
            if (str.getSpanEnd(styleSpan) > selectionEnd) {
                underlineEnd = str.getSpanEnd(styleSpan);
            }
            str.removeSpan(styleSpan);
            Log.i(TAG, "REMOVING UNDERLINE");
            exists = true;
        }
        if (underlineStart > -1) {
            str.setSpan(new UnderlineSpan(), underlineStart, selectionStart,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (underlineEnd > -1) {
            str.setSpan(new UnderlineSpan(), selectionEnd, underlineEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        // Else we set UNDERLINE style on it
        if (!exists) {
            str.setSpan(new UnderlineSpan(), selectionStart, selectionEnd,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            underlinedBtn.setChecked(false);
        }

        this.setSelection(selectionStart, selectionEnd);
    }

    private void italicButtonClick(int selectionStart, int selectionEnd) {
        handleStyleSpannable(selectionStart, selectionEnd, new ItalicSpan());
    }

    private void boldButtonClick(int selectionStart, int selectionEnd) {
        handleStyleSpannable(selectionStart, selectionEnd, new BoldSpan());
    }

    private void handleStyleSpannable(int selectionStart, int selectionEnd, BoldSpan style) {
        boolean exists = false;
        Spannable str = this.getText();
        BoldSpan[] styleSpans = str.getSpans(selectionStart, selectionEnd, BoldSpan.class);
        // If the selected text-part already has BOLD style on it,
        // then
        // we need to disable it
        int styleStart = -1;
        int styleEnd = -1;
        for (BoldSpan styleSpan : styleSpans) {
            if (styleSpan != null) {
                if (str.getSpanStart(styleSpan) < selectionStart) {
                    styleStart = str.getSpanStart(styleSpan);
                }
                if (str.getSpanEnd(styleSpan) > selectionEnd) {
                    styleEnd = str.getSpanEnd(styleSpan);
                }
                Log.i(TAG, "---REMOVING---");
                str.removeSpan(styleSpan);
                exists = true;
            }
        }
        if (styleStart > -1) {
            str.setSpan(style, styleStart, selectionStart,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.i(TAG, "---ADDING STYLESPAN---in handleStyleSpannable");
        }
        if (styleEnd > -1) {
            str.setSpan(style, selectionEnd, styleEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.i(TAG, "---ADDING STYLESPAN---");
        }

        // Else we set BOLD style on it
        if (!exists) {
            str.setSpan(style, selectionStart, selectionEnd,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            Log.i(TAG, "---ADDING ITALIC---");

        } else {
            boldBtn.setChecked(false);

        }
        this.setSelection(selectionStart, selectionEnd);
    }
    private void handleStyleSpannable(int selectionStart, int selectionEnd, ItalicSpan style) {
        boolean exists = false;
        Spannable str = this.getText();
        ItalicSpan[] styleSpans = str.getSpans(selectionStart, selectionEnd, ItalicSpan.class);
        // If the selected text-part already has BOLD style on it,
        // then
        // we need to disable it
        int styleStart = -1;
        int styleEnd = -1;
        for (ItalicSpan styleSpan : styleSpans) {
            if (styleSpan != null) {
                if (str.getSpanStart(styleSpan) < selectionStart) {
                    styleStart = str.getSpanStart(styleSpan);
                }
                if (str.getSpanEnd(styleSpan) > selectionEnd) {
                    styleEnd = str.getSpanEnd(styleSpan);
                }
                Log.i(TAG, "---REMOVING---");
                str.removeSpan(styleSpan);
                exists = true;
            }
        }
        if (styleStart > -1) {
            str.setSpan(style, styleStart, selectionStart,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.i(TAG, "---ADDING STYLESPAN---in handleStyleSpannable");
        }
        if (styleEnd > -1) {
            str.setSpan(style, selectionEnd, styleEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.i(TAG, "---ADDING STYLESPAN---");
        }

        // Else we set BOLD style on it
        if (!exists) {
            str.setSpan(style, selectionStart, selectionEnd,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            Log.i(TAG, "---ADDING ITALIC---");

        } else {
            italicBtn.setChecked(false);

        }
        this.setSelection(selectionStart, selectionEnd);
    }
    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        //TODO review this...
        boolean boldExists = false;
        boolean italicsExists = false;
        boolean underlinedExists = false;
        boolean numberExists = false;
        boolean bulletExists = false;

        // If the user only placed the cursor around
        if (selStart > 0 && selStart == selEnd) {

            CharacterStyle[] styleSpans = this.getText().getSpans(selStart - 1, selStart, CharacterStyle.class);

            ParagraphStyle[] appliedParagraphStyle = this.getText().getSpans(selStart - 1, selEnd, ParagraphStyle.class);
            for (ParagraphStyle anAppliedParagraphStyle : appliedParagraphStyle) {
                if (anAppliedParagraphStyle instanceof BulletSpan) {
                    bulletExists = true;
                    Log.i(TAG, "---FOUNDED BULLET WITH THE CURSOR AROUND---");
                }
                if (anAppliedParagraphStyle instanceof NumberSpan) {
                    numberExists = true;
                    Log.i(TAG, "---FOUNDED NUMBER WITH THE CURSOR AROUND---");
                }

            }
            for (CharacterStyle styleSpan : styleSpans) {
                if ((styleSpan) instanceof BoldSpan) {
                    Log.i(TAG, "---FOUNDED BOLD---with cursor around");
                    boldExists = true;
                } else if (styleSpan instanceof ItalicSpan) {
                    Log.i(TAG, "---FOUNDED ITALIC---with cursor around");
                    italicsExists = true;
                    //TODO fix
                } else if (styleSpan instanceof ItalicSpan && styleSpan instanceof BoldSpan) {
                    italicsExists = true;
                    boldExists = true;
                } else if (styleSpan instanceof UnderlineSpan) {
                    underlinedExists = true;
                }
            }
        }
        // Else if the user selected multiple characters
        else if (!TextUtils.isEmpty(CustomEditText.this.getText())) {
            CharacterStyle[] styleSpans = this.getText().getSpans(selStart, selEnd,
                    CharacterStyle.class);

            for (CharacterStyle styleSpan : styleSpans) {
                if (styleSpan instanceof BoldSpan) {
                    if (this.getText().getSpanStart(styleSpan) <= selStart
                            && this.getText().getSpanEnd(styleSpan) >= selEnd) {
                        Log.i(TAG, "---FOUNDED BOLD---");
                        boldExists = true;
                    }
                } else if ((styleSpan) instanceof ItalicSpan) {
                    if (this.getText().getSpanStart(styleSpan) <= selStart
                            && this.getText().getSpanEnd(styleSpan) >= selEnd) {
                        Log.i(TAG, "---FOUNDED ITALIC---");
                        italicsExists = true;
                    }
                } else if ((styleSpan) instanceof BoldSpan && (styleSpan) instanceof ItalicSpan) {
                    if (this.getText().getSpanStart(styleSpan) <= selStart
                            && this.getText().getSpanEnd(styleSpan) >= selEnd) {
                        italicsExists = true;
                        boldExists = true;
                    }
                } else if (styleSpan instanceof UnderlineSpan) {
                    if (this.getText().getSpanStart(styleSpan) <= selStart
                            && this.getText().getSpanEnd(styleSpan) >= selEnd) {
                        underlinedExists = true;
                    }
                } else if (styleSpan instanceof AbsoluteSizeSpan) {
                    if (this.getText().getSpanStart(styleSpan) <= selStart
                            && this.getText().getSpanEnd(styleSpan) >= selEnd) {
                        //TODO---
                    }
                }
            }
        } else if (!TextUtils.isEmpty(CustomEditText.this.getText())) {
            ParagraphStyle[] appliedParagraphStyle = this.getText().getSpans(selStart, selEnd,
                    ParagraphStyle.class);
            for (ParagraphStyle anAppliedParagraphStyle : appliedParagraphStyle) {
                if (anAppliedParagraphStyle instanceof BulletSpan) {
                    bulletExists = true;
                    Log.i(TAG, "---FOUNDED BULLET----");
                }
                if (anAppliedParagraphStyle instanceof NumberSpan) {
                    numberExists = true;
                    Log.i(TAG, "---FOUNDED NUMBER----");
                }
            }
        }
        // Display the format settings
        //TODO
         if (boldBtn != null) {
         if (boldExists)
         boldBtn.setChecked(true);
         else
         boldBtn.setChecked(false);
         }
         if (italicBtn != null) {
         if (italicsExists)
         italicBtn.setChecked(true);
         else
         italicBtn.setChecked(false);
         }
         if (underlinedBtn != null) {
         if (underlinedExists)
         underlinedBtn.setChecked(true);
         else
         underlinedBtn.setChecked(false);
         }
         if (bulletedBtn != null) {
            if (bulletExists)
                bulletedBtn.setChecked(true);
           // else
                //bulletedBtn.setChecked(false);
            //Log.i(TAG, " BULLET TOGGLE UNCHECKED!!!");
        }
        if (numberedBtn != null) {
            if (numberExists)
                numberedBtn.setChecked(true);
             //else
            //TODO see
               // numberedBtn.setChecked(false);
            //Log.i(TAG, " BULLET TOGGLE UNCHECKED!!!");
        }

    }

    // Get and set Spanned, styled text
    public Spanned getSpannedText() {
        return this.getText();
    }

    public void setSpannedText(Spanned text) {
        this.setText(text);
    }

    // Get and set simple text as simple strings
    public String getStringText() {
        return this.getText().toString();
    }

    public void setStringText(String text) {
        this.setText(text);
    }

    // Get and set styled HTML text
    public String getTextHTML() {
        Spanned fromInput = this.getText();
        //TODO SEE THIS
        return HtmlHandler.toHtml(fromInput, RTFormat.HTML);
        /**SpanTagRoster tagRoster = new SpanTagRoster();
         Spanned fromInput = this.getText();
         return new SpannedXhtmlGenerator(tagRoster).toXhtml(fromInput);
         */
        //return Html.toHtml(this.getText());
    }

    public void setTextHTML(String text) {
        this.setText(Html.fromHtml(text, imageGetter, null));
    }

    // Set the default image getter that handles the loading of inline images
    public void setImageGetter(Html.ImageGetter imageGetter) {
        this.imageGetter = imageGetter;
    }

    // Style toggle button setters
    public void setBoldButton(ToolbarImageButton button) {
        boldBtn = button;

        boldBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.toolbar_bold) {
                    boldBtn.setChecked(!boldBtn.isChecked());
                    toggleStyle(STYLE_BOLD);
                }
            }
        });
    }

    public void setItalicButton(ToolbarImageButton button) {
        italicBtn = button;

        italicBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.toolbar_italic) {
                    italicBtn.setChecked(!italicBtn.isChecked());
                    toggleStyle(STYLE_ITALIC);
                }
            }
        });
    }

    public void setUnderlineButton(ToolbarImageButton button) {
        underlinedBtn = button;
        underlinedBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.toolbar_underlined) {
                    underlinedBtn.setChecked(!underlinedBtn.isChecked());
                    toggleStyle(STYLE_UNDERLINED);
                }
            }
        });
    }

    public void setBulletButton(ToolbarImageButton button) {
        bulletedBtn = button;

        bulletedBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                int id = v.getId();
                if (id == R.id.toolbar_bulleted) {
                    bulletedBtn.setChecked(!bulletedBtn.isChecked());
                }
                toggleStyle(PARAGRAPH_BULLET);

                Editable editable = getEditableText();

                if(numberedBtn.isChecked()){
                    NumberSpan[] appliedNumbers = editable.getSpans(0, length(), NumberSpan.class);
                    removeNumber(editable);
                    numberedBtn.setChecked(false);
                }
                BulletSpan bulletSpan = new BulletSpan(margin, false, false, false);

                if (bulletedBtn.isChecked()) {
                if (getText().toString().trim().isEmpty()) {
                    //TODO test this
                    append("");
                } else {
                    int start = getSelectionStart();
                    int end = getSelectionEnd();

                    BulletSpan[] appliedStyles = editable.getSpans(start, end, BulletSpan.class);
                    if (appliedStyles.length == 0) {
                        int lineCount = getLineCount();
                        if (lineCount >= 1) {
                            for (int i = 0; i < lineCount; i++) {
                                int startPos = getLayout().getLineStart(i);
                                int endPos = getLayout().getLineEnd(i);

                                String theLine = getText().toString().substring(startPos, endPos);
/**
 Log.i(TAG, "Start line --- " + startPos + " end line is -- " + endPos + " line length is " + theLine.length());
 Log.i(TAG, "Cursor position  --- " + getSelectionEnd());
 Log.i(TAG, "Line  --- " + i);
 Log.i(TAG, "Amount of lines  --- " + getLineCount());
 Log.i(TAG, "Selection start  --- " + getSelectionStart());

 */
                                int lineTxt = getSelectionEnd() + 1 - theLine.length();
                                if (lineTxt == startPos) {
                                    Log.i(TAG, "---CURSOR IS AT THE END---ADDING BULLET");
                                    editable.setSpan(bulletSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    hasBullet=true;
                                }
                                if (getLineCount() - 1 == i) {
                                    lineTxt = getSelectionEnd() - theLine.length();
                                    if (lineTxt == startPos) {
                                        Log.i(TAG, "---CURSOR IS AT THE END of LAST LINE---ADDING BULLET");
                                        editable.setSpan(bulletSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        hasBullet=true;
                                    }
                                    if (getSelectionStart() == startPos + 1) {
                                        Log.i(TAG, "---CURSOR IS AT THE START of LAST LINE---ADDING BULLET");
                                        editable.setSpan(bulletSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        hasBullet=true;
                                    }
                                }
                                if (getSelectionStart() == startPos) {
                                    Log.i(TAG, "---CURSOR IS AT THE START---ADDING BULLET");
                                    editable.setSpan(bulletSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    hasBullet=true;
                                }
                            }
                        }
                    }
                }
                } else {
                    if (getText().toString().trim().isEmpty()) {
                        setText("");
                    }
                    removeBullet(getEditableText());
                }
            }

        });


    }
    public void setNumberButton(ToolbarImageButton button) {
        numberedBtn = button;

        numberedBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                int id = v.getId();
                if (id == R.id.toolbar_numbered) {
                    numberedBtn.setChecked(!numberedBtn.isChecked());

                }
                toggleStyle(PARAGRAPH_NUMBER);

                Editable editable = getEditableText();
                if(bulletedBtn.isChecked()){
                    BulletSpan[] appliedNumbers = editable.getSpans(0, length(), BulletSpan.class);
                    Log.i(TAG, "---BULLETED BUTTON IS CHECKED---"+ appliedNumbers.length);
                    removeBullet(editable);
                    bulletedBtn.setChecked(false);
                }

                if (numberedBtn.isChecked()) {

                        numberButtonIsChecked=true;

                        Log.i(TAG, "---NUMBER BUTTON IS CHECKED--- NUMBER IS"+ nr);
                        int start = getSelectionStart();
                        int end = getSelectionEnd();
                        NumberSpan[] appliedStyles = editable.getSpans(start, end, NumberSpan.class);
                        NumberSpan numberSpan;
                        nr=0;
                        if (appliedStyles.length == 0) {
                            int lineCount = getLineCount();
                            if (lineCount >= 1) {
                                for (int i = 0; i < lineCount; i++) {
                                    int startPos = getLayout().getLineStart(i);
                                    int endPos = getLayout().getLineEnd(i);
                                    String theLine = getText().toString().substring(startPos, endPos);
                                    nr++;
                                    if(i>0) {
                                        int previouStartPos = getLayout().getLineStart(i - 1);
                                        int previousEndPos = getLayout().getLineEnd(i - 1);

                                        String previousLine = getText().toString().substring(previouStartPos, previousEndPos);
                                        NumberSpan[] previousLineHasNumber = editable.getSpans(previouStartPos, previousEndPos, NumberSpan.class);
                                        if (previousLineHasNumber.length > 0) {
                                            Log.i(TAG, "---PREVIOUS LINE HAS NUMBER is " + previousLine);
                                            hasPreviousLineNumber=true;
                                        } else {
                                            Log.i(TAG, "---PREVIOUS LINE HAS NOT NUMBER is " + previousLine);
                                            hasPreviousLineNumber=false;
                                        }
                                    }
                                    if(resetNumber && !hasPreviousLineNumber){
                                        nr=0;
                                        nr++;
                                            numberSpan= new NumberSpan(nr, margin, true, true, true);
                                            number = nr;
                                        Log.i(TAG, "--NUMBER in setNumberButton--" + number);
                                    }else {
                                        if(removedNumber>0) {
                                            numberSpan= new NumberSpan(removedNumber, margin, true, true, true);
                                            removedNumber=0;
                                        }else{
                                            numberSpan= new NumberSpan(nr, margin, true, true, true);
                                            number=nr;
                                            Log.i(TAG, "--NUMBER in setNumberButton--" + number);
                                        }
                                    }
                                    int lineTxt = getSelectionEnd() + 1 - theLine.length();
                                    if (lineTxt == startPos) {
                                        Log.i(TAG, "---CURSOR IS AT THE END---ADDING NUMBER");
                                        Log.i(TAG, "---LINE---" +i + "NUMBER "+nr);
                                        editable.setSpan(numberSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        hasNumber = true;
                                    }
                                    if (getLineCount() - 1 == i) {
                                        lineTxt = getSelectionEnd() - theLine.length();
                                        if (lineTxt == startPos) {
                                            Log.i(TAG, "---CURSOR IS AT THE END of LAST LINE---ADDING NUMBER");
                                            Log.i(TAG, "---LINE---" +i + "NUMBER "+nr);

                                            editable.setSpan(numberSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            //TODO SEE THIS
                                            addedNumber1withSetNumberButton=true;
                                            hasNumber=true;
                                        }
                                        if (getSelectionStart() == startPos + 1) {
                                            Log.i(TAG, "---CURSOR IS AT THE START of LAST LINE---ADDING NUMBER");
                                            Log.i(TAG, "---LINE---" +i + "NUMBER "+nr);
                                            editable.setSpan(numberSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            hasNumber=true;

                                        }
                                    }
                                    if (getSelectionStart() == startPos) {
                                        Log.i(TAG, "---CURSOR IS AT THE START---ADDING NUMBER");
                                        Log.i(TAG, "---LINE---" +i + "NUMBER "+nr);
                                        editable.setSpan(numberSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        cursorAtStart=true;
                                        hasNumber=true;

                                    }
                                }
                            }
                        }
                }else{
                    numberButtonIsChecked=false;
                    //hasNumber=false;
                    removeNumber(getEditableText());
                }
            }

        });

    }
    public int setBGColorGroup(final RadioGroup radioGroup, final ToolbarImageButton fillColorButton){
        groupColors=radioGroup;
        groupColors.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                lastFillColorChecked = checkedId;
                if (checkedId == R.id.white_btn) {
                    Log.i(TAG, "---CHECKED WHITE---");
                    setBGColor(ContextCompat.getColor(getContext(), R.color.transparent_gray_50), getSelectionStart(), getSelectionEnd());
                    fillColorButton.setBackgroundResource(R.drawable.ic_fill_color_selected);
                } else if (checkedId == R.id.orange_btn) {
                    Log.i(TAG, "---CHECKED ORANGE---");
                    setBGColor(ContextCompat.getColor(getContext(), R.color.deep_orange_200), getSelectionStart(), getSelectionEnd());
                    fillColorButton.setBackgroundResource(R.drawable.ic_fill_color_selected_orange);
                } else if (checkedId == R.id.green_btn) {
                    Log.i(TAG, "---CHECKED GREEN---");
                    setBGColor(ContextCompat.getColor(getContext(), R.color.green_400), getSelectionStart(), getSelectionEnd());
                    fillColorButton.setBackgroundResource(R.drawable.ic_fill_color_selected_green);
                } else if (checkedId == R.id.purple_btn) {
                    Log.i(TAG, "---CHECKED PURPLE---");
                    setBGColor(ContextCompat.getColor(getContext(), R.color.purple_200), getSelectionStart(), getSelectionEnd());
                    fillColorButton.setBackgroundResource(R.drawable.ic_fill_color_selected_purple);
                }else if(checkedId ==  R.id.light_blue_btn)
                Log.i(TAG, "---CHECKED BLUE LIGHT---");
                setBGColor(ContextCompat.getColor(getContext(), R.color.teal_100), getSelectionStart(), getSelectionEnd());
                fillColorButton.setBackgroundResource(R.drawable.ic_fill_color_selected_blue);
                }
        });
        return lastFillColorChecked;
        }
    public void collapseToolbar(ToolbarParams params){
        final ImageButton collapseToolbar= params.getCollapseToolbar();
        final LinearLayout toolbarButtons= params.getToolbarButtons();
        final ToolbarImageButton fontColorButton= params.getFontColorBtn();
        final ToolbarImageButton fillColorButton= params.getFillColorBtn();
        final RadioGroup fillColorGroup= params.getFillGroup();
        final RadioGroup fontColorGroup= params.getFontGroup();
        final LinearLayout sizesLayout= params.getSizesLayout();
        final ToolbarImageButton sizeButton= params.getSizeBtn();

        collapseToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                        R.anim.slide_in_up);
                Animation bottomDown = AnimationUtils.loadAnimation(getContext(),
                        R.anim.slide_out_up);
                expanded = !expanded;
                collapseToolbar.setImageResource(expanded ? R.drawable.ic_arrow_up_blue_grey : R.drawable.ic_arrow_down_blue_grey);
                if (toolbarButtons != null) {
                    for (int i = 0; i < toolbarButtons.getChildCount(); i++) {
                        View child = toolbarButtons.getChildAt(i);
                        if (expanded) {
                            child.setAnimation(bottomDown);
                            child.setVisibility(View.INVISIBLE);
                            if(fontColorButton!= null) {
                                if(fontColorButton.isChecked()) {
                                    handleCheckedFontColors(fontColorButton);
                                    fontColorButton.setChecked(!fontColorButton.isChecked());
                                }
                            }
                            if (fillColorButton != null) {
                                if(fillColorButton.isChecked()) {
                                    handleCheckedFillColors(fillColorButton);
                                    fillColorButton.setChecked(false);
                                }
                            }
                            if(sizeButton != null)
                                sizeButton.setChecked(false);
                            fillColorGroup.setVisibility(View.GONE);
                            fontColorGroup.setVisibility(View.GONE);
                            sizesLayout.setVisibility(GONE);
                            collapseToolbar.setAnimation(bottomUp);
                        } else {
                            child.setAnimation(bottomUp);
                            child.setVisibility(View.VISIBLE);
                        }
                        if (i == 8) {
                            child.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }
    public void handleClickInGroupButtons(ToolbarParams params){
        final ToolbarImageButton fillColorBtn= params.getFillColorBtn();
        final ToolbarImageButton fontColorBtn= params.getFontColorBtn();
        final ToolbarImageButton sizeButton= params.getSizeBtn();
        final RadioGroup fillColorsGroup= params.getFillGroup();
        final RadioGroup fontColorsGroup=params.getFontGroup();
        final LinearLayout sizesLayout=params.getSizesLayout();
        if (fillColorBtn != null) {
            fillColorBtn.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    handleCheckedFillColors(fillColorBtn);
                    int id = v.getId();
                    if (id == R.id.toolbar_fill_color) {
                        fillColorBtn.setChecked(!fillColorBtn.isChecked());
                        if (fillColorsGroup != null) {
                            fillColorsGroup.setVisibility(fillColorBtn.isChecked() ? View.VISIBLE : View.GONE);
                        }
                        }
                        if (fontColorsGroup != null || sizesLayout != null) {
                            fontColorsGroup.setVisibility(GONE);
                            sizesLayout.setVisibility(View.GONE);
                            if(fontColorBtn!= null) {
                                if(fontColorBtn.isChecked()) {
                                    handleCheckedFontColors(fontColorBtn);
                                    fontColorBtn.setChecked(!fontColorBtn.isChecked());
                                }
                            }
                            if (sizeButton != null)
                                sizeButton.setChecked(false);
                            }
                        }
            });
        }
        if (fontColorBtn != null) {
            fontColorBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleCheckedFontColors(fontColorBtn);
                    int id = v.getId();
                    if (id == R.id.toolbar_font_color) {
                        fontColorBtn.setChecked(!fontColorBtn.isChecked());
                    }
                    if (fontColorsGroup != null) {
                        fontColorsGroup.setVisibility(fontColorBtn.isChecked() ? View.VISIBLE : View.GONE);
                    }
                    if (fillColorsGroup != null || sizesLayout != null) {
                        fillColorsGroup.setVisibility(View.GONE);
                        sizesLayout.setVisibility(View.GONE);
                        if (fillColorBtn != null) {
                            if(fillColorBtn.isChecked()) {
                                handleCheckedFillColors(fillColorBtn);
                                fillColorBtn.setChecked(false);
                            }
                        }
                        if (sizeButton != null)
                            sizeButton.setChecked(false);
                        }
                }
            });
        }
        if (sizeButton != null) {
            sizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    if (id == R.id.toolbar_size) {
                        sizeButton.setChecked(!sizeButton.isChecked());
                    }
                    if (sizesLayout != null) {
                        sizesLayout.setVisibility(sizeButton.isChecked() ? View.VISIBLE : View.GONE);
                    }
                    if (fillColorsGroup != null || fontColorsGroup != null) {
                        fillColorsGroup.setVisibility(View.GONE);
                        fontColorsGroup.setVisibility(View.GONE);
                        if (fillColorBtn != null) {
                            if(fillColorBtn.isChecked()) {
                                handleCheckedFillColors(fillColorBtn);
                                fillColorBtn.setChecked(false);
                            }
                        }
                        if (fontColorBtn != null) {
                            if(fontColorBtn.isChecked()) {
                                handleCheckedFontColors(fontColorBtn);
                                fontColorBtn.setChecked(false);
                            }
                        }
                    }
                }
            });
        }
    }
    private void handleCheckedFillColors(final ToolbarImageButton fillColorButton){
        if(lastFillColorChecked == R.id.white_btn){
            fillColorButton.setBackgroundResource(fillColorButton.isChecked() ?
                    R.drawable.ic_format_color_fill_gray50 : R.drawable.ic_fill_color_selected);
        }else if(lastFillColorChecked == R.id.orange_btn){
            fillColorButton.setBackgroundResource(fillColorButton.isChecked() ?
                    R.drawable.ic_format_color_fill_orange : R.drawable.ic_fill_color_selected_orange);
        }else if(lastFillColorChecked == R.id.green_btn){
            fillColorButton.setBackgroundResource(fillColorButton.isChecked() ?
                    R.drawable.ic_format_color_fill_green : R.drawable.ic_fill_color_selected_green);
        }else if(lastFillColorChecked == R.id.light_blue_btn) {
            fillColorButton.setBackgroundResource(fillColorButton.isChecked() ?
                    R.drawable.ic_format_color_fill_bluelight : R.drawable.ic_fill_color_selected_blue);
        }else if(lastFillColorChecked== R.id.purple_btn){
            fillColorButton.setBackgroundResource(fillColorButton.isChecked() ?
                    R.drawable.ic_format_color_fill_purple : R.drawable.ic_fill_color_selected_purple);
        }
    }
    public int setFontColorGroup(final RadioGroup radioGroup, final ToolbarImageButton fontColorButton){
        groupColors=radioGroup;
        groupColors.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                lastFontColorChecked=checkedId;
                if(checkedId == R.id.black_btn){
                    Log.i(TAG, "---CHECKED BLACK---");
                    setFontColor(Color.BLACK, getSelectionStart(), getSelectionEnd());
                    fontColorButton.setBackgroundResource(R.drawable.ic_font_color_selected_black);
                }else if(checkedId == R.id.red_btn){
                    Log.i(TAG, "---CHECKED RED---");
                    setFontColor(ContextCompat.getColor(getContext(), R.color.red_500), getSelectionStart(), getSelectionEnd());
                    fontColorButton.setBackgroundResource(R.drawable.ic_font_color_selected_red);
                    lastFontColorChecked=checkedId;
                }else if(checkedId == R.id.green_font_btn){
                    Log.i(TAG, "---CHECKED GREEN---");
                    setFontColor(ContextCompat.getColor(getContext(), R.color.green_500), getSelectionStart(), getSelectionEnd());
                    fontColorButton.setBackgroundResource(R.drawable.ic_font_color_selected_green);
                    lastFontColorChecked=checkedId;
                }else if(checkedId == R.id.blue_btn){
                    Log.i(TAG, "---CHECKED BLUE---");
                    setFontColor(ContextCompat.getColor(getContext(), R.color.blue_500), getSelectionStart(), getSelectionEnd());
                    fontColorButton.setBackgroundResource(R.drawable.ic_font_color_selected_blue);
                    lastFontColorChecked=checkedId;
                }else if(checkedId == R.id.grey_btn){
                    Log.i(TAG, "---CHECKED GREY---");
                    setFontColor(ContextCompat.getColor(getContext(), R.color.gray_500), getSelectionStart(), getSelectionEnd());
                    fontColorButton.setBackgroundResource(R.drawable.ic_font_color_selected_grey);
                    lastFontColorChecked=checkedId;
                }
            }
        });
        return lastFontColorChecked;
    }
    private void handleCheckedFontColors(final ToolbarImageButton fontColorButton){
         if(lastFontColorChecked == R.id.black_btn){
             fontColorButton.setBackgroundResource(fontColorButton.isChecked() ?
                     R.drawable.ic_format_color_text_bl_black_24dp :
                     R.drawable.ic_font_color_selected_black);
         }else if(lastFontColorChecked == R.id.red_btn){
             fontColorButton.setBackgroundResource(fontColorButton.isChecked() ?
                     R.drawable.ic_format_color_text_red_24dp :
                     R.drawable.ic_font_color_selected_red);
         }else if(lastFontColorChecked == R.id.green_font_btn){
             fontColorButton.setBackgroundResource(fontColorButton.isChecked() ?
                     R.drawable.ic_format_color_text_green_24dp :
                     R.drawable.ic_font_color_selected_green);
         }else if(lastFontColorChecked == R.id.blue_btn) {
             fontColorButton.setBackgroundResource(fontColorButton.isChecked() ?
                     R.drawable.ic_format_color_text_blue_24dp :
                     R.drawable.ic_font_color_selected_blue);
         }else if(lastFontColorChecked== R.id.grey_btn){
                    fontColorButton.setBackgroundResource(fontColorButton.isChecked() ?
                            R.drawable.ic_format_color_text:
                            R.drawable.ic_font_color_selected_grey);
            }

    }
    private void setFontColor(int color, int selectionStart, int selectionEnd) {
        currentColor = color;
        // Reverse if the case is what's noted above
        if (selectionStart > selectionEnd) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }
        // The selectionEnd is only greater then the selectionStart position
        // when the user selected a section of the text. Otherwise, the 2
        // variables
        // should be equal (the cursor position).
        if (selectionEnd > selectionStart) {
            Spannable spannable = this.getText();
            android.text.style.ForegroundColorSpan[] appliedStyles = spannable.getSpans(selectionStart, selectionEnd,
                    android.text.style.ForegroundColorSpan.class);
            if (appliedStyles != null && appliedStyles.length > 0) {
                int colorStart = -1;
                int colorEnd = -1;
                int beforeColor = 0;
                int afterColor = 0;
                for (android.text.style.ForegroundColorSpan foregroundColorSpan : appliedStyles) {
                    if (spannable.getSpanStart(foregroundColorSpan) < selectionStart) {
                        colorStart = spannable.getSpanStart(foregroundColorSpan);
                        beforeColor = foregroundColorSpan.getForegroundColor();
                    }
                    if (spannable.getSpanEnd(foregroundColorSpan) > selectionEnd) {
                        colorEnd = spannable.getSpanEnd(foregroundColorSpan);
                        afterColor = foregroundColorSpan.getForegroundColor();
                    }
                    spannable.removeSpan(foregroundColorSpan);
                }
                if (colorStart > -1) {
                    spannable.setSpan(new android.text.style.ForegroundColorSpan(beforeColor), colorStart,
                            selectionStart,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (colorEnd > -1) {
                    spannable.setSpan(new android.text.style.ForegroundColorSpan(afterColor), selectionEnd, colorEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                spannable.setSpan(new android.text.style.ForegroundColorSpan(color), selectionStart, selectionEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannable.setSpan(new android.text.style.ForegroundColorSpan(color), selectionStart, selectionEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            this.setSelection(selectionStart, selectionEnd);
        }
    }
    private void setBGColor(int color, int selectionStart, int selectionEnd) {
        currentBGColor = color;
        // Reverse if the case is what's noted above
        if (selectionStart > selectionEnd) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }
        // The selectionEnd is only greater then the selectionStart position
        // when the user selected a section of the text. Otherwise, the 2
        // variables
        // should be equal (the cursor position).
        if (selectionEnd > selectionStart) {
            Spannable spannable = this.getText();
            BackgroundColorSpan[] appliedStyles = spannable.getSpans(selectionStart, selectionEnd, BackgroundColorSpan.class);
            if (appliedStyles != null && appliedStyles.length > 0) {
                int colorStart = -1;
                int colorEnd = -1;
                int beforeColor = 0;
                int afterColor = 0;
                for (BackgroundColorSpan bgColorSpan : appliedStyles) {
                    if (spannable.getSpanStart(bgColorSpan) < selectionStart) {
                        colorStart = spannable.getSpanStart(bgColorSpan);
                        beforeColor = bgColorSpan.getBackgroundColor();
                    }
                    if (spannable.getSpanEnd(bgColorSpan) > selectionEnd) {
                        colorEnd = spannable.getSpanEnd(bgColorSpan);
                        afterColor = bgColorSpan.getBackgroundColor();
                    }
                    spannable.removeSpan(bgColorSpan);
                }
                    if (colorStart > -1) {
                        spannable.setSpan(new BackgroundColorSpan(beforeColor), colorStart,
                                selectionStart,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    if (colorEnd > -1) {
                        spannable.setSpan(new BackgroundColorSpan(afterColor), selectionEnd, colorEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    spannable.setSpan(new BackgroundColorSpan(color), selectionStart, selectionEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    spannable.setSpan(new BackgroundColorSpan(color), selectionStart, selectionEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            this.setSelection(selectionStart, selectionEnd);
    }
    private void removeBullet(Editable editable) {
        int lineCount = getLineCount();
        if (lineCount >= 1) {
            for (int i = 0; i < lineCount; i++) {
                int startPos = getLayout().getLineStart(i);
                int endPos = getLayout().getLineEnd(i);

                BulletSpan[] appliedStyles = editable.getSpans(startPos, endPos, BulletSpan.class);
                if (appliedStyles.length > 0) {

                    Log.i(TAG, "---THERE ARE BULLETS---");
                    BulletSpan colorSpan = appliedStyles[0];
                    String theLine = getText().toString().substring(startPos, endPos);
                    int lineTxt = getSelectionEnd() + 1 - theLine.length();
                    if (lineTxt == startPos) {
                        Log.i(TAG, "---CURSOR IS AT THE END---REMOVING BULLET");
                        editable.removeSpan(colorSpan);
                        hasBullet=false;
                        bulletedBtn.setChecked(false);
                    }
                    if (getLineCount() - 1 == i) {
                        lineTxt = getSelectionEnd() - theLine.length();
                        if (lineTxt == startPos) {
                            Log.i(TAG, "---CURSOR IS AT THE END of LAST LINE---REMOVING BULLET");
                            editable.removeSpan(colorSpan);
                            hasBullet=false;
                            bulletedBtn.setChecked(false);

                        }
                        if (getSelectionStart() == startPos + 1) {
                            Log.i(TAG, "---CURSOR IS AT THE START of LAST LINE---REMOVING BULLET");
                            editable.removeSpan(colorSpan);
                            hasBullet=false;
                            bulletedBtn.setChecked(false);
                        }
                    }
                    if (getSelectionStart() == startPos) {
                        Log.i(TAG, "---CURSOR IS AT THE START---REMOVING BULLET");
                        editable.removeSpan(colorSpan);
                        hasBullet=false;
                        bulletedBtn.setChecked(false);
                    }

                }
            }
        }
    }
        private void removeNumber (Editable editable) {
            int lineCount = getLineCount();
            if (lineCount >= 1) {
                for (int i = 0; i < lineCount; i++) {
                    int startPos = getLayout().getLineStart(i);
                    int endPos = getLayout().getLineEnd(i);
                    NumberSpan[] appliedNumber = editable.getSpans(startPos, endPos, NumberSpan.class);
                    if (appliedNumber.length > 0) {
                        Log.i(TAG, "---THERE ARE NUMBERS---");
                        NumberSpan colorSpan = appliedNumber[0];
                        String theLine = getText().toString().substring(startPos, endPos);
                        int lineTxt = getSelectionEnd() + 1 - theLine.length();
                        if (lineTxt == startPos) {
                            Log.i(TAG, "---CURSOR IS AT THE END---REMOVING NUMBER");
                            editable.removeSpan(colorSpan);
                            numberedBtn.setChecked(false);
                            hasNumber = false;

                        }
                        if (getLineCount() - 1 == i) {
                            lineTxt = getSelectionEnd() - theLine.length();
                            if (lineTxt == startPos) {
                                Log.i(TAG, "---CURSOR IS AT THE END of LAST LINE---REMOVING NUMBER");
                                editable.removeSpan(colorSpan);
                                numberedBtn.setChecked(false);
                                hasNumber = false;
                                removedNumber = nr;
                                Log.i(TAG, "---HAS NUMBER is  " + hasNumber);
                                Log.i(TAG, "---NUMBER REMOVED IS " + removedNumber);
                            }
                            if (getSelectionStart() == startPos + 1) {
                                Log.i(TAG, "---CURSOR IS AT THE START of LAST LINE---REMOVING NUMBER");
                                editable.removeSpan(colorSpan);
                                numberedBtn.setChecked(false);
                                hasNumber = false;
                            }
                        }
                        if (getSelectionStart() == startPos) {
                            Log.i(TAG, "---CURSOR IS AT THE START---REMOVING NUMBER");
                            editable.removeSpan(colorSpan);
                            numberedBtn.setChecked(false);
                            hasNumber = false;
                        }

                    }
                }
            }
        }
    public List<Paragraph> getParagraphs() {
        RTLayout layout = getRTLayout();
        return layout.getParagraphs();
    }

    private RTLayout getRTLayout() {
        synchronized (this) {
            if (mRTLayout == null || mLayoutChanged) {
                mRTLayout = new RTLayout(getText());
                mLayoutChanged = false;
            }
        }
        return mRTLayout;
    }
    public Selection getParagraphsInSelection() {
        RTLayout layout = getRTLayout();

        Selection selection = new Selection(this);
        int firstLine = layout.getLineForOffset(selection.start());
        int end = selection.isEmpty() ? selection.end() : selection.end() - 1;
        int lastLine = layout.getLineForOffset(end);

        return new Selection(layout.getLineStart(firstLine), layout.getLineEnd(lastLine));
    }

    private class DWTextWatcher implements TextWatcher {
        // private BulletSpan bulletSpan;
        private int beforeChangeTextLength = 0;
        private int appendTextLength = 0;
        /**
         * After text Change
         */
        @Override
        public void afterTextChanged(Editable editable) {
            // Add style as the user types if a toggle button is enabled
            int position = getSelectionStart();

            appendTextLength = Math.abs(position - beforeChangeTextLength);

            //XXX: Fixed bold error when text size not change
            if (appendTextLength == 0 || isDeleteCharaters) {
                return;
            }
            if (position < 0) {
                position = 0;
            }
            if (position > 0) {

                CharacterStyle[] appliedStyles = editable.getSpans(position - 1, position, CharacterStyle.class);
                ParagraphStyle[] appliedParagraphStyle = editable.getSpans(position - 1, position, ParagraphStyle.class);

                BoldSpan currentBoldSpan = null;
                ItalicSpan currentItalicSpan = null;
                UnderlineSpan currentAgsUnderlineSpan = null;
                ForegroundColorSpan currentForegroundColorSpan = null;
                BackgroundColorSpan currentBackgroundColorSpan = null;
                AbsoluteSizeSpan currentAbsoluteSizeSpan = null;
                BulletSpan currentBulletSpan = null;
                NumberSpan currentNumberSpan = null;

                for (ParagraphStyle anAppliedParagraphStyle : appliedParagraphStyle) {
                    if (anAppliedParagraphStyle instanceof BulletSpan) {
                        if (currentBulletSpan == null) {
                            currentBulletSpan = (BulletSpan) anAppliedParagraphStyle;
                            Log.i(TAG, "--Current bullet NULL");
                        }
                    }else if(anAppliedParagraphStyle instanceof NumberSpan){
                        if (currentNumberSpan == null) {
                            currentNumberSpan = (NumberSpan) anAppliedParagraphStyle;
                            //Log.i(TAG, "--Current bullet NULL");
                        }
                    }
                }
                // Look for possible styles already applied to the entered text
                for (CharacterStyle appliedStyle : appliedStyles) {
                    if (appliedStyle instanceof BoldSpan) {
                        // Bold style found
                        Log.i(TAG, "---FOUNDED BOLD in afterTextChanged---");
                        currentBoldSpan = (BoldSpan) appliedStyle;

                    } else if ((appliedStyle) instanceof ItalicSpan) {
                        // Italic style found
                        Log.i(TAG, "---FOUNDED ITALIC in afterTextChanged---");
                        currentItalicSpan = (ItalicSpan) appliedStyle;

                    } else if (appliedStyle instanceof UnderlineSpan) {
                        // Underlined style found
                        currentAgsUnderlineSpan = (UnderlineSpan) appliedStyle;

                    } else if (appliedStyle instanceof ForegroundColorSpan) {
                        if (currentForegroundColorSpan == null) {
                            currentForegroundColorSpan = (ForegroundColorSpan) appliedStyle;
                        }
                    } else if (appliedStyle instanceof BackgroundColorSpan) {
                        if (currentBackgroundColorSpan == null) {
                            currentBackgroundColorSpan = (BackgroundColorSpan) appliedStyle;
                        }
                    } else if (appliedStyle instanceof AbsoluteSizeSpan) {
                        if (currentAbsoluteSizeSpan == null) {
                            currentAbsoluteSizeSpan = (AbsoluteSizeSpan) appliedStyle;
                        }
                    }
                }
                handleInsertBoldCharacter(editable, position, currentBoldSpan);
                handleInsertItalicCharacter(editable, position, currentItalicSpan);
                handleInsertUnderlineCharacter(editable, position, currentAgsUnderlineSpan);
                handleInsertFontColor(editable, position, currentForegroundColorSpan);
                handleInsertBGColor(editable, position, currentBackgroundColorSpan);
                handleInsertSize(editable, position, currentAbsoluteSizeSpan);
                handleInsertBullet(editable, position, currentBulletSpan);
                handleInsertNumber(editable,position, currentNumberSpan);
            }
        }
        private void handleInsertBullet(Editable editable, int position, BulletSpan currentBulletSpan) {
            int linecount = getLineCount();
            if (bulletedBtn != null && bulletedBtn.isChecked()
                    && currentBulletSpan == null) {
                if (linecount >= 0) {
                    for (int i = 0; i < linecount; i++) {
                        int startPos = getLayout().getLineStart(i);
                        int endPos = getLayout().getLineEnd(i);
                        Log.i(TAG, " Position start " + startPos + " Position end " + endPos);
                        BulletSpan[] bulletExists = editable.getSpans(startPos, endPos, BulletSpan.class);
                        if (bulletExists.length == 0 && hasBullet) {
                            currentBulletSpan = new BulletSpan(margin, true, true, true);
                            editable.setSpan(currentBulletSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            hasBullet=false;
                            Log.i(TAG, "---ADDING BULLET in AFTERTEXTCHANGED---");
                        } else {
                            editable.removeSpan(currentBulletSpan);
                            Log.i(TAG, "---REMOVING BULLET in AFTERTEXTCHANGED---");
                            hasBullet=true;

                        }
                    }
                }
            }
        }
        private void handleInsertNumber(Editable editable, int position, NumberSpan currentNumberSpan) {
            boolean previousNumber = true;
            int lineCount = getLineCount();
            if (numberedBtn != null && numberedBtn.isChecked()
                    && currentNumberSpan == null) {
                int nr = 0;
                if (lineCount >= 0) {
                    for (int i = 0; i < lineCount; i++) {
                        int startPos = getLayout().getLineStart(i);
                        int endPos = getLayout().getLineEnd(i);

                        if (i > 0) {
                            int previousStartPos = getLayout().getLineStart(i - 1);
                            int previousEndPos = getLayout().getLineEnd(i - 1);
                            NumberSpan[] numberExists = editable.getSpans(previousStartPos, previousEndPos, NumberSpan.class);
                            if (numberExists.length == 0) {
                                previousNumber=false;
                            }else{
                                previousNumber=true;
                            }
                        }
                        NumberSpan[] numberExists = editable.getSpans(startPos, endPos, NumberSpan.class);
                        if (i == 0) {
                            hasNumber = true;
                        }
                        if (!previousNumber) {
                            nr = 1;
                        }else{
                            nr++;
                        }
                        if(!hasNumber){
                            nr=1;
                        }
                        Log.i(TAG, "LINE-- " + i + " HAS NUMBER-- " + hasNumber + " NUMBER is " + nr);
                        if (numberExists.length == 0 && hasNumber) {
                            currentNumberSpan = new NumberSpan(nr, margin, true, true, true);
                            editable.setSpan(currentNumberSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            hasNumber = false;
                            Log.i(TAG, "--ADDING NUMBER--" + nr);
                        } else {
                            editable.removeSpan(currentNumberSpan);
                            hasNumber = true;
                            Log.i(TAG, "---REMOVING NUMBER--");
                        }
                    }
                }
            }else{
                resetNumber=true;
            }
            }
        private void handleInsertSize(Editable editable, int position, AbsoluteSizeSpan currentAbsoluteSizeSpan) {

            //Log.i(TAG, "---POSITION--- "+position);
            //Log.i(TAG, "---LENGTH--- "+length());
            /**
            int textSize = Math.round(getTextSize());
            i = Helper.convertPxToSp(textSize);
            currentSize = i;
            for (int p : values) {
                if (seekBarSizes.getProgress() >= p) {
                    do {
                        editable.setSpan(new AbsoluteSizeSpan(currentSize = currentSize + 2), position - appendTextLength, position,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }while (currentSize==i+i);
                }
            }
             */
             //Fix for exception in setSpan() starts before 0---
                      if(position==1 && length()==1) {
                Log.i(TAG, "FIX");
                appendTextLength=1;
            }
            Log.i(TAG, "CURRENT SIZE " +currentSize);
           // Log.i(TAG, "currentAbsoluteSizeSpan " +currentAbsoluteSizeSpan.getSize());

            if (currentAbsoluteSizeSpan != null) {
                if (currentAbsoluteSizeSpan.getSize() != currentSize) {

                    int sizeStart = editable.getSpanStart(currentAbsoluteSizeSpan);
                    int sizeEnd = editable.getSpanEnd(currentAbsoluteSizeSpan);

                    Log.i(TAG, "SIZE START " +sizeStart);
                    Log.i(TAG, "SIZE END " +sizeEnd);
                    if (position == sizeEnd) {
                        AbsoluteSizeSpan nextSpan = getNextAbsoluteSizeSpan(editable, position);

                        if (nextSpan != null) {
                            Log.i(TAG, "NEXT SPAN " +nextSpan.getSize());

                            if (currentSize == nextSpan.getSize()) {
                                int colorEndNextSpan = editable.getSpanEnd(nextSpan);
                                editable.removeSpan(currentAbsoluteSizeSpan);
                                editable.removeSpan(nextSpan);

                                Log.i(TAG, "currentAbsoluteSizeSpan " +currentAbsoluteSizeSpan.getSize());
                                // set before span
                                editable.setSpan(new AbsoluteSizeSpan(currentAbsoluteSizeSpan.getSize()),sizeStart,
                                        sizeEnd - appendTextLength,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                // set after span
                                editable.setSpan(new AbsoluteSizeSpan(nextSpan.getSize()),
                                        position - appendTextLength,
                                        colorEndNextSpan,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                return;
                            }
                        }
                    }
                    editable.removeSpan(currentAbsoluteSizeSpan);
                    if (position - appendTextLength < sizeEnd && sizeStart != sizeEnd) {
                        // Cursor in the text's middle with different color
                        int oldSize = currentAbsoluteSizeSpan.getSize();

                        if (sizeStart < position - appendTextLength) {
                            // Before inserting text
                            editable.setSpan(new AbsoluteSizeSpan(oldSize), sizeStart,
                                    position - appendTextLength,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                        // At inserting
                        editable.setSpan(new AbsoluteSizeSpan(currentSize), position - appendTextLength,
                                position,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                        if (position < sizeEnd) {
                            // After inserting
                            editable.setSpan(new AbsoluteSizeSpan(oldSize), position, sizeEnd,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                    } else {
                        // Cursor in the end
                        editable.setSpan(new AbsoluteSizeSpan(currentSize), position - appendTextLength,
                                sizeEnd,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                }
            }
            else if (currentSize != -1) {
                AbsoluteSizeSpan nextSpan = getNextAbsoluteSizeSpan(editable, position);
                if (nextSpan != null) {
                    int sizeEndNextSpan = editable.getSpanEnd(nextSpan);
                    Log.i(TAG, "NEXT SPAN " +nextSpan.getSize());
                    if (currentSize == nextSpan.getSize()) {
                        editable.removeSpan(nextSpan);
                        // set before span
                        editable.setSpan(new AbsoluteSizeSpan(nextSpan.getSize()),
                                position - appendTextLength,
                                sizeEndNextSpan,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                        return;
                    }
                }
                editable.setSpan(new AbsoluteSizeSpan(currentSize),
                        position - appendTextLength, position,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            /**
             i = 18;
             for (int p : values) {
             if (fontSizeSeekBar.getProgress() >= p) {
             do {
             editable.setSpan(new AbsoluteSizeSpan(i = i + 2, true), position - appendTextLength, position,
             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             }while (i==42);
             }
             }
             */
        }
        private AbsoluteSizeSpan getNextAbsoluteSizeSpan(Editable editable, int position) {
            AbsoluteSizeSpan nextSpans[] = editable.getSpans(position, position + 1, AbsoluteSizeSpan.class);
            if (nextSpans.length > 0) {
                return nextSpans[0];
            }
            return null;
        }
        private void handleInsertFontColor(Editable editable, int position, ForegroundColorSpan currentForegroundColorSpan) {
            // Handle color
            if (currentForegroundColorSpan != null) {
                if (currentForegroundColorSpan.getForegroundColor() != currentColor) {
                    int colorStart = editable.getSpanStart(currentForegroundColorSpan);
                    int colorEnd = editable.getSpanEnd(currentForegroundColorSpan);

                    if (position == colorEnd) {
                        ForegroundColorSpan nextSpan = getNextForegroundColorSpan(editable, position);
                        if (nextSpan != null) {
                            if (currentColor == nextSpan.getForegroundColor()) {
                                int colorEndNextSpan = editable.getSpanEnd(nextSpan);
                                editable.removeSpan(currentForegroundColorSpan);
                                editable.removeSpan(nextSpan);
                                // set before span
                                editable.setSpan(new ForegroundColorSpan(currentForegroundColorSpan.getForegroundColor()), colorStart,
                                        colorEnd - appendTextLength,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                // set after span
                                editable.setSpan(new ForegroundColorSpan(nextSpan.getForegroundColor()),
                                        position - appendTextLength,
                                        colorEndNextSpan,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                return;
                            }
                        }

                    }
                    editable.removeSpan(currentForegroundColorSpan);
                    if (position - appendTextLength < colorEnd && colorStart != colorEnd) {
                        // Cursor in the text's middle with different color
                        int oldColor = currentForegroundColorSpan.getForegroundColor();

                        if (colorStart < position - appendTextLength) {
                            // Before inserting text
                            editable.setSpan(new ForegroundColorSpan(oldColor), colorStart,
                                    position - appendTextLength,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }

                        // At inserting
                        editable.setSpan(new ForegroundColorSpan(currentColor), position - appendTextLength,
                                position,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                        if (position < colorEnd) {
                            // After inserting
                            editable.setSpan(new ForegroundColorSpan(oldColor), position, colorEnd,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                    } else
                    {
                        // Cursor in the end
                        editable.setSpan(new ForegroundColorSpan(currentColor), position - appendTextLength,
                                colorEnd,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                }
            }
            else if (currentColor != -1) {
                ForegroundColorSpan nextSpan = getNextForegroundColorSpan(editable, position);
                if (nextSpan != null)
                {
                    int colorEndNextSpan = editable.getSpanEnd(nextSpan);
                    if (currentColor == nextSpan.getForegroundColor())
                    {
                        editable.removeSpan(nextSpan);
                        // set before span
                        editable.setSpan(new ForegroundColorSpan(nextSpan.getForegroundColor()),
                                position - appendTextLength,
                                colorEndNextSpan,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                        return;
                    }
                }
                editable.setSpan(new ForegroundColorSpan(currentColor),
                        position - appendTextLength, position,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }

        private ForegroundColorSpan getNextForegroundColorSpan(Editable editable, int position) {
            ForegroundColorSpan nextSpans[] = editable.getSpans(position, position + 1, ForegroundColorSpan.class);
            if (nextSpans.length > 0) {
                return nextSpans[0];
            }
            return null;
        }
        private void handleInsertBGColor(Editable editable, int position, BackgroundColorSpan currentBackGroundColorSpan) {
            // Handle color
            if (currentBackGroundColorSpan != null) {
                if (currentBackGroundColorSpan.getBackgroundColor() != currentBGColor) {
                    int colorStart = editable.getSpanStart(currentBackGroundColorSpan);
                    int colorEnd = editable.getSpanEnd(currentBackGroundColorSpan);

                    if (position == colorEnd) {
                        BackgroundColorSpan nextSpan = getNextBackgroundColorSpan(editable, position);
                        if (nextSpan != null) {
                            if (currentBGColor == nextSpan.getBackgroundColor()) {

                                Log.i(TAG, "NEXT SPAN IS NOT NULL");

                                int colorEndNextSpan = editable.getSpanEnd(nextSpan);
                                editable.removeSpan(currentBackGroundColorSpan);
                                editable.removeSpan(nextSpan);
                                // set before span
                                editable.setSpan(new BackgroundColorSpan(currentBackGroundColorSpan.getBackgroundColor()), colorStart,
                                        colorEnd - appendTextLength,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                // set after span
                                editable.setSpan(new BackgroundColorSpan(nextSpan.getBackgroundColor()),
                                        position - appendTextLength,
                                        colorEndNextSpan,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                return;
                            }
                        }

                    }
                    Log.i(TAG, "REMOVING");
                    editable.removeSpan(currentBackGroundColorSpan);
                    if (position - appendTextLength < colorEnd && colorStart != colorEnd) {
                        // Cursor in the text's middle with different color
                        int oldColor = currentBackGroundColorSpan.getBackgroundColor();

                        if (colorStart < position - appendTextLength) {
                            // Before inserting text
                            editable.setSpan(new BackgroundColorSpan(oldColor), colorStart,
                                    position - appendTextLength,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                            Log.i(TAG, "INSERTING");
                        }

                        // At inserting
                        editable.setSpan(new BackgroundColorSpan(currentBGColor), position - appendTextLength,
                                position,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        Log.i(TAG, "INSERTING AT START");

                        if (position < colorEnd) {
                            // After inserting
                            editable.setSpan(new BackgroundColorSpan(oldColor), position, colorEnd,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                    } else
                    {
                        // Cursor in the end
                        editable.setSpan(new BackgroundColorSpan(currentBGColor), position - appendTextLength,
                                colorEnd,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        Log.i(TAG, "INSERTING AT THE END");
                    }
                }
            }
            else if (currentBGColor != -1) {
                BackgroundColorSpan nextSpan = getNextBackgroundColorSpan(editable, position);
                if (nextSpan != null)
                {
                    int colorEndNextSpan = editable.getSpanEnd(nextSpan);
                    if (currentBGColor == nextSpan.getBackgroundColor())
                    {
                        editable.removeSpan(nextSpan);
                        // set before span
                        editable.setSpan(new BackgroundColorSpan(nextSpan.getBackgroundColor()),
                                position - appendTextLength,
                                colorEndNextSpan,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        Log.i(TAG, "--INSERTING-- current color != -1");

                        return;
                    }
                }
                editable.setSpan(new BackgroundColorSpan(currentBGColor),
                        position - appendTextLength, position,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                Log.i(TAG, "--INSERTING-- current color == -1");
            }
        }

        private BackgroundColorSpan getNextBackgroundColorSpan(Editable editable, int position) {
            BackgroundColorSpan nextSpans[] = editable.getSpans(position, position + 1, BackgroundColorSpan.class);
            if (nextSpans.length > 0) {
                return nextSpans[0];
            }
            return null;
        }

        private void handleInsertUnderlineCharacter(Editable editable, int position,
                                                    UnderlineSpan currentAgsUnderlineSpan) {
            // Handle the underlined style toggle button if it's present
            if (underlinedBtn != null && underlinedBtn.isChecked()
                    && currentAgsUnderlineSpan == null) {
                editable.setSpan(new UnderlineSpan(), position - appendTextLength, position,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else if (underlinedBtn != null && !underlinedBtn.isChecked()
                    && currentAgsUnderlineSpan != null) {
                int underLineStart = editable.getSpanStart(currentAgsUnderlineSpan);
                int underLineEnd = editable.getSpanEnd(currentAgsUnderlineSpan);

                editable.removeSpan(currentAgsUnderlineSpan);
                if (underLineStart <= (position - appendTextLength)) {
                    editable.setSpan(new UnderlineSpan(), underLineStart, position - appendTextLength,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // We need to split the span
                if (underLineEnd > position) {
                    editable.setSpan(new UnderlineSpan(), position, underLineEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        private void handleInsertItalicCharacter(Editable editable, int position,
                                                 ItalicSpan currentItalicSpan) {
            // Handle the italics style toggle button if it's present
            if (italicBtn != null && italicBtn.isChecked() && currentItalicSpan == null) {
                editable.setSpan(new ItalicSpan(), position - appendTextLength,
                        position,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                Log.i(TAG, "---ADDING ITALIc in atc");
            } else if (italicBtn != null && !italicBtn.isChecked()
                    && currentItalicSpan != null) {
                int italicStart = editable.getSpanStart(currentItalicSpan);
                int italicEnd = editable.getSpanEnd(currentItalicSpan);

                Log.i(TAG, "---REMOVING ITALIc in atc");
                editable.removeSpan(currentItalicSpan);
                if (italicStart <= (position - appendTextLength)) {
                    editable.setSpan(new ItalicSpan(),
                            italicStart, position - appendTextLength,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // Split the span
                if (italicEnd > position) {
                    Log.i(TAG, "---ADDING ITALIc in atc");
                    editable.setSpan(new ItalicSpan(), position,
                            italicEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        private void handleInsertBoldCharacter(Editable editable, int position,
                                               BoldSpan currentBoldSpan) {
            // Handle the bold style toggle button if it's present
            if (boldBtn != null) {
                if (boldBtn.isChecked() && currentBoldSpan == null) {
                    // The user switched the bold style button on and the
                    // character doesn't have any bold
                    // style applied, so we start a new bold style span. The
                    // span is inclusive,
                    // so any new characters entered right after this one
                    // will automatically get this style.
                    Log.i(TAG, "---ADDING BOLD in atc");
                    editable.setSpan(new BoldSpan(),
                            position - appendTextLength, position,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                } else if (!boldBtn.isChecked() && currentBoldSpan != null) {
                    // The user switched the bold style button off and the
                    // character has bold style applied.
                    // We need to remove the old bold style span, and define
                    // a new one that end 1 position right
                    // before the newly entered character.
                    int boldStart = editable.getSpanStart(currentBoldSpan);
                    int boldEnd = editable.getSpanEnd(currentBoldSpan);
                    Log.i(TAG, "---REMOVING BOLD in atc");
                    editable.removeSpan(currentBoldSpan);
                    if (boldStart <= (position - appendTextLength)) {
                        editable.setSpan(new BoldSpan(),
                                boldStart, position - appendTextLength,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    // The old bold style span end after the current cursor
                    // position, so we need to define a
                    // second newly created style span too, which begins
                    // after the newly entered character and
                    // ends at the old span's ending position. So we split
                    // the span.
                    if (boldEnd > position) {
                        Log.i(TAG, "---ADDING BOLD in atcc ");
                        editable.setSpan(new BoldSpan(),
                                position, boldEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        /**
         * Before Text Change
         */
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            int position = getSelectionStart();
            if (position < 0) {
                position = 0;
            }
            beforeChangeTextLength = position;

            if ((count - after == 1) || (s.length() == 0) && position > 0) { // Delete character
                Editable editable = CustomEditText.this.getText();
                removeSizeSpan(position, editable);
                removeForegroundColorSpan(position, editable);
                removeAgsUnderlineSpan(position, editable);
                removeStyleSpan(position, editable, new ItalicSpan());
                removeStyleSpan(position, editable, new BoldSpan());
                //removeBulletSpan(position, editable);
            }

        }
        private void removeBulletSpan(int position, Editable editable) {

            BulletSpan[] appliedStyles = editable.getSpans(position - 1, position, BulletSpan.class);

            if (appliedStyles.length > 0 ) {
                BulletSpan colorSpan = appliedStyles[0];
                int underLineStart = editable.getSpanStart(colorSpan);
                int underLineEnd = editable.getSpanEnd(colorSpan);

                editable.removeSpan(colorSpan);
                if (underLineStart < (position - 1)) {
                    editable.setSpan(new BulletSpan(margin, false, false, false), underLineStart, position - 1,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                }
                // We need to split the span
                if (underLineEnd > position) {
                    editable.setSpan(new BulletSpan(margin, false, false, false), position, underLineEnd,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                }
            }
        }
        private void removeForegroundColorSpan(int position, Editable editable) {
            ForegroundColorSpan previousColorSpan = (ForegroundColorSpan) getPreviousForegroundColorSpan(editable, position, ForegroundColorSpan.class);
            ForegroundColorSpan[] appliedStyles = editable.getSpans(position - 1, position, ForegroundColorSpan.class);

            if (appliedStyles.length > 0 && appliedStyles[0] != null && previousColorSpan != null
                    && previousColorSpan.getForegroundColor() !=  appliedStyles[0].getForegroundColor()) {
                ForegroundColorSpan colorSpan = (ForegroundColorSpan) appliedStyles[0];
                int colorStart = editable.getSpanStart(colorSpan);
                int colorEnd = editable.getSpanEnd(colorSpan);

                editable.removeSpan(colorSpan);
                if (colorStart < (position - 1)) {
                    editable.setSpan(new AgsUnderlineSpan(), colorStart, position - 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // We need to split the span
                if (colorEnd > position) {
                    editable.setSpan(new AgsUnderlineSpan(), position, colorEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        private void removeSizeSpan(int position, Editable editable) {

            /**
             AbsoluteSizeSpan[] appliedSizes = editable.getSpans(position - 1, position, AbsoluteSizeSpan.class);
             if (appliedSizes.length > 0) {
             AbsoluteSizeSpan sizeSpan = appliedSizes[0];
             editable.removeSpan(sizeSpan);
             }
             */

            AbsoluteSizeSpan[] appliedStyles = editable.getSpans(position - 1, position, AbsoluteSizeSpan.class);
            AbsoluteSizeSpan currentAbsoluteSizeSpan = null;
            for (AbsoluteSizeSpan appliedStyle : appliedStyles) {
                if (appliedStyle != null) {
                    if (currentAbsoluteSizeSpan == null) {
                        currentAbsoluteSizeSpan = appliedStyle;
                    }
                }
            }

            if (appliedStyles.length > 0 ) {
                AbsoluteSizeSpan colorSpan = appliedStyles[0];
                int underLineStart = editable.getSpanStart(colorSpan);
                int underLineEnd = editable.getSpanEnd(colorSpan);

                editable.removeSpan(colorSpan);
                if(currentAbsoluteSizeSpan!=null) {
                    if (underLineStart < (position - 1)) {
                        editable.setSpan(new AbsoluteSizeSpan(currentAbsoluteSizeSpan.getSize()), underLineStart, position - 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    }
                    // We need to split the span
                    if (underLineEnd > position) {
                        editable.setSpan(new AbsoluteSizeSpan(currentAbsoluteSizeSpan.getSize()), position, underLineEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        private void removeAgsUnderlineSpan(int position, Editable editable) {
            AgsUnderlineSpan previousColorSpan = (AgsUnderlineSpan) getPreviousForegroundColorSpan(editable, position, AgsUnderlineSpan.class);
            AgsUnderlineSpan[] appliedStyles = editable.getSpans(position - 1, position, AgsUnderlineSpan.class);

            if (appliedStyles.length > 0 && previousColorSpan == null) {
                AgsUnderlineSpan colorSpan = (AgsUnderlineSpan) appliedStyles[0];
                int underLineStart = editable.getSpanStart(colorSpan);
                int underLineEnd = editable.getSpanEnd(colorSpan);

                editable.removeSpan(colorSpan);
                if (underLineStart < (position - 1)) {
                    editable.setSpan(new AgsUnderlineSpan(), underLineStart, position - 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                // We need to split the span
                if (underLineEnd > position) {
                    editable.setSpan(new AgsUnderlineSpan(), position, underLineEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        private void removeStyleSpan(int position, Editable editable, Object object) {
            StyleSpan previousColorSpan = (StyleSpan) getPreviousForegroundColorSpan(editable, position, StyleSpan.class);
            StyleSpan[] appliedStyles = editable.getSpans(position - 1, position, StyleSpan.class);

            StyleSpan styleSpan = null;
            for (StyleSpan span : appliedStyles) {
                if (span.equals(object)) {
                    styleSpan = span;
                }
            }
            if (styleSpan != null && previousColorSpan == null) {
                int styleStart = editable.getSpanStart(styleSpan);
                int styleEnd = editable.getSpanEnd(styleSpan);
                Log.i(TAG, "---REMOVING STYLESPAN in btc ");
                editable.removeSpan(styleSpan);
                if (styleStart < (position - 1)) {
                    Log.i(TAG, "---Adding STYLESPAN in btc ");
                    editable.setSpan(object, styleStart, position - 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                // We need to split the span
                if (styleEnd > position) {
                    Log.i(TAG, "---Adding STYLESPAN in btc ");
                    editable.setSpan(object, position, styleEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        private Object getPreviousForegroundColorSpan(Editable editable, int position, Class<?> clss) {
            if (position - 2 >= 0) {
                Object[] nextSpans = editable.getSpans(position - 2, position - 1, clss);
                if (nextSpans.length > 0) {
                    return nextSpans[0];
                }
            }
            return null;
        }
        /**
         * On Text Change
         */
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isDeleteCharaters = count == 0 ? true : false;
            // Remove all span when EditText is empty
            if (CustomEditText.this.getText().toString().isEmpty()) {
                CharacterStyle[] appliedStyles = CustomEditText.this.getText().getSpans(0,
                        CustomEditText.this.getText().length(), CharacterStyle.class);
                for (CharacterStyle characterStyle : appliedStyles) {
                    CustomEditText.this.getText().removeSpan(characterStyle);
                }
            }
        }
    }
}


