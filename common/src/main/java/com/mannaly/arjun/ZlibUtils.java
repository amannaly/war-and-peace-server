package com.mannaly.arjun;

public class ZlibUtils {

    public static int getUpperBoundForZlibOutput(int inputLength) {
        // ripped out straight from zlib source code.
        // https://github.com/madler/zlib/blob/50893291621658f355bc5b4d450a8d06a563053d/deflate.c#L566
        // we use raw deflate without any wrapper which gives this number.
        return inputLength + ((inputLength + 7) >> 3) + ((inputLength + 63) >> 6) + 5;
    }
}
