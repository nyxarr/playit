package com.viby.playit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.*;

import com.squareup.picasso.Transformation;

public class BlurTransformation implements Transformation {
    private Context mContext;

    public BlurTransformation(Context context) {
        mContext = context;
    }

    @Override
    public String key() {
        return "102";
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final RenderScript renderScript = RenderScript.create(mContext);
        final Allocation input = Allocation.createFromBitmap( renderScript, source, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT );
        final Allocation output = Allocation.createTyped( renderScript, input.getType() );
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create( renderScript, Element.U8_4( renderScript ) );
        script.setRadius( 3.f);
        script.setInput( input );
        script.forEach( output );
        output.copyTo( source );
        output.destroy();

        return null;
    }
}
