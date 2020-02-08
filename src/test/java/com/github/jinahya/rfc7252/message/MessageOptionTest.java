package com.github.jinahya.rfc7252.message;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A class for testing {@link Message.Option} class.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
public class MessageOptionTest {

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tests {@link Message.Option#getValueAsUint()} method.
     */
    @Test
    void testGetValueAsBigInt() {
        final Message.Option option = new Message.Option();
        assertEquals(BigInteger.ZERO, option.getValueAsUint());
    }

    /**
     * Asserts {@link Message.Option#setValueAsUint(BigInteger)} method throws a {@code NullPointerException} when
     * {@code valueAsUint} argument is {@code null}.
     */
    @Test
    void assertSetValueThrowsNullPointerExceptionWhenValueAsUintIsNull() {
        final Message.Option option = new Message.Option();
        assertThrows(NullPointerException.class, () -> option.setValueAsUint(null));
    }

    /**
     * Asserts {@link Message.Option#setValueAsUint(BigInteger)} method throws a {@code IllegalArgumentException} when
     * {@code valueAsUint} argument is negative
     */
    @Test
    void assertSetValueThrowsNullPointerExceptionWhenValueAsUintIsNegative() {
        final Message.Option option = new Message.Option();
        final BigInteger valueAsUint = BigInteger.valueOf(current().nextLong() | Long.MIN_VALUE);
        assertThrows(IllegalArgumentException.class, () -> option.setValueAsUint(valueAsUint));
    }

    /**
     * Tests both {@link Message.Option#getValueAsUint()} method and {@link Message.Option#setValueAsUint(BigInteger)}
     * method.
     */
    @Test
    void testValueAsBigInt() {
        {
            final Message.Option option = new Message.Option();
            final BigInteger expected = BigInteger.valueOf(0L);
            option.setValueAsUint(expected);
            final BigInteger actual = option.getValueAsUint();
            assertEquals(expected, actual);
            assertEquals(0, option.getValue().length);
        }
        {
            final Message.Option option = new Message.Option();
            final BigInteger expected = BigInteger.valueOf(1L);
            option.setValueAsUint(expected);
            final BigInteger actual = option.getValueAsUint();
            assertEquals(expected, actual);
            assertEquals(1, option.getValue().length);
        }
    }
}
