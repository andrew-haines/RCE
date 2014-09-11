package com.haines.ml.rce.io.protostuff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.ByteString;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
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

/*
Copyright 2014 Yahoo! Inc.
Copyrights licensed under the [name of] License. See the accompanying LICENSE
file for terms.
*/

public class ProtostuffEventMarshalBufferUnitTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProtostuffEventMarshalBufferUnitTest.class);
	
	private static final Pattern BYTE_ARRAY_EXTRACTOR = Pattern.compile(".*\\[(.*)\\]");

	private static final ByteString TEST_BYTE_ARRAY = ByteString.copyFrom(new byte[]{45, 34, 4, -5, 6, 23, 71, 44, 110}); //45, 34, 4, -5, 6, 23, 71, 44, 110
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
	public void givenCandidateWithTestInnerMessagesSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		List<TestInnerMessage> messages = new ArrayList<TestInnerMessage>();
		messages.add(createInnerMessage(TEST_INNER_STRING+"_1"));
		messages.add(createInnerMessage(TEST_INNER_STRING+"_2"));
		
		message.setInnerMessagesList(messages);
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[5];
		byte[] array2 = new byte[10];
		byte[] array3 = new byte[5];
		byte[] array4 = new byte[3];
		byte[] array5 = new byte[2];
		byte[] array6= new byte[5];
		byte[] array7 = new byte[5];
		byte[] array8 = new byte[7];
		System.arraycopy(array, 0, array1, 0, 5);
		System.arraycopy(array, 5, array2, 0, 10);
		System.arraycopy(array, 15, array3, 0, 5);
		System.arraycopy(array, 20, array4, 0, 3);
		System.arraycopy(array, 23, array5, 0, 2);
		System.arraycopy(array, 25, array6, 0, 5);
		System.arraycopy(array, 30, array7, 0, 5);
		System.arraycopy(array, 35, array8, 0, 7);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		ByteBuffer buffer5 = ByteBuffer.wrap(array5);
		ByteBuffer buffer6 = ByteBuffer.wrap(array6);
		ByteBuffer buffer7 = ByteBuffer.wrap(array7);
		ByteBuffer buffer8 = ByteBuffer.wrap(array8);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(false))); 
		assertThat(candidateOptional.marshal(buffer4), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer5), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer6), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer7), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer8), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getInnerMessagesList(), is(not(nullValue())));
		assertThat(demarshalledMessage.getInnerMessagesList(), hasSize(2));
		assertThat(demarshalledMessage.getInnerMessagesList().get(0).getFeatureId(), is(equalTo(TEST_INNER_STRING+"_1")));
		assertThat(demarshalledMessage.getInnerMessagesList().get(1).getFeatureId(), is(equalTo(TEST_INNER_STRING+"_2")));
	}
	
	@Test
	public void givenCandidateWithTestInnerMessageSet_whenCallingMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		TestMessageOptional message = new TestMessageOptional();
		
		message.setInnerMessage(createInnerMessage(TEST_INNER_STRING));
		
		ProtostuffIOUtil.writeTo(outputStream, message, TestMessageOptional.getSchema(), LinkedBuffer.allocate(1024));
		
		byte[] array = outputStream.toByteArray();
		byte[] array1 = new byte[5];
		byte[] array2 = new byte[4];
		byte[] array3 = new byte[5];
		byte[] array4 = new byte[5];
		System.arraycopy(array, 0, array1, 0, 5);
		System.arraycopy(array, 5, array2, 0, 4);
		System.arraycopy(array, 9, array3, 0, 5);
		System.arraycopy(array, 14, array4, 0, 5);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		
		assertThat(candidateOptional.marshal(buffer1), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer2), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer3), is(equalTo(false)));
		assertThat(candidateOptional.marshal(buffer4), is(equalTo(true)));
		
		TestMessageOptional demarshalledMessage = candidateOptional.buildEventAndResetBuffer();
		
		assertThat(demarshalledMessage.getInnerMessage(), is(not(nullValue())));
		assertThat(demarshalledMessage.getInnerMessage().getFeatureId(), is(equalTo(TEST_INNER_STRING)));
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
	
	@Test
	public void givenCandidate_whenCallingFullMarshalWithSmallBuffer_thenExpectedMessageReturned() throws IOException{
		
		byte[] array = getFullMessageByteArray();
		byte[] array1 = new byte[100];
		byte[] array2 = new byte[100];
		byte[] array3 = new byte[100];
		byte[] array4 = new byte[100];
		byte[] array5 = new byte[100];
		byte[] array6 = new byte[100];
		byte[] array7 = new byte[108];
		System.arraycopy(array, 0, array1, 0, 100);
		System.arraycopy(array, 100, array2, 0, 100);
		System.arraycopy(array, 200, array3, 0, 100);
		System.arraycopy(array, 300, array4, 0, 100);
		System.arraycopy(array, 400, array5, 0, 100);
		System.arraycopy(array, 500, array6, 0, 100);
		System.arraycopy(array, 600, array7, 0, 108);
		
		ByteBuffer buffer1 = ByteBuffer.wrap(array1);
		ByteBuffer buffer2 = ByteBuffer.wrap(array2);
		ByteBuffer buffer3 = ByteBuffer.wrap(array3);
		ByteBuffer buffer4 = ByteBuffer.wrap(array4);
		ByteBuffer buffer5 = ByteBuffer.wrap(array5);
		ByteBuffer buffer6 = ByteBuffer.wrap(array6);
		ByteBuffer buffer7 = ByteBuffer.wrap(array7);
		
		assertThat(candidate.marshal(buffer1), is(equalTo(false)));
		assertThat(candidate.marshal(buffer2), is(equalTo(false)));
		assertThat(candidate.marshal(buffer3), is(equalTo(false)));
		assertThat(candidate.marshal(buffer4), is(equalTo(false)));
		assertThat(candidate.marshal(buffer5), is(equalTo(false)));
		assertThat(candidate.marshal(buffer6), is(equalTo(false)));
		assertThat(candidate.marshal(buffer7), is(equalTo(true)));
		
		//assertThat(candidate.marshal(ByteBuffer.wrap(outputStream.toByteArray())), is(equalTo(true)));
		
		TestMessage message = candidate.buildEventAndResetBuffer();
		
		assertFullySerialisedMessage(message, true);
	}
	
	private void assertFullySerialisedMessage(TestMessage message, boolean includeEmbeddedObjects){
		
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
		
		if (includeEmbeddedObjects){
			for (TestMessage messageInner: message.getMessageArrayList()){
				assertFullySerialisedMessage(messageInner, false);
			}
			
			assertFullySerialisedMessage(message.getMessage(), false);
		}
	}
	
	@Test
	public void givenCandidate_whenCallingFullMarshalWithEdgeCaseBufferSize_thenExpectedMessageReturned() throws IOException{
		
		Iterable<ByteBuffer> buffers = loadByteBuffersFromFile("testBuffer1.txt");
		Iterator<ByteBuffer> buffersIt = buffers.iterator();
		
		int i = 0;
		int totalBytesRead = 0;
		while (buffersIt.hasNext()){
			ByteBuffer buffer = buffersIt.next();
			
			totalBytesRead += buffer.remaining();
			//LOG.debug("trying buffer: "+(i++));
			
			boolean moreToRead = candidate.marshal(buffer);
			
			//LOG.debug("inner message now: "+ReflectionToStringBuilder.toString(candidate.messageBuffer, ToStringStyle.MULTI_LINE_STYLE));
			assertThat(moreToRead, is(equalTo(!buffersIt.hasNext())));
		}
		
		System.out.println(totalBytesRead+" bytes read");
		
		TestMessage message = candidate.buildEventAndResetBuffer();
		
		assertFullySerialisedMessage(message, true);
	}
	
	@Test
	public void givenCandidate_whenCallingFullMarshalWithEdgeCase2BufferSize_thenExpectedMessageReturned() throws IOException{
		
		Iterable<ByteBuffer> buffers = loadByteBuffersFromFile("testBuffer2.txt");
		Iterator<ByteBuffer> buffersIt = buffers.iterator();
		
		int i = 0;
		int totalBytesRead = 0;
		while (buffersIt.hasNext()){
			ByteBuffer buffer = buffersIt.next();
			
			totalBytesRead += buffer.remaining();
			LOG.debug("trying buffer: "+(i++));
			
			boolean moreToRead = candidate.marshal(buffer);
			
			LOG.debug("inner message now: "+ReflectionToStringBuilder.toString(candidate.messageBuffer, ToStringStyle.MULTI_LINE_STYLE));
			assertThat(moreToRead, is(equalTo(!buffersIt.hasNext())));
		}
		
		System.out.println(totalBytesRead+" bytes read");
		
		TestMessage message = candidate.buildEventAndResetBuffer();
		
		assertFullySerialisedMessage(message, true);
	}
	
	private Iterable<ByteBuffer> loadByteBuffersFromFile(String splitFileName) throws IOException {
		
		InputStream stream = ProtostuffEventMarshalBufferUnitTest.class.getResourceAsStream("/testBufferSplits/"+splitFileName);
		
		Iterable<String> bufferStrings = IOUtils.readLines(stream, Charset.defaultCharset());
		
		return Iterables.transform(bufferStrings, new Function<String, ByteBuffer>(){

			@Override
			public ByteBuffer apply(String input) {
				
				Matcher matcher = BYTE_ARRAY_EXTRACTOR.matcher(input); // strip out everything except the bytes
				
				matcher.find();
				input = matcher.group(1);
				
				String[] byteStrs = input.split(",");
				
				byte[] bytes = new byte[byteStrs.length];
				
				for (int i = 0; i < byteStrs.length; i++){
					bytes[i] = Byte.parseByte(byteStrs[i].trim());
				}
				return ByteBuffer.wrap(bytes);
			}
			
		});
	}

	@Test // this test performs multiple serialisations of the Fully marshalled object with random buffers to brute force the serialisation process over multiple buffers
	public void givenCandidate_whenCallingFullMarshalWithRandomBufferSizes_thenExpectedMessageReturned() throws IOException{
		
		byte[] array = getFullMessageByteArray();
		
		for (int i = 0; i < 20; i ++){ // attempt 20 random iterations
			
			int numBuffers = (int)(Math.random() * 100); // up to 100 buffers
			
			LOG.info("serializing random buffer attempt num : "+i+" using "+numBuffers+" buffers");
			int bytesReadSoFar = 0;
			
			for (int j = 0; j < numBuffers; j++){ // now create the buffers with random split points of at least 1 byte big
				
				int maxBufferSize = ((array.length - bytesReadSoFar - numBuffers) / (numBuffers - j));
				
				int newBufferSize;
				if (j+1 == numBuffers){
					newBufferSize = array.length - bytesReadSoFar; // add the remaining bytes to this last buffer (with at least 1 byte)
				} else{
					newBufferSize = 1 + (int)(Math.random() * maxBufferSize);
				}
				
				byte[] bytes = new byte[newBufferSize];
				
				System.arraycopy(array, bytesReadSoFar, bytes, 0, bytes.length);
				bytesReadSoFar += bytes.length;
				
				//LOG.info("Creating random buffer: "+j+" with size "+newBufferSize+" ("+remainingBytesToCopy+" - "+ (array.length - bytesReadSoFar) +" bytes left to copy)");
				
				System.out.println(j+": = "+Arrays.toString(bytes));
				
				ByteBuffer newBuffer = ByteBuffer.wrap(bytes);
				
				assertThat(candidate.marshal(newBuffer), is(equalTo(j+1 == numBuffers))); // if it's the last buffer then this should be true
			}
			TestMessage message = candidate.buildEventAndResetBuffer();
			
			assertFullySerialisedMessage(message, true);
		}
	}
	
	private byte[] getFullMessageByteArray() throws IOException {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
		
		ProtostuffIOUtil.writeTo(outputStream, createTestMessage(), TestMessage.getSchema(), LinkedBuffer.allocate(1024));
		
		return outputStream.toByteArray();
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
