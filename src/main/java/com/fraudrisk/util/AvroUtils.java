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
        return ByteBuffer.wrap(decimal.unscaledValue().toByteArray());
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

        bytes.rewind();
        byte[] array = new byte[bytes.remaining()];
        bytes.get(array);

        return new BigDecimal(new java.math.BigInteger(array), scale);
    }
}