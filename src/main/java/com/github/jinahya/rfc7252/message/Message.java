package com.github.jinahya.rfc7252.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.json.bind.annotation.JsonbTransient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.sort;

/**
 * .
 *
 * @see <a href="https://tools.ietf.org/html/rfc7252#section-3">3. Message Format (RFC 7252)</a>
 */
public class Message {

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The minimum value for {@code version} property. The value is {@value}.
     */
    public static final int MIN_VERSION = 0;

    /**
     * The maximum value for {@code version} property. The value is {@value}.
     */
    public static final int MAX_VERSION = 3;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The minimum value for {@code type} property. The value is {@value}.
     */
    public static final int MIN_TYPE = 0;

    /**
     * The maximum value for {@code type} property. The value is {@value}.
     */
    public static final int MAX_TYPE = 3;

    public static final int TYPE_CONFIRMABLE = 0;

    public static final int TYPE_NON_CONFIRMABLE = 1;

    public static final int TYPE_ACKNOWLEDGEMENT = 2;

    public static final int TYPE_RESET = 3;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The minimum value for {@code code} property. The value is {@value}.
     */
    public static final int MIN_CODE = 0;

    /**
     * The maximum value for {@code code} property. The value is {@value}.
     */
    public static final int MAX_CODE = 255;

    // -----------------------------------------------------------------------------------------------------------------
    private static final int SIZE_CODE_CLASS = 3;

    public static final int MIN_CODE_CLASS = 0;

    public static final int MAX_CODE_CLASS = 7;

    public static final int CODE_CLASS_REQUEST = 0;

    public static final int CODE_CLASS_SUCCESS_RESPONSE = 1;

    public static final int CODE_CLASS_CLIENT_ERROR_RESPONSE = 4;

    public static final int CODE_CLASS_SERVER_ERROR_RESPONSE = 5;

    // -----------------------------------------------------------------------------------------------------------------
    private static final int SIZE_CODE_DETAIL = 5;

    private static final int MIN_CODE_DETAIL = 0;

    private static final int MAX_CODE_DETAIL = 31;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MIN_MESSAGE_ID = 0;

    public static final int MAX_MESSAGE_ID = 65535;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MIN_TOKEN_LENGTH = 0;

    public static final int MAX_TOKEN_LENGTH = 8;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int PAYLOAD_MARKER = 0xFF;

    // -----------------------------------------------------------------------------------------------------------------
    public static Message newEmptyInstance() {
        final Message instance = new Message();
        instance.setCode(0);
        return instance;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * .
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-3.1">3.1. Option Format (RFC 7252)</a>
     */
    public static class Option implements Comparable<Option> {

        // -------------------------------------------------------------------------------------------------------------
        static final int MAX_DELTA = 65535 + 269;

        // -------------------------------------------------------------------------------------------------------------
        public static final int MAX_VALUE_LENGTH = 65535 + 269;

        public static final byte[] VALUE_EMPTY = new byte[0];

        // -------------------------------------------------------------------------------------------------------------
        @Override
        public int compareTo(final Option o) {
            if (getNumber() < o.getNumber()) {
                return -1;
            }
            if (getNumber() == o.getNumber()) {
                return 0;
            }
            return 1;
        }

        @Override
        public String toString() {
            return super.toString() + "{"
                   + "number=" + number
                   + ",value=" + Arrays.toString(value)
                   + "}";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof Option)) return false;
            final Option option = (Option) o;
            if (number != null ? !number.equals(option.number) : option.number != null) return false;
            return Arrays.equals(value, option.value);
        }

        @Override
        public int hashCode() {
            int result = number != null ? number.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(value);
            return result;
        }

        // -------------------------------------------------------------------------------------------------------------

        /**
         * Reads contents of this object, except the first byte, from specified data input.
         *
         * @param b     the first byte.
         * @param input the data input from which this object's contents are read.
         * @throws IOException if an I/O error occurs.
         */
        void read(final int b, final DataInput input) throws IOException {
            if ((b | 0xFF) != 0xFF) {
                throw new IllegalArgumentException("illegal b: " + b);
            }
            int length;
            {
                delta = b >> 4;
                length = b & 0xF;
            }
            if (delta == 13) {
                delta = input.readUnsignedByte() + 13;
            } else if (delta == 14) {
                delta = input.readUnsignedShort() + 269;
            } else if (delta == 15) {
                throw new RuntimeException("illegal delta: " + delta);
            }
            if (length == 13) {
                length = input.readUnsignedByte() + 13;
            } else if (length == 14) {
                length = input.readUnsignedShort() + 269;
            } else if (length == 15) {
                throw new RuntimeException("illegal length: " + length);
            }
            value = new byte[length];
            input.readFully(value);
            setValue(value);
        }

        /**
         * Reads this object's contents from specified data input.
         *
         * @param input the data input from which contents are read.
         * @throws IOException if an I/O error occurs.
         */
        public void read(final DataInput input) throws IOException {
            if (input == null) {
                throw new NullPointerException("input is null");
            }
            read(input.readUnsignedByte(), input);
            number = null;
        }

        /**
         * Writes this object's contents to specified data output.
         *
         * @param output the data output to which this object's contents is written.
         * @throws IOException if an I/O error occurs.
         */
        public void write(final DataOutput output) throws IOException {
            if (delta == null) {
                throw new IllegalStateException("(internal) delta is currently null");
            }
            int delta_ = delta;
            Integer deltaExtended_;
            if (delta_ <= 13) {
                deltaExtended_ = null;
            } else if (delta_ <= (255 + 13)) {
                deltaExtended_ = delta_ - 13;
                delta_ = 13;
            } else {
                deltaExtended_ = delta_ - 269;
                delta_ = 14;
            }
            int length_ = value.length;
            Integer lengthExtended_;
            if (length_ <= 13) {
                lengthExtended_ = null;
            } else if (length_ <= (255 + 13)) {
                lengthExtended_ = length_ - 13;
                length_ = 13;
            } else {//if (length_ <= MAX_VALUE_LENGTH) {
                lengthExtended_ = length_ - 269;
                length_ = 14;
            }
            output.writeByte((delta_ << 4) | length_);
            if (deltaExtended_ != null) {
                if (delta_ == 13) {
                    output.write(deltaExtended_);
                } else {
                    output.writeShort(deltaExtended_);
                }
            }
            if (lengthExtended_ != null) {
                if (length_ == 13) {
                    output.writeByte(lengthExtended_);
                } else {
                    output.writeShort(lengthExtended_);
                }
            }
            output.write(value);
        }

        // ---------------------------------------------------------------------------------------------- previousNumber
        void setPreviousNumber(final int previousNumber) {
            if (previousNumber < 0) {
                throw new IllegalArgumentException("previousNumber(" + previousNumber + ") < 0");
            }
            if (delta != null) { // after read
                setNumber(previousNumber + delta);
            } else { // before write
                setDelta(number - previousNumber);
            }
        }

        // ---------------------------------------------------------------------------------------------------- previous
        void setPrevious(final Option previous) {
            if (previous == null) {
                throw new NullPointerException("previous is null");
            }
            setPreviousNumber(previous.getNumber());
        }

        // ------------------------------------------------------------------------------------------------------- delta
        void setDelta(final int delta) {
            if (delta < 0) {
                throw new IllegalArgumentException("delta(" + delta + ") < 0");
            }
            if (delta > MAX_DELTA) {
                throw new IllegalArgumentException("delta(" + delta + ") > " + MAX_DELTA);
            }
            this.delta = delta;
        }

        // ------------------------------------------------------------------------------------------------------ number
        public int getNumber() {
            if (number == null) {
                throw new IllegalStateException("number is currently null");
            }
            return number;
        }

        public void setNumber(final int number) {
            if (number < 0) {
                throw new IllegalArgumentException("number(" + number + ") < 0");
            }
            this.number = number;
            delta = null;
        }

        // ------------------------------------------------------------------------------------------------------- value

        /**
         * Returns the current value of {@code value} property.
         *
         * @return the current value of {@code value} property.
         * @see <a href="https://tools.ietf.org/html/rfc7252#section-3.2">3.2 Option Value Formats (RFC 7252)</a>
         */
        public byte[] getValue() {
            return value;
        }

        /**
         * Replaces the current value of {@code value} property with specified value.
         *
         * @param value a new value for {@code value} property; must not {@code null}.
         * @see #MAX_VALUE_LENGTH
         * @see #VALUE_EMPTY
         * @see <a href="https://tools.ietf.org/html/rfc7252#section-3.2">3.2 Option Value Formats (RFC 7252)</a>
         */
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
            if (value.length == 0) {
                return BigInteger.ZERO;
            }
            final BigInteger valueAsUint = new BigInteger(getValue());
            if (valueAsUint.signum() == -1) {
                throw new IllegalStateException("valueAsUint.signum == -1");
            }
            return valueAsUint;
        }

        public void setValueAsUint(final BigInteger valueAsUint) {
            if (valueAsUint == null) {
                throw new NullPointerException("valueAsUint is null");
            }
            if (valueAsUint.signum() == -1) {
                throw new IllegalArgumentException("valueAsUint.signum == -1");
            }
            if (valueAsUint.equals(BigInteger.ZERO)) {
                setValue(VALUE_EMPTY);
                return;
            }
            setValue(valueAsUint.toByteArray());
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

        // -------------------------------------------------------------------------------------------------------------
        @JsonIgnore
        @JsonbTransient
        @PositiveOrZero
        @Setter(AccessLevel.NONE)
        @Getter(AccessLevel.NONE)
        private transient Integer delta;

        @PositiveOrZero
        @NotNull
        private Integer number;

        @NotNull
        private byte[] value = VALUE_EMPTY;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return super.toString() + "{"
               + "version=" + version
               + ",type=" + type
               + ",code=" + code
               + ",messageId=" + messageId
               + ",token=" + Arrays.toString(token)
               + ",options=" + options +
               ",payload=" + Arrays.toString(payload)
               + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        final Message message = (Message) o;
        if (version != message.version) return false;
        if (type != message.type) return false;
        if (code != message.code) return false;
        if (messageId != message.messageId) return false;
        if (!Arrays.equals(token, message.token)) return false;
        if (options != null ? !options.equals(message.options) : message.options != null) return false;
        return Arrays.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + type;
        result = 31 * result + code;
        result = 31 * result + messageId;
        result = 31 * result + Arrays.hashCode(token);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public void read(final DataInput input) throws IOException {
        final int length;
        {
            final int b = input.readUnsignedByte();
            setVersion((b >> 6));
            setType((b >> 4) & 0x3);
            length = b & 0xF;
        }
        setCode(input.readUnsignedByte());
        setMessageId(input.readUnsignedShort());
        final byte[] token = new byte[length];
        input.readFully(token);
        setToken(token);
        for (int b; ; ) {
            try {
                b = input.readUnsignedByte();
            } catch (final EOFException eofe) {
                break;
            }
            if (b == PAYLOAD_MARKER) { // payload marker
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true) {
                    try {
                        baos.write(input.readByte());
                    } catch (final EOFException eofe) {
                        break;
                    }
                }
                setPayload(baos.toByteArray());
                return;
            } else {
                final Option option = new Option();
                option.read(b, input);
                if (options == null) {
                    options = new ArrayList<Option>();
                }
                options.add(option);
            }
        }
        if (options != null && options.size() > 0) {
            options.get(0).setPreviousNumber(0);
            for (int i = 1; i < options.size(); i++) {
                options.get(i).setPrevious(options.get(i - 1));
            }
        }
    }

    public void read(final DatagramPacket packet) throws IOException {
        if (packet == null) {
            throw new NullPointerException("package is null");
        }
        final DataInputStream input = new DataInputStream(new ByteArrayInputStream(
                packet.getData(), packet.getOffset(), packet.getLength()));
        try {
            read(input);
        } finally {
            input.close();
        }
    }

    public void write(final DataOutput output) throws IOException {
        output.write((version << 6) | (type << 4) | (token == null ? 0 : token.length));
        output.write(code);
        output.writeShort(messageId);
        if (token != null && token.length > 0) {
            output.write(token);
        }
        if (options != null && options.size() > 0) {
            sort(options);
            options.get(0).setPreviousNumber(0);
            for (int i = 1; i < options.size(); i++) {
                options.get(i).setPrevious(options.get(i - 1));
            }
            for (final Option option : options) {
                option.write(output);
            }
        }
        if (payload != null && payload.length > 0) {
            output.write(payload);
        }
    }

    // --------------------------------------------------------------------------------------------------------- version
    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        if (version < MIN_VERSION) {
            throw new IllegalArgumentException("version(" + version + ") < " + MIN_VERSION);
        }
        if (version > MAX_VERSION) {
            throw new IllegalArgumentException("version(" + version + ") > " + MAX_VERSION);
        }
        this.version = version;
    }

    // ------------------------------------------------------------------------------------------------------------ type

    /**
     * Returns the current value of {@code type} property.
     *
     * @return the current value of {@code type} property.
     */
    public int getType() {
        return type;
    }

    /**
     * Replaces the current value of {@code type} property with specified value.
     *
     * @param type new value for {@code type} property.
     * @see #MIN_TYPE
     * @see #MAX_TYPE
     */
    public void setType(final int type) {
        if (type < MIN_TYPE) {
            throw new IllegalArgumentException("type(" + type + ") < " + MIN_TYPE);
        }
        if (type > MAX_TYPE) {
            throw new IllegalArgumentException("type(" + type + ") > " + MAX_TYPE);
        }
        this.type = type;
    }

    // ------------------------------------------------------------------------------------------------------------ code
    public int getCode() {
        return code;
    }

    public void setCode(final int code) {
        if (code < MIN_CODE) {
            throw new IllegalArgumentException("code(" + code + ") < " + MIN_CODE);
        }
        if (code > MAX_CODE) {
            throw new IllegalArgumentException("code(" + code + ") > " + MAX_CODE);
        }
        this.code = code;
    }

    @JsonIgnore
    @JsonbTransient
    public int getCodeClass() {
        return getCode() & MAX_CODE_CLASS;
    }

    public void setCodeClass(final int codeClass) {
        if (codeClass < MIN_CODE_CLASS) {
            throw new IllegalArgumentException("codeClass(" + codeClass + ") < " + MIN_CODE_CLASS);
        }
        if (codeClass > MAX_CODE_CLASS) {
            throw new IllegalArgumentException("codeClass(" + codeClass + ") > " + MAX_CODE_CLASS);
        }
        setCode(((codeClass & MAX_CODE_CLASS) << SIZE_CODE_CLASS) | getCodeDetail());
    }

    @JsonIgnore
    @JsonbTransient
    public int getCodeDetail() {
        return getCode() & MAX_CODE_DETAIL;
    }

    public void setCodeDetail(final int codeDetail) {
        if (codeDetail < MIN_CODE_DETAIL) {
            throw new IllegalArgumentException("codeDetail(" + codeDetail + ") < " + MIN_CODE_DETAIL);
        }
        if (codeDetail > MAX_CODE_DETAIL) {
            throw new IllegalArgumentException("codeDetail(" + codeDetail + ") > " + MAX_CODE_DETAIL);
        }
        setCode(getCodeClass() | (codeDetail & MAX_CODE_DETAIL));
    }

    public boolean isCodeForEmpty() {
        return getCode() == 0; // 0.00
    }

    public boolean isCodeForRequest() {
        return !isCodeForEmpty() && getCodeClass() >= 0 && getCodeClass() < 2; // 0.01-0.31
    }

    public boolean isCodeForResponse() {
        return getCodeClass() >= 2 && getCodeClass() < 6; // 2.00-5.31
    }

    // ------------------------------------------------------------------------------------------------------- messageId
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(final int messageId) {
        if (messageId < MIN_MESSAGE_ID) {
            throw new IllegalArgumentException("messageId(" + messageId + ") < " + MIN_MESSAGE_ID);
        }
        if (messageId > MAX_MESSAGE_ID) {
            throw new IllegalArgumentException("messageId(" + messageId + ") > " + MAX_MESSAGE_ID);
        }
        this.messageId = messageId;
    }

    // ------------------------------------------------------------------------------------------------------ tokenValue
    public byte[] getToken() {
        return token;
    }

    public void setToken(final byte[] token) {
        if (token != null) {
            if (token.length < MIN_TOKEN_LENGTH) {
                throw new IllegalArgumentException("token.length(" + token.length + ") < " + MIN_TOKEN_LENGTH);
            }
            if (token.length > MAX_TOKEN_LENGTH) {
                throw new IllegalArgumentException("token.length(" + token.length + ") > " + MAX_TOKEN_LENGTH);
            }
        }
        this.token = token;
    }

    // --------------------------------------------------------------------------------------------------------- options
    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(final List<Option> options) {
        this.options = options;
    }

    // --------------------------------------------------------------------------------------------------------- payload
    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Max(MAX_VERSION)
    @Min(MIN_VERSION)
    private int version = 1;

    @Max(MAX_TYPE)
    @Min(MIN_TYPE)
    private int type;

    @Max(MAX_CODE)
    @Min(MIN_CODE)
    private int code;

    @Max(MAX_MESSAGE_ID)
    @Min(MIN_MESSAGE_ID)
    private int messageId;

    private byte[] token;

    //    @NotNull
    private List</*@Valid @NotNull*/ Option> options;

    private byte[] payload;
}
