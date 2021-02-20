package com.github.jinahya.rfc7252.message;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A class for testing {@link Option} class.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Slf4j
class OptionTest {

    static Option newRandomInstance() {
        final Option instance = new Option();
        instance.setNumber(current().nextInt(1024));
        instance.setValue(new byte[current().nextInt(1024)]);
        current().nextBytes(instance.getValue());
        return instance;
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Test
    void hashCode_SameHashCode_SameValues() {
        final int number = current().nextInt() >>> 1;
        final byte[] value = new byte[current().nextInt(1024)];
        current().nextBytes(value);
        final Option o1 = Option.of(number, value);
        final Option o2 = Option.of(number, value);
        assertThat(o2).hasSameHashCodeAs(o1);
    }

    @Test
    void equals_Equals_SameValues() {
        final int number = current().nextInt() >>> 1;
        final byte[] value = new byte[current().nextInt(1024)];
        current().nextBytes(value);
        final Option o1 = Option.of(number, value);
        final Option o2 = Option.of(number, value);
        assertThat(o2).isEqualTo(o1);
    }

    // -------------------------------------------------------------------------------------------------- getValueAsUint

    /**
     * Tests {@link Option#getValueAsUint()} method.
     */
    @Test
    void testGetValueAsUint() {
        final Option option = new Option();
        assertEquals(BigInteger.ZERO, option.getValueAsUint());
    }

    // -------------------------------------------------------------------------------------------------- setValueAsUint

    /**
     * Asserts {@link Option#setValueAsUint(BigInteger)} method throws a {@code NullPointerException} when {@code
     * valueAsUint} argument is {@code null}.
     */
    @Test
    void assertSetValueThrowsNullPointerExceptionWhenValueAsUintIsNull() {
        final Option option = new Option();
        assertThrows(NullPointerException.class, () -> option.setValueAsUint(null));
    }

    /**
     * Asserts {@link Option#setValueAsUint(BigInteger)} method throws a {@code IllegalArgumentException} when {@code
     * valueAsUint.signum} is {@code -1}.
     */
    @Test
    void assertSetValueThrowsNullPointerExceptionWhenValueAsUintIsNegative() {
        final Option option = new Option();
        final BigInteger valueAsUint = BigInteger.valueOf(current().nextLong() | Long.MIN_VALUE);
        assertThrows(IllegalArgumentException.class, () -> option.setValueAsUint(valueAsUint));
    }

    /**
     * Tests both {@link Option#getValueAsUint()} method and {@link Option#setValueAsUint(BigInteger)} method.
     */
    @Test
    void testValueAsBigInt() {
        {
            final Option option = new Option();
            final BigInteger expected = BigInteger.valueOf(0L);
            option.setValueAsUint(expected);
            final BigInteger actual = option.getValueAsUint();
            assertEquals(expected, actual);
            assertEquals(0, option.getValue().length);
        }
        {
            final Option option = new Option();
            final BigInteger expected = BigInteger.valueOf(1L);
            option.setValueAsUint(expected);
            final BigInteger actual = option.getValueAsUint();
            assertEquals(expected, actual);
            assertEquals(1, option.getValue().length);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    @RepeatedTest(16)
    void equals_Equal_WriteRead() throws IOException {
        final Option expected = newRandomInstance();
        log.debug("expected: {}", expected);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expected.write(new DataOutputStream(baos));
        final Option actual = new Option();
        actual.read(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        log.debug("actual: {}", actual);
        assertThat(actual).isEqualTo(expected);
    }
}
