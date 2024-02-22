package cipher;

/**
 * Created by njoy on 2015-06-10.
 */
public class AnsiX923Padding extends BlockPadding {
    private static final char PADDING_VALUE = 0x00;

    @Override
    public String getPaddingType() {
        return ANSIX;
    }

    @Override
    public byte[] addPadding(byte[] source, int blockSize) {

        int paddingCount = blockSize - (source.length % blockSize);
        if( paddingCount == 0 || paddingCount == blockSize ) {
            return source;
        }

        byte[] buffer = new byte[ source.length + paddingCount ];
        System.arraycopy(source, 0, buffer, 0, source.length);
        buffer[buffer.length-1] = (byte)paddingCount;
        for( int i = 0; i < (paddingCount-1); i++ ) {
            buffer[source.length + i] = PADDING_VALUE;
        }

        return buffer;
    }

    @Override
    public byte[] removePadding(byte[] source, int blockSize) {

        int paddingCount = source[source.length-1];
        if( paddingCount >= (blockSize-1) ) {
            return source;
        }

        int zeroPaddingCount = paddingCount - 1;
        for( int i = 2; i < (zeroPaddingCount+2); i++ ) {
            if( source[source.length - i] != PADDING_VALUE ) {
                return source;
            }
        }

        if( source.length % blockSize == 0 ) {
            if( paddingCount < 0 ) {
                return source;
            }
        }

        byte[] buffer = new byte[source.length-paddingCount];
        System.arraycopy(source, 0, buffer, 0, buffer.length);
        return buffer;
    }
}