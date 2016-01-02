package com.haines.ml.rce.test.load;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.transport.Event;

/**
 * Super simple hello world serlvet for testing tomcat throughput for a reference. It is not intended to be efficient!
 * @author haines
 *
 */
public class ReferenceHelloWorldServlet extends HttpServlet {

	@Override
	  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	      // Set response content type
	      response.setContentType("text/html");

	      // Actual logic goes here.
	      PrintWriter out = response.getWriter();
	      
	      // deliberately recreate buffer to simulate a benchmark without any of the optimizations used in this project
	      ByteBuffer byteBuffer = ByteBuffer.wrap(IOUtils.toByteArray(request.getInputStream()));
	      ProtostuffEventMarshalBuffer<Event> buffer = new ProtostuffEventMarshalBuffer<Event>(Event.getSchema());
	      
	      if (!buffer.marshal(byteBuffer)){
	    	  throw new IllegalStateException("buffer not big enough");
	      }
	      
	      Event event = buffer.buildEventAndResetBuffer();
	      
	      System.out.println(event);
	      
	      out.println("<h1>" + request.getParameter("message") + "</h1>");
	  }
}
