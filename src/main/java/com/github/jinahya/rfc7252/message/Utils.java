package com.github.jinahya.rfc7252.message;

final class Utils {

    static int hashCode(final byte[] value) {
        if (value == null) {
            return 0;
        }
        int result = 0;
        for (byte b : value) {
            result = 31 * result + (int) b;
        }
        return result;
    }

    private Utils() {
        super();
        //throw new AssertionError("instantiation is not allowed");
    }
}
