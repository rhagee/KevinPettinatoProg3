package communication;

import java.util.UUID;

public class ChunkRange {
    private final UUID id;
    private final int start;
    private final int end;

    public ChunkRange(UUID id, int start, int end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public UUID getId() {
        return id;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
