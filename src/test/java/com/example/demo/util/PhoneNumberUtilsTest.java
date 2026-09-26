package com.example.demo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberUtilsTest {

    @Test
    void testFormatTenDigitsPhoneNumber() {
        String input = "0716755350";
        String expected = "071 675 5350";
        assertEquals(expected, PhoneNumberUtils.formatPhoneNumber(input));
    }

    @Test
    void testFormatNineDigitsPhoneNumber() {
        String input = "012345678";
        String expected = "012 345 678";
        assertEquals(expected, PhoneNumberUtils.formatPhoneNumber(input));
    }

    @Test
    void testFormatAlreadyFormattedPhoneNumber() {
        String input = "071 675 5350";
        String expected = "071 675 5350";
        assertEquals(expected, PhoneNumberUtils.formatPhoneNumber(input));
    }

    @Test
    void testFormatWithDashes() {
        String input = "071-675-5350";
        String expected = "071 675 5350";
        assertEquals(expected, PhoneNumberUtils.formatPhoneNumber(input));
    }

    @Test
    void testNullAndBlankInput() {
        assertNull(PhoneNumberUtils.formatPhoneNumber(null));
        assertEquals("", PhoneNumberUtils.formatPhoneNumber(""));
        assertEquals("   ", PhoneNumberUtils.formatPhoneNumber("   "));
    }
}
