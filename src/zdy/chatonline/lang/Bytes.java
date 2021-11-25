package zdy.chatonline.lang;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 字节串类
 *
 * @author <a target="_blank" href="https://github.com/devilspiderx">devilspiderx</a>
 */
public class Bytes implements java.io.Serializable, Comparable<Bytes>, ByteSequence {
    /**
     * 字节容器
     */
    private byte[] value;
    /**
     * 哈希值
     */
    private int hash;
    /**
     * 字节串长度
     */
    private int count;

    /**
     * 构造一个没有字节的字节串，初始容量为16个字节。
     */
    public Bytes() {
        value = new byte[16];
    }

    /**
     * 构造一个没有字节的字节串，由<code>capacity</code>参数指定的初始容量。
     *
     * @param capacity 指定的初始容量
     */
    public Bytes(int capacity) {
        value = new byte[capacity];
    }

    /**
     * 构建一个与<code>value</code>值相同的字节串。
     *
     * @param value 字节串的初始内容
     */
    public Bytes(byte[] value) {
        this.value = Arrays.copyOf(value, value.length);
        count = value.length;
    }

    /**
     * 构建一个初始内容为<code>value[offset:len-offset]</code>的字节串。
     *
     * @param value  含有初始内容的字节数组
     * @param offset 偏移量
     * @param len    字节串初始内容的长度
     * @throws BytesIndexOutOfBoundsException 如果offset &lt; 0或len &lt; 0或offset + len &gt; value.length
     */
    public Bytes(byte[] value, int offset, int len) {
        if (offset < 0) {
            throw new BytesIndexOutOfBoundsException(offset);
        }
        if (len <= 0) {
            if (len < 0) {
                throw new BytesIndexOutOfBoundsException(len);
            }
            if (offset <= value.length) {
                this.value = new Bytes().value;
                return;
            }
        }
        if (offset + len > value.length) {
            throw new BytesIndexOutOfBoundsException(offset + len);
        }
        this.value = Arrays.copyOfRange(value, offset, offset + len);
        count = len;
    }

    /**
     * 构建一个与<code>seq</code>内容相同的字节串。
     *
     * @param seq 要复制的字节序列
     */
    public Bytes(ByteSequence seq) {
        value = new byte[seq.length() + 16];
        append(seq);
    }

    /**
     * 通过使用平台的默认字符集编码指定的字符串来构造新的字节串。
     * 新的<code>Bytes</code>的长度是字符集的函数，因此可能不等于字符串的长度。
     * 指定字节在默认字符集中无效时，此构造函数的行为是未指定的。
     * 当需要对编码过程进行更多的控制时，应使用{@link java.nio.charset.CharsetEncoder}类。
     *
     * @param string 要编码的字符串
     */
    public Bytes(String string) {
        value = string.getBytes();
        count = value.length;
    }

    /**
     * 通过使用指定名称<code>charsetName</code>的字符集编码指定的字符串来构造新的字节串。
     * 新的<code>Bytes</code>的长度是字符集的函数，因此可能不等于字符串的长度。
     * 指定字节在默认字符集中无效时，此构造函数的行为是未指定的。
     * 当需要对编码过程进行更多的控制时，应使用{@link java.nio.charset.CharsetEncoder}类。
     *
     * @param string      要编码的字符串
     * @param charsetName 指定字符集名称
     * @throws UnsupportedEncodingException 如果是不支持的字符集的名称
     */
    public Bytes(String string, String charsetName) throws UnsupportedEncodingException {
        value = string.getBytes(charsetName);
        count = value.length;
    }

    /**
     * 通过使用指定字符集{@linkplain Charset charset}编码指定的字符串来构造新的字节串。
     * 新的<code>Bytes</code>的长度是字符集的函数，因此可能不等于字符串的长度。
     * 指定字节在默认字符集中无效时，此构造函数的行为是未指定的。
     * 当需要对编码过程进行更多的控制时，应使用{@link java.nio.charset.CharsetEncoder}类。
     *
     * @param string  要编码的字符串
     * @param charset 该{@linkplain Charset charset}被用于编码字符串
     */
    public Bytes(String string, Charset charset) {
        value = string.getBytes(charset);
        count = value.length;
    }

    /**
     * 返回此字节串的哈希码。
     * <code>Bytes</code>对象的哈希代码计算为
     * <blockquote><pre>
     * b[0]*31^(n-1) + b[1]*31^(n-2) + ... + b[n-1]
     * </pre></blockquote>
     * 使用<code>int</code>算术，其中<code>b[i]</code>是字节串的第i个字节，<code>n</code>是字节串的长度，<code>^</code>表示取幂。
     * （空字节串的哈希值为零）
     *
     * @return 该对象的哈希码值
     */
    public int hashCode() {
        int h = hash;
        if (h == 0 && count > 0) {
            for (int i = 0; i < count; i++) {
                h = 31 * h + value[i];
            }
            hash = h;
        }
        return h;
    }


    /**
     * 返回此字节串的长度
     *
     * @return 由该对象表示的字节序列的长度
     */
    @Override
    public int length() {
        return count;
    }

    /**
     * 返回当前容量。
     * 容量是新插入字节可用的存储量，超过此值将进行分配。
     *
     * @return 当前的容量
     */
    public int capacity() {
        return value.length;
    }

    /**
     * 返回<code>byte</code>s指定索引处的值。
     * 索引范围从零到<code>length() - 1</code>。
     * 序列的第一个<code>byte</code>值在索引为零，下一个索引为1，依此类推，就像数组索引一样。
     *
     * @param index 要返回的<code>byte</code>值的索引
     * @return 指定的<code>byte</code>值
     * @throws BytesIndexOutOfBoundsException 如果index &lt; 0或index &gt;= count
     */
    @Override
    public byte byteAt(int index) {
        if ((index < 0) || (index >= count)) {
            throw new BytesIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    /**
     * 返回此对象（已经是字节串！）本身。
     *
     * @return 字节串本身
     */
    @Override
    public Bytes toBytes() {
        return this;
    }

    /**
     * 返回一个<code>ByteSequence</code>，这是这个序列的一个子序列。
     * 子序列以指定索引的<code>byte</code>值开始，以索引<code>end - 1</code>的<code>byte</code>值结束。
     * 返回序列的长度（<code>byte</code>s）为<code>end - start</code>，因此如果<code>start == end</code>则返回一个空序列。
     *
     * @param beginIndex 起始索引，包含
     * @param endIndex   结束索引，不包含
     * @return 指定的子序列
     */
    @Override
    public ByteSequence subSequence(int beginIndex, int endIndex) {
        return subBytes(beginIndex, endIndex);
    }

    /**
     * 返回一个字节串，该字节串是此字节串的子字节串。
     * 子字节串以指定索引处的字节开头，并扩展到该字节串的末尾。
     *
     * @param beginIndex 起始索引，包含
     * @return 指定的子字节串
     * @throws BytesIndexOutOfBoundsException 如果beginIndex &lt; 0或length() &lt; beginIndex
     */
    public Bytes subBytes(int beginIndex) {
        if (beginIndex < 0) {
            throw new BytesIndexOutOfBoundsException(beginIndex);
        }
        int subLen = count - beginIndex;
        if (subLen < 0) {
            throw new BytesIndexOutOfBoundsException(subLen);
        }
        return (beginIndex == 0) ? this : new Bytes(value, beginIndex, subLen);
    }


    /**
     * 返回一个字节串，该字节串是此字节串的子字节串。
     * 子串开始于指定<code>beginIndex</code>并延伸到字节索引<code>endIndex - 1</code>。
     * 因此，子串的长度为<code>endIndex-beginIndex</code>。
     *
     * @param beginIndex 起始索引，包含
     * @param endIndex   结束索引，不包含
     * @return 指定的子字节串
     * @throws BytesIndexOutOfBoundsException 如果beginIndex &lt; 0或endIndex &gt; length()或endIndex &lt; beginIndex
     */
    public Bytes subBytes(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new BytesIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > count) {
            throw new BytesIndexOutOfBoundsException(endIndex);
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new BytesIndexOutOfBoundsException(subLen);
        }
        return ((beginIndex == 0) && (endIndex == count)) ?
                this : new Bytes(value, beginIndex, subLen);
    }

    /**
     * 返回<code>true</code>，且仅当<code>length()</code>为<code>0</code>的时候。
     *
     * @return <code>true</code>如果<code>length()</code>是<code>0</code>，否则<code>false</code>
     */
    public boolean isEmpty() {
        return length() == 0;
    }

    private void ensureCapacityInternal(int minimumCapacity) {
        if (minimumCapacity - value.length > 0) {
            value = Arrays.copyOf(value, newCapacity(minimumCapacity));
        }
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private int newCapacity(int minCapacity) {
        int newCapacity = (value.length << 1) + 2;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        return (newCapacity <= 0 || MAX_ARRAY_SIZE - newCapacity < 0) ? hugeCapacity(minCapacity) : newCapacity;
    }

    private int hugeCapacity(int minCapacity) {
        if (Integer.MAX_VALUE - minCapacity < 0) {
            throw new OutOfMemoryError();
        }
        return Math.max(minCapacity, MAX_ARRAY_SIZE);
    }

    /**
     * 尝试减少用于字节序列的存储。
     * 如果缓冲区大于保持其当前字节序列所需的缓冲区，则可以将其调整大小以变得更加空间有效。
     * 调用此方法可能但不是要求影响后续调用{@link #capacity()}方法返回的值。
     */
    public void trimToSize() {
        if (count < value.length) {
            value = Arrays.copyOf(value, count);
        }
    }

    /**
     * 设置字节序列的长度。
     * 该序列被更改为一个新的字节序列，其长度由参数指定。
     * 对于小于每非负索引<code>k</code> <code>newLength</code>，在新的字节序列的索引k处的字节是相同的在旧序列索引k如果k小于原字节序列的长度的字节; 否则为0。
     * 换句话说，如果<code>newLength</code>参数小于当前长度，则长度将更改为指定的长度。
     * 如果<code>newLength</code>参数大于或等于当前长度，则会附加足够的0字节，以使长度成为<code>newLength</code>参数。
     * <code>newLength</code>参数必须大于或等于0。
     *
     * @param newLength 新的长度
     * @throws BytesIndexOutOfBoundsException 如果newLength &lt; 0
     */
    public void setLength(int newLength) {
        if (newLength < 0)
            throw new BytesIndexOutOfBoundsException(newLength);
        ensureCapacityInternal(newLength);

        if (count < newLength) {
            Arrays.fill(value, count, newLength, (byte) 0);
        }

        count = newLength;
    }

    /**
     * 将指定的字节串附加到此字节序列。
     * 附加<code>Bytes</code>参数的字节，以便<code>Bytes</code>参数的长度增加此序列的长度。
     * 如果<code>bytes</code>是<code>null</code>，则它将不附加到该附录。
     * 令n是在执行<code>append</code>方法之前的这个字节序列的长度。
     * 那么如果k小于n则新字节序列中索引k处的字节等于旧字节序列中索引k处的字节; 否则，它等于参数<code>bytes</code>中的索引kn处的<code>bytes</code>。
     *
     * @param bytes 一个字节串
     * @return 对这个对象的引用
     */
    public Bytes append(Bytes bytes) {
        if (bytes == null)
            return appendNull();
        int len = bytes.length();
        ensureCapacityInternal(count + len);
        bytes.getBytes(0, len, value, count);
        count += len;
        return this;
    }

    /**
     * 将指定的字节序列追加到此。
     *
     * @param seq 要追加的字节序列。
     *            如果bsq是null，那么它将不附加到该附录。
     * @return 对这个对象的引用
     */
    public Bytes append(ByteSequence seq) {
        if (seq == null)
            return appendNull();
        if (seq instanceof Bytes)
            return this.append((Bytes) seq);

        return this.append(seq, 0, seq.length());
    }

    private Bytes appendNull() {
        return this;
    }

    /**
     * 将指定的子序列<code>ByteSequence</code>这个序列。
     * 从索引<code>start</code>开始的参数b按顺序附加到该序列的内容，直到（独占）索引<code>end</code>。
     * 此序列的长度增加的值<code>end - start</code>。
     * 令n是在执行<code>append</code>方法之前这个字节序列的长度。
     * 那么如果k小于n，则该字节序列中的索引k处的字节变得等于该序列中索引k处的字节; 否则，它等于参数b中索引<code>k + start-n</code>处的b 。
     * 如果b是null ，那么这个方法不会附加字节.
     *
     * @param seq   要追加的顺序
     * @param start 要追加的子序列的起始索引，包含
     * @param end   要附加的子序列的结束索引，不包含
     * @return 对这个对象的引用
     * @throws IndexOutOfBoundsException 如果start &lt; 0或start &gt; end或end &gt; seq.length()
     */
    public Bytes append(ByteSequence seq, int start, int end) {
        if (seq == null)
            return appendNull();
        if ((start < 0) || (start > end) || (end > seq.length()))
            throw new IndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", s.length() " + seq.length());
        int len = end - start;
        ensureCapacityInternal(count + len);
        for (int i = start, j = count; i < end; i++, j++)
            value[j] = seq.byteAt(i);
        count += len;
        return this;
    }

    /**
     * 将byte数组参数的字节串表示附加到此序列。
     * 数组参数的字节按顺序附加到此序列的内容。
     * 该序列的长度增加参数的长度。
     *
     * @param bytes 要附加的字节
     * @return 对这个对象的引用
     */
    public Bytes append(byte[] bytes) {
        int len = bytes.length;
        ensureCapacityInternal(count + len);
        System.arraycopy(bytes, 0, value, count, len);
        count += len;
        return this;
    }

    /**
     * 将<code>byte</code>数组参数的子阵列的字节串表示附加到此序列。
     * 从索引<code>offset</code>开始的<code>byte</code>数组<code>bytes</code>按顺序附加到该序列的内容。
     * 此序列的长度由的值增加<code>len</code>。
     *
     * @param bytes  要附加的字节
     * @param offset 第一的指数<code>byte</code>追加
     * @param len    要追加的<code>byte</code>的数量
     * @return 对这个对象的引用
     * @throws IndexOutOfBoundsException 如果 offset &lt; 0或 len &lt; 0或 offset+len &gt; bytes.length
     */
    public Bytes append(byte[] bytes, int offset, int len) {
        if (offset < 0 || len < 0 || offset + len > bytes.length) {
            throw new IndexOutOfBoundsException(
                    "start " + offset + ", end " + (offset + len) + ", b.length() " + bytes.length);
        }
        if (len > 0) {
            ensureCapacityInternal(count + len);
        }
        System.arraycopy(bytes, offset, value, count, len);
        count += len;
        return this;
    }

    /**
     * 删除此序列的子字节串中的字节。
     * 子串开始于指定<code>start</code>并延伸到字节索引<code>end - 1</code>，或如果没有这样的字节存在的序列的结束。
     * 如果<code>start</code>等于<code>end</code> ，则不作任何更改。
     *
     * @param start 起始索引，包含
     * @param end   结束索引，不包含
     * @return 对这个对象的引用
     * @throws BytesIndexOutOfBoundsException 如果start &lt; 0或start &gt; end
     */
    public Bytes delete(int start, int end) {
        if (start < 0)
            throw new BytesIndexOutOfBoundsException(start);
        if (end > count)
            end = count;
        if (start > end)
            throw new BytesIndexOutOfBoundsException();
        int len = end - start;
        if (len > 0) {
            System.arraycopy(value, start + len, value, start, count - end);
            count -= len;
        }
        return this;
    }

    /**
     * 删除<code>byte</code>在这个序列中的指定位置。
     * 这个序列缩短了一个<code>byte</code> 。
     *
     * @param index 要删除的索引
     * @return 对这个对象的引用
     * @throws BytesIndexOutOfBoundsException 如果index &lt; 0或index &gt;= length()
     */
    public Bytes deleteByteAt(int index) {
        if ((index < 0) || (index >= count))
            throw new BytesIndexOutOfBoundsException(index);
        System.arraycopy(value, index + 1, value, index, count - index - 1);
        count--;
        return this;
    }

    /**
     * 将此字节串与指定对象进行比较。
     * 其结果是<code>true</code>当且仅当该参数不是<code>null</code>并且是<code>Bytes</code>对象，表示相同的字节序列作为该对象。
     *
     * @param anObject 对比的<code>Bytes</code>对象
     * @return <code>true</code>如果给定的对象代表一个<code>Bytes</code>等效于这个字节串，否则<code>false</code>
     */
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Bytes) {
            Bytes anotherString = (Bytes) anObject;
            int n = count;
            if (n == anotherString.count) {
//                int i = 0;
//                while (n-- != 0) {
//                    if (value[i] != anotherString.value[i])
//                        return false;
//                    i++;
//                }
                for (int i = 0; i < n; i++) {
                    if (value[i] != anotherString.value[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 按字典顺序比较两个字节串。
     * 比较是基于字节串中每个字节的值。
     * 由该<code>Bytes</code>对象表示的字节序列按字典顺序与由参数字节串表示的字节序列进行比较。
     * 如果<code>Bytes</code>对象按字典顺序先于在参数字节串，结果为负整数。
     * 如果<code>Bytes</code>对象按字典顺序跟随参数字节串，结果为正整数。
     * 如果字节串相等，结果为零;
     * 当<code>equals(Object)</code>方法返回<code>true</code>时，<code>compareTo</code>返回<code>0</code>。
     *
     * @param anotherBytes 要比较的<code>Bytes</code>
     * @return 如果参数字节串等于此字节串，则值为0;如果这个字节串的字典比参数字节串小，则值小于0;如果此字节串的字典大小超过参数字节串，则值大于0
     */
    @Override
    public int compareTo(Bytes anotherBytes) {
        int len1 = count;
        int len2 = anotherBytes.count;
        int min = Math.min(len1, len2);

        for (int k = 0; k < min; k++) {
            byte c1 = value[k];
            byte c2 = anotherBytes.value[k];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }

    /**
     * 从<code>dstBegin</code>开始，将字节串中的字节复制到<code>dst</code>中。
     * 此方法不执行任何范围检查。
     *
     * @param dst      目标数组
     * @param dstBegin 目标数组中的起始偏移量
     */
    void getBytes(byte[] dst, int dstBegin) {
        System.arraycopy(value, 0, dst, dstBegin, count);
    }

    /**
     * 将此字节串中的字节复制到目标字节数组中。
     * 要复制的第一个字节是索引<code>srcBegin</code>;要复制的最后一个字节在索引<code>srcEnd-1</code>
     * （因此要复制的<code>srcEnd-srcBegin</code>总数为<code>srcEnd-srcBegin</code>）。
     * 字节被复制到的子阵列<code>dst</code>开始于索引<code>dstBegin</code>和在索引结束：
     * <blockquote><pre>
     *      dstBegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param srcBegin 要复制的字节串中第一个字节的索引
     * @param srcEnd   要复制的字节串中最后一个字节后面的索引
     * @param dst      目标数组
     * @param dstBegin 目标数组中的起始偏移量
     * @throws BytesIndexOutOfBoundsException 如果srcBegin &lt; 0或srcEnd &gt; length()或srcBegin &gt; srcEnd
     */
    public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
        if (srcBegin < 0) {
            throw new BytesIndexOutOfBoundsException(srcBegin);
        }
        if (srcEnd > count) {
            throw new BytesIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin - srcEnd > 0) {
            throw new BytesIndexOutOfBoundsException(srcEnd - srcBegin);
        }
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    /**
     * 将此字节串转换为新的字节数组。
     *
     * @return 一个新分配的字节数组，其长度是该字节串的长度，其内容被初始化为包含由该字节串表示的字节序列。
     */
    public byte[] toByteArray() {
        byte[] result = new byte[count];
        System.arraycopy(value, 0, result, 0, count);
        return result;
    }

    /**
     * 使用平台的默认字符集将此<code>Bytes</code>解码为字符串，将结果存储到新的字符串中。
     * 当该字节串不能在默认字符集中解码时，此方法的行为是未指定的。
     * 当需要对解码过程进行更多控制时，应使用{@link java.nio.charset.CharsetDecoder}类。
     *
     * @return 结果字符串
     */
    public String getString() {
        return new String(value, 0, count);
    }

    /**
     * 使用命名的字符集将此<code>Bytes</code>解码为字符串，将结果存储到新的字符串中。
     * 当该字节串不能在给定字符集中解码时，此方法的行为是未指定的。
     * 当需要对解码过程进行更多控制时，应使用{@link java.nio.charset.CharsetDecoder}类。
     *
     * @param charsetName 支持的{@linkplain Charset charset}名称
     * @return 结果字符串
     * @throws UnsupportedEncodingException 如果是不支持的字符集的名称
     */
    public String getString(String charsetName) throws UnsupportedEncodingException {
        return new String(value, 0, count, charsetName);
    }

    /**
     * 使用给定的{@linkplain Charset charset}将该<code>Bytes</code>解码为一个字符串，将结果存储到新的字符串中。
     * 此方法总是用此字符集的默认替换字符串替换格式错误的输入和不可映射字节序列。
     * 当需要对解码过程的更多控制时，应使用{@link java.nio.charset.CharsetDecoder}类。
     *
     * @param charset 该{@linkplain Charset charset}被用于解码Bytes
     * @return 结果字符串
     */
    public String getString(Charset charset) {
        return new String(value, 0, count, charset);
    }

    /**
     * 当且仅当此字节串包含指定的byte值序列时，才返回true。
     *
     * @param b 要搜索的byte序列
     * @return 指定子字节串首次出现的索引，从指定索引开始；如果没有出现，则为<code>-1</code>
     */
    public boolean contains(ByteSequence b) {
        return indexOf(b.toBytes()) > -1;
    }

    /**
     * 从指定的索引开始，返回指定子字节串首次出现时在此字节串内的索引。
     * 返回的索引是为其的最小值<i>k</i>
     * <blockquote><pre>
     * this.startsWith(bytes, <i>k</i>)
     * </pre></blockquote>
     * 如果不存在这样的<i>k</i>值，则返回<code>-1</code>。
     *
     * @param bytes 要搜索的子字节串
     * @return 指定子字节串首次出现的索引，从指定索引开始；如果没有出现，则为<code>-1</code>
     */
    public int indexOf(Bytes bytes) {
        return indexOf(bytes, 0);
    }

    /**
     * 从指定的索引开始，返回指定子字节串首次出现时在此字节串内的索引。
     * 返回的索引是为其的最小值<i>k</i>
     * <blockquote><pre>
     * <i>k</i> &gt;= fromIndex {@code &&} this.startsWith(bytes, <i>k</i>)
     * </pre></blockquote>
     * 如果不存在这样的<i>k</i>值，则返回<code>-1</code>。
     *
     * @param bytes     要搜索的子字节串
     * @param fromIndex 从中开始搜索的索引
     * @return 指定子字节串首次出现的索引，从指定索引开始；如果没有出现，则为<code>-1</code>
     */
    public int indexOf(Bytes bytes, int fromIndex) {
        return indexOf(value, 0, count,
                bytes.value, 0, bytes.count, fromIndex);
    }

    /**
     * 源是要搜索的字节数组，目标是要搜索的字节串。
     *
     * @param source       被搜索的字节数组
     * @param sourceOffset 源字节串的偏移量
     * @param sourceCount  源字节串的计数
     * @param target       要搜索的字节串
     * @param fromIndex    开始搜索的索引
     * @return 指定子字节串首次出现的索引，从指定索引开始；如果没有出现，则为<code>-1</code>
     */
    static int indexOf(byte[] source, int sourceOffset, int sourceCount,
                       Bytes target, int fromIndex) {
        return indexOf(source, sourceOffset, sourceCount,
                target.value, 0, target.count, fromIndex);
    }

    /**
     * 源是要搜索的字节数组，目标是要搜索的字节串。
     *
     * @param source       被搜索的字节数组
     * @param sourceOffset 源字节串的偏移量
     * @param sourceCount  源字节串的计数
     * @param target       要搜索的字节数组
     * @param targetOffset 目标字节串的偏移量
     * @param targetCount  目标字节串的计数
     * @param fromIndex    开始搜索的索引
     * @return 指定子字节串首次出现的索引，从指定索引开始；如果没有出现，则为<code>-1</code>
     */
    @SuppressWarnings("StatementWithEmptyBody")
    static int indexOf(byte[] source, int sourceOffset, int sourceCount,
                       byte[] target, int targetOffset, int targetCount,
                       int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        byte first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first byte. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first) ;
            }

            /* Found first byte, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++) ;

                if (j == end) {
                    /* Found whole bytes. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }
}


