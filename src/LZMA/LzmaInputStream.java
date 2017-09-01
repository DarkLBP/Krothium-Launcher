//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package LZMA;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LzmaInputStream extends FilterInputStream {
    boolean isClosed = false;
    CRangeDecoder RangeDecoder;
    byte[] dictionary;
    int dictionarySize;
    int dictionaryPos;
    int GlobalPos;
    int rep0;
    int rep1;
    int rep2;
    int rep3;
    int lc;
    int lp;
    int pb;
    int State;
    boolean PreviousIsMatch;
    int RemainLen;
    int[] probs;
    byte[] uncompressed_buffer;
    int uncompressed_size;
    int uncompressed_offset;
    long GlobalNowPos;
    long GlobalOutSize;
    static final int LZMA_BASE_SIZE = 1846;
    static final int LZMA_LIT_SIZE = 768;
    static final int kBlockSize = 65536;
    static final int kNumStates = 12;
    static final int kStartPosModelIndex = 4;
    static final int kEndPosModelIndex = 14;
    static final int kNumFullDistances = 128;
    static final int kNumPosSlotBits = 6;
    static final int kNumLenToPosStates = 4;
    static final int kNumAlignBits = 4;
    static final int kAlignTableSize = 16;
    static final int kMatchMinLen = 2;
    static final int IsMatch = 0;
    static final int IsRep = 192;
    static final int IsRepG0 = 204;
    static final int IsRepG1 = 216;
    static final int IsRepG2 = 228;
    static final int IsRep0Long = 240;
    static final int PosSlot = 432;
    static final int SpecPos = 688;
    static final int Align = 802;
    static final int LenCoder = 818;
    static final int RepLenCoder = 1332;
    static final int Literal = 1846;

    public LzmaInputStream(InputStream var1) throws IOException {
        super(var1);
        this.readHeader();
        this.fill_buffer();
    }

    private void LzmaDecode(int var1) throws IOException {
        int var3 = (1 << this.pb) - 1;
        int var4 = (1 << this.lp) - 1;
        this.uncompressed_size = 0;
        if (this.RemainLen != -1) {
            int var5;
            for(; this.RemainLen > 0 && this.uncompressed_size < var1; --this.RemainLen) {
                var5 = this.dictionaryPos - this.rep0;
                if (var5 < 0) {
                    var5 += this.dictionarySize;
                }

                this.uncompressed_buffer[this.uncompressed_size++] = this.dictionary[this.dictionaryPos] = this.dictionary[var5];
                if (++this.dictionaryPos == this.dictionarySize) {
                    this.dictionaryPos = 0;
                }
            }

            byte var2;
            if (this.dictionaryPos == 0) {
                var2 = this.dictionary[this.dictionarySize - 1];
            } else {
                var2 = this.dictionary[this.dictionaryPos - 1];
            }

            while(this.uncompressed_size < var1) {
                var5 = this.uncompressed_size + this.GlobalPos & var3;
                int var6;
                int var7;
                if (this.RangeDecoder.BitDecode(this.probs, 0 + (this.State << 4) + var5) == 0) {
                    var6 = 1846 + 768 * (((this.uncompressed_size + this.GlobalPos & var4) << this.lc) + ((var2 & 255) >> 8 - this.lc));
                    if (this.State < 4) {
                        this.State = 0;
                    } else if (this.State < 10) {
                        this.State -= 3;
                    } else {
                        this.State -= 6;
                    }

                    if (this.PreviousIsMatch) {
                        var7 = this.dictionaryPos - this.rep0;
                        if (var7 < 0) {
                            var7 += this.dictionarySize;
                        }

                        byte var8 = this.dictionary[var7];
                        var2 = this.RangeDecoder.LzmaLiteralDecodeMatch(this.probs, var6, var8);
                        this.PreviousIsMatch = false;
                    } else {
                        var2 = this.RangeDecoder.LzmaLiteralDecode(this.probs, var6);
                    }

                    this.uncompressed_buffer[this.uncompressed_size++] = var2;
                    this.dictionary[this.dictionaryPos] = var2;
                    if (++this.dictionaryPos == this.dictionarySize) {
                        this.dictionaryPos = 0;
                    }
                } else {
                    this.PreviousIsMatch = true;
                    if (this.RangeDecoder.BitDecode(this.probs, 192 + this.State) == 1) {
                        if (this.RangeDecoder.BitDecode(this.probs, 204 + this.State) == 0) {
                            if (this.RangeDecoder.BitDecode(this.probs, 240 + (this.State << 4) + var5) == 0) {
                                if (this.uncompressed_size + this.GlobalPos == 0) {
                                    throw new LzmaException("LZMA : Data Error");
                                }

                                this.State = this.State < 7 ? 9 : 11;
                                var6 = this.dictionaryPos - this.rep0;
                                if (var6 < 0) {
                                    var6 += this.dictionarySize;
                                }

                                var2 = this.dictionary[var6];
                                this.dictionary[this.dictionaryPos] = var2;
                                if (++this.dictionaryPos == this.dictionarySize) {
                                    this.dictionaryPos = 0;
                                }

                                this.uncompressed_buffer[this.uncompressed_size++] = var2;
                                continue;
                            }
                        } else {
                            if (this.RangeDecoder.BitDecode(this.probs, 216 + this.State) == 0) {
                                var6 = this.rep1;
                            } else {
                                if (this.RangeDecoder.BitDecode(this.probs, 228 + this.State) == 0) {
                                    var6 = this.rep2;
                                } else {
                                    var6 = this.rep3;
                                    this.rep3 = this.rep2;
                                }

                                this.rep2 = this.rep1;
                            }

                            this.rep1 = this.rep0;
                            this.rep0 = var6;
                        }

                        this.RemainLen = this.RangeDecoder.LzmaLenDecode(this.probs, 1332, var5);
                        this.State = this.State < 7 ? 8 : 11;
                    } else {
                        this.rep3 = this.rep2;
                        this.rep2 = this.rep1;
                        this.rep1 = this.rep0;
                        this.State = this.State < 7 ? 7 : 10;
                        this.RemainLen = this.RangeDecoder.LzmaLenDecode(this.probs, 818, var5);
                        var6 = this.RangeDecoder.BitTreeDecode(this.probs, 432 + ((this.RemainLen < 4 ? this.RemainLen : 3) << 6), 6);
                        if (var6 >= 4) {
                            var7 = (var6 >> 1) - 1;
                            this.rep0 = (2 | var6 & 1) << var7;
                            if (var6 < 14) {
                                this.rep0 += this.RangeDecoder.ReverseBitTreeDecode(this.probs, 688 + this.rep0 - var6 - 1, var7);
                            } else {
                                this.rep0 += this.RangeDecoder.DecodeDirectBits(var7 - 4) << 4;
                                this.rep0 += this.RangeDecoder.ReverseBitTreeDecode(this.probs, 802, 4);
                            }
                        } else {
                            this.rep0 = var6;
                        }

                        ++this.rep0;
                    }

                    if (this.rep0 == 0) {
                        this.RemainLen = -1;
                        break;
                    }

                    if (this.rep0 > this.uncompressed_size + this.GlobalPos) {
                        throw new LzmaException("LZMA : Data Error");
                    }

                    this.RemainLen += 2;

                    while(true) {
                        var6 = this.dictionaryPos - this.rep0;
                        if (var6 < 0) {
                            var6 += this.dictionarySize;
                        }

                        var2 = this.dictionary[var6];
                        this.dictionary[this.dictionaryPos] = var2;
                        if (++this.dictionaryPos == this.dictionarySize) {
                            this.dictionaryPos = 0;
                        }

                        this.uncompressed_buffer[this.uncompressed_size++] = var2;
                        --this.RemainLen;
                        if (this.RemainLen <= 0 || this.uncompressed_size >= var1) {
                            break;
                        }
                    }
                }
            }

            this.GlobalPos += this.uncompressed_size;
        }
    }

    private void fill_buffer() throws IOException {
        if (this.GlobalNowPos < this.GlobalOutSize) {
            this.uncompressed_offset = 0;
            long var1 = this.GlobalOutSize - this.GlobalNowPos;
            int var3;
            if (var1 > 65536L) {
                var3 = 65536;
            } else {
                var3 = (int)var1;
            }

            this.LzmaDecode(var3);
            if (this.uncompressed_size == 0) {
                this.GlobalOutSize = this.GlobalNowPos;
            } else {
                this.GlobalNowPos += (long)this.uncompressed_size;
            }
        }

    }

    private void readHeader() throws IOException {
        byte[] var1 = new byte[5];
        if (5 != this.in.read(var1)) {
            throw new LzmaException("LZMA header corrupted : Properties error");
        } else {
            this.GlobalOutSize = 0L;

            int var2;
            int var3;
            for(var2 = 0; var2 < 8; ++var2) {
                var3 = this.in.read();
                if (var3 == -1) {
                    throw new LzmaException("LZMA header corrupted : Size error");
                }

                this.GlobalOutSize += (long)var3 << var2 * 8;
            }

            if (this.GlobalOutSize == -1L) {
                this.GlobalOutSize = 9223372036854775807L;
            }

            var2 = var1[0] & 255;
            if (var2 >= 225) {
                throw new LzmaException("LZMA header corrupted : Properties error");
            } else {
                for(this.pb = 0; var2 >= 45; var2 -= 45) {
                    ++this.pb;
                }

                for(this.lp = 0; var2 >= 9; var2 -= 9) {
                    ++this.lp;
                }

                this.lc = var2;
                var3 = 1846 + (768 << this.lc + this.lp);
                this.probs = new int[var3];
                this.dictionarySize = 0;

                int var4;
                for(var4 = 0; var4 < 4; ++var4) {
                    this.dictionarySize += (var1[1 + var4] & 255) << var4 * 8;
                }

                this.dictionary = new byte[this.dictionarySize];
                if (this.dictionary == null) {
                    throw new LzmaException("LZMA : can't allocate");
                } else {
                    var4 = 1846 + (768 << this.lc + this.lp);
                    this.RangeDecoder = new CRangeDecoder(this.in);
                    this.dictionaryPos = 0;
                    this.GlobalPos = 0;
                    this.rep0 = this.rep1 = this.rep2 = this.rep3 = 1;
                    this.State = 0;
                    this.PreviousIsMatch = false;
                    this.RemainLen = 0;
                    this.dictionary[this.dictionarySize - 1] = 0;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        this.probs[var5] = 1024;
                    }

                    this.uncompressed_buffer = new byte[65536];
                    this.uncompressed_size = 0;
                    this.uncompressed_offset = 0;
                    this.GlobalNowPos = 0L;
                }
            }
        }
    }

    public int read(byte[] var1, int var2, int var3) throws IOException {
        if (this.isClosed) {
            throw new IOException("stream closed");
        } else if ((var2 | var3 | var2 + var3 | var1.length - (var2 + var3)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (var3 == 0) {
            return 0;
        } else {
            if (this.uncompressed_offset == this.uncompressed_size) {
                this.fill_buffer();
            }

            if (this.uncompressed_offset == this.uncompressed_size) {
                return -1;
            } else {
                int var4 = Math.min(var3, this.uncompressed_size - this.uncompressed_offset);
                System.arraycopy(this.uncompressed_buffer, this.uncompressed_offset, var1, var2, var4);
                this.uncompressed_offset += var4;
                return var4;
            }
        }
    }

    public void close() throws IOException {
        this.isClosed = true;
        super.close();
    }
}
