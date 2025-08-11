package com.example.lineserver.content.model;

/**
 * Represents metadata with offset information for a given amount of lines.
 *
 * @param startLineNumber Starting line number for a given chunk.
 * @param startByteOffset Starting byte offset for this chunk.
 * @param localOffsets List of local offset positions for the remaining lines of this chunk. When we want a specific line,
 *                     we get a relevant chunk and then traverse this very localOffsets list.
 */
public record ChunkMetadata(
        int startLineNumber,
        long startByteOffset,
        long[] localOffsets
) {
}
