package com.github.jinahya.rfc7252.message;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.jinahya.rfc7252.message.Message.MAX_CODE;
import static com.github.jinahya.rfc7252.message.Message.MAX_MESSAGE_ID;
import static com.github.jinahya.rfc7252.message.Message.MAX_TOKEN_LENGTH;
import static com.github.jinahya.rfc7252.message.Message.MAX_TYPE;
import static com.github.jinahya.rfc7252.message.Message.MAX_VERSION;
import static com.github.jinahya.rfc7252.message.Message.MIN_CODE;
import static com.github.jinahya.rfc7252.message.Message.MIN_MESSAGE_ID;
import static com.github.jinahya.rfc7252.message.Message.MIN_TOKEN_LENGTH;
import static com.github.jinahya.rfc7252.message.Message.MIN_TYPE;
import static com.github.jinahya.rfc7252.message.Message.MIN_VERSION;
import static com.github.jinahya.rfc7252.message.Message.Option.MAX_DELTA;
import static java.util.concurrent.ThreadLocalRandom.current;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A class for testing {@link Message} class.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
public class MessageTest {

    // -----------------------------------------------------------------------------------------------------------------
    @RepeatedTest(16)
//    @Test
    public void writeRead() throws IOException {
        final Message expected = new Message();
        expected.setVersion(current().nextInt(MIN_VERSION, MAX_VERSION + 1));
        expected.setType(current().nextInt(MIN_TYPE, MAX_TYPE + 1));
        expected.setCode(current().nextInt(MIN_CODE, MAX_CODE + 1));
        expected.setMessageId(current().nextInt(MIN_MESSAGE_ID, MAX_MESSAGE_ID + 1));
        final byte[] token = new byte[current().nextInt(MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH + 1)];
        current().nextBytes(token);
        expected.setToken(token);
        if (current().nextBoolean()) {
            final List<Message.Option> options = new ArrayList<>();
            final int size = current().nextInt(1, 8);
            for (int i = 0; i < size; i++) {
                final Message.Option option = new Message.Option();
                option.setNumber(current().nextInt(0, MAX_DELTA + 1));
                final byte[] value = new byte[current().nextInt(8)]; // (MAX_VALUE_LENGTH >> 3) + 1)];
                current().nextBytes(value);
                option.setValue(value);
                options.add(option);
            }
            expected.setOptions(options);
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expected.write(new DataOutputStream(baos));
        final Message actual = new Message();
        actual.read(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        assertEquals(expected, actual);
    }
}
