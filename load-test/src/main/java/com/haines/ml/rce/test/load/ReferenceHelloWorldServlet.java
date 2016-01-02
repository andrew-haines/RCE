package com.haines.ml.rce.test.load;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Super simple hello world serlvet for testing tomcat throughput for a reference.
 * @author haines
 *
 */
public class ReferenceHelloWorldServlet extends HttpServlet {

	  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	      // Set response content type
	      response.setContentType("text/html");

	      // Actual logic goes here.
	      PrintWriter out = response.getWriter();
	      out.println("<h1>" + request.getParameter("message") + "</h1>");
	  }
}
