from net.grinder.script.Grinder import grinder
from com.haines.ml.rce.main import RCEApplicationStartupTest
from com.haines.ml.rce.test.data import SyntheticTestDataset
from com.haines.ml.rce.main.config import RCEConfig

class TestRunner:
	
	dataset = SyntheticTestDataset(3, 10, 0.6)
	
	defaultConfig = RCEConfig.UTIL.loadConfig(None)
	serverAddress = defaultConfig.getEventStreamSocketAddress()
	
	
	for event in dataset.getEventsFromDistribution(1000):
		RCEApplicationStartupTest.sendViaSelector(event, serverAddress)
		