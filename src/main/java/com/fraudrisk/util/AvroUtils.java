package com.fraudrisk.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * Utility class for handling Avro specific type conversions
 */
public class AvroUtils {

    /**
     * Converts a BigDecimal to ByteBuffer for Avro logical decimal type
     *
     * @param decimal The BigDecimal to convert
     * @return ByteBuffer representation
     */
    public static ByteBuffer bigDecimalToBytes(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }

        byte[] unscaledBytes = decimal.unscaledValue().toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(unscaledBytes.length);
        buffer.put(unscaledBytes);

        // Important: rewind the buffer so it can be read from the beginning
        buffer.rewind();

        return buffer;
    }

    /**
     * Converts a ByteBuffer from Avro to BigDecimal
     *
     * @param bytes The ByteBuffer to convert
     * @param scale The scale of the decimal
     * @return BigDecimal representation
     */
    public static BigDecimal bytesToBigDecimal(ByteBuffer bytes, int scale) {
        if (bytes == null) {
            return null;
        }

        // Make sure we don't modify the original buffer's position
        ByteBuffer duplicate = bytes.duplicate();
        duplicate.rewind();

        byte[] array = new byte[duplicate.remaining()];
        duplicate.get(array);

        return new BigDecimal(new java.math.BigInteger(array), scale);
    }

    /**
     * Method to handle any type of object that might come from an Avro decimal type
     * and convert it to a BigDecimal
     *
     * @param value Object that might be a decimal value
     * @param scale The scale of the decimal
     * @return BigDecimal representation
     */
    public static BigDecimal toDecimal(Object value, int scale) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof ByteBuffer) {
            return bytesToBigDecimal((ByteBuffer) value, scale);
        } else if (value instanceof Number) {
            // For other numeric types
            return new BigDecimal(value.toString());
        } else {
            // Try string conversion as last resort
            try {
                return new BigDecimal(value.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to convert " + value + " to BigDecimal", e);
            }
        }
    }
}