package com.github.jinahya.rfc7252.message;

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

/**
 * A class for binding messages.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7252#section-3">3. Message Format (RFC 7252)</a>
 */
public class Message {

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The number of bits for {@code version} property.
     */
    private static final int SIZE_VERSION = 2;

    /**
     * The maximum value for {@code version} property. The value is {@value}.
     */
    public static final int MAX_VERSION = -1 >>> (Integer.SIZE - SIZE_VERSION);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The number of bits for {@code type} property.
     */
    private static final int SIZE_TYPE = 2;

    /**
     * The maximum value for {@code type} property. The value is {@value}.
     */
    public static final int MAX_TYPE = 3;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int TYPE_CONFIRMABLE = 0;

    public static final int TYPE_NON_CONFIRMABLE = 1;

    public static final int TYPE_ACKNOWLEDGEMENT = 2;

    public static final int TYPE_RESET = 3;

    // -----------------------------------------------------------------------------------------------------------------
    private static final int SIZE_TOKEN_LENGTH = 4;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The number of bits for {@code code} property.
     */
    public static final int SIZE_CODE = 8;

//    /**
//     * The minimum value for {@code code} property. The value is {@value}.
//     */
//    public static final int MIN_CODE = 0;

    /**
     * The maximum value for {@code code} property. The value is {@value}.
     */
    public static final int MAX_CODE = -1 >>> (Integer.SIZE - SIZE_CODE);

    // -----------------------------------------------------------------------------------------------------------------
    public static final int CODE_EMPTY_MESSAGE = 0; // 0.00

    // -----------------------------------------------------------------------------------------------------------------
    public static final int METHOD_CODE_GET = 1; // 0.01

    public static final int METHOD_CODE_POST = 2; // 0.02

    public static final int METHOD_CODE_PUT = 3; // 0.03

    public static final int METHOD_CODE_DELETE = 4; // 0.04

    // -----------------------------------------------------------------------------------------------------------------
    public static final int RESPONSE_CODE_CREATED = 0x41; // 2.01

    public static final int RESPONSE_CODE_DELETED = 0x42; // 2.02

    public static final int RESPONSE_CODE_VALID = 0x43; // 2.03

    public static final int RESPONSE_CODE_CHANGED = 0x44; // 2.04

    public static final int RESPONSE_CODE_CONTENT = 0x45; // 2.05

    public static final int RESPONSE_CODE_BAD_REQUEST = 0x80; // 4.00

    public static final int RESPONSE_CODE_UNAUTHORIZED = 0x81; // 4.01

    public static final int RESPONSE_CODE_BAD_OPTION = 0x82; // 4.02

    public static final int RESPONSE_CODE_FORBIDDEN = 0x83; // 4.03

    public static final int RESPONSE_CODE_NOT_FOUND = 0x84; // 4.04

    public static final int RESPONSE_CODE_METHOD_NOT_ALLOWED = 0x85; // 4.05

    public static final int RESPONSE_CODE_NOT_ACCEPTABLE = 0x86; // 4.06

    public static final int RESPONSE_CODE_PRECONDITION_FAILED = 0x8C; // 4.12

    public static final int RESPONSE_CODE_REQUEST_ENTITY_TOO_LARGE = 0x8D; // 4.13

    public static final int RESPONSE_CODE_UNSUPPORTED_CONTENT_FORMAT = 0x8F; // 4.15

    public static final int RESPONSE_CODE_INTERNAL_SERVER_ERROR = 0xA0; // 5.00

    public static final int RESPONSE_CODE_NOT_IMPLEMENTED = 0xA1; // 5.01

    public static final int RESPONSE_CODE_BAD_GATEWAY = 0xA2; // 5.02

    public static final int RESPONSE_CODE_SERVICE_UNAVAILABLE = 0xA3; // 5.03

    public static final int RESPONSE_CODE_GATEWAY_TIMEOUT = 0xA4; // 5.04

    public static final int RESPONSE_CODE_PROXING_NOT_SUPPORTED = 0xA5; // 5.05

    // -----------------------------------------------------------------------------------------------------------------
    private static final int SIZE_CODE_CLASS = 3;

    public static final int MIN_CODE_CLASS = 0;

    public static final int MAX_CODE_CLASS = -1 >>> (Integer.SIZE - SIZE_CODE_CLASS);

    public static final int CODE_CLASS_REQUEST = 0;

    public static final int CODE_CLASS_SUCCESS_RESPONSE = 1;

    public static final int CODE_CLASS_CLIENT_ERROR_RESPONSE = 4;

    public static final int CODE_CLASS_SERVER_ERROR_RESPONSE = 5;

    // -----------------------------------------------------------------------------------------------------------------
    private static final int SIZE_CODE_DETAIL = 5;

    private static final int MIN_CODE_DETAIL = 0;

    private static final int MAX_CODE_DETAIL = -1 >>> (Integer.SIZE - SIZE_CODE_DETAIL);

    // -----------------------------------------------------------------------------------------------------------------
    public static final int SIZE_MESSAGE_ID = 16;

    public static final int MIN_MESSAGE_ID = 0;

    public static final int MAX_MESSAGE_ID = -1 >>> (Integer.SIZE - SIZE_MESSAGE_ID);

    // -----------------------------------------------------------------------------------------------------------------
    //public static final int MIN_TOKEN_LENGTH = 0;

    public static final int MAX_TOKEN_LENGTH = 8;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int PAYLOAD_MARKER = 0xFF;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Reads a new instance from specified datagram packet's data.
     *
     * @param packet the datagram packet from which a new instance is read.
     * @return a new instance read from the packet.
     * @throws IOException if an I/O error occurs.
     */
    public static Message readInstance(final DatagramPacket packet) throws IOException {
        if (packet == null) {
            throw new NullPointerException("package is null");
        }
        final Message instance = new Message();
        final DataInputStream input = new DataInputStream(new ByteArrayInputStream(
                packet.getData(), packet.getOffset(), packet.getLength()));
        try {
            instance.read(input);
        } finally {
            input.close();
        }
        return instance;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A class for binding options.
     *
     * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
     * @see <a href="https://tools.ietf.org/html/rfc7252#section-3.1">3.1. Option Format (RFC 7252)</a>
     */
    public static class Option implements Comparable<Option> {

        // -------------------------------------------------------------------------------------------------------------
        static final int MAX_DELTA = 65535 + 269;

        // -------------------------------------------------------------------------------------------------------------
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
                   + ",value=" + value
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
        private transient Integer delta;

        private Integer number;

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
               + ",options=" + getOptions()
//               + ",payload=" + Arrays.toString(payload)
               + ",payload=" + payload
               + "}";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        final Message message = (Message) o;
        if (version != message.version) return false;
        if (type != message.type) return false;
        if (code != message.code) return false;
        if (messageId != message.messageId) return false;
        if (!Arrays.equals(token, message.token)) return false;
        if (!getOptions().equals(message.getOptions())) return false;
        return Arrays.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + type;
        result = 31 * result + code;
        result = 31 * result + messageId;
        result = 31 * result + Arrays.hashCode(token);
        result = 31 * result + getOptions().hashCode();
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
                break;
            }
            final Option option = new Option();
            option.read(b, input);
            {
                final int previousNumber;
                final List<Option> options = getOptions();
                previousNumber = options.isEmpty() ? 0 : options.get(options.size() - 1).getNumber();
                option.setPreviousNumber(previousNumber);
            }
            options.add(option);
        }
    }

    public void write(final DataOutput output) throws IOException {
        output.write((version << (SIZE_TYPE + SIZE_TOKEN_LENGTH))
                     | (type << SIZE_TOKEN_LENGTH)
                     | (token == null ? 0 : token.length)
        );
        output.write(code);
        output.writeShort(messageId);
        if (token != null) {
            output.write(token);
        }
        if (options != null) {
            int previousNumber = 0;
            for (int i = 0; i < options.size(); i++) {
                final Option current = options.get(i);
                current.setPreviousNumber(previousNumber);
                current.write(output);
                previousNumber = current.getNumber();
            }
        }
        if (payload != null) {
            output.write(PAYLOAD_MARKER);
            output.write(payload);
        }
    }

    // --------------------------------------------------------------------------------------------------------- version
    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        if (version < 0) {
            throw new IllegalArgumentException("version(" + version + ") < 0");
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
     * @see #MAX_TYPE
     */
    public void setType(final int type) {
        if (type < 0) {
            throw new IllegalArgumentException("type(" + type + ") < 0");
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
        if (code < 0) {
            throw new IllegalArgumentException("code(" + code + ") < 0");
        }
        if (code > MAX_CODE) {
            throw new IllegalArgumentException("code(" + code + ") > " + MAX_CODE);
        }
        this.code = code;
    }

    // ------------------------------------------------------------------------------------------------------- codeClass
    public int getCodeClass() {
        return getCode() >> SIZE_CODE_DETAIL;
    }

    public void setCodeClass(final int codeClass) {
        if (codeClass < MIN_CODE_CLASS) {
            throw new IllegalArgumentException("codeClass(" + codeClass + ") < " + MIN_CODE_CLASS);
        }
        if (codeClass > MAX_CODE_CLASS) {
            throw new IllegalArgumentException("codeClass(" + codeClass + ") > " + MAX_CODE_CLASS);
        }
        setCode((codeClass << SIZE_CODE_DETAIL) | getCodeDetail());
    }

    // ------------------------------------------------------------------------------------------------------ codeDetail
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
        setCode((getCodeClass() << SIZE_CODE_DETAIL) | codeDetail);
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
//            if (token.length < MIN_TOKEN_LENGTH) {
//                throw new IllegalArgumentException("token.length(" + token.length + ") < " + MIN_TOKEN_LENGTH);
//            }
            if (token.length > MAX_TOKEN_LENGTH) {
                throw new IllegalArgumentException("token.length(" + token.length + ") > " + MAX_TOKEN_LENGTH);
            }
        }
        this.token = token;
    }

    // --------------------------------------------------------------------------------------------------------- options
    private List<Option> getOptions() {
        if (options == null) {
            options = new ArrayList<Option>();
        }
        return options;
    }

    public void addOption(final Option option) {
        if (option == null) {
            throw new NullPointerException("option is null");
        }
        final int number = option.getNumber();
        final int previous = getOptions().isEmpty() ? 0 : getOptions().get(0).getNumber();
        if (number < previous) {
            throw new IllegalArgumentException(
                    "option.number(" + number + ") < previous option's number(" + previous + ")");
        }
        getOptions().add(option);
    }

    // --------------------------------------------------------------------------------------------------------- payload
    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    // -----------------------------------------------------------------------------------------------------------------
    private int version = 1;

    private int type;

    private int code;

    private int messageId;

    private byte[] token;

    private List<Option> options;

    private byte[] payload;
}
