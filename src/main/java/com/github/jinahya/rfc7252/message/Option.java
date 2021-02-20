package com.github.jinahya.rfc7252.message;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * A class for binding options.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 * @see <a href="https://tools.ietf.org/html/rfc7252#section-3.1">3.1. Option Format (RFC 7252)</a>
 */
public class Option implements Serializable, Comparable<Option> {

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MAX_VALUE_LENGTH = 65535 + 269;

    static final int C_13 = 13;

    static final int C_13_268 = C_13 + 255; // 268

    static final int C_269 = 256 + C_13;

    static final int C_65805 = 65536 + C_269;

    static final int C_12 = 12;

    static final int C_268 = 256 + C_12;

    static final int C_65804 = 65536 + C_268;

    public static final int NUMBER_IF_MATCH = 0;

    public static final int NUMBER_URI_HOST = 3;

    public static final int NUMBER_ETAG = 4;

    public static final int NUMBER_IF_NON_MATCH = 5;

    public static final int NUMBER_URI_PORT = 7;

    /**
     * Predefined option number for {@code Location-Path}.
     */
    public static final int NUMBER_LOCATION_PATH = 8;

    /**
     * The option number for {@code Uri-Path}.
     */
    public static final int NUMBER_URI_PATH = 11;

    /**
     * Predefined option number for {@code Content-Format}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_CONTENT_FORMAT = 12;

    /**
     * Predefined option number for {@code Max-Age}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_MAX_AGE = 14;

    /**
     * Predefined option number for {@code Uri-Query}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_URI_QUERY = 15;

    /**
     * Predefined option number for {@code Accept}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_ACCEPT = 17;

    /**
     * Predefined option number for {@code Location-Query}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_LOCATION_QUERY = 20;

    /**
     * Predefined option number for {@code Proxy-Uri}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_PROXY_URI = 35;

    /**
     * Predefined option number for {@code Proxy-Scheme}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_PROXY_SCHEME = 35;

    /**
     * Predefined option number for {@code Size1}. The value is {@value}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-12.2">12.2. CoAP Option Numbers Registry (RFC
     * 7252)</a>
     */
    public static final int NUMBER_SIZE1 = 60;

    // -----------------------------------------------------------------------------------------------------------------
    static Option of(final int number, final byte[] value) {
        final Option instance = new Option();
        instance.setNumber(number);
        instance.setValue(value);
        return instance;
    }

    static Option from(final Option option) {
        if (option == null) {
            throw new NullPointerException("option is null");
        }
        return of(option.getNumber(), option.getValue());
    }

    // -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance.
     */
    public Option() {
        super();
    }

    // -------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return super.toString() + '{'
               + "number=" + number
               + ",value=" + value
               + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Option that = (Option) obj;
        if (number != that.number) return false;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + Utils.hashCode(value);
        return result;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int compareTo(final Option o) {
        if (o == null) {
            throw new NullPointerException("o is null");
        }
        if (number < o.number) {
            return -1;
        }
        if (number == o.number) {
            return 0;
        }
        return 1;
    }

    // -----------------------------------------------------------------------------------------------------------------
    void read(final DataInput input) throws IOException {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        int delta;
        int length;
        {
            final int b1 = input.readUnsignedByte();
            length = b1 & 0xF;
            delta = b1 >> 4;
        }
        if (delta < 13) {
            // use delta as read
        } else if (delta == 13) {
            delta = input.readUnsignedByte() + 13;
        } else if (delta == 14) {
            delta = input.readUnsignedShort() + 269;
        } else { // delta == 15
            throw new RuntimeException("message format error");
        }
        setNumber((previous == null ? 0 : previous.number) + delta);
        if (length < 13) {
            // use length as read
        } else if (length == 13) {
            length = input.readUnsignedByte() + 13;
        } else if (length == 14) {
            length = input.readUnsignedShort() + 269;
        } else { // length == 15
            throw new RuntimeException("message format error");
        }
        setValue(new byte[length]);
        input.readFully(getValue());
    }

    void write(final DataOutput output) throws IOException {
        if (output == null) {
            throw new NullPointerException("output is null");
        }
        if (previous != null && previous.number > number) {
            throw new IllegalStateException("previous.number(" + previous.number + ") > number(" + number + ")");
        }
        int delta = number - (previous == null ? 0 : previous.number);
        Integer deltaExtended;
        if (delta < 13) {
            deltaExtended = null;
        } else if (delta < 269) {
            deltaExtended = delta - 13;
            delta = 13;
        } else if (delta < 65805) {
            deltaExtended = delta - 269;
            delta = 14;
        } else {
            throw new RuntimeException("message format error");
        }
        int length = value.length;
        Integer lengthExtended;
        if (length < 13) {
            lengthExtended = null;
        } else if (length < 269) {
            lengthExtended = length - 13;
            length = 13;
        } else if (length < 65805) {
            lengthExtended = length - 269;
            length = 14;
        } else {
            throw new RuntimeException("message format error");
        }
        output.writeByte((delta << 4) | length);
        if (deltaExtended != null) {
            if (delta == 13) {
                output.writeByte(deltaExtended);
            } else {
                output.writeShort(deltaExtended);
            }
        }
        if (lengthExtended != null) {
            if (length == 13) {
                output.writeByte(lengthExtended);
            } else {
                output.writeShort(lengthExtended);
            }
        }
        output.write(value);
    }

    // ---------------------------------------------------------------------------------------------------------- number
    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        if (number < 0) {
            throw new IllegalArgumentException("number(" + number + ") < 0");
        }
        this.number = number;
    }

    // ----------------------------------------------------------------------------------------------------------- value
    public byte[] getValue() {
        return value;
    }

    public void setValue(final byte[] value) {
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        if (value.length > MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException("value.length(" + value.length + ") > " + MAX_VALUE_LENGTH);
        }
        this.value = value;
    }

    public BigInteger getValueAsUint() {
        return new BigInteger(1, getValue());
    }

    public void setValueAsUint(final BigInteger valueAsUint) {
        if (valueAsUint == null) {
            throw new NullPointerException("valueAsUint is null");
        }
        if (valueAsUint.signum() == -1) {
            throw new IllegalArgumentException("valueAsUint.signum == -1");
        }
        if (valueAsUint.signum() == 0) {
            setValue(new byte[0]);
            return;
        }
        final byte[] bytes = valueAsUint.toByteArray();
        // https://stackoverflow.com/a/4408124/330457
        if (bytes[0] == 0x00) {
            final byte[] b = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, b, 0, b.length);
            setValue(b);
            return;
        }
        setValue(bytes);
    }

    public String getValueAsString() {
        try {
            return new String(getValue(), "UTF-8");
        } catch (final UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    public void setValueAsString(final String valueAsString) {
        if (valueAsString == null) {
            throw new NullPointerException("valueAsString is null");
        }
        try {
            setValue(valueAsString.getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    transient Option previous;

    // -----------------------------------------------------------------------------------------------------------------
    @PositiveOrZero
    private int number;

    @NotNull
    private byte[] value = new byte[0];
}
