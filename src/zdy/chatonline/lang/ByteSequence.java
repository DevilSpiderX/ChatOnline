package zdy.chatonline.lang;

/**
 * ByteSequence是byte值的可读序列。
 * 该界面提供统一的，只读访问许多不同类型的byte序列。
 * byte值代表一个字节。
 *
 * @author <a target="_blank" href="https://github.com/devilspiderx">devilspiderx</a>
 */
public interface ByteSequence {
    /**
     * 返回此字节序列的长度。 长度是序列中的16位<code>byte</code>s的数量。
     *
     * @return 在这个序列中<code>byte</code>s的数量
     */
    int length();

    /**
     * 返回<code>byte</code>s指定索引处的值。
     * 索引范围从零到<tt>length() - 1</tt>。
     * 序列的第一个<code>byte</code>值在索引为零，下一个索引为1，依此类推，就像数组索引一样。
     *
     * @param index 要返回的<code>byte</code>值的索引
     * @return 指定的<code>byte</code>值
     */
    byte byteAt(int index);

    /**
     * 返回一个ByteSequence ，这是这个序列的一个子序列。
     * 子序列以指定索引的<code>byte</code>值开始，以索引end - 1的<code>byte</code>值结束。
     * 返回序列的长度（<code>byte</code>s）为<tt>end - start</tt>，因此如果<tt>start == end</tt>则返回一个空序列。
     *
     * @param start 起始索引，包含
     * @param end   结束索引，不包含
     * @return 指定的子序列
     */
    ByteSequence subSequence(int start, int end);

    /**
     * 以与此顺序相同的顺序返回包含此序列中的字节的字节串。
     * 字节串的长度将是此序列的长度。
     *
     * @return 一个由这个字节序列组成的字节串
     */
    Bytes toBytes();
}

