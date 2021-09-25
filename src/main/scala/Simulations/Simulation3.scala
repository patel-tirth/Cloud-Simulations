package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.TableColumn
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared

import scala.language.postfixOps
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{DatacenterCharacteristics, DatacenterSimple}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*
import scala.jdk.javaapi.CollectionConverters.asJava

// First simulation time with TimeShared VmSchedular as well as best fit Vm Allocation Policy
class Simulation3

object Simulation3 extends App:
  //  val config = ObtainConfigReference("cloudSimulator") match {
  //    case Some(value) => value
  //    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  //  }
  def totalCostOfCloudlet(xs: Seq[CloudletSimple]): Double = {
    if(xs.isEmpty) 0
    else xs.head.getTotalCost + totalCostOfCloudlet(xs.tail)
  }
  val config: Config = ConfigFactory.load("simulation3.conf")
  val logger = CreateLogger(classOf[Simulation3])
  val utilizationModel = new UtilizationModelDynamic(config.getDouble("simulation3.utilizationRatio"));
  val vmList = createVM(4)
  val cloudletList = createCloudlets(20)
  def createVM(num : Int)  = {
    (1 to num).map(_ => new VmSimple(1000, 4)
      .setRam(config.getLong("simulation3.vm.RAMInMBs"))
      .setBw(config.getLong("simulation3.vm.BandwidthInMBps"))
      .setSize(config.getLong("simulation3.vm.StorageInMBs"))).toList
  }
  def createCloudlets(numCloudlets: Int)= {
    (1 to numCloudlets).map(_ => new CloudletSimple(config.getLong("simulation3.cloudlet.size"), config.getInt("simulation3.cloudlet.PEs"), utilizationModel)
    )
  }

  main()
  def main() =
    val cloudsim = new CloudSim();
    val broker0 = new DatacenterBrokerSimple(cloudsim);

    val cost = config.getDouble("simulation3.datacenter.cost")
    val costPerBw = config.getDouble("simulation3.datacenter.costPerBw")
    val costPerMem = config.getDouble("simulation3.datacenter.costPerMem")
    val costPerStorage = config.getDouble("simulation3.datacenter.costPerStorage")
    val costPerSecond = config.getDouble("simulation3.datacenter.costPerSecond")
    val hostPes = List(new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
      new PeSimple(config.getLong("simulation3.host.mipsCapacity")),
    )

    val hostList = List(new HostSimple(config.getLong("simulation3.host.RAMInMBs"),
      config.getLong("simulation3.host.StorageInMBs"),
      config.getLong("simulation3.host.BandwidthInMBps"),
      hostPes.asJava).setVmScheduler(new VmSchedulerSpaceShared()))
    logger.info(s"Created one host: $hostList")

    val dc0 = new NetworkDatacenter(cloudsim, hostList.asJava, new VmAllocationPolicyFirstFit())
    dc0.getCharacteristics.setCostPerBw(costPerBw).setCostPerMem(costPerMem).setCostPerStorage(costPerStorage).setCostPerSecond(costPerSecond)
//    dc0.setVmAllocationPolicy(new VmAllocationPolicyBestFit);

    logger.info(s"Created one virtual machine: ${vmList.size}")


    logger.info(s"Created a list of cloudlets: $cloudletList")

    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava)
    logger.info("Starting cloud simulation...")
    cloudsim.start();
    print("Total Cost: "+ totalCostOfCloudlet(cloudletList))
    //    print(cloudletList.head.getUtilizationOfRam())
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
