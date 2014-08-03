package com.dyuproject.protostuff;

import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_BITS;
import static com.dyuproject.protostuff.WireFormat.TAG_TYPE_MASK;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_END_GROUP;
import static com.dyuproject.protostuff.WireFormat.WIRETYPE_TAIL_DELIMITER;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferInput implements Input{
	
	private final static ByteBuffer DEFAULT_LOOKBACK_BUFFER = ByteBuffer.allocate(1024); // standard lookback buffer size

	private static final int NO_VALUE_INT = -1;
	
	private final ByteBuffer buffer;
	
	// a dynamic growing buffer used to store required data that atomically spans over multiple
	// buffers
	private ByteBuffer lookBackBuffer = DEFAULT_LOOKBACK_BUFFER; 
	
	public ByteBufferInput(ByteBuffer buffer){
		this.buffer = buffer;
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
		
		if (getTotalBytesAvailable() > 3){ // do we have at least 4 bytes to read the next field number?
			final int tag = readRawInt32();
			
			final int fieldNumber = tag >>> TAG_TYPE_BITS;
			
			if (fieldNumber == 0){
				throw ProtobufException.invalidTag();
			}
			
			return fieldNumber;
		} else{
			// now store the remaining bytes into a temporary buffer prior to the next invocation
			
			lookBackBuffer.put(buffer);
			
			return 0;
		}
		
	}

	public int readRawInt32() throws IOException{
		
		if (lookBackBuffer.remaining() > 3){
			return readRawVarInt32(lookBackBuffer);
		}
		
        return readRawVarInt32(buffer);
    }

	@Override
	public int readInt32() throws IOException {
		if (getTotalBytesAvailable() > 3){
			int value = readRawInt32();
			
			lookBackBuffer = DEFAULT_LOOKBACK_BUFFER;
			lookBackBuffer.clear();
			
			return value;
		} else{
			pushToLookbackBuffer();
			
			return NO_VALUE_INT;
		}
	}
	
	private static int readRawVarInt32(ByteBuffer buffer)
			throws ProtobufException {
		byte tmp = buffer.get();
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = buffer.get()) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = buffer.get()) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = buffer.get()) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = buffer.get()) << 28;
					if (tmp < 0) {
						// Discard upper 32 bits.
						for (int i = 0; i < 5; i++) {
							if (buffer.get() >= 0) {
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float readFloat() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double readDouble() throws IOException {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public <T> T mergeObject(T value, Schema<T> schema) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void transferByteRangeTo(Output output, boolean utf8String,
			int fieldNumber, boolean repeated) throws IOException {
		// TODO Auto-generated method stub
		
	}

	
}
