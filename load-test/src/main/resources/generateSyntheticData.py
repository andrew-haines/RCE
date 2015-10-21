from net.grinder.script.Grinder import grinder
from net.grinder.script import Test
from com.haines.ml.rce.main import RCEApplicationStartupTest
from com.haines.ml.rce.test.load import SimpleLoadTest
from com.haines.ml.rce.test.data import SyntheticTestDataset
from com.haines.ml.rce.main.config import RCEConfig

test1 = Test(1, "Send Events")
dataset = SimpleLoadTest.DATASET;

defaultConfig = RCEConfig.UTIL.loadConfig(None)

test1.record(RCEApplicationStartupTest.sendViaSelector)

class TestRunner:
	
	def __call__(self):
	
		for event in dataset.getEventsFromDistribution(1):
			grinder.sleep(100) 
			RCEApplicationStartupTest.sendViaSelector(event, defaultConfig)
		