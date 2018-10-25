package topk.util;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Utility class for reading in stream of trace log data.
 */
public class TraceLogStream {
  /**
   * Representation of one entry in the log.
   */
  public static class LogEntry {
    public final long timestamp;
    public final int clientID;
    public final int objectID;
    public final int size;
    public final byte method;
    public final byte status;
    public final byte type;
    public final byte server;

    /**
     * @param in
     */
    public LogEntry(DataInputStream in) throws IOException {
        this.timestamp = in.readInt();
        this.clientID = in.readInt();
        this.objectID = in.readInt();
        this.size = in.readInt();
        this.method = in.readByte();
        this.status = in.readByte();
        this.type = in.readByte();
        this.server = in.readByte();
    }

    @Override
    public String toString() {
        return "[t=" + timestamp + ", client=" + clientID + ", object="
                + objectID + ", size=" + size + ", method=" + method
                + ", status=" + status + ", type=" + type + ", server="
                + server + "]";
    }

    public byte getRegion() {
        return (byte) ((server >> 5) & 0x7);
    }

    public byte getServerInRegion() {
        return (byte) (server & 0x1f);
    }
  }

  private DataInputStream in;

  private TreeMap<Long, Set<LogEntry>> buffer;

  public TraceLogStream(DataInputStream in) {
    this.in = in;
    buffer = new TreeMap<>();
  }

  private void fillBuffer() {
    // Fills the buffer with all entries for the next available second.
    LogEntry nextEntry = null;
    do {
      try {
        nextEntry = new LogEntry(in);
        if (!buffer.containsKey(nextEntry.timestamp))
          buffer.put(nextEntry.timestamp, new HashSet<>());
        buffer.get(nextEntry.timestamp).add(nextEntry);
      } catch (EOFException e) {
        break;
      } catch (IOException e) {
        System.err.println("Something went wrong reading the log " + e);
        e.printStackTrace();
        System.exit(1);
      }
    } while (nextEntry != null && nextEntry.timestamp <= buffer.firstKey().longValue());
  }

  public Set<LogEntry> readNextSecond() {
    // Fill the buffer.
    fillBuffer();
    // Return the lowest group.
    Map.Entry<Long, Set<LogEntry>> next = buffer.pollFirstEntry();
    if (next == null)
      return null;
    return next.getValue();
  }
}
