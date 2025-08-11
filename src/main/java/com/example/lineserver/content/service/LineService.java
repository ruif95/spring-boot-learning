package com.example.lineserver.content.service;

import com.example.lineserver.content.exception.BeyondEndOfFileException;
import com.example.lineserver.content.model.ChunkLineIndex;
import com.example.lineserver.content.model.ChunkMetadata;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Service
public class LineService {

    private static final Logger logger = LoggerFactory.getLogger(LineService.class);

    /**
     * Amount of lines per chunk offset metadata.
     * <p>
     * Note: value chosen a bit randomly - 1GB file had around
     */
    public static final int LINES_PER_CHUNK = 1_000_000;
    public static final int CACHE_MAXIMUM_SIZE = 1000;

    private final ChunkLineIndex index;
    private final ThreadLocal<RandomAccessFile> threadLocalFile;

    /**
     * Caches gets on getting a line value per index, in-memory.
     * <p>
     * Guava (used lib) uses LRU as its eviction policy by default.
     * We're using {@link #CACHE_MAXIMUM_SIZE} as the limit before it starts removing
     * entries.
     */
    private final LoadingCache<Integer, String> linePerIndexCache;

    public LineService(@Value("${filepath}") String filepath) throws IOException {
        File file = new File(filepath);
        Path path = file.toPath();

        long startTime = System.nanoTime();

        LongBuffer offsets = ChunkLineIndex.buildNewLineOffsetsBuffer(path);
        this.index = ChunkLineIndex.buildFromOffsets(offsets, LINES_PER_CHUNK);

        long durationNs = System.nanoTime() - startTime;
        double durationMs = durationNs / 1_000_000.0;

        logger.info("Time taken to read file: {} ms", String.format("%.3f", durationMs));

        linePerIndexCache = CacheBuilder.newBuilder()
                .maximumSize(CACHE_MAXIMUM_SIZE)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(final Integer lineIndex) throws Exception {
                        return getLine(lineIndex);
                    }
                });

        this.threadLocalFile = ThreadLocal.withInitial(() -> {
            try {
                return new RandomAccessFile(file, "r");
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String getLineByIndex(final Integer lineNumber) throws ExecutionException {
        return linePerIndexCache.get(lineNumber);
    }

    /**
     * Gets a line value from a given line number.
     *
     * @param lineNumber line number from which to get the value
     * @return Line value
     * @throws IOException something goes wrong when reading the file.
     * @implSpec gets a chunk metadata offset from the given line number.
     * From it, we get the offset and then seek our file helper to get the offset value from there.
     */
    private String getLine(final Integer lineNumber) throws IOException {
        if (lineNumber < 0 || lineNumber >= index.getTotalLines()) {
            throw new BeyondEndOfFileException("Invalid line number");
        }

        ChunkMetadata chunk = index.getChunkForLine(lineNumber);
        int localIndex = lineNumber - chunk.startLineNumber();
        long offset = chunk.startByteOffset() + chunk.localOffsets()[localIndex];

        RandomAccessFile raf = threadLocalFile.get();
        raf.seek(offset);
        return raf.readLine();
    }
}
