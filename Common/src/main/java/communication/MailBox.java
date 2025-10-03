package communication;

import java.io.Serializable;
import java.util.*;

public class MailBox implements Serializable {

    private static final int CHUNK_SIZE = 50;

    private String mail;
    private int received = 0;
    private int toRead = 0;
    private int sent = 0;

    private LinkedHashMap<UUID, Integer> receivedBucket = new LinkedHashMap<>();
    private LinkedHashMap<UUID, Integer> sentBucket = new LinkedHashMap<>();

    public MailBox() {
    }

    public MailBox(String mail) {
        this.mail = mail;
    }

    public MailBox(String mail, int received, int toRead, int sent, LinkedHashMap<UUID, Integer> receivedBucket, LinkedHashMap<UUID, Integer> sentBucket) {
        this.mail = mail;
        this.received = received;
        this.toRead = toRead;
        this.sent = sent;
        this.receivedBucket = receivedBucket;
        this.sentBucket = sentBucket;
    }

    public MailBox(MailBox toCopy) {
        this.mail = toCopy.mail;
        this.received = toCopy.received;
        this.toRead = toCopy.toRead;
        this.sent = toCopy.sent;
        this.receivedBucket.putAll(toCopy.receivedBucket);
        this.sentBucket.putAll(toCopy.sentBucket);
    }

    public void IncrementReceived() {
        received++;
    }

    public void DecrementReceived() {
        received--;
    }

    public void IncrementToRead() {
        toRead++;
    }

    public void DecrementToRead() {
        toRead--;
    }

    public void IncrementSent() {
        sent++;
    }

    public UUID addSent() {
        IncrementSent();
        return addToChunk(sentBucket);
    }

    public UUID addReceived() {
        IncrementReceived();
        IncrementToRead();
        return addToChunk(receivedBucket);
    }

    //In case the caller will try to search for a file with this UUID and can't find it
    //It will be entitled of creating the new file and will be "obvious" that the chunk is a new one
    private UUID addToChunk(LinkedHashMap<UUID, Integer> bucket) {
        UUID firstID = bucket.firstEntry().getKey();
        if (bucket.get(firstID) >= CHUNK_SIZE) {
            return createChunk(bucket);
        }

        bucket.merge(firstID, 1, Integer::sum);
        return firstID;
    }

    private UUID createChunk(LinkedHashMap<UUID, Integer> bucket) {
        UUID id = UUID.randomUUID();
        bucket.putFirst(UUID.randomUUID(), 1);
        return id;
    }

    public List<ChunkRange> getReceivedChunks(int startElementIndex, int elements) {
        return getChunks(startElementIndex, elements, receivedBucket);
    }

    public List<ChunkRange> getSentChunks(int startElementIndex, int elements) {
        return getChunks(startElementIndex, elements, sentBucket);
    }

    private List<ChunkRange> getChunks(int startElementIndex, int elements, LinkedHashMap<UUID, Integer> bucket) {
        int currElements = elements;
        int accumulator = 0;
        List<ChunkRange> result = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : bucket.entrySet()) {

            if (result.isEmpty() && accumulator + entry.getValue() > startElementIndex) {
                int start = startElementIndex - accumulator;
                int takenAmount = Math.min(currElements, entry.getValue() - start);
                int end = Math.min(takenAmount, entry.getValue());
                currElements -= takenAmount;
                result.add(new ChunkRange(entry.getKey(), start, end));
            } else if (!result.isEmpty()) {
                int start = 0;
                int takenAmount = Math.min(currElements, entry.getValue());
                currElements -= takenAmount;
                result.add(new ChunkRange(entry.getKey(), start, takenAmount));
            }

            if (currElements <= 0) {
                break;
            }
        }
        return result;
    }

}