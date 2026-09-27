package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a data center with one host and run one cloudlet on it.
 */
public class GenerativeAISimulation {
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
        try {
            // Initialize CloudSim
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            CloudSim.init(numUsers, calendar, false);

            // Create Data Center
            Datacenter datacenter = createDataCenter("GenAI_DataCenter");

            // Create Broker
            DatacenterBroker broker = new DatacenterBroker("GenAI_Broker");
            int brokerId = broker.getId();

            // Define Multiple VMs (Simulating Multiple GPUs for AI)
            List<Vm> vmList = new ArrayList<>();
            int numVMs = 3;
            for (int vmId = 0; vmId < numVMs; vmId++) {
                int mips = 12000 + (vmId * 2000); // Varying processing power
                int ram = 32000; // 32GB RAM
                long bw = 20000;
                long size = 2000000; // 2TB storage
                int pesNumber = 8; // 8 Cores per VM
                String vmm = "Xen";
                Vm vm = new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
                vmList.add(vm);
            }
            broker.submitVmList(vmList);

            // Define AI Workload Cloudlets with Advanced Scheduling
            List<Cloudlet> cloudletList = new ArrayList<>();
            int numCloudlets = 6;
            for (int cloudletId = 0; cloudletId < numCloudlets; cloudletId++) {
                long length = 10_000_000 + (cloudletId * 500_000); // Increasing computational load
                long fileSize = 500;
                long outputSize = 500;
                int numCPU = 4;
                UtilizationModel utilizationModel = new UtilizationModelFull();
                Cloudlet cloudlet = new Cloudlet(cloudletId, length, numCPU, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                cloudlet.setVmId(cloudletId % numVMs); // Assigning cloudlets to different VMs
                cloudletList.add(cloudlet);
            }
            broker.submitCloudletList(cloudletList);

            // Start Simulation
            CloudSim.startSimulation();
            List<Cloudlet> results = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            // Print Results
            for (Cloudlet result : results) {
                System.out.println("Cloudlet " + result.getCloudletId() + " executed on VM " + result.getVmId() + " with status: " + result.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDataCenter(String name)throws Exception {
        List<Host> hostList = new ArrayList<>();
        int numHosts = 2;
        for (int hostId = 0; hostId < numHosts; hostId++) {
            List<Pe> peList = new ArrayList<>();
            int mips = 25000 + (hostId * 5000);
            peList.add(new Pe(0, new PeProvisionerSimple(mips)));

            int ram = 128000; // 128GB RAM
            long storage = 5000000; // 5TB Storage
            int bw = 100000;

            Host host = new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList));
            hostList.add(host);
        }

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double costPerSec = 0.01;
        double costPerMem = 0.002;
        double costPerStorage = 0.001;
        double costPerBw = 0.0005;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            arch, os, vmm, hostList, timeZone, costPerSec, costPerMem, costPerStorage, costPerBw);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}