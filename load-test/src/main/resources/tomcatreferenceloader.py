import string
from net.grinder.script.Grinder import grinder
from net.grinder.plugin.http import HTTPRequest, HTTPPluginControl
from com.haines.ml.rce.main import RCEApplicationStartupTest
from com.haines.ml.rce.test.load import SimpleLoadTest
from net.grinder.script import Test

test1 = Test(1, "Send Events")
dataset = SimpleLoadTest.DATASET;

class TestRunner:
  def __call__(self):
  
  	request = HTTPRequest()
  	
  	test1.record(request)
  	
  	for event in dataset.getEventsFromDistribution(1):
  		grinder.sleep(100) 
  		
  		data = RCEApplicationStartupTest.getBytesOfProtostuffMessage(event)
  		
  		grinder.logger.info("Sending bytes: "+str(len(data)))
  		result = request.POST("http://localhost:8080/rce-loadtest/ReferenceServlet?message=Testing", data)
  	
  		if string.find(result.getText(), "Testing") < 1:
			grinder.statistics.forLastTest.setSuccess(0)