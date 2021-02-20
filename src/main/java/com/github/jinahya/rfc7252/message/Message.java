package com.github.jinahya.rfc7252.message;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class for binding messages.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7252#section-3">3. Message Format (RFC 7252)</a>
 */
public class Message implements Serializable {

    // --------------------------------------------------------------------------------------------------- Version (Ver)

    /**
     * The number of bits for {@code version} property.
     */
    private static final int SIZE_VERSION = 2;

    /**
     * The minimum value for {@code version} property. The value is {@value}.
     */
    public static final int MIN_VERSION = 0;

    /**
     * The maximum value for {@code version} property. The value is {@value}.
     */
    public static final int MAX_VERSION = 3;

    public static final int VERSION00 = 0;

    public static final int VERSION01 = 1;

    public static final int VERSION02 = 2;

    public static final int VERSION03 = 3;

    // -------------------------------------------------------------------------------------------------------- Type (T)

    /**
     * The number of bits for {@code type} property.
     */
    private static final int SIZE_TYPE = 2;

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

    // ---------------------------------------------------------------------------------------------- Token Length (TKL)

    /**
     * The number of bits for {@code Token Length (TKL)}. The value is {@value}.
     */
    private static final int SIZE_TKL = 4;

    private static final int MIN_TKL = 0;

    /**
     * <blockquote>Lengths 9-15 are reserved, MUST NOT be sent, and MUST be processed as a message format
     * error.</blockquote>
     */
    private static final int MAX_TKL = 8;

    // ------------------------------------------------------------------------------------------------------------ Code

    /**
     * The number of bits for {@code Code} property. The value is {@value}.
     */
    public static final int SIZE_CODE = 8;

    /**
     * The minimum value for {@code code} property. The value is {@value}.
     */
    public static final int MIN_CODE = 0;

    /**
     * The maximum value for {@code code} property. The value is {@value}.
     */
    public static final int MAX_CODE = 255;

    public static final int SIZE_CODE_CLASS = 3;

    public static final int MIN_CODE_CLASS = 0;

    public static final int MAX_CODE_CLASS = 7;

    public static final int CODE_CLASS_REQUEST = 0;

    public static final int CODE_CLASS_SUCCESS_RESPONSE = 2;

    public static final int CODE_CLASS_CLIENT_ERROR_RESPONSE = 4;

    public static final int CODE_CLASS_SERVER_ERROR_RESPONSE = 5;

    public static final int SIZE_CODE_DETAIL = 5; // 0b111_xxxxx

    public static final int MIN_CODE_DETAIL = 0;

    public static final int MAX_CODE_DETAIL = 31; // 0bxxx_11111

    public static final int CODE_EMPTY_MESSAGE = 0x00; // 0.00

    public static final int CODE_REQUEST_METHOD_GET = 0x01; // 0.01

    public static final int CODE_REQUEST_METHOD_POST = 0x02; // 0.02

    public static final int CODE_REQUEST_METHOD_PUT = 0x03; // 0.03

    public static final int CODE_REQUEST_METHOD_DELETE = 0x04; // 0.04

    public static final int CODE_RESPONSE_CREATED = 0x41; // 2.01

    public static final int CODE_RESPONSE_DELETED = 0x42; // 2.02

    public static final int CODE_RESPONSE_VALID = 0x43; // 2.03

    public static final int CODE_RESPONSE_CHANGED = 0x44; // 2.04

    public static final int CODE_RESPONSE_CONTENT = 0x45; // 2.05

    public static final int CODE_RESPONSE_BAD_REQUEST = 0x80; // 4.00

    public static final int CODE_RESPONSE_UNAUTHORIZED = 0x81; // 4.01

    public static final int CODE_RESPONSE_BAD_OPTION = 0x82; // 4.02

    public static final int CODE_RESPONSE_FORBIDDEN = 0x83; // 4.03

    public static final int CODE_RESPONSE_NOT_FOUND = 0x84; // 4.04

    public static final int CODE_RESPONSE_METHOD_NOT_ALLOWED = 0x85; // 4.05

    public static final int CODE_RESPONSE_NOT_ACCEPTABLE = 0x86; // 4.06

    public static final int CODE_RESPONSE_PRECONDITION_FAILED = 0x8C; // 4.12

    public static final int CODE_RESPONSE_REQUEST_ENTITY_TOO_LARGE = 0x8D; // 4.13

    public static final int CODE_RESPONSE_UNSUPPORTED_CONTENT_FORMAT = 0x8F; // 4.15

    public static final int CODE_RESPONSE_INTERNAL_SERVER_ERROR = 0xA0; // 5.00

    public static final int CODE_RESPONSE_NOT_IMPLEMENTED = 0xA1; // 5.01

    public static final int CODE_RESPONSE_BAD_GATEWAY = 0xA2; // 5.02

    public static final int CODE_RESPONSE_SERVICE_UNAVAILABLE = 0xA3; // 5.03

    public static final int CODE_RESPONSE_GATEWAY_TIMEOUT = 0xA4; // 5.04

    public static final int CODE_RESPONSE_PROXING_NOT_SUPPORTED = 0xA5; // 5.05

    // ------------------------------------------------------------------------------------------------------ Message ID
    public static final int SIZE_MESSAGE_ID = 16;

    public static final int MIN_MESSAGE_ID = 0;

    public static final int MAX_MESSAGE_ID = 65535;

    // ----------------------------------------------------------------------------------------------------------- Token
    public static final int MIN_TOKEN_LENGTH = 0;

    public static final int MAX_TOKEN_LENGTH = 8;

    private static final byte[] TOKEN_VALUE_EMPTY = new byte[0];

    // --------------------------------------------------------------------------------------------------------- Options
    public static final int MIN_OPTION_NUMBER = 0;

    public static final int MAX_OPTION_VALUE_LENGTH = 65535 + 269;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int PAYLOAD_MARKER = 0xFF;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance.
     */
    public Message() {
        super();
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return super.toString() + '{'
               + "version=" + version
               + ",type=" + type
               + ",code=" + code
               + ",messageId=" + messageId
               + ",token=" + token
               + ",options=" + options
               + ",payload=" + payload
               + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final Message that = (Message) obj;
        if (version != that.version) return false;
        if (type != that.type) return false;
        if (code != that.code) return false;
        if (messageId != that.messageId) return false;
        if (!Arrays.equals(token, that.token)) return false;
        if (options != null ? !options.equals(that.options) : that.options != null) return false;
        return Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + type;
        result = 31 * result + code;
        result = 31 * result + messageId;
        result = 31 * result + Utils.hashCode(token);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + Utils.hashCode(payload);
        return result;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public void read(final DataInput input) throws IOException {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        final int tokenLength;
        {
            int b = input.readUnsignedByte();
            tokenLength = b & 0xF;
            b >>= 4;
            setType(b & 0x3);
            b >>= 2;
            setVersion(b);
        }
        if (tokenLength > MAX_TKL) {
            throw new RuntimeException("invalid token length: " + tokenLength);
        }
        setCode(input.readUnsignedByte());
        setMessageId(input.readUnsignedShort());
        setToken(new byte[tokenLength]);
        input.readFully(getToken());
        Option previous = null;
        for (int b; ; ) {
            try {
                b = input.readUnsignedByte();
            } catch (final EOFException eofe) {
                break;
            }
            if (b == PAYLOAD_MARKER) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true) {
                    try {
                        baos.write(input.readByte());
                    } catch (final EOFException eofe) {
                        break;
                    }
                }
                if (baos.size() == 0) {
                    throw new RuntimeException("message format error");
                }
                setPayload(baos.toByteArray());
                break;
            }
            final Option option = new Option();
            option.previous = previous;
            option.read(input);
            getOptions().add(option);
            previous = option;
        }
    }

    /**
     * Read values from specified input stream.
     *
     * @param input the input stream from which values are read.
     * @throws IOException if an I/O error occurs.
     */
    public void read(final InputStream input) throws IOException {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        read((DataInput) new DataInputStream(input));
    }

    /**
     * Reads values from specified byte array.
     *
     * @param data the byte array from which values are read.
     * @throws IOException if an I/O error occurs.
     */
    public void read(final byte[] data) throws IOException {
        if (data == null) {
            throw new NullPointerException("data is null");
        }
        read(new ByteArrayInputStream(data));
    }

    /**
     * Reads values from specified packet's {@link DatagramPacket#getData() data}.
     *
     * @param packet the packet from which values are read.
     * @throws IOException if an I/O error occurs.
     */
    public void read(final DatagramPacket packet) throws IOException {
        if (packet == null) {
            throw new NullPointerException("package is null");
        }
        read(packet.getData());
    }

    /**
     * Writes this message to specified data output.
     *
     * @param output the data output to which this message is written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(final DataOutput output) throws IOException {
        if (output == null) {
            throw new NullPointerException("output is null");
        }
        {
            int b = version;
            b <<= SIZE_TYPE;
            b |= type;
            b <<= SIZE_TKL;
            b |= token == null ? 0 : token.length;
            output.writeByte(b);
        }
        output.write(code);
        output.writeShort(messageId);
        if (token != null) {
            output.write(token);
        }
        if (options != null && !options.isEmpty()) {
            Collections.sort(options);
            Option previous = null;
            for (final Option option : options) {
                option.previous = previous;
                option.write(output);
                previous = option;
            }
        }
        if (payload != null && payload.length > 0) {
            output.write(PAYLOAD_MARKER);
            output.write(payload);
        }
    }

    /**
     * Writes values to specified output stream.
     *
     * @param output the output stream to which values are written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(final OutputStream output) throws IOException {
        write((DataOutput) new DataOutputStream(output));
    }

    /**
     * Writes values to a buffer and returns it.
     *
     * @return an array of bytes.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] write() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos);
        return baos.toByteArray();
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

    /**
     * Returns current value of {@code code} property.
     *
     * @return current value of {@code code} property.
     */
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

    public Message code(final int code) {
        setCode(code);
        return this;
    }

    /**
     * Returns current value of {@code class} part of {@code code} property.
     *
     * @return current value of {@code class} part of {@code code} property.
     */
    public int getCodeClass() {
        return getCode() >> SIZE_CODE_DETAIL;
    }

    /**
     * Replaces current value of {@code class} part of {@code code} property with specified value.
     *
     * @param codeClass new value for {@code class} part of {@code code} property.
     */
    public void setCodeClass(final int codeClass) {
        if (codeClass < MIN_CODE_CLASS) {
            throw new IllegalArgumentException("codeClass(" + codeClass + ") < " + MIN_CODE_CLASS);
        }
        if (codeClass > MAX_CODE_CLASS) {
            throw new IllegalArgumentException("codeClass(" + codeClass + ") > " + MAX_CODE_CLASS);
        }
        setCode((codeClass << SIZE_CODE_DETAIL) | getCodeDetail());
    }

    public Message codeClass(final int codeClass) {
        setCodeClass(codeClass);
        return this;
    }

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

    public Message codeDetail(final int codeDetail) {
        setCodeDetail(codeDetail);
        return this;
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

    public Message messageId(final int messageId) {
        setMessageId(messageId);
        return this;
    }

    // ----------------------------------------------------------------------------------------------------------- token
    public byte[] getToken() {
        return token;
    }

    public void setToken(final byte[] token) {
        if (token != null && token.length > MAX_TOKEN_LENGTH) {
            throw new IllegalArgumentException("token.length(" + token.length + ") > " + MAX_TOKEN_LENGTH);
        }
        this.token = token;
    }

    public Message token(final byte[] token) {
        setToken(token);
        return this;
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
        getOptions().add(Option.from(option));
    }

    public Message option(final Option option) {
        addOption(option);
        return this;
    }

    public void addOption(final int number, final byte[] value) {
        addOption(Option.of(number, value));
    }

    public Message option(final int number, final byte[] value) {
        addOption(number, value);
        return this;
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
    private int version = VERSION01;

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

    private List</*@Valid @NotNull*/Option> options;

    private byte[] payload;
}
