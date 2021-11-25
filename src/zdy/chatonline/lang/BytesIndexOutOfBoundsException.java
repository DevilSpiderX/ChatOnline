package zdy.chatonline.lang;

/**
 * 抛出异常来指示索引为负或大于字节串的大小。
 * 对于某些方法（如byteAt方法），当索引等于字节串的大小时，也会抛出此异常。
 *
 * @author <a target="_blank" href="https://github.com/devilspiderx">devilspiderx</a>
 */
public class BytesIndexOutOfBoundsException extends IndexOutOfBoundsException {

    public BytesIndexOutOfBoundsException() {
        super();
    }

    public BytesIndexOutOfBoundsException(String s) {
        super(s);
    }

    public BytesIndexOutOfBoundsException(int index) {
        super("Bytes index out of range: " + index);
    }
}
