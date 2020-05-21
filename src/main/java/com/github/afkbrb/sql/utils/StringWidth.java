package com.github.afkbrb.sql.utils;

public final class StringWidth {

    /**
     * 计算一个字符串的宽度。
     * <p>
     * from: https://github.com/sindresorhus/string-width/blob/master/index.js
     */
    public static int stringWidth(String str) {
        int width = 0;
        for (char ch : str.toCharArray()) {
            // Ignore control characters
            if (ch <= 0x1F || (ch >= 0x7F && ch <= 0x9F)) continue;

            // Ignore combining characters
            if (ch >= 0x300 && ch <= 0x36F) continue;

            width += isFullWidth(ch) ? 2 : 1;
        }
        return width;
    }

    /**
     * 判断一个字符是否是全角字符。
     * <p>
     * from: https://github.com/sindresorhus/is-fullwidth-code-point/blob/master/index.js
     */
    @SuppressWarnings("all")
    private static boolean isFullWidth(char ch) {
        return ch >= 0x1100 && (
                ch <= 0x115F || // Hangul Jamo
                        ch == 0x2329 || // LEFT-POINTING ANGLE BRACKET
                        ch == 0x232A || // RIGHT-POINTING ANGLE BRACKET
                        // CJK Radicals Supplement .. Enclosed CJK Letters and Months
                        (0x2E80 <= ch && ch <= 0x3247 && ch != 0x303F) ||
                        // Enclosed CJK Letters and Months .. CJK Unified Ideographs Extension A
                        (0x3250 <= ch && ch <= 0x4DBF) ||
                        // CJK Unified Ideographs .. Yi Radicals
                        (0x4E00 <= ch && ch <= 0xA4C6) ||
                        // Hangul Jamo Extended-A
                        (0xA960 <= ch && ch <= 0xA97C) ||
                        // Hangul Syllables
                        (0xAC00 <= ch && ch <= 0xD7A3) ||
                        // CJK Compatibility Ideographs
                        (0xF900 <= ch && ch <= 0xFAFF) ||
                        // Vertical Forms
                        (0xFE10 <= ch && ch <= 0xFE19) ||
                        // CJK Compatibility Forms .. Small Form Variants
                        (0xFE30 <= ch && ch <= 0xFE6B) ||
                        // Halfwidth and Fullwidth Forms
                        (0xFF01 <= ch && ch <= 0xFF60) ||
                        (0xFFE0 <= ch && ch <= 0xFFE6) ||
                        // Kana Supplement
                        (0x1B000 <= ch && ch <= 0x1B001) ||
                        // Enclosed Ideographic Supplement
                        (0x1F200 <= ch && ch <= 0x1F251) ||
                        // CJK Unified Ideographs Extension B .. Tertiary Ideographic Plane
                        (0x20000 <= ch && ch <= 0x3FFFD)
        );
    }

    private StringWidth() {}
}
