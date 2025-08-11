package com.example.lineserver.content.model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents metadata for the memory offsets of the text storage.
 * <p>
 * This is done by holding an internal list of {@link ChunkMetadata} instances. Each chunk holds
 * {@code linesPerChunk} lines. This is configurable for this class's scope.
 */
public class ChunkLineIndex {

    private final int linesPerChunk;
    private final int totalLines;
    private final List<ChunkMetadata> chunks;

    public ChunkLineIndex(List<ChunkMetadata> chunks, int linesPerChunk, int totalLines) {
        this.chunks = chunks;
        this.linesPerChunk = linesPerChunk;
        this.totalLines = totalLines;
        /*this.totalLines = chunks.stream()
                .mapToInt(c -> c.localOffsets().length)
                .sum();*/
    }

    /**
     * Gets a chunk offset metadata for a specific line.
     *
     * @param lineNumber the line number from which to get the specific chunk.
     * @return Chunk offset metadata.
     */
    public ChunkMetadata getChunkForLine(int lineNumber) {
        int chunkIndex = lineNumber / linesPerChunk;
        return chunks.get(chunkIndex);
    }

    /**
     * Gets the total amount of lines from the text file storage.
     *
     * @return the total amount of lines from the file.
     */
    public int getTotalLines() {
        return totalLines;
    }

    /**
     * Builds an instance of {@link ChunkLineIndex} from a buffer with the offset values for each line,
     * and a number of lines per internal chunk to hold.
     *
     * @param offsets a {@link LongBuffer} with each line's offset
     * @param linesPerChunk number of lines per internal chunk (this ties directly to {@link ChunkLineIndex#chunks})
     * @return a new {@link ChunkLineIndex}
     */
    public static ChunkLineIndex buildFromOffsets(LongBuffer offsets, int linesPerChunk) {
        List<ChunkMetadata> chunks = new ArrayList<>();
        int totalLines = offsets.remaining();

        for (int i = 0; i < totalLines; i += linesPerChunk) {
            int end = Math.min(i + linesPerChunk, totalLines);
            long startOffset = offsets.get(i);

            long[] localOffsets = new long[end - i];
            for (int j = 0; j < end - i; j++) {
                localOffsets[j] = offsets.get(i + j) - startOffset;
            }

            chunks.add(new ChunkMetadata(i, startOffset, localOffsets));
        }

        return new ChunkLineIndex(chunks, linesPerChunk, totalLines);
    }

    /**
     * Builds a longbuffer where each long represents a new line position offset.
     *
     * @param filePath The path from where to read the buffer.
     * @return A long buffer.
     * @throws IOException
     * @implSpec We read byte by byte. Each time a byte equals '\n' , we add the current position to the list
     * of offsets. Then, we allocate the amount of offsets onto a buffer, turn it into a long buffer, flip it and
     * return it.
     */
    public static LongBuffer buildNewLineOffsetsBuffer(Path filePath) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
            List<Long> offsets = new ArrayList<>();
            offsets.add(0L);

            int b;
            long pos = 0;
            while ((b = bis.read()) != -1) {
                pos++;

                if (b == '\n') {
                    offsets.add(pos);
                }
            }

            ByteBuffer raw = ByteBuffer.allocateDirect(offsets.size() * Long.BYTES)
                    .order(ByteOrder.nativeOrder());
            LongBuffer out = raw.asLongBuffer();
            offsets.forEach(out::put);
            out.flip();
            return out;
        }
    }
}
