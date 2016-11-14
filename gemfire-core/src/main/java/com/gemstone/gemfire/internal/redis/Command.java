package com.gemstone.gemfire.internal.redis;

import io.netty.buffer.ByteBuf;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * The command class is used in holding a received Redis command. Each sent 
 * command resides in an instance of this class. This class is designed to be
 * used strictly by getter and setter methods.
 * <p>
 * The contents include the following
 * <p>
 * <li>The channel from which the command was read and to where
 * the response will be written.</li>
 * <li>The list of elements in the command. Every command is sent as an array of BulkStrings.
 * For example if the command "SADD key member1 member2 member3" was sent, the list would contain
 * {"SADD", "key", "member1", "member2", "member3"}.
 * <li>The {@link RedisCommandType}</li>
 * <li>The response to be written back to the client</li>
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class Command {

  private final List<byte[]> commandElems;
  private final RedisCommandType commandType;
  private ByteBuf response;
  private String key;
  private ByteArrayWrapper bytes;

  /**
   * Constructor for {@link Command}. Must initialize Command with a {@link SocketChannel}
   * and a {@link List} of command elements
   * 
   * @param commandElems List of elements in command
   */
  public Command (List<byte[]> commandElems) {
    if (commandElems == null || commandElems.isEmpty())
      throw new IllegalArgumentException("List of command elements cannot be empty -> List:" + commandElems);
    this.commandElems = commandElems;
    this.response = null;

    RedisCommandType type;

    try {
      byte[] charCommand = commandElems.get(0);
      String commandName = Coder.bytesToString(charCommand).toUpperCase();
      type = RedisCommandType.valueOf(commandName);
    } catch (Exception e) {
      type = RedisCommandType.UNKNOWN;
    }
    this.commandType = type;

  }

  /**
   * Used to get the command element list
   * 
   * @return List of command elements in form of {@link List}
   */
  public List<byte[]> getProcessedCommand() {
    return this.commandElems;
  }

  /**
   * Getter method for the command type
   * 
   * @return The command type
   */
  public RedisCommandType getCommandType() {
    return this.commandType;
  }

  /**
   * Getter method to get the response to be sent
   * 
   * @return The response
   */
  public ByteBuf getResponse() {
    return response;
  }

  /**
   * Setter method to set the response to be sent
   * 
   * @param response The response to be sent
   */
  public void setResponse(ByteBuf response) {
    this.response = response;
  }

  public boolean hasError() {
    if (response == null)
      return false;

    if (response.getByte(0) == Coder.ERROR_ID)
      return true;

    return false;
  }

  /**
   * Convenience method to get a String representation of the key
   * in a Redis command, always at the second position in the sent
   * command array
   * 
   * @return Returns the second element in the parsed command
   * list, which is always the key for commands indicating
   * a key
   */
  public String getStringKey() {
    if (this.commandElems.size() > 1) {
      if (this.key == null) {
        this.bytes = new ByteArrayWrapper(this.commandElems.get(1));
        this.key = this.bytes.toString();
      }
      return this.key;
    } else 
      return null;
  }

  public ByteArrayWrapper getKey() {
    if (this.commandElems.size() > 1) {
      if (this.bytes == null)
        this.bytes = new ByteArrayWrapper(this.commandElems.get(1));
      return this.bytes;
    } else 
      return null;
  }
}