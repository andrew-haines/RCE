package com.haines.ml.rce.io.protostuff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dyuproject.protostuff.ByteString;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.haines.ml.rce.io.proto.model.EnumType;
import com.haines.ml.rce.io.proto.model.TestInnerMessage;
import com.haines.ml.rce.io.proto.model.TestMessage;
import com.haines.ml.rce.io.proto.model.TestMessageOptional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasSize;

public class ProtostuffEventMarshalBufferUnitTest {

	private static final ByteString TEST_BYTE_ARRAY = ByteString.copyFrom(new byte[]{45, 34, 4, -5, 6, 23,71,44, 110});
	private static final String TEST_INNER_STRING = "testInnerString";
	private static final Double TEST_DOUBLE = (Double.MAX_VALUE - 3433) + 0.05;
	private static final int TEST_FIXED_32 = 345464;
	private static final long TEST_FIXED_64 = 75847392754l;
	private static final Float TEST_FLOAT = 0.567433f;
	private static final Integer TEST_INT_32 = 7684734;
	private static final Long TEST_LONG_64 = -342255665l;
	private static final Integer TEST_S_FIXED_32 = -6566743;
	private static final Long TEST_S_FIXED_64 = -5883757823L;
	private static final Integer TEST_S_INT_32 = -578592947;
	private static final Long TEST_S_INT_64 = -5738384747627l;
	private static final String TEST_STRING = "testString";
	private static final Integer TEST_U_INT_32 = 5746295;
	private static final Long TEST_U_INT_64 = 85738272848905232l;
	private ProtostuffEventMarshalBuffer<TestMessage> candidate;
	private ProtostuffEventMarshalBuffer<TestMessageOptional> candidateOptional;
	
	@Before
	public void before(){
		candidate = new ProtostuffEventMarshalBuffer<TestMessage>(TestMessage.getSchema());
		candidateOptional = new ProtostuffEventMarshalBuffer<TestMessageOptional>(TestMessageOptional.getSchema());
	}
	
	@Test
	public void givenCandidateWithInt32Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestInt32(TEST_INT_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestInt32(), is(equalTo(TEST_INT_32)));
	}
	
	@Test
	public void givenCandidateWithInt64Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestInt64(TEST_LONG_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestInt64(), is(equalTo(TEST_LONG_64)));
	}
	
	@Test
	public void givenCandidateWithUInt32Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestUint32(TEST_U_INT_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestUint32(), is(equalTo(TEST_U_INT_32)));
	}
	
	@Test
	public void givenCandidateWithUInt64Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestUint64(TEST_U_INT_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestUint64(), is(equalTo(TEST_U_INT_64)));
	}
	
	@Test
	public void givenCandidateWithEnumSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestEnumType(EnumType.TYPE3);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestEnumType(), is(equalTo(EnumType.TYPE3)));
	}
	
	@Test
	public void givenCandidateWithFixed32Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestFixed32(TEST_FIXED_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestFixed32(), is(equalTo(TEST_FIXED_32)));
	}
	
	@Test
	public void givenCandidateWithByteArraySet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setByteArray(TEST_BYTE_ARRAY);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getByteArray(), is(equalTo(TEST_BYTE_ARRAY)));
	}
	
	@Test
	public void givenCandidateWithBooleanSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestBool(true);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestBool(), is(equalTo(true)));
	}
	
	@Test
	public void givenCandidateWithFixed64Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestFixed64(TEST_FIXED_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestFixed64(), is(equalTo(TEST_FIXED_64)));
	}
	
	@Test
	public void givenCandidateWithTestInnerMessageSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setInnerMessage(createInnerMessage(TEST_INNER_STRING));
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getInnerMessage(), is(not(nullValue())));
		assertThat(demarshalledMessage.getInnerMessage().getFeatureId(), is(equalTo(TEST_INNER_STRING)));
	}
	
	@Test
	public void givenCandidateWithTestInnerMessagesSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		List<TestInnerMessage> messages = new ArrayList<TestInnerMessage>();
		messages.add(createInnerMessage(TEST_INNER_STRING+"_1"));
		messages.add(createInnerMessage(TEST_INNER_STRING+"_2"));
		
		message.setInnerMessagesList(messages);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getInnerMessagesList(), is(not(nullValue())));
		assertThat(demarshalledMessage.getInnerMessagesList(), hasSize(2));
		assertThat(demarshalledMessage.getInnerMessagesList().get(0).getFeatureId(), is(equalTo(TEST_INNER_STRING+"_1")));
		assertThat(demarshalledMessage.getInnerMessagesList().get(1).getFeatureId(), is(equalTo(TEST_INNER_STRING+"_2")));
	}
	
	@Test
	public void givenCandidateWithSFixed32Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSFixed32(TEST_S_FIXED_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSFixed32(), is(equalTo(TEST_S_FIXED_32)));
	}
	
	@Test
	public void givenCandidateWithSFixed64Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSFixed64(TEST_S_FIXED_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSFixed64(), is(equalTo(TEST_S_FIXED_64)));
	}
	
	@Test
	public void givenCandidateWithSInt32Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSint32(TEST_S_INT_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSint32(), is(equalTo(TEST_S_INT_32)));
	}
	
	@Test
	public void givenCandidateWithSInt64Set_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSint64(TEST_S_INT_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSint64(), is(equalTo(TEST_S_INT_64)));
	}
	
	@Test
	public void givenCandidateWithMultipleFieldsSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestBool(true);
		message.setTestDouble(TEST_DOUBLE);
		message.setTestFixed32(TEST_FIXED_32);
		message.setTestFixed64(TEST_FIXED_64);
		message.setTestFloat(TEST_FLOAT);
		message.setTestInt32(TEST_INT_32);
		message.setTestInt64(TEST_LONG_64);
		message.setTestSFixed32(TEST_S_FIXED_32);
		message.setTestSFixed64(TEST_S_FIXED_64);
		message.setTestSint32(TEST_S_INT_32);
		message.setTestSint64(TEST_S_INT_64);
		message.setTestString(TEST_STRING);
		message.setTestUint32(TEST_U_INT_32);
		message.setTestUint64(TEST_U_INT_64);
		message.setTestEnumType(EnumType.TYPE1);
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSint64(), is(equalTo(TEST_S_INT_64)));
		assertThat(demarshalledMessage.getTestSint32(), is(equalTo(TEST_S_INT_32)));
		assertThat(demarshalledMessage.getTestBool(), is(equalTo(true)));
		assertThat(demarshalledMessage.getTestEnumType(), is(equalTo(EnumType.TYPE1)));
		assertThat(demarshalledMessage.getTestDouble(), is(equalTo(TEST_DOUBLE)));
		assertThat(demarshalledMessage.getTestFloat(), is(equalTo(TEST_FLOAT)));
		assertThat(demarshalledMessage.getTestFixed32(), is(equalTo(TEST_FIXED_32)));
		assertThat(demarshalledMessage.getTestFixed64(), is(equalTo(TEST_FIXED_64)));
		assertThat(demarshalledMessage.getTestInt32(), is(equalTo(TEST_INT_32)));
		assertThat(demarshalledMessage.getTestInt64(), is(equalTo(TEST_LONG_64)));
		assertThat(demarshalledMessage.getTestSFixed32(), is(equalTo(TEST_S_FIXED_32)));
		assertThat(demarshalledMessage.getTestSFixed64(), is(equalTo(TEST_S_FIXED_64)));
		assertThat(demarshalledMessage.getTestString(), is(equalTo(TEST_STRING)));
		assertThat(demarshalledMessage.getTestUint32(), is(equalTo(TEST_U_INT_32)));
		assertThat(demarshalledMessage.getTestUint64(), is(equalTo(TEST_U_INT_64)));
	}
	
	@Test
	public void givenCandidateWithMultipleFieldsSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestBool(true);
		message.setTestDouble(TEST_DOUBLE);
		message.setTestFixed32(TEST_FIXED_32);
		message.setTestFixed64(TEST_FIXED_64);
		message.setTestFloat(TEST_FLOAT);
		message.setTestInt32(TEST_INT_32);
		message.setTestInt64(TEST_LONG_64);
		message.setTestSFixed32(TEST_S_FIXED_32);
		message.setTestSFixed64(TEST_S_FIXED_64);
		message.setTestSint32(TEST_S_INT_32);
		message.setTestSint64(TEST_S_INT_64);
		message.setTestString(TEST_STRING);
		message.setTestUint32(TEST_U_INT_32);
		message.setTestUint64(TEST_U_INT_64);
		message.setTestEnumType(EnumType.TYPE1);
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[34]; // 106
		byte[] array2 = new byte[21];
		byte[] array3 = new byte[5];
		byte[] array4 = new byte[29];
		byte[] array5 = new byte[17];
		System.arraycopy(array, 0, array1, 0, 34);
		System.arraycopy(array, 34, array2, 0, 21);
		System.arraycopy(array, 55, array3, 0, 5);
		System.arraycopy(array, 60, array4, 0, 29);
		System.arraycopy(array, 89, array5, 0, 17);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		ByteBuffer buffer5 = ByteBuffer.wrap(array5);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer4), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer5), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSint64(), is(equalTo(TEST_S_INT_64)));
		assertThat(demarshalledMessage.getTestSint32(), is(equalTo(TEST_S_INT_32)));
		assertThat(demarshalledMessage.getTestBool(), is(equalTo(true)));
		assertThat(demarshalledMessage.getTestEnumType(), is(equalTo(EnumType.TYPE1)));
		assertThat(demarshalledMessage.getTestDouble(), is(equalTo(TEST_DOUBLE)));
		assertThat(demarshalledMessage.getTestFloat(), is(equalTo(TEST_FLOAT)));
		assertThat(demarshalledMessage.getTestFixed32(), is(equalTo(TEST_FIXED_32)));
		assertThat(demarshalledMessage.getTestFixed64(), is(equalTo(TEST_FIXED_64)));
		assertThat(demarshalledMessage.getTestInt32(), is(equalTo(TEST_INT_32)));
		assertThat(demarshalledMessage.getTestInt64(), is(equalTo(TEST_LONG_64)));
		assertThat(demarshalledMessage.getTestSFixed32(), is(equalTo(TEST_S_FIXED_32)));
		assertThat(demarshalledMessage.getTestSFixed64(), is(equalTo(TEST_S_FIXED_64)));
		assertThat(demarshalledMessage.getTestString(), is(equalTo(TEST_STRING)));
		assertThat(demarshalledMessage.getTestUint32(), is(equalTo(TEST_U_INT_32)));
		assertThat(demarshalledMessage.getTestUint64(), is(equalTo(TEST_U_INT_64)));
	}
	
	@Test
	public void givenCandidate_whenCallingResetAndSubsequentRead_thenBothMessagesReadSuccessfully() throws IOException{
		ProtostuffEventMarshalBuffer<TestMessageOptional> candidateOptional = this.candidateOptional;
		
		givenCandidateWithMultipleFieldsSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned();
		
		assertThat(candidateOptional, is(equalTo(this.candidateOptional))); // ensure we havent created a new version of this
		
		givenCandidateWithMultipleFieldsSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned();
		
		assertThat(candidateOptional, is(equalTo(this.candidateOptional))); // ensure we havent created a new version of this
		
	}
	
	@Test
	public void givenCandidateWithUInt32Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestUint32(TEST_U_INT_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[2];
		byte[] array2 = new byte[2];
		byte[] array3 = new byte[1];
		System.arraycopy(array, 0, array1, 0, 2);
		System.arraycopy(array, 2, array2, 0, 2);
		System.arraycopy(array, 4, array3, 0, 1);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestUint32(), is(equalTo(TEST_U_INT_32)));
	}
	
	@Test
	public void givenCandidateWithByteArraySet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setByteArray(TEST_BYTE_ARRAY);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[3];
		byte[] array2 = new byte[2];
		byte[] array3 = new byte[6];
		System.arraycopy(array, 0, array1, 0, 3);
		System.arraycopy(array, 3, array2, 0, 2);
		System.arraycopy(array, 5, array3, 0, 6);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getByteArray(), is(equalTo(TEST_BYTE_ARRAY)));
	}
	
	@Test
	public void givenCandidateWithUInt64Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestUint64(TEST_U_INT_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[2];
		byte[] array2 = new byte[2];
		byte[] array3 = new byte[6];
		System.arraycopy(array, 0, array1, 0, 2);
		System.arraycopy(array, 2, array2, 0, 2);
		System.arraycopy(array, 4, array3, 0, 6);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestUint64(), is(equalTo(TEST_U_INT_64)));
	}
	
	@Test
	public void givenCandidateWithEnumSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestEnumType(EnumType.TYPE3);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[2];
		byte[] array2 = new byte[1];
		
		System.arraycopy(array, 0, array1, 0, 2);
		System.arraycopy(array, 2, array2, 0, 1);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestEnumType(), is(equalTo(EnumType.TYPE3)));
	}
	
	@Test
	public void givenCandidateWithBooleanSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestBool(true);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[1];
		byte[] array2 = new byte[1];
		
		System.arraycopy(array, 0, array1, 0, 1);
		System.arraycopy(array, 1, array2, 0, 1);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestBool(), is(equalTo(true)));
	}
	
	@Test
	public void givenCandidateWithInt64Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestInt64(TEST_LONG_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[3];
		byte[] array2 = new byte[4];
		byte[] array3 = new byte[4];
		System.arraycopy(array, 0, array1, 0, 3);
		System.arraycopy(array, 3, array2, 0, 4);
		System.arraycopy(array, 7, array3, 0, 4);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestInt64(), is(equalTo(TEST_LONG_64)));
	}
	
	@Test
	public void givenCandidateWithSFixed32Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSFixed32(TEST_S_FIXED_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[2];
		byte[] array2 = new byte[1];
		byte[] array3 = new byte[2];
		System.arraycopy(array, 0, array1, 0, 2);
		System.arraycopy(array, 2, array2, 0, 1);
		System.arraycopy(array, 3, array3, 0, 2);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSFixed32(), is(equalTo(TEST_S_FIXED_32)));
	}
	
	@Test
	public void givenCandidateWithSInt64Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSint64(TEST_S_INT_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[1];
		byte[] array2 = new byte[3];
		byte[] array3 = new byte[2];
		byte[] array4 = new byte[2];
		System.arraycopy(array, 0, array1, 0, 1);
		System.arraycopy(array, 1, array2, 0, 3);
		System.arraycopy(array, 4, array3, 0, 2);
		System.arraycopy(array, 6, array4, 0, 2);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer4), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSint64(), is(equalTo(TEST_S_INT_64)));
	}
	
	@Test
	public void givenCandidateWithSInt32Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSint32(TEST_S_INT_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[3];
		byte[] array2 = new byte[3];
		
		System.arraycopy(array, 0, array1, 0, 3);
		System.arraycopy(array, 3, array2, 0, 3);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSint32(), is(equalTo(TEST_S_INT_32)));
	}
	
	@Test
	public void givenCandidateWithSFixed64Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestSFixed64(TEST_S_FIXED_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[3];
		byte[] array2 = new byte[2];
		byte[] array3 = new byte[2];
		byte[] array4 = new byte[2];
		System.arraycopy(array, 0, array1, 0, 3);
		System.arraycopy(array, 3, array2, 0, 2);
		System.arraycopy(array, 5, array3, 0, 2);
		System.arraycopy(array, 7, array4, 0, 2);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer4), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestSFixed64(), is(equalTo(TEST_S_FIXED_64)));
	}
	
	@Test
	public void givenCandidateWithFixed64Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestFixed64(TEST_FIXED_64);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[2];
		byte[] array2 = new byte[1];
		byte[] array3 = new byte[2];
		byte[] array4 = new byte[4];
		System.arraycopy(array, 0, array1, 0, 2);
		System.arraycopy(array, 2, array2, 0, 1);
		System.arraycopy(array, 3, array3, 0, 2);
		System.arraycopy(array, 5, array4, 0, 4);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer4), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestFixed64(), is(equalTo(TEST_FIXED_64)));
	}
	
	@Test
	public void givenCandidateWithFixed32Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestFixed32(TEST_FIXED_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[2];
		byte[] array2 = new byte[1];
		byte[] array3 = new byte[1];
		byte[] array4 = new byte[1];
		System.arraycopy(array, 0, array1, 0, 2);
		System.arraycopy(array, 2, array2, 0, 1);
		System.arraycopy(array, 3, array3, 0, 1);
		System.arraycopy(array, 4, array4, 0, 1);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer4), is(equalTo(true)));
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestFixed32(), is(equalTo(TEST_FIXED_32)));
	}
	
	@Test
	public void givenCandidateWithDoubleSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestDouble(TEST_DOUBLE);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestDouble(), is(equalTo(TEST_DOUBLE)));
	}
	
	@Test
	public void givenCandidateWithFloatSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestFloat(TEST_FLOAT);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestFloat(), is(equalTo(TEST_FLOAT)));
	}
	
	@Test
	public void givenCandidateWithFloatSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestFloat(TEST_FLOAT);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[1];
		byte[] array2 = new byte[3];
		byte[] array3 = new byte[2];
		System.arraycopy(array, 0, array1, 0, 1);
		System.arraycopy(array, 1, array2, 0, 3);
		System.arraycopy(array, 4, array3, 0, 2);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestFloat(), is(equalTo(TEST_FLOAT)));
	}
	
	@Test
	public void givenCandidateWithDoubleSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestDouble(TEST_DOUBLE);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[4];
		byte[] array2 = new byte[3];
		byte[] array3 = new byte[3];
		System.arraycopy(array, 0, array1, 0, 4);
		System.arraycopy(array, 4, array2, 0, 3);
		System.arraycopy(array, 7, array3, 0, 3);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestDouble(), is(equalTo(TEST_DOUBLE)));
	}
	
	@Test
	public void givenCandidateWithStringSet_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestString(TEST_STRING);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		assertThat(candidateOptional.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestString(), is(equalTo(TEST_STRING)));
	}
	
	@Test
	public void givenCandidateWithStringSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestString(TEST_STRING);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[4];
		byte[] array2 = new byte[4];
		byte[] array3 = new byte[4];
		System.arraycopy(array, 0, array1, 0, 4);
		System.arraycopy(array, 4, array2, 0, 4);
		System.arraycopy(array, 8, array3, 0, 4);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestString(), is(equalTo(TEST_STRING)));
	}
	
	@Test
	public void givenCandidateWithInt32Set_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setTestInt32(TEST_INT_32);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[1];
		byte[] array2 = new byte[2];
		byte[] array3 = new byte[2];
		System.arraycopy(array, 0, array1, 0, 1);
		System.arraycopy(array, 1, array2, 0, 2);
		System.arraycopy(array, 3, array3, 0, 2);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getTestInt32(), is(equalTo(TEST_INT_32)));
	}
	
	@Test
	public void givenCandidate_whenCallingFullMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		
		ProtostuffIOUtil.writeTo(outputStream, createTestMessage(), TestMessage.getSchema(), LinkedBuffer.allocate(1024));
		
		//TestMessage message = TestMessage.getSchema().newMessage();
		
		//ByteBuffer buffer = ByteBuffer.wrap(outputStream.toByteArray());
		
		//byte[] array = new byte[buffer.remaining()];
		
		//buffer.get(array);
		
		//ProtostuffIOUtil.mergeFrom(array, message, TestMessage.getSchema());
		
		assertThat(candidate.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessage message = candidate.buildEventAndResetBuffer();
		
		assertThat(message.getTestInt32(), is(equalTo(TEST_INT_32)));
		assertThat(message.getTestInt64(), is(equalTo(TEST_LONG_64)));
		assertThat(message.getByteArray(), is(equalTo(TEST_BYTE_ARRAY)));
		assertThat(message.getTestBool(), is(equalTo(true)));
		assertThat(message.getTestDouble(), is(equalTo(TEST_DOUBLE)));
		assertThat(message.getTestFloat(), is(equalTo(TEST_FLOAT)));
		assertThat(message.getTestSFixed32(), is(equalTo(TEST_S_FIXED_32)));
		assertThat(message.getTestSFixed64(), is(equalTo(TEST_S_FIXED_64)));
		assertThat(message.getTestFixed32(), is(equalTo(TEST_FIXED_32)));
		assertThat(message.getTestFixed64(), is(equalTo(TEST_FIXED_64)));
		assertThat(message.getTestSint32(), is(equalTo(TEST_S_INT_32)));
		assertThat(message.getTestSint64(), is(equalTo(TEST_S_INT_64)));
		assertThat(message.getTestString(), is(equalTo(TEST_STRING)));
		assertThat(message.getInnerMessage(), is(not(nullValue())));
		assertThat(message.getInnerMessage().getFeatureId(), is(equalTo(TEST_INNER_STRING)));
		assertThat(message.getInnerMessagesList().size(), is(equalTo(2)));
		assertThat(message.getInnerMessagesList().get(0).getFeatureId(), is(equalTo(TEST_INNER_STRING)));
		assertThat(message.getInnerMessagesList().get(1).getFeatureId(), is(equalTo(TEST_INNER_STRING)));
	}
	
	private static TestMessage createTestMessage(){
		TestMessage message = createSimpleTestMessage();
		message.setMessage(createSimpleTestMessage());
		
		List<TestMessage> messages = new ArrayList<TestMessage>();
		
		messages.add(createSimpleTestMessage());
		messages.add(createSimpleTestMessage());
		
		message.setMessageArrayList(messages);
		
		return message;
	}
	
	private static TestInnerMessage createInnerMessage(String testString){
		TestInnerMessage innerMessage = new TestInnerMessage();
		innerMessage.setFeatureId(testString);
		
		return innerMessage;
	}
	
	private static TestMessage createSimpleTestMessage(){
		TestMessage message = new TestMessage();
		
		message.setByteArray(TEST_BYTE_ARRAY);
		List<TestInnerMessage> innerMessages = new ArrayList<TestInnerMessage>();
		
		innerMessages.add(createInnerMessage(TEST_INNER_STRING));
		innerMessages.add(createInnerMessage(TEST_INNER_STRING));
		
		message.setTestEnumType(EnumType.TYPE2);
		message.setInnerMessagesList(innerMessages);
		
		message.setInnerMessage(createInnerMessage(TEST_INNER_STRING));
		message.setTestBool(true);
		message.setTestDouble(TEST_DOUBLE);
		message.setTestFixed32(TEST_FIXED_32);
		message.setTestFixed64(TEST_FIXED_64);
		message.setTestFloat(TEST_FLOAT);
		message.setTestInt32(TEST_INT_32);
		message.setTestInt64(TEST_LONG_64);
		message.setTestSFixed32(TEST_S_FIXED_32);
		message.setTestSFixed64(TEST_S_FIXED_64);
		message.setTestSint32(TEST_S_INT_32);
		message.setTestSint64(TEST_S_INT_64);
		message.setTestString(TEST_STRING);
		message.setTestUint32(TEST_U_INT_32);
		message.setTestUint64(TEST_U_INT_64);
		
		return message;
	}
}
