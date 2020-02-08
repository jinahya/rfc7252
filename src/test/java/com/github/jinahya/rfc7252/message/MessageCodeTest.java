package com.github.jinahya.rfc7252.message;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MessageCodeTest {

    // -----------------------------------------------------------------------------------------------------------------
    private static void test(final int codeClass, final int codeDetail, final int code) {
        final Message message = new Message();
        message.setCodeClass(codeClass);
        message.setCodeDetail(codeDetail);
        assertEquals(code, message.getCode());
        assertEquals(codeClass, message.getCodeClass());
        assertEquals(codeDetail, message.getCodeDetail());
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Test
    void test_0_01_METHOD_CODE_GET() {
        test(0, 1, Message.METHOD_CODE_GET);
    }

    @Test
    void test_0_02_METHOD_CODE_POST() {
        test(0, 2, Message.METHOD_CODE_POST);
    }

    @Test
    void test_0_03_METHOD_CODE_PUT() {
        test(0, 3, Message.METHOD_CODE_PUT);
    }

    @Test
    void test_0_04_METHOD_CODE_DELETE() {
        test(0, 4, Message.METHOD_CODE_DELETE);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Test
    void test_2_01_RESPONSE_CODE_CREATED() {
        test(2, 1, Message.RESPONSE_CODE_CREATED);
    }

    @Test
    void test_2_02_RESPONSE_CODE_DELETED_2_02() {
        test(2, 2, Message.RESPONSE_CODE_DELETED);
    }

    @Test
    void test_2_03_RESPONSE_CODE_VALID() {
        test(2, 3, Message.RESPONSE_CODE_VALID);
    }

    @Test
    void test_2_04_RESPONSE_CODE_CHANGED() {
        test(2, 4, Message.RESPONSE_CODE_CHANGED);
    }

    @Test
    void test_2_05_RESPONSE_CODE_CONTENT() {
        test(2, 5, Message.RESPONSE_CODE_CONTENT);
    }

    @Test
    void test_4_00_RESPONSE_CODE_BAD_REQUEST() {
        test(4, 0, Message.RESPONSE_CODE_BAD_REQUEST);
    }

    @Test
    void test_4_01_RESPONSE_CODE_UNAUTHORIZED() {
        test(4, 1, Message.RESPONSE_CODE_UNAUTHORIZED);
    }

    @Test
    void test_4_02_RESPONSE_CODE_BAD_OPTION() {
        test(4, 2, Message.RESPONSE_CODE_BAD_OPTION);
    }

    @Test
    void test_4_03_RESPONSE_CODE_FORBIDDEN() {
        test(4, 3, Message.RESPONSE_CODE_FORBIDDEN);
    }

    @Test
    void test_4_04_RESPONSE_CODE_NOT_FOUND() {
        test(4, 4, Message.RESPONSE_CODE_NOT_FOUND);
    }

    @Test
    void test_4_05_RESPONSE_CODE_METHOD_NOT_ALLOWED() {
        test(4, 5, Message.RESPONSE_CODE_METHOD_NOT_ALLOWED);
    }

    @Test
    void test_4_06_RESPONSE_CODE_NOT_ACCEPTABLE() {
        test(4, 6, Message.RESPONSE_CODE_NOT_ACCEPTABLE);
    }

    @Test
    void test_4_12_RESPONSE_CODE_PRECONDITION_FAILED() {
        test(4, 12, Message.RESPONSE_CODE_PRECONDITION_FAILED);
    }

    @Test
    void test_4_13_RESPONSE_CODE_REQUEST_ENTITY_TOO_LARGE() {
        test(4, 13, Message.RESPONSE_CODE_REQUEST_ENTITY_TOO_LARGE);
    }

    @Test
    void test_4_15_RESPONSE_CODE_UNSUPPORTED_CONTENT_FORMAT() {
        test(4, 15, Message.RESPONSE_CODE_UNSUPPORTED_CONTENT_FORMAT);
    }

    @Test
    void test_5_00_RESPONSE_CODE_INTERNAL_SERVER_ERROR() {
        test(5, 0, Message.RESPONSE_CODE_INTERNAL_SERVER_ERROR);
    }

    @Test
    void test_5_01_RESPONSE_CODE_NOT_IMPLEMENTED() {
        test(5, 1, Message.RESPONSE_CODE_NOT_IMPLEMENTED);
    }

    @Test
    void test_5_02_RESPONSE_CODE_BAD_GATEWAY() {
        test(5, 2, Message.RESPONSE_CODE_BAD_GATEWAY);
    }

    @Test
    void test_5_03_RESPONSE_CODE_SERVICE_UNAVAILABLE() {
        test(5, 3, Message.RESPONSE_CODE_SERVICE_UNAVAILABLE);
    }

    @Test
    void test_5_04_RESPONSE_CODE_GATEWAY_TIMEOUT() {
        test(5, 4, Message.RESPONSE_CODE_GATEWAY_TIMEOUT);
    }

    @Test
    void test_5_05_RESPONSE_CODE_PROXING_NOT_SUPPORTED() {
        test(5, 5, Message.RESPONSE_CODE_PROXING_NOT_SUPPORTED);
    }
}
