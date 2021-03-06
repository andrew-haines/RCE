package com.dyuproject.protostuff;

import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_BITS;
import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_MASK;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_END_GROUP;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_START_GROUP;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_TAIL_DELIMITER;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import com.dyuproject.protostuff.StringSerializer.STRING;

/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the [name of] License. See the accompanying LICENSE
file for terms.
*/
public class ByteBufferInput implements Input{

	private static final int NO_VALUE_INT = -1;

	private static final int NO_TAG_SET = -1;

	private static final int READING_INNER_MESSAGE_FIELD = -1;
	
	private ByteBuffer buffer;
	
	// a dynamic growing buffer used to store required data that atomically spans over multiple
	// buffers
	private Deque<ByteBuffer> lookBackBuffer = new ArrayDeque<ByteBuffer>();
	private int lookBackBufferSize = 0;
	private int cachedLastReadTag = NO_TAG_SET;
	private int offset, limit = 0;
	private final boolean decodeNestedMessageAsGroup;
	private boolean readEnoughBytes = true;
	private final ByteBuffer toLookbackBuffer = ByteBuffer.allocate(9); // maximum is 9 bytes
	private int fieldLength = NO_VALUE_INT;
	private int groupReadDepth = 0;
	private final Deque<Object> innerMessageCandidateStack = new ArrayDeque<Object>(1);
	private final Deque<Object> currentReadingInnerMessageStack = new ArrayDeque<Object>(1);
	
	public ByteBufferInput(boolean decodeNestedMessageAsGroup){
		this.decodeNestedMessageAsGroup = decodeNestedMessageAsGroup;
	}
	
	public void setNextBuffer(ByteBuffer buffer){
		this.buffer = buffer;
		this.limit += buffer.remaining();
		this.readEnoughBytes = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void handleUnknownField(int fieldNumber, Schema<T> schema) throws IOException {
		
		if (fieldNumber == READING_INNER_MESSAGE_FIELD){
			// this indicates that we are mid way through reading an embedded message object from the stream. This is required as list reading will create
			// additional objects for each read which is not what we want. Thus we use the stack to push half read objects back into the deserialisation process
			
			T innerMessage = (T)innerMessageCandidateStack.pop();
			
			mergeObject(innerMessage, ((Message<T>)innerMessage).cachedSchema());
		} else{
			throw new UnsupportedOperationException("Unable to handle unknown field ("+fieldNumber+")");
		}
		
	}
	
	private final int getTotalBytesAvailable(){
		
		return lookBackBufferSize + buffer.remaining();
	}

	@Override
	public <T> int readFieldNumber(Schema<T> schema) throws IOException {
		
		if (!readEnoughBytes){
			return 0;
		}
		
		if (!innerMessageCandidateStack.isEmpty()){
			return READING_INNER_MESSAGE_FIELD;
		}
		
		if (cachedLastReadTag != NO_TAG_SET){
			return cachedLastReadTag;
		}
		
		if (offset == limit && currentReadingInnerMessageStack.isEmpty()){
            return 0;
        }
		
		readEnoughBytes = true;
		
		final int tag = readRawVarInt32();
		
		if (readEnoughBytes){
		
			final int fieldNumber = tag >>> TAG_TYPE_BITS;
			
			if (fieldNumber == 0) {
	            if (decodeNestedMessageAsGroup && 
	                    WIRETYPE_TAIL_DELIMITER == (tag & TAG_TYPE_MASK)) {
	                // protostuff's tail delimiter for streaming
	                // 2 options: length-delimited or tail-delimited.
	                //lastTag = 0;
	                return 0;
	            }
	            // If we actually read zero, that's not a valid tag.
	            throw ProtobufException.invalidTag();
	        }
			
			if (decodeNestedMessageAsGroup && WIRETYPE_START_GROUP == (tag & TAG_TYPE_MASK)){
				groupReadDepth = currentReadingInnerMessageStack.size();
			}
			
	        if (decodeNestedMessageAsGroup && WIRETYPE_END_GROUP == (tag & TAG_TYPE_MASK)) {
	        	groupReadDepth = currentReadingInnerMessageStack.size() -1;
	        	//cachedLastReadTag = fieldNumber;
	            return 0;
	        }
			
			cachedLastReadTag = fieldNumber;
			
			return fieldNumber;
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
				lookBackBuffer.remove().clear();
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
	
	private void get(byte[] bytes){ // only call this if getTotalBytesAvailable() > bytes.length. This method does not do any checking!
		
		int bytesRead = 0;
		while (!lookBackBuffer.isEmpty() && bytesRead < bytes.length){
			
			ByteBuffer nextBuffer = lookBackBuffer.peek();
			int bytesToRead = Math.min(nextBuffer.remaining(), bytes.length);
			nextBuffer.get(bytes, bytesRead, bytesToRead);
			
			bytesRead += bytesToRead;
			lookBackBufferSize -= bytesToRead;
			
			assert(lookBackBufferSize >=0);
			
			if (!nextBuffer.hasRemaining()){ // nothing left in this buffer now so remove from queue
				lookBackBuffer.remove().clear();
			}
		}
		
		if (bytesRead < bytes.length){
			int moreBytesToRead = bytes.length - bytesRead;
			
			buffer.get(bytes, bytesRead, moreBytesToRead); //{45, 34, 4, -5, 6, 23,71,44, 110}); //45, 34, 4, 114, 83, 116, 114, 105, 110
			
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
            	for (int i = 0; i < bytes; i++){
            		toLookbackBuffer.put((byte)(0x7F & result >> (7*i) | 0x80));
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
												toLookbackBuffer.put((byte)((0x7F & result) | 0x80));
												toLookbackBuffer.put((byte)((0x7F & result >> 7) | 0x80));
												toLookbackBuffer.put((byte)((0x7F & result >> 14) | 0x80));
												toLookbackBuffer.put((byte)((0x7F & result >> 21) | 0x80));
												return NO_VALUE_INT;
											}
										}
										throw ProtobufException.malformedVarint();
									}
								} else{
									toLookbackBuffer.put((byte)((0x7F & result) | 0x80));
									toLookbackBuffer.put((byte)((0x7F & result >> 7) | 0x80));
									toLookbackBuffer.put((byte)((0x7F & result >> 14) | 0x80));
									toLookbackBuffer.put((byte)((0x7F & result >> 21) | 0x80));
									return NO_VALUE_INT;
								}
							} else{
								toLookbackBuffer.put((byte)((0x7F & result) | 0x80));
								toLookbackBuffer.put((byte)((0x7F & result >> 7) | 0x80));
								toLookbackBuffer.put((byte)((0x7F & result >> 14) | 0x80));
								return NO_VALUE_INT;
							}
						}
					} else{
						toLookbackBuffer.put((byte)((0x7F & result) | 0x80));
						toLookbackBuffer.put((byte)((0x7F & result >> 7) | 0x80));
						return NO_VALUE_INT;
					}
				}
			} else{
				toLookbackBuffer.put((byte)((0x7F & result) | 0x80));
				return NO_VALUE_INT;
			}
			
		}

		return result;
	}

	private void pushToLookbackBuffer() {
		
		readEnoughBytes = false;// belts and braces assignment
		
		if (toLookbackBuffer.remaining() != toLookbackBuffer.capacity()){ // only flip if there is actually some data to read
			toLookbackBuffer.flip();
			lookBackBuffer.add(toLookbackBuffer);
			lookBackBufferSize += toLookbackBuffer.remaining();
			offset -= toLookbackBuffer.remaining(); // rewind offset
		}
		
		assert (offset >= 0);
		
		if (buffer.hasRemaining()){
			byte[] newBuffer = new byte[buffer.remaining()];
			
			buffer.get(newBuffer);
			
			lookBackBuffer.add(ByteBuffer.wrap(newBuffer));
			lookBackBufferSize += newBuffer.length;
		}
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
		}
		if (readEnoughBytes){
			fieldLength = length;
			
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
		byte[] array = readByteArray();
		
		if (array != null){
			return ByteString.wrap(array);
		} else{
			return null;
		}
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
	        if(length < 0){
	            throw ProtobufException.negativeSize();
	        }
	        
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
        if(value == null){
            value = schema.newMessage();
         // must read the end group tag so set the limit to be at least 1 byte more
//        	if (isReadingGroup){
//        		limit++;
//        	}
        }
        
        currentReadingInnerMessageStack.push(value);
        
        if (innerMessageCandidateStack.isEmpty()){
        	cachedLastReadTag = NO_TAG_SET;
        }
        
        schema.mergeFrom(this, value);
        currentReadingInnerMessageStack.pop();
        if (!readEnoughBytes){
        	
        	// push the inner message field tag back into the lookback buffer.
        	
        	if (cachedLastReadTag != NO_TAG_SET){
	        	ByteBuffer field = ByteBuffer.allocate(4);
	        	        	
	        	putRawVarInt32(field, cachedLastReadTag << TAG_TYPE_BITS | WIRETYPE_START_GROUP);
	        	
	        	field.flip();
	        	
	        	lookBackBufferSize += field.remaining();
	        	offset -= field.remaining();
	        	
	        	lookBackBuffer.addFirst(field);
        	}
        	
        	// push half read message onto stack and set the field to be the READING_INNER_MESSAGE_FIELD so that reads can continue as expected
        	
        	innerMessageCandidateStack.push(value);
        	cachedLastReadTag = READING_INNER_MESSAGE_FIELD;
        } else{
        	if (!schema.isInitialized(value)){
        		//throw new UninitializedMessageException(value, schema);
        	}
        	
        }
    	/* 
    	 * return the partially read message so that it can be appended to as new buffers come in. 
    	 * Note that call by reference will mean that subsequent writes to this object will mean that 
    	 * it's variable assignment location need not be remembered
    	 */
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
		return getTotalBytesAvailable() == 0 && readEnoughBytes && offset == limit && innerMessageCandidateStack.isEmpty() && groupReadDepth == 0;
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
		groupReadDepth = 0;
		innerMessageCandidateStack.clear();
		currentReadingInnerMessageStack.clear();
		toLookbackBuffer.clear();
	}
	
}
