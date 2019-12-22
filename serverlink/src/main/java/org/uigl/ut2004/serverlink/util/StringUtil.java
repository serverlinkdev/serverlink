package org.uigl.ut2004.serverlink.util;

public class StringUtil {

    public static String join(CharSequence separator, CharSequence ... items)
    {
        StringBuilder builder = new StringBuilder();

        if (items != null && items.length > 0)
        {
            builder.append(items[0]);
            for (int i = 1; i<items.length; i++) {
                builder.append(separator).append(items[i]);
            }
        }

        return builder.toString();
    }
}
