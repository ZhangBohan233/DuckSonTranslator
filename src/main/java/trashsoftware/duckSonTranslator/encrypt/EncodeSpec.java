package trashsoftware.duckSonTranslator.encrypt;

public class EncodeSpec {
    
    private final int inChunkByteLength;
    private final int outChunkByteLength;
    private final int chunkCharCount;
    
    public EncodeSpec(RSAKey key) {
        int maxUnitBits = key.getN().bitLength();

        int chunkBits = (maxUnitBits - 1) / 24 * 24;
        inChunkByteLength = chunkBits >> 3;
        int outChunkBits = maxUnitBits / 24 * 24;
        if (outChunkBits != maxUnitBits) outChunkBits += 24;
        outChunkByteLength = outChunkBits >> 3;

        chunkCharCount = outChunkByteLength * 8 / 6;
    }

    public int getInChunkByteLength() {
        return inChunkByteLength;
    }

    public int getOutChunkByteLength() {
        return outChunkByteLength;
    }

    public int getChunkCharCount() {
        return chunkCharCount;
    }

    @Override
    public String toString() {
        return "EncodeSpec{" +
                "inChunkByteLength=" + inChunkByteLength +
                ", outChunkByteLength=" + outChunkByteLength +
                '}';
    }
}
