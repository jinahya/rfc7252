package com.github.jinahya.rfc7252.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.json.bind.annotation.JsonbTransient;

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
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Message {

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MIN_VERSION = 0;

    public static final int MAX_VERSION = 3;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MIN_TYPE = 0;

    public static final int MAX_TYPE = 3;

    public static final int TYPE_CONFIRMABLE = 0;

    public static final int TYPE_NON_CONFIRMABLE = 1;

    public static final int TYPE_ACKNOWLEDGEMENT = 2;

    public static final int TYPE_REST = 3;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MIN_CODE = 0;

    public static final int MAX_CODE = 255;

    public static final int SIZE_CODE_CLASS = 3;

    private static final int MASK_CODE_CLASS = 7; // 0b111

    public static final int CODE_CLASS_REQUEST = 0;

    public static final int CODE_CLASS_SUCCESS_RESPONSE = 1;

    public static final int CODE_CLASS_CLIENT_ERROR_RESPONSE = 4;

    public static final int CODE_CLASS_SERVER_ERROR_RESPONSE = 5;

    public static final int SIZE_CODE_DETAIL = 5;

    private static final int MASK_CODE_DETAIL = 31; // 0b111111

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MIN_MESSAGE_ID = 0;

    public static final int MAX_MESSAGE_ID = 65535;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int MIN_TOKEN_LENGTH = 0;

    public static final int MAX_TOKEN_LENGTH = 8;

    // -----------------------------------------------------------------------------------------------------------------
    public static final int PAYLOAD_MARKER = 0xFF;

    // -----------------------------------------------------------------------------------------------------------------
    public static class Option implements Comparable<Option> {

        // -------------------------------------------------------------------------------------------------------------
        public static final int MIN_NUMBER = 0;

//        public static final int MAX_OPTION_NUMBER = 65535 + 269;

        // -------------------------------------------------------------------------------------------------------------
        static final int MIN_DELTA = 0;

        static final int MAX_DELTA = 65535 + 269;

        // -------------------------------------------------------------------------------------------------------------
        @Deprecated
        public static final int MIN_VALUE_LENGTH = 0;

        public static final int MAX_VALUE_LENGTH = 65535 + 269;

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

        // -------------------------------------------------------------------------------------------------------------
        void read(final int b, final DataInput input) throws IOException {
            int length;
            {
                delta = b >> 4;
                length = b & 0xF;
            }
            if (delta < 13) {
                // do nothing
            } else if (delta == 13) {
                delta = input.readUnsignedByte() + 13;
            } else if (delta == 14) {
                delta = input.readUnsignedShort() + 269;
            } else {
                assert delta == 0xF;
                throw new RuntimeException("illegal delta: " + delta);
            }
            if (length < 13) {
                // do nothing
            } else if (length == 13) {
                length = input.readUnsignedByte() + 13;
            } else if (length == 14) {
                length = input.readUnsignedShort() + 269;
            } else {
                assert length == 0xF;
                throw new RuntimeException("illegal length: " + length);
            }
            value = new byte[length];
            input.readFully(value);
            setValue(value);
        }

        public void read(final DataInput input) throws IOException {
            if (input == null) {
                throw new NullPointerException("input is null");
            }
            read(input.readUnsignedByte(), input);
        }

        public void write(final DataOutput output) throws IOException {
            int delta_ = delta;
            Integer deltaExtended_ = null;
            int length_ = value.length;
            Integer lengthExtended_ = null;
            if (delta_ < 13) {
                // do nothing
            } else if (delta_ <= (255 + 13)) {
                deltaExtended_ = delta_ - 13;
                delta_ = 13;
            } else if (delta_ <= (65535 + 269)) {
                deltaExtended_ = delta_ - 269;
                delta_ = 14;
            }
            if (length_ < 13) {
                // do nothing
            } else if (length_ <= (255 + 13)) {
                lengthExtended_ = length_ - 13;
                length_ = 13;
            } else if (length_ <= (65535 + 269)) {
                lengthExtended_ = length_ = 269;
                length_ = 14;
            }
            output.writeByte((delta_ << 4) | length_);
            if (deltaExtended_ != null) {
                if (delta_ == 13) {
                    output.write(deltaExtended_);
                } else {
                    assert delta_ == 14;
                    output.writeShort(deltaExtended_);
                }
            }
            if (lengthExtended_ != null) {
                if (length_ == 13) {
                    output.writeByte(lengthExtended_);
                } else {
                    assert length_ == 14;
                    output.writeShort(lengthExtended_);
                }
            }
        }

        // ---------------------------------------------------------------------------------------------- previousNumber
        void setPreviousNumber(final int previousNumber) {
            if (previousNumber > number) {
                throw new IllegalArgumentException(
                        "previousNumber(" + previousNumber + " > number(" + number + ")");
            }
            setDelta(number - previousNumber);
        }

        // ---------------------------------------------------------------------------------------------------- previous
        void setPrevious(final Option previous) {
            if (previous == null) {
                throw new NullPointerException("previous is null");
            }
            final int previousNumber = previous.getNumber();
            if (previousNumber > number) {
                throw new IllegalArgumentException("previous.number(" + previousNumber + ") > number(" + number + ")");
            }
            setPreviousNumber(previousNumber);
        }

        // ------------------------------------------------------------------------------------------------------- delta
        void setDelta(final int delta) {
            if (delta < MIN_DELTA) {
                throw new IllegalArgumentException("delta(" + delta + ") < " + MIN_DELTA);
            }
            if (delta > MAX_DELTA) {
                throw new IllegalArgumentException("delta(" + delta + ") > " + MAX_DELTA);
            }
            this.delta = delta;
        }

        // ------------------------------------------------------------------------------------------------------ number
        public int getNumber() {
            return number;
        }

        public void setNumber(final int number) {
            if (number < MIN_NUMBER) {
                throw new IllegalArgumentException("number(" + number + ") < " + MIN_NUMBER);
            }
            this.number = number;
        }

        // ------------------------------------------------------------------------------------------------------- value
        public byte[] getValue() {
            return value;
        }

        private void setValue(final byte[] value) {
            if (value == null) {
                throw new NullPointerException("value is null");
            }
            if (value.length < MIN_VALUE_LENGTH) {
                throw new IllegalArgumentException("value.length(" + value.length + ") < " + MIN_VALUE_LENGTH);
            }
            if (value.length > MAX_VALUE_LENGTH) {
                throw new IllegalArgumentException("value.length(" + value.length + ") < " + MAX_VALUE_LENGTH);
            }
            this.value = value;
        }

        // -------------------------------------------------------------------------------------------------------------
        @PositiveOrZero
        private int delta;

        @JsonIgnore
        @JsonbTransient
        @Min(MIN_NUMBER)
        @PositiveOrZero
        private transient int number;

        @NotNull
        private byte[] value;
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
                return;
            }
            if (b == 0xFF) { // payload marker
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
            Collections.sort(options);
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
        if (version < MAX_VERSION) {
            throw new IllegalArgumentException("version(" + version + ") > " + MAX_VERSION);
        }
        this.version = version;
    }

    // ------------------------------------------------------------------------------------------------------------ type
    public int getType() {
        return type;
    }

    public void setType(final int type) {
        if (type < MIN_TYPE) {
            throw new IllegalArgumentException("type(" + type + ") < " + MIN_TYPE);
        }
        if (type < MAX_TYPE) {
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
        if (code < MAX_CODE) {
            throw new IllegalArgumentException("code(" + code + ") > " + MAX_CODE);
        }
        this.code = code;
    }

    @JsonIgnore
    @JsonbTransient
    public int getCodeClass() {
        return getCode() & MASK_CODE_CLASS;
    }

    public void setCodeClass(final int codeClass) {
        setCode(((codeClass & MASK_CODE_CLASS) << SIZE_CODE_CLASS) | getCodeDetail());
    }

    @JsonIgnore
    @JsonbTransient
    public int getCodeDetail() {
        return getCode() & MASK_CODE_DETAIL;
    }

    public void setCodeDetail(final int codeDetail) {
        setCode(getCodeClass() | (codeDetail & MASK_CODE_DETAIL));
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
            if (token.length <= MIN_TOKEN_LENGTH) {
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

    @NotNull
    private List</*@Valid @NotNull*/ Option> options;

    private byte[] payload;
}
