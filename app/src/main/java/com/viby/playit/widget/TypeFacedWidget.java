package com.viby.playit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.viby.playit.R;

public class TypeFacedWidget extends TextView {
    public TypeFacedWidget(Context context) {
        super(context);
    }
    public TypeFacedWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(
                attrs,
                R.styleable.TypefacedTextView
        );

        String fontName = styledAttrs.getString(R.styleable.TypefacedTextView_typeface);

        styledAttrs.recycle();

        if (fontName != null) {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
            setTypeface(typeface);
        }
    }

    public TypeFacedWidget(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
}
