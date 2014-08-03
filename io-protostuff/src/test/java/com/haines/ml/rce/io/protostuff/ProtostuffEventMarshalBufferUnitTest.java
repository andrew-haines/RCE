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
import com.haines.ml.rce.io.proto.model.TestMessage;
import com.haines.ml.rce.io.proto.model.TestMessage.TestInnerMessage;

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
	
	@Before
	public void before(){
		candidate = new ProtostuffEventMarshalBuffer<TestMessage>(TestMessage.getSchema());
	}
	
	@Test
	public void givenCandidate_whenCallingMarshalWithLargeBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		
		ProtostuffIOUtil.writeTo(outputStream, createTestMessage(), TestMessage.getSchema(), LinkedBuffer.allocate(1024));
		
		candidate.marshal(ByteBuffer.wrap(outputStream.toByteArray()));
		
		TestMessage message = candidate.buildEventAndResetBuffer();
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