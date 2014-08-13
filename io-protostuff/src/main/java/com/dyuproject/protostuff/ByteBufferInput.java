package com.dyuproject.protostuff;

import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_BITS;
import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_MASK;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_END_GROUP;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_TAIL_DELIMITER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import com.dyuproject.protostuff.StringSerializer.STRING;

public class ByteBufferInput implements Input{
	
	private final static byte[] NO_BYTES = new byte[0];

	private static final int NO_VALUE_INT = -1;

	private static final int NO_TAG_SET = -1;
	
	private ByteBuffer buffer;
	
	// a dynamic growing buffer used to store required data that atomically spans over multiple
	// buffers
	private Deque<ByteBuffer> lookBackBuffer = new ArrayDeque<ByteBuffer>();
	private int lookBackBufferSize = 0;
	private int cachedLastReadTag = NO_TAG_SET;
	private int offset, limit = 0;
	private final boolean decodeNestedMessageAsGroup;
	private boolean readEnoughBytes = true;
	private byte[] toLookbackBuffer = NO_BYTES;
	private int fieldLength = NO_VALUE_INT;
	
	public ByteBufferInput(boolean decodeNestedMessageAsGroup){
		this.decodeNestedMessageAsGroup = decodeNestedMessageAsGroup;
	}
	
	public void setNextBuffer(ByteBuffer buffer){
		this.buffer = buffer;
		this.limit += buffer.remaining();
		this.readEnoughBytes = true;
	}

	@Override
	public <T> void handleUnknownField(int fieldNumber, Schema<T> schema) throws IOException {
		throw new UnsupportedOperationException("Unable to handle unknown fields");
		
	}
	
	private final int getTotalBytesAvailable(){
		
		return lookBackBufferSize + buffer.remaining();
	}

	@Override
	public <T> int readFieldNumber(Schema<T> schema) throws IOException {
		
		if (!readEnoughBytes){
			return 0;
		}
		
		if (cachedLastReadTag != NO_TAG_SET){
			return cachedLastReadTag;
		}
		
		if (offset == limit)
        {
            return 0;
        }
		
		readEnoughBytes = true;
		
		if (getTotalBytesAvailable() > 0){ // do we have at least 1 byte to read the next field number?
			final int tag = readRawVarInt32();
			
			if (readEnoughBytes){
			
				final int fieldNumber = tag >>> TAG_TYPE_BITS;
				
				if (fieldNumber == 0)
		        {
		            if (decodeNestedMessageAsGroup && 
		                    WIRETYPE_TAIL_DELIMITER == (tag & TAG_TYPE_MASK))
		            {
		                // protostuff's tail delimiter for streaming
		                // 2 options: length-delimited or tail-delimited.
		                //lastTag = 0;
		                return 0;
		            }
		            // If we actually read zero, that's not a valid tag.
		            throw ProtobufException.invalidTag();
		        }
		        if (decodeNestedMessageAsGroup && WIRETYPE_END_GROUP == (tag & TAG_TYPE_MASK))
		        {
		            //lastTag = 0;
		            return 0;
		        }
				
				cachedLastReadTag = fieldNumber;
				
				return fieldNumber;
			}
		} 
			// now store the remaining bytes into a temporary buffer prior to the next invocation
			
		pushToLookbackBuffer();
			
		return 0;
		
	}

	@Override
	public int readInt32() throws IOException {
		int value = readRawVarInt32();
		
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} 
		pushToLookbackBuffer();
			
		return NO_VALUE_INT;
	}
	
	@Override
	public long readInt64() throws IOException {
		long value = readRawVarint64();
		
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} 
		pushToLookbackBuffer();
		
		return NO_VALUE_INT;
	}
	
	@Override
	public int readUInt32() throws IOException {
		int value = readRawVarInt32();
		
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} 
		pushToLookbackBuffer();
			
		return NO_VALUE_INT;
	}
	
	@Override
	public long readUInt64() throws IOException {
		long value = readRawVarint64();
		
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} 
		pushToLookbackBuffer();
		
		return NO_VALUE_INT;
	}

	
	private byte get(){
		byte value;
		if (!lookBackBuffer.isEmpty()){
			
			// load the next byte from the buffer.
			
			ByteBuffer nextBuffer = lookBackBuffer.peek();
			
			value = nextBuffer.get();
			lookBackBufferSize--;
			
			assert(lookBackBufferSize >=0);
			
			if (!nextBuffer.hasRemaining()){ // nothing left in this buffer now so remove from queue
				lookBackBuffer.remove();
			}
			
			offset++;
		} else if (buffer.hasRemaining()){ // only count the reads from the actual buffer
			value = buffer.get();
			offset++;
		} else{
			readEnoughBytes = false;
			value = Byte.MIN_VALUE;
		}
		
		return value;
	}
	
	private void get(byte[] bytes){
		
		int bytesRead = 0;
		while (!lookBackBuffer.isEmpty()){
			
			ByteBuffer nextBuffer = lookBackBuffer.peek();
			int bytesToRead = Math.min(nextBuffer.remaining(), bytes.length);
			nextBuffer.get(bytes, bytesRead, bytesToRead);
			
			bytesRead += bytesToRead;
			lookBackBufferSize -= bytesToRead;
			
			assert(lookBackBufferSize >=0);
			
			if (!nextBuffer.hasRemaining()){ // nothing left in this buffer now so remove from queue
				lookBackBuffer.remove();
			}
		}
		
		if (bytesRead != bytes.length){
			int moreBytesToRead = bytes.length - bytesRead;
			
			buffer.get(bytes, bytesRead, moreBytesToRead);
			
			bytesRead += moreBytesToRead;
		}
		
		offset += bytesRead;
	}
	
	private long readRawVarint64() throws IOException {
		
		readEnoughBytes = true;

        int shift = 0;
        long result = 0;
        int bytes = 0;
        while (shift < 64) {
            final byte b = get();
            if (readEnoughBytes){
	            result |= (long)(b & 0x7F) << shift;
	            if ((b & 0x80) == 0) {
	                return result;
	            }
	            shift += 7;
	            bytes++;
            } else if (bytes > 0){
            	toLookbackBuffer = new byte[bytes];
            	for (int i = 0; i < bytes; i++){
            		toLookbackBuffer[i] = (byte)(0x7F & result >> (7*i) | 0x80);
            	}
				return NO_VALUE_INT;
            } else{
            	return NO_VALUE_INT;
            }
        }
        throw ProtobufException.malformedVarint();
    }
	
	private int readRawVarInt32() throws ProtobufException {
		
		readEnoughBytes = true;
		
		byte tmp = get();
		if (tmp >= 0) {
			return tmp;
		}
		if (!readEnoughBytes){
			
			return NO_VALUE_INT;
		}
		int result = tmp & 0x7f;
		if ((tmp = get()) >= 0) {
			result |= tmp << 7;
		} else {
			if (readEnoughBytes){
				result |= (tmp & 0x7f) << 7;
				if ((tmp = get()) >= 0) {
					result |= tmp << 14;
				} else {
					if (readEnoughBytes){
						result |= (tmp & 0x7f) << 14;
						if ((tmp = get()) >= 0) {
							result |= tmp << 21;
						} else {
							if (readEnoughBytes){
								result |= (tmp & 0x7f) << 21;
								tmp = get();
								if (readEnoughBytes){
									result |= tmp << 28;
									if (tmp < 0) {
										// Discard upper 32 bits.
										for (int i = 0; i < 5; i++) {
											if (get() >= 0) {
												return result;
											} else if (!readEnoughBytes){
												toLookbackBuffer = new byte[4];
												toLookbackBuffer[0] = (byte)((0x7F & result) | 0x80);
												toLookbackBuffer[1] = (byte)((0x7F & result >> 7) | 0x80);
												toLookbackBuffer[2] = (byte)((0x7F & result >> 14) | 0x80);
												toLookbackBuffer[3] = (byte)((0x7F & result >> 21) | 0x80);
												return NO_VALUE_INT;
											}
										}
										throw ProtobufException.malformedVarint();
									}
								} else{
									toLookbackBuffer = new byte[4];
									toLookbackBuffer[0] = (byte)((0x7F & result) | 0x80);
									toLookbackBuffer[1] = (byte)((0x7F & result >> 7) | 0x80);
									toLookbackBuffer[2] = (byte)((0x7F & result >> 14) | 0x80);
									toLookbackBuffer[3] = (byte)((0x7F & result >> 21) | 0x80);
									return NO_VALUE_INT;
								}
							} else{
								toLookbackBuffer = new byte[3];
								toLookbackBuffer[0] = (byte)((0x7F & result) | 0x80);
								toLookbackBuffer[1] = (byte)((0x7F & result >> 7) | 0x80);
								toLookbackBuffer[2] = (byte)((0x7F & result >> 14) | 0x80);;
								return NO_VALUE_INT;
							}
						}
					} else{
						toLookbackBuffer = new byte[2];
						toLookbackBuffer[0] = (byte)((0x7F & result) | 0x80);
						toLookbackBuffer[1] = (byte)((0x7F & result >> 7) | 0x80);
						return NO_VALUE_INT;
					}
				}
			} else{
				toLookbackBuffer = new byte[1];
				toLookbackBuffer[0] = (byte)((0x7F & result) | 0x80);
				return NO_VALUE_INT;
			}
			
		}

		return result;
	}

	private void pushToLookbackBuffer() {
		
		readEnoughBytes = false;// belts and braces assignment
		
		if (toLookbackBuffer != NO_BYTES){
			lookBackBuffer.add(ByteBuffer.wrap(toLookbackBuffer));
			lookBackBufferSize += toLookbackBuffer.length;
			offset -= toLookbackBuffer.length; // rewind offset
			toLookbackBuffer = NO_BYTES;
		}		
		
		assert (offset >= 0);
		
		if (buffer.hasRemaining()){
			byte[] newBuffer = new byte[buffer.remaining()];
			
			buffer.get(newBuffer);
			
			lookBackBuffer.add(ByteBuffer.wrap(newBuffer));
			lookBackBufferSize += newBuffer.length;
		}
	}
	
	private static void showStats( String where, ByteBuffer b ){
    System.out.println( where +
                 " bufferPosition: " +
                 b.position() +
                 " limit: " +
                 b.limit() +
                 " remaining: " +
                 b.remaining() +
                 " capacity: " +
                 b.capacity() +" backing array: "+Arrays.toString(b.array()));
    }

	@Override
	public int readSInt32() throws IOException {
		final int n = readRawVarInt32();
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET;
			return (n >>> 1) ^ -(n & 1);
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}
	
	@Override
	public long readSInt64() throws IOException {
		final long n = readRawVarint64();
        
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET;
			return (n >>> 1) ^ -(n & 1);
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}

	@Override
	public int readFixed32() throws IOException {
		if (getTotalBytesAvailable() >3){ // 4 or more bytes
			cachedLastReadTag = NO_TAG_SET;
			return readRawLittleEndian32();
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}

	@Override
	public int readSFixed32() throws IOException {
		if (getTotalBytesAvailable() >3){ // 4 or more bytes
			cachedLastReadTag = NO_TAG_SET;
			return readRawLittleEndian32();
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}

	@Override
	public long readFixed64() throws IOException {
		if (getTotalBytesAvailable() >7){ // 8 or more bytes
			cachedLastReadTag = NO_TAG_SET;
			return readRawLittleEndian64();
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}

	@Override
	public long readSFixed64() throws IOException {
		if (getTotalBytesAvailable() > 7){ // 8 or more bytes
			
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			return readRawLittleEndian64();
			
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}

	@Override
	public float readFloat() throws IOException {
		if (getTotalBytesAvailable() >3){ // 4 or more bytes
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			return Float.intBitsToFloat(readRawLittleEndian32());
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}

	@Override
	public double readDouble() throws IOException {
		if (getTotalBytesAvailable() > 7){ // 8 or more bytes
			long value = readRawLittleEndian64();
			
			cachedLastReadTag = NO_TAG_SET;
			
			return Double.longBitsToDouble(value);
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
		
	}
	
	private int readRawLittleEndian32() throws IOException {
        
        final byte b1 = get();
        final byte b2 = get();
        final byte b3 = get();
        final byte b4 = get();
        
        return (((int)b1 & 0xff)    ) |
             (((int)b2 & 0xff) <<  8) |
             (((int)b3 & 0xff) << 16) |
             (((int)b4 & 0xff) << 24);
    }
    
    /** Read a 64-bit little-endian integer from the internal byte buffer. */
    private long readRawLittleEndian64() throws IOException {
        
        final byte b1 = get();
        final byte b2 = get();
        final byte b3 = get();
        final byte b4 = get();
        final byte b5 = get();
        final byte b6 = get();
        final byte b7 = get();
        final byte b8 = get();
        
        return (((long)b1 & 0xff)    ) |
             (((long)b2 & 0xff) <<  8) |
             (((long)b3 & 0xff) << 16) |
             (((long)b4 & 0xff) << 24) |
             (((long)b5 & 0xff) << 32) |
             (((long)b6 & 0xff) << 40) |
             (((long)b7 & 0xff) << 48) |
             (((long)b8 & 0xff) << 56);
    }
	

	@Override
	public boolean readBool() throws IOException {
		boolean value = get() != 0;
		
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} 
		pushToLookbackBuffer();
		
		return false;
	}

	@Override
	public int readEnum() throws IOException {
		int value = readRawVarInt32();
		
		if (readEnoughBytes){
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} 
		pushToLookbackBuffer();
		
		return NO_VALUE_INT;
	}

	@Override
	public String readString() throws IOException {
		
		int length;
		if (fieldLength != NO_VALUE_INT){
			length = fieldLength;
		} else{
			length = readRawVarInt32();
			fieldLength = length;
		}
		if (readEnoughBytes){
			
	        if(length < 0)
	            throw ProtobufException.negativeSize();
	        
	        if (getTotalBytesAvailable() < length){ // we dont have enough to read this section of data so copy to the lookback buffer
	        	
	        	pushToLookbackBuffer();
	        	
	        	return null;
	        } else{
	        
	        	byte[] stringBuffer = new byte[length];
	        
	        	get(stringBuffer);
	        
	        	String value =  STRING.deser(stringBuffer, 0, length);
	        	
	        	cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
	        	fieldLength = NO_VALUE_INT;
	        	
	        	return value;
	        }
		} else{
			pushToLookbackBuffer();
			
			return null;
		}
	}

	private void putRawVarInt32(ByteBuffer buffer, int value) {
		
		while(true){
            
            if ((value & ~0x7F) == 0) {
            	buffer.put((byte)value);
            	return;
            }

            buffer.put((byte)((value & 0x7F) | 0x80));
            value >>>= 7;
        }
	}

	@Override
	public ByteString readBytes() throws IOException {
		return ByteString.wrap(readByteArray());
	}

	@Override
	public byte[] readByteArray() throws IOException {
		int length;
		if (fieldLength != NO_VALUE_INT){
			length = fieldLength;
		} else{
			length = readRawVarInt32();
			fieldLength = length;
		}
		if (readEnoughBytes){
	        if(length < 0)
	            throw ProtobufException.negativeSize();
	        
	        if (getTotalBytesAvailable() < length){ // we dont have enough to read this section of data so copy to the lookback buffer
	        	
	        	pushToLookbackBuffer();
	        	
	        	return null;
	        } else{
	        
	        	byte[] byteBuffer = new byte[length];
		        
	        	get(byteBuffer);
	        	
	        	cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
	        	fieldLength = NO_VALUE_INT;
	        	
	        	return byteBuffer;
	        }
		} else{
			pushToLookbackBuffer();
			
			return null;
		}
	}
	
	private <T> T mergeObjectEncodedAsGroup(T value, final Schema<T> schema) throws IOException{
        if(value == null)
            value = schema.newMessage();
        
        int savedLastTag = cachedLastReadTag;
        cachedLastReadTag = NO_TAG_SET;
        schema.mergeFrom(this, value);
        if (!readEnoughBytes){
        	cachedLastReadTag = savedLastTag;
        }
        return value;
    }

	@Override
	public <T> T mergeObject(T value, Schema<T> schema) throws IOException {
		if(decodeNestedMessageAsGroup){
            return mergeObjectEncodedAsGroup(value, schema);
		}
		
		final int length = readRawVarInt32();
        if(length < 0){
            throw ProtobufException.negativeSize();
        }
        
        // save old limit
        final int oldLimit = this.limit;
        final int oldOffset = this.offset;
        final int oldCachedLastReadTag = this.cachedLastReadTag;
        this.limit = length;
        this.offset = 0;
        this.cachedLastReadTag = NO_TAG_SET;
        
        if(value == null)
            value = schema.newMessage();
        schema.mergeFrom(this, value);
        if(!schema.isInitialized(value))
            throw new UninitializedMessageException(value, schema);

     // restore old limit/offset
        this.limit = oldLimit;
        this.offset = oldOffset;
        this.cachedLastReadTag = oldCachedLastReadTag;
        
        return value;
	}

	@Override
	public void transferByteRangeTo(Output output, boolean utf8String, int fieldNumber, boolean repeated) throws IOException {
		int length;
		if (fieldLength != NO_VALUE_INT){
			length = fieldLength;
		} else{
			length = readRawVarInt32();
			fieldLength = length;
		}
		if (readEnoughBytes){
			if(length < 0){
	            throw ProtobufException.negativeSize();
	        }
	        
	        if (getTotalBytesAvailable() < length){ // we dont have enough to read this section of data so copy to the lookback buffer
	        	
	        	pushToLookbackBuffer();
	        } else{
	        	
	        	byte[] buffer = new byte[length];
	        	
	        	get(buffer);
	        
	        	output.writeByteRange(utf8String, fieldNumber, buffer, 0, length, repeated);
	        
	        	cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
	        	fieldLength = NO_VALUE_INT;
	        }
		 }
	}

	public boolean hasReadEnoughBytes() {
		return getTotalBytesAvailable() == 0 && readEnoughBytes && offset == limit;
	}

	public void resetBuffered(){
		lookBackBuffer.clear();
		lookBackBufferSize = 0;
		offset = 0;
		limit = 0;
		fieldLength = NO_VALUE_INT;
		cachedLastReadTag = NO_TAG_SET;
		readEnoughBytes = true;
		cachedLastReadTag = NO_TAG_SET;
		toLookbackBuffer = NO_BYTES;
	}
	
}
