package demo_usr.energy.energymodel;

public class EnergyModel {

	// hardware related coefficient for energy consumption of cpu (user+system mode)
	private double cpuLoadCoefficient;

	// hardware related coefficient for energy consumption of cpu (idle mode)
	private double cpuIdleCoefficient;

	// hardware related coefficient for energy consumption of used memory
	private double memoryAllocationCoefficient;

	// hardware related coefficient for energy consumption of unused memory
	private double freeMemoryCoefficient;

	// hardware related coefficient for energy consumption of network (outbound traffic) - per byte
	private double networkOutboundBytesCoefficient;

	// hardware related coefficient for energy consumption of network (incoming traffic) - per byte
	private double networkIncomingBytesCoefficient;

	// average energy consumption of all server devices, besides network, cpu and memory
	private double baseLineEnergyConsumption;

	// keeps track of cumulative energy consumption of particular server
	private double cumulativeEnergyConsumption;

	// coefficients for application performance in terms of energy consumption
	private float HighProcessingImpactValueCoefficient;
	private float MediumProcessingImpactValueCoefficient;
	private float LowProcessingImpactValueCoefficient;
	private int HighMemoryImpactValueCoefficient;
	private int MediumMemoryImpactValueCoefficient;
	private int LowMemoryImpactValueCoefficient;
	private long HighOutgoingTrafficValueCoefficient;
	private long MediumOutgoingTrafficValueCoefficient;
	private long LowOutgoingTrafficValueCoefficient;
	private long HighIncomingTrafficValueCoefficient;
	private long MediumIncomingTrafficValueCoefficient;
	private long LowIncomingTrafficValueCoefficient;

	// calculate total time (for cumulative energy calculation) - in sampling periods
	private int totalTime;

	// keep track of last cpu loads
	float lastAverageCPULoad;
	float lastAverageIdleCPU;

	// keep track of last memory usage
	int lastMemoryUsed;
	int lastFreeMemory;

	// keep track of last data communicated
	int lastNetworkOutboundBytes;
	int lastNetworkIncomingBytes;

	// keep track of last energy measurement
	double lastEnergyMeasurement; 

	// enumeration with level of application impact on the physical server resources
	public enum ImpactLevel {Low, Medium, High};

	// EnergyModel constructor function
	public EnergyModel (double cpuLoadCoefficient_, double cpuIdleCoefficient_, double memoryAllocationCoefficient_, double freeMemoryCoefficient_, double networkOutboundBytesCoefficient_, double networkIncomingBytesCoefficient_, double baseLineEnergyConsumption_) {
		// initialise hardware related coefficients
		cpuLoadCoefficient = cpuLoadCoefficient_;
		cpuIdleCoefficient = cpuIdleCoefficient_;

		memoryAllocationCoefficient = memoryAllocationCoefficient_;
		freeMemoryCoefficient = freeMemoryCoefficient_;
		networkOutboundBytesCoefficient = networkOutboundBytesCoefficient_;
		networkIncomingBytesCoefficient = networkIncomingBytesCoefficient_;

		// initialise baseline energy consumption 
		baseLineEnergyConsumption = baseLineEnergyConsumption_;

		// reset application coefficients - adding some indicative default values
		HighProcessingImpactValueCoefficient = 0.10F;
		MediumProcessingImpactValueCoefficient = 0.05F;
		LowProcessingImpactValueCoefficient = 0.01F;
		HighMemoryImpactValueCoefficient = 1000; //1GB
		MediumMemoryImpactValueCoefficient = 500; //500MB
		LowMemoryImpactValueCoefficient = 50; //50MB
		HighOutgoingTrafficValueCoefficient = 1000000000; //100MB
		MediumOutgoingTrafficValueCoefficient = 10000000; //10MB
		LowOutgoingTrafficValueCoefficient = 100000; //100KB
		HighIncomingTrafficValueCoefficient = 1000000000; //100MB
		MediumIncomingTrafficValueCoefficient = 10000000; //10MB
		LowIncomingTrafficValueCoefficient = 100000; //100KB

		// start keeping track of cumulative energy consumption
		cumulativeEnergyConsumption=0.0;

		// initialise total time
		totalTime = 0;

		// initialise last energy measurement
		lastEnergyMeasurement = 0;
	}

	// configure application related coefficients
	public void ConfigureApplicationCoefficients (		
			float HighProcessingImpactValueCoefficient_,
			float MediumProcessingImpactValueCoefficient_,
			float LowProcessingImpactValueCoefficient_,
			int HighMemoryImpactValueCoefficient_,
			int MediumMemoryImpactValueCoefficient_,
			int LowMemoryImpactValueCoefficient_,
			long HighOutgoingTrafficValueCoefficient_,
			long MediumOutgoingTrafficValueCoefficient_,
			long LowOutgoingTrafficValueCoefficient_,
			long HighIncomingTrafficValueCoefficient_,
			long MediumIncomingTrafficValueCoefficient_,
			long LowIncomingTrafficValueCoefficient_) {

		// configure application related coefficients
		HighProcessingImpactValueCoefficient = HighProcessingImpactValueCoefficient_;
		MediumProcessingImpactValueCoefficient = MediumProcessingImpactValueCoefficient_;
		LowProcessingImpactValueCoefficient = LowProcessingImpactValueCoefficient_;
		HighMemoryImpactValueCoefficient = HighMemoryImpactValueCoefficient_;
		MediumMemoryImpactValueCoefficient = MediumMemoryImpactValueCoefficient_;
		LowMemoryImpactValueCoefficient = LowMemoryImpactValueCoefficient_;
		HighOutgoingTrafficValueCoefficient = HighOutgoingTrafficValueCoefficient_;
		MediumOutgoingTrafficValueCoefficient = MediumOutgoingTrafficValueCoefficient_;
		LowOutgoingTrafficValueCoefficient = LowOutgoingTrafficValueCoefficient_;
		HighIncomingTrafficValueCoefficient = HighIncomingTrafficValueCoefficient_;
		MediumIncomingTrafficValueCoefficient = MediumIncomingTrafficValueCoefficient_;
		LowIncomingTrafficValueCoefficient = LowIncomingTrafficValueCoefficient_;
	}

	// Calculate EnergyConsumption for that particular timeframe - the HostInfo probing period (assuming resource usage is relatively stable)
	// Assuming execution of this function per fixed sampling period
	public double CurrentEnergyConsumption (float averageCPULoad, float averageIdleCPU, int memoryUsed, int freeMemory, long networkOutboundBytes, long networkIncomingBytes) {
		double currentEnergy = baseLineEnergyConsumption +  ProcessingConsumptionFunction (averageCPULoad, averageIdleCPU) + MemoryConsumptionFunction (memoryUsed, freeMemory) + NetworkLoadConsumptionFunction (networkOutboundBytes, networkIncomingBytes);

		System.out.println ("Energy Consumption: baseline " + baseLineEnergyConsumption+" processing " + ProcessingConsumptionFunction (averageCPULoad, averageIdleCPU)+ " memory " + MemoryConsumptionFunction (memoryUsed, freeMemory) + " network " + NetworkLoadConsumptionFunction (networkOutboundBytes, networkIncomingBytes));

		// calculate totalTime (in sampling periods)
		totalTime += 1;

		// calculate cumulativeEnergyConsumption
		cumulativeEnergyConsumption += currentEnergy;

		// keep track of last values
		lastAverageCPULoad = averageCPULoad;
		lastAverageIdleCPU = averageIdleCPU;
		lastMemoryUsed = memoryUsed;
		lastFreeMemory = freeMemory;

		// keep track of last energy measurement
		lastEnergyMeasurement = currentEnergy;

		return currentEnergy;
	}

	// Predict energy consumption in physical machine after the deployment of a specific application, knowing its level of impact
	// in terms of memory, processing and network resource utilisation.
	public double PredictEnergyConsumptionAfterApplicationDeploymentFromLevelOfImpact (float averageCPULoad, float averageIdleCPU, int memoryUsed, int freeMemory, long networkOutboundBytes, long networkIncomingBytes, ImpactLevel processingImpact, ImpactLevel memoryImpact, ImpactLevel outgoingTrafficImpact, ImpactLevel incomingTrafficImpact) {

		// predict impact of application on processing load
		float processingImpactValue = 0;
		if (processingImpact==ImpactLevel.High) {
			processingImpactValue = HighProcessingImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			processingImpactValue = MediumProcessingImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			processingImpactValue = LowProcessingImpactValueCoefficient;
		} 

		// predict impact of application on memory utilisation
		int memoryImpactValue = 0;
		if (memoryImpact==ImpactLevel.High) {
			memoryImpactValue = HighMemoryImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			memoryImpactValue = MediumMemoryImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			memoryImpactValue = LowMemoryImpactValueCoefficient;
		}

		// predict impact of application on outgoing traffic
		long outgoingTrafficImpactValue = 0;
		if (outgoingTrafficImpact==ImpactLevel.High) {
			outgoingTrafficImpactValue = HighOutgoingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			outgoingTrafficImpactValue = MediumOutgoingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			outgoingTrafficImpactValue = LowOutgoingTrafficValueCoefficient;
		}

		// predict impact of application on incoming traffic
		long incomingTrafficImpactValue = 0;
		if (incomingTrafficImpact==ImpactLevel.High) {
			incomingTrafficImpactValue = HighIncomingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			incomingTrafficImpactValue = MediumIncomingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			incomingTrafficImpactValue = LowIncomingTrafficValueCoefficient;
		}

		double predictedEnergy = baseLineEnergyConsumption +  ProcessingConsumptionFunction (averageCPULoad + processingImpactValue, averageIdleCPU - processingImpactValue) + MemoryConsumptionFunction (memoryUsed + memoryImpactValue, freeMemory - memoryImpactValue) + NetworkLoadConsumptionFunction (networkOutboundBytes + outgoingTrafficImpactValue, networkIncomingBytes + incomingTrafficImpactValue);

		System.out.println ("Predicted Energy Consumption of application: baseline " + baseLineEnergyConsumption+" processing " + ProcessingConsumptionFunction (averageCPULoad + processingImpactValue, averageIdleCPU - processingImpactValue)+ " memory " + MemoryConsumptionFunction (memoryUsed + memoryImpactValue, freeMemory - memoryImpactValue) + " network " + NetworkLoadConsumptionFunction (networkOutboundBytes + outgoingTrafficImpactValue, networkIncomingBytes + incomingTrafficImpactValue));

		return predictedEnergy;
	}

	// Predict energy consumption in physical machine after the deployment of a specific application, knowing its level of impact
	// in terms of memory, processing and network resource utilisation (uses latest measurements)
	public double PredictEnergyConsumptionAfterApplicationDeploymentFromLevelOfImpact (ImpactLevel processingImpact, ImpactLevel memoryImpact, ImpactLevel outgoingTrafficImpact, ImpactLevel incomingTrafficImpact) {
		// predict impact of application on processing load
		float processingImpactValue = 0;
		if (processingImpact==ImpactLevel.High) {
			processingImpactValue = HighProcessingImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			processingImpactValue = MediumProcessingImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			processingImpactValue = LowProcessingImpactValueCoefficient;
		} 

		// predict impact of application on memory utilisation
		int memoryImpactValue = 0;
		if (memoryImpact==ImpactLevel.High) {
			memoryImpactValue = HighMemoryImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			memoryImpactValue = MediumMemoryImpactValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			memoryImpactValue = LowMemoryImpactValueCoefficient;
		}

		// predict impact of application on outgoing traffic
		long outgoingTrafficImpactValue = 0;
		if (outgoingTrafficImpact==ImpactLevel.High) {
			outgoingTrafficImpactValue = HighOutgoingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			outgoingTrafficImpactValue = MediumOutgoingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			outgoingTrafficImpactValue = LowOutgoingTrafficValueCoefficient;
		}

		// predict impact of application on incoming traffic
		long incomingTrafficImpactValue = 0;
		if (incomingTrafficImpact==ImpactLevel.High) {
			incomingTrafficImpactValue = HighIncomingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Medium) {
			incomingTrafficImpactValue = MediumIncomingTrafficValueCoefficient;
		} else if (processingImpact==ImpactLevel.Low) {
			incomingTrafficImpactValue = LowIncomingTrafficValueCoefficient;
		}

		double predictedEnergy = baseLineEnergyConsumption +  ProcessingConsumptionFunction (lastAverageCPULoad + processingImpactValue, lastAverageIdleCPU - processingImpactValue) + MemoryConsumptionFunction (lastMemoryUsed + memoryImpactValue, lastFreeMemory - memoryImpactValue) + NetworkLoadConsumptionFunction (lastNetworkOutboundBytes + outgoingTrafficImpactValue, lastNetworkIncomingBytes + incomingTrafficImpactValue);

		System.out.println ("Predicted Energy Consumption of application: baseline " + baseLineEnergyConsumption+" processing " + ProcessingConsumptionFunction (lastAverageCPULoad + processingImpactValue, lastAverageIdleCPU - processingImpactValue)+ " memory " + MemoryConsumptionFunction (lastMemoryUsed + memoryImpactValue, lastFreeMemory - memoryImpactValue) + " network " + NetworkLoadConsumptionFunction (lastNetworkOutboundBytes + outgoingTrafficImpactValue, lastNetworkIncomingBytes + incomingTrafficImpactValue));

		return predictedEnergy;
	}

	// Predict energy consumption in physical machine after the deployment of a specific application, knowing its average impact
	// in terms of memory, processing and network resource utilisation.
	public double PredictEnergyConsumptionAfterApplicationDeploymentFromHistoricalInformation (float averageCPULoad, float averageIdleCPU, int memoryUsed, int freeMemory, long networkOutboundBytes, long networkIncomingBytes, float applicationAverageProcessingImpact, int applicationAverageMemoryImpact, long applicationAverageIncomingTrafficImpact, long applicationAverageOutgoingTrafficImpact) {
		double predictedEnergy = baseLineEnergyConsumption +  ProcessingConsumptionFunction (averageCPULoad + applicationAverageProcessingImpact, averageIdleCPU - applicationAverageProcessingImpact) + MemoryConsumptionFunction (memoryUsed + applicationAverageMemoryImpact, freeMemory - applicationAverageMemoryImpact) + NetworkLoadConsumptionFunction (networkOutboundBytes + applicationAverageOutgoingTrafficImpact, networkIncomingBytes - applicationAverageIncomingTrafficImpact);

		System.out.println ("Energy Consumption: baseline " + baseLineEnergyConsumption+" processing " + ProcessingConsumptionFunction (averageCPULoad, averageIdleCPU)+ " memory " + MemoryConsumptionFunction (memoryUsed, freeMemory) + " network " + NetworkLoadConsumptionFunction (networkOutboundBytes, networkIncomingBytes));

		return predictedEnergy;
	}

	// Predict energy consumption in physical machine after the deployment of a specific application, knowing its average impact
	// in terms of memory, processing and network resource utilisation (uses latest measurements)
	public double PredictEnergyConsumptionAfterApplicationDeploymentFromHistoricalInformation (float applicationAverageProcessingImpact, int applicationAverageMemoryImpact, long applicationAverageIncomingTrafficImpact, long applicationAverageOutgoingTrafficImpact) {
		double predictedEnergy = baseLineEnergyConsumption +  ProcessingConsumptionFunction (lastAverageCPULoad + applicationAverageProcessingImpact, lastAverageIdleCPU - applicationAverageProcessingImpact) + MemoryConsumptionFunction (lastMemoryUsed + applicationAverageMemoryImpact, lastFreeMemory - applicationAverageMemoryImpact) + NetworkLoadConsumptionFunction (lastNetworkOutboundBytes + applicationAverageOutgoingTrafficImpact, lastNetworkIncomingBytes + applicationAverageIncomingTrafficImpact);

		System.out.println ("Energy Consumption: baseline " + baseLineEnergyConsumption+" processing " + ProcessingConsumptionFunction (lastAverageCPULoad, lastAverageIdleCPU)+ " memory " + MemoryConsumptionFunction (lastMemoryUsed, lastFreeMemory) + " network " + NetworkLoadConsumptionFunction (lastNetworkOutboundBytes, lastNetworkIncomingBytes));

		return predictedEnergy;
	}

	// returns cumulative energy consumption of particular physical machine
	public double GetCumulativeEnergyConsumption () {
		return cumulativeEnergyConsumption;
	}

	// returns average energy consumption of particular physical machine
	public double GetAverageEnergyConsumption () {
		return cumulativeEnergyConsumption / totalTime;
	}

	// function that estimates energy consumption based on the averageCPULoad - could be extended to consider more than one cpu,
	// multiple cpu running states etc.
	// returns a value in Watts (at this point we assume to running states, working and idle)
	private double ProcessingConsumptionFunction (double averageCPULoad, double averageCPUIdle) {
		// start by having a linear approach
		return cpuLoadCoefficient * averageCPULoad + cpuIdleCoefficient * averageCPUIdle;
	}

	// function that estimates energy consumption based on the memory utilisation.
	// returns a value in Watts
	private double MemoryConsumptionFunction (int memoryUsed, int freeMemory) {
		// start by having a linear approach
		return memoryAllocationCoefficient * memoryUsed + freeMemoryCoefficient * freeMemory;
	}

	// function that estimates energy consumption based on the network load - could be extended to consider multiple running states
	// of network card, etc.
	// returns a value in Watts - currently assuming two running states: send and receive
	private double NetworkLoadConsumptionFunction (long networkOutboundBytes, long networkIncomingBytes) {

		// start by having a linear approach
		return networkOutboundBytesCoefficient * networkOutboundBytes + networkIncomingBytesCoefficient * networkIncomingBytes;
	}
}
