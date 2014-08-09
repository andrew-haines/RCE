package com.dyuproject.protostuff;

import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_BITS;
import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_MASK;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_END_GROUP;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_TAIL_DELIMITER;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dyuproject.protostuff.StringSerializer.STRING;

public class ByteBufferInput implements Input{
	
	private final static ByteBuffer NO_BUFFER = ByteBuffer.allocate(0);

	private static final int NO_VALUE_INT = -1;

	private static final int NO_TAG_SET = -1;
	
	private ByteBuffer buffer;
	
	private final ByteBuffer defaultLookbackBuffer = ByteBuffer.allocate(1024); // standard lookback buffer size
	
	// a dynamic growing buffer used to store required data that atomically spans over multiple
	// buffers
	private ByteBuffer lookBackBuffer = NO_BUFFER;
	private int cachedLastReadTag = NO_TAG_SET;
	private int offset, limit = 0;
	private final boolean decodeNestedMessageAsGroup;
	
	public ByteBufferInput(boolean decodeNestedMessageAsGroup){
		this.decodeNestedMessageAsGroup = decodeNestedMessageAsGroup;
	}
	
	public void setNextBuffer(ByteBuffer buffer){
		this.buffer = buffer;
		this.limit = buffer.remaining();
	}

	@Override
	public <T> void handleUnknownField(int fieldNumber, Schema<T> schema) throws IOException {
		throw new UnsupportedOperationException("Unable to handle unknown fields");
		
	}
	
	private final int getTotalBytesAvailable(){
		return lookBackBuffer.remaining() + buffer.remaining();
	}

	@Override
	public <T> int readFieldNumber(Schema<T> schema) throws IOException {
		
		if (cachedLastReadTag != NO_TAG_SET){
			return cachedLastReadTag;
		}
		
		if (offset == limit)
        {
            return 0;
        }
		
		if (getTotalBytesAvailable() > 3){ // do we have at least 4 bytes to read the next field number?
			final int tag = readRawInt32();
			
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
		} else{
			// now store the remaining bytes into a temporary buffer prior to the next invocation
			
			lookBackBuffer.put(buffer);
			
			return 0;
		}
		
	}

	private int readRawInt32() throws IOException{
		
		int value = readRawVarInt32();
		
		
		
		return value;
    }

	@Override
	public int readInt32() throws IOException {
		if (getTotalBytesAvailable() > 3){
			int value = readRawInt32();
			
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}
	
	private byte get(){
		byte value;
		if (lookBackBuffer.hasRemaining()){
			value = lookBackBuffer.get();
		} else{
			value = buffer.get();
		}
		
		offset++;
		
		return value;
	}
	
	private void get(byte[] bytes){
		
		int bytesRead = 0;
		if (lookBackBuffer.remaining() > 0){
			bytesRead = Math.min(lookBackBuffer.remaining(), bytes.length);
			lookBackBuffer.get(bytes, 0, bytesRead);
		}
		
		if (bytesRead != bytes.length){
			
			buffer.get(bytes, bytesRead, bytes.length - bytesRead);
		}
		
		offset += bytes.length;
	}
	
	private int readRawVarInt32()
			throws ProtobufException {
		
		byte tmp = get();
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = get()) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = get()) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = get()) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = get()) << 28;
					if (tmp < 0) {
						// Discard upper 32 bits.
						for (int i = 0; i < 5; i++) {
							if (get() >= 0) {
								return result;
							}
						}
						throw ProtobufException.malformedVarint();
					}
				}
			}
		}

		return result;
	}

	private void pushToLookbackBuffer() {
		
		if (lookBackBuffer == NO_BUFFER){
			lookBackBuffer = defaultLookbackBuffer;
		}
		
		if (lookBackBuffer.remaining() < buffer.remaining()){
			// need to grow the lookback buffer
			
			ByteBuffer newBuffer = ByteBuffer.allocate(lookBackBuffer.capacity() * 2);
			
			newBuffer.put(lookBackBuffer);
			
			lookBackBuffer = newBuffer;
		}
		
		lookBackBuffer.put(buffer);
	}

	@Override
	public int readUInt32() throws IOException {
		return readRawInt32();
	}

	@Override
	public int readSInt32() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readFixed32() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readSFixed32() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readInt64() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readUInt64() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readSInt64() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readFixed64() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readSFixed64() throws IOException {
		if (getTotalBytesAvailable() > 7){ // 8 or more bytes
			long value = readRawLittleEndian64();
			
			cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
			
			return value;
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}

	@Override
	public float readFloat() throws IOException {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int readEnum() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String readString() throws IOException {
		final int length = readRawVarInt32();
        if(length < 0)
            throw ProtobufException.negativeSize();
        
        if (getTotalBytesAvailable() < length){ // we dont have enough to read this section of data so copy to the lookback buffer
        	
        	buffer.flip(); // put length back into buffer
        	
        	putRawVarInt32(buffer, length);
        	
        	buffer.flip(); // set to be read again
        	
        	pushToLookbackBuffer();
        	
        	return null;
        } else{
        
        	byte[] stringBuffer = new byte[length];
        
        	get(stringBuffer);
        
        	String value =  STRING.deser(stringBuffer, 0, length);
        	
        	cachedLastReadTag = NO_TAG_SET; // successfully read this tag so return
        	
        	return value;
        }
	}

	private void putRawVarInt32(ByteBuffer buffer2, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ByteString readBytes() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] readByteArray() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private <T> T mergeObjectEncodedAsGroup(T value, final Schema<T> schema) throws IOException{
        if(value == null)
            value = schema.newMessage();
        schema.mergeFrom(this, value);
        return value;
    }

	@Override
	public <T> T mergeObject(T value, Schema<T> schema) throws IOException {
		if(decodeNestedMessageAsGroup){
            return mergeObjectEncodedAsGroup(value, schema);
		}
		
		final int length = readRawInt32();
        if(length < 0)
            throw ProtobufException.negativeSize();
        
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
	public void transferByteRangeTo(Output output, boolean utf8String,
			int fieldNumber, boolean repeated) throws IOException {
		// TODO Auto-generated method stub
		
	}

	
}
