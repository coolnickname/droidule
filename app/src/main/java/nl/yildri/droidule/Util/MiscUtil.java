package nl.yildri.droidule.Util;

import android.content.res.Resources;
import android.util.TypedValue;

public class MiscUtil
{
    public static float getPx(float x, Resources resources)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, resources.getDisplayMetrics());
    }
}
