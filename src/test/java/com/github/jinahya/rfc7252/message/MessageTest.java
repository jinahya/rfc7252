package com.github.jinahya.rfc7252.message;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.github.jinahya.rfc7252.message.Message.MAX_CODE;
import static com.github.jinahya.rfc7252.message.Message.MAX_MESSAGE_ID;
import static com.github.jinahya.rfc7252.message.Message.MAX_TOKEN_LENGTH;
import static com.github.jinahya.rfc7252.message.Message.MAX_TYPE;
import static com.github.jinahya.rfc7252.message.Message.MAX_VERSION;
import static com.github.jinahya.rfc7252.message.Message.MIN_CODE;
import static com.github.jinahya.rfc7252.message.Message.MIN_MESSAGE_ID;
import static com.github.jinahya.rfc7252.message.Message.MIN_TYPE;
import static com.github.jinahya.rfc7252.message.Message.MIN_VERSION;
import static java.util.concurrent.ThreadLocalRandom.current;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A class for testing {@link Message} class.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class MessageTest {

    static Message newRandomInstance() throws IOException {
        final Message message = new Message();
        message.setVersion(current().nextInt(MIN_VERSION, MAX_VERSION + 1));
        message.setType(current().nextInt(MIN_TYPE, MAX_TYPE + 1));
        message.setCode(current().nextInt(MIN_CODE, MAX_CODE + 1));
        message.setMessageId(current().nextInt(MIN_MESSAGE_ID, MAX_MESSAGE_ID + 1));
        message.setToken(new byte[current().nextInt(MAX_TOKEN_LENGTH + 1)]);
        current().nextBytes(message.getToken());
        {
            final int count = 0; //current().nextInt(8);
            for (int i = 0; i < count; i++) {
                final int number = current().nextInt(128);
                final byte[] value = new byte[current().nextInt(65536)];
                current().nextBytes(value);
                message.option(number, value);
            }
        }
        message.setPayload(new byte[current().nextInt(1024)]);
        current().nextBytes(message.getPayload());
        return message;
    }

    // -----------------------------------------------------------------------------------------------------------------
    @RepeatedTest(16)
    void equals__() throws IOException {
        final Message expected = newRandomInstance();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expected.write(baos);
        final Message actual = new Message();
        actual.read(baos.toByteArray());
        assertThat(actual).isEqualTo(expected);
    }
}
