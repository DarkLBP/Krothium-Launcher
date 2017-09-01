//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package LZMA;

import java.io.IOException;
import java.io.InputStream;

class CRangeDecoder {
    static final int kNumTopBits = 24;
    static final int kTopValue = 16777216;
    static final int kTopValueMask = -16777216;
    static final int kNumBitModelTotalBits = 11;
    static final int kBitModelTotal = 2048;
    static final int kNumMoveBits = 5;
    InputStream inStream;
    int Range;
    int Code;
    byte[] buffer = new byte[16384];
    int buffer_size;
    int buffer_ind;
    static final int kNumPosBitsMax = 4;
    static final int kNumPosStatesMax = 16;
    static final int kLenNumLowBits = 3;
    static final int kLenNumLowSymbols = 8;
    static final int kLenNumMidBits = 3;
    static final int kLenNumMidSymbols = 8;
    static final int kLenNumHighBits = 8;
    static final int kLenNumHighSymbols = 256;
    static final int LenChoice = 0;
    static final int LenChoice2 = 1;
    static final int LenLow = 2;
    static final int LenMid = 130;
    static final int LenHigh = 258;
    static final int kNumLenProbs = 514;

    CRangeDecoder(InputStream var1) throws IOException {
        this.inStream = var1;
        this.Code = 0;
        this.Range = -1;

        for(int var2 = 0; var2 < 5; ++var2) {
            this.Code = this.Code << 8 | this.Readbyte();
        }

    }

    int Readbyte() throws IOException {
        if (this.buffer_size == this.buffer_ind) {
            this.buffer_size = this.inStream.read(this.buffer);
            this.buffer_ind = 0;
            if (this.buffer_size < 1) {
                throw new LzmaException("LZMA : Data Error");
            }
        }

        return this.buffer[this.buffer_ind++] & 255;
    }

    int DecodeDirectBits(int var1) throws IOException {
        int var2 = 0;

        for(int var3 = var1; var3 > 0; --var3) {
            this.Range >>>= 1;
            int var4 = this.Code - this.Range >>> 31;
            this.Code -= this.Range & var4 - 1;
            var2 = var2 << 1 | 1 - var4;
            if (this.Range < 16777216) {
                this.Code = this.Code << 8 | this.Readbyte();
                this.Range <<= 8;
            }
        }

        return var2;
    }

    int BitDecode(int[] var1, int var2) throws IOException {
        int var3 = (this.Range >>> 11) * var1[var2];
        if (((long)this.Code & 4294967295L) < ((long)var3 & 4294967295L)) {
            this.Range = var3;
            var1[var2] += 2048 - var1[var2] >>> 5;
            if ((this.Range & -16777216) == 0) {
                this.Code = this.Code << 8 | this.Readbyte();
                this.Range <<= 8;
            }

            return 0;
        } else {
            this.Range -= var3;
            this.Code -= var3;
            var1[var2] -= var1[var2] >>> 5;
            if ((this.Range & -16777216) == 0) {
                this.Code = this.Code << 8 | this.Readbyte();
                this.Range <<= 8;
            }

            return 1;
        }
    }

    int BitTreeDecode(int[] var1, int var2, int var3) throws IOException {
        int var4 = 1;

        for(int var5 = var3; var5 > 0; --var5) {
            var4 = var4 + var4 + this.BitDecode(var1, var2 + var4);
        }

        return var4 - (1 << var3);
    }

    int ReverseBitTreeDecode(int[] var1, int var2, int var3) throws IOException {
        int var4 = 1;
        int var5 = 0;

        for(int var6 = 0; var6 < var3; ++var6) {
            int var7 = this.BitDecode(var1, var2 + var4);
            var4 = var4 + var4 + var7;
            var5 |= var7 << var6;
        }

        return var5;
    }

    byte LzmaLiteralDecode(int[] var1, int var2) throws IOException {
        int var3 = 1;

        do {
            var3 = var3 + var3 | this.BitDecode(var1, var2 + var3);
        } while(var3 < 256);

        return (byte)var3;
    }

    byte LzmaLiteralDecodeMatch(int[] var1, int var2, byte var3) throws IOException {
        int var4 = 1;

        do {
            int var5 = var3 >> 7 & 1;
            var3 = (byte)(var3 << 1);
            int var6 = this.BitDecode(var1, var2 + (1 + var5 << 8) + var4);
            var4 = var4 << 1 | var6;
            if (var5 != var6) {
                while(var4 < 256) {
                    var4 = var4 + var4 | this.BitDecode(var1, var2 + var4);
                }

                return (byte)var4;
            }
        } while(var4 < 256);

        return (byte)var4;
    }

    int LzmaLenDecode(int[] var1, int var2, int var3) throws IOException {
        if (this.BitDecode(var1, var2 + 0) == 0) {
            return this.BitTreeDecode(var1, var2 + 2 + (var3 << 3), 3);
        } else {
            return this.BitDecode(var1, var2 + 1) == 0 ? 8 + this.BitTreeDecode(var1, var2 + 130 + (var3 << 3), 3) : 16 + this.BitTreeDecode(var1, var2 + 258, 8);
        }
    }
}
