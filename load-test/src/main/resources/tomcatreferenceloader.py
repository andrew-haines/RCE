import string
from net.grinder.script.Grinder import grinder
from net.grinder.plugin.http import HTTPRequest, HTTPPluginControl
from net.grinder.script import Test

test1 = Test(1, "Send Events")

class TestRunner:
  def __call__(self):
  
  	request = HTTPRequest()
  	
  	test1.record(request)
  	
  	grinder.sleep(100) 
  	result = request.GET("http://localhost:8080/rce-loadtest/ReferenceServlet?message=Testing")
  	
  	if string.find(result.getText(), "Testing") < 1:
		grinder.statistics.forLastTest.setSuccess(0)