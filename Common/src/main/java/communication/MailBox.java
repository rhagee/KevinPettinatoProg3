package communication;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

public class MailBox implements Serializable {

    @JsonIgnore
    private static final int CHUNK_SIZE = 2; //Supposed to be 50 -> Setting 2 for "HARD" testing

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


    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Integer getReceivedMaxPages(int pageSize) {
        if (pageSize == 0) {
            return 1;
        }

        return Math.max(1, (received + pageSize - 1) / pageSize);
    }

    public Integer getSentMaxPages(int pageSize) {
        if (pageSize == 0) {
            return 1;
        }

        return Math.max(1, (sent + pageSize - 1) / pageSize);
    }

    public int getReceived() {
        return received;
    }


    public void setReceived(int received) {
        this.received = received;
    }

    public int getToRead() {
        return toRead;
    }

    public void setToRead(int toRead) {
        this.toRead = toRead;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public LinkedHashMap<UUID, Integer> getReceivedBucket() {
        return receivedBucket;
    }

    public void setReceivedBucket(LinkedHashMap<UUID, Integer> receivedBucket) {
        this.receivedBucket = receivedBucket;
    }

    public LinkedHashMap<UUID, Integer> getSentBucket() {
        return sentBucket;
    }

    public void setSentBucket(LinkedHashMap<UUID, Integer> sentBucket) {
        this.sentBucket = sentBucket;
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

    public void DecrementSent() {
        sent--;
    }

    public UUID addSent() {
        IncrementSent();
        return addToChunk(sentBucket);
    }

    public void TempReceivedSetter(LinkedHashMap<UUID, Integer> temp) {
        receivedBucket = temp;
    }

    public UUID addReceived() {
        IncrementReceived();
        IncrementToRead();
        return addToChunk(receivedBucket);
    }

    //In case the caller will try to search for a file with this UUID and can't find it
    //It will be entitled of creating the new file and will be "obvious" that the chunk is a new one
    private UUID addToChunk(LinkedHashMap<UUID, Integer> bucket) {
        //Empty Bucket
        if (bucket.isEmpty()) {
            return createChunk(bucket);
        }

        //First Chunk is Full
        UUID firstID = bucket.firstEntry().getKey();
        if (bucket.get(firstID) >= CHUNK_SIZE) {
            return createChunk(bucket);
        }

        //AddElement to current first chunk
        bucket.merge(firstID, 1, Integer::sum);
        return firstID;
    }

    private UUID createChunk(LinkedHashMap<UUID, Integer> bucket) {
        UUID id = UUID.randomUUID();
        bucket.putFirst(id, 1);
        return id;
    }

    public List<ChunkRange> getReceivedChunks(int startElementIndex, int elements) {
        return getChunks(startElementIndex, elements, receivedBucket);
    }

    public List<ChunkRange> getSentChunks(int startElementIndex, int elements) {
        return getChunks(startElementIndex, elements, sentBucket);
    }

    public boolean deleteReceivedMail(UUID id, boolean isRead) {

        boolean result = deleteMail(id, receivedBucket);
        if (result) {
            DecrementReceived();
            if (!isRead) {
                DecrementToRead();
            }
        }

        return result;
    }

    public boolean deleteSentMail(UUID id) {
        boolean result = deleteMail(id, sentBucket);
        if (result) {
            DecrementSent();
        }
        return result;
    }

    private boolean deleteMail(UUID id, LinkedHashMap<UUID, Integer> map) {
        if (!map.containsKey(id)) {
            return false;
        }

        int value = map.get(id);
        if (value <= 1) {
            map.remove(id);
        } else {
            map.put(id, value - 1);
        }

        return true;
    }

    private List<ChunkRange> getChunks(int startElementIndex, int elements, LinkedHashMap<UUID, Integer> bucket) {
        //Safety Clamp (avoid negative values)
        startElementIndex = Math.max(0, startElementIndex);
        elements = Math.max(0, elements);

        //Initialization
        int currElements = elements;
        int accumulator = 0;
        List<ChunkRange> result = new ArrayList<>();

        //Early return in case of 0 elements
        if (elements == 0) {
            return result;
        }

        for (Map.Entry<UUID, Integer> entry : bucket.entrySet()) {

            //If we haven't found any result yet, check if we'v arrived in the right chunk
            //To check it we add to the accumulator the current entry level, if that's greater than startElementIndex this is the starting chunk
            if (result.isEmpty() && accumulator + entry.getValue() > startElementIndex) {

                //To calculate the starting position we should remove "accumulator" from the value of StartingElementIndex
                //Accumulator has the amount of elements present in previous chunks, removing it from startElementIndex will give us how many elements
                //we should ignore from the start of the current chunk
                int start = startElementIndex - accumulator;
                //The taken amount from the current chunk could be the total missing elements (currElements) OR all the elements of the chunk (entry value - start index)
                //The lowest one will b the picked
                int takenAmount = Math.min(currElements, entry.getValue() - start);
                //The end is "EXCLUDED" since the result will be used for a sublist, so we get the min from start + takenAmount that can be less than the total entries
                int end = Math.min(start + takenAmount, entry.getValue());

                //Remove takenAmount from currElements
                currElements -= takenAmount;
                //Add ChunkRange
                result.add(new ChunkRange(entry.getKey(), start, end));
            }
            //If the result is not empty it means we are still gathering Chunks and we already got the first one
            //So if we still are in the loop it means this chunk HAS to be considered
            else if (!result.isEmpty()) {
                //From the start of the chunk
                int start = 0;
                //All the missing elements OR the total amount we have in the chunk
                int takenAmount = Math.min(currElements, entry.getValue());
                //Remove them from the currentElements
                currElements -= takenAmount;
                //Add ChunkRange
                result.add(new ChunkRange(entry.getKey(), start, takenAmount));
            } else {
                //Step
                accumulator += entry.getValue();
            }

            //Exit since we have finished gathering elements
            if (currElements <= 0) {
                break;
            }
        }

        return result;
    }

}