package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.TableColumn
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared

import scala.language.postfixOps
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterCharacteristics, DatacenterSimple}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*
import scala.::
import scala.jdk.javaapi.CollectionConverters.asJava
import scala.util.Random

class Simulation2

// This is a simulation with a mix of three or more datacenters (SaaS, PaaS, IaaS)
// In this simulation, a broker will decide which data center to connect to based on the config provided by the client
// for example, in Iaas, the client provides all the configuration of the Vms as per their choice
// In PaaS, the client provides the number of Vms

object Simulation2 extends App:

  def totalCostOfCloudlet(xs: Seq[CloudletSimple]): Double = {
    if(xs.isEmpty) 0
    else xs.head.getTotalCost + totalCostOfCloudlet(xs.tail)
  }
  val config: Config = ConfigFactory.load("simulation2.conf")
  val logger = CreateLogger(classOf[Simulation2])
  val utilizationModel = new UtilizationModelDynamic(config.getDouble("simulation2.utilizationRatio"));
  val vmList = createVM(4)
  val cloudletList = createCloudlets(30)

  val cloudletListSaaS = createCloudletsSaas(12)

  // time shared cloudlet scheduler for saas
  def createCloudletsSaas(numCloudlets: Int) = {
    (1 to numCloudlets).map(_ => new CloudletSchedulerTimeShared()
    )  }

  val hostPes = List(new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
    new PeSimple(config.getLong("simulation2.host.mipsCapacity")),
  )

  val hostList = List(new HostSimple(config.getLong("simulation2.host.RAMInMBs"),
    config.getLong("simulation2.host.StorageInMBs"),
    config.getLong("simulation2.host.BandwidthInMBps"),
    hostPes.asJava).setVmScheduler(new VmSchedulerTimeShared()),new HostSimple(config.getLong("simulation2.host.RAMInMBs"),
    config.getLong("simulation2.host.StorageInMBs"),
    config.getLong("simulation2.host.BandwidthInMBps"),
    hostPes.asJava) )
  def createVM(num : Int)  = {
    (1 to num).map(_ => new VmSimple(1000, 4)
      .setRam(config.getLong("simulation2.vm.RAMInMBs"))
      .setBw(config.getLong("simulation2.vm.BandwidthInMBps"))
      .setSize(config.getLong("simulation2.vm.StorageInMBs"))).toList
  }

  def createVMByUser(num: Int) = {
    (1 to num).map(_ => new VmSimple(1000, 4)
      .setRam(1000)
      .setBw(1000)
      .setSize(1200)).toList
  }

  def createCloudlets(numCloudlets: Int)= {
    (1 to numCloudlets).map(_ => new CloudletSimple(config.getLong("simulation2.cloudlet.size"), config.getInt("simulation2.cloudlet.PEs"), utilizationModel)
    )
  }
  def createVMList(config: Config,  infrastructure: String = ""): List[Vm] =
    infrastructure match {
      // the number of vms are defined by the user as well as all the characteristics
      case "IaaS" => createVMByUser(3).toList
      // the vm characteristics are Not configured by the user
      case "SaaS"  => createVM(3).toList
      // the number of vms are configured by the user and they do not have to worry about managing vms but can assign policies to vms
      case "PaaS"  =>  createVM(3).toList

    }

  // function that creates a SaaS datacenter
  def createDCs(sim: CloudSim , config : Config) : DatacenterSimple = {
    new NetworkDatacenter(sim, hostList.asJava, new VmAllocationPolicyBestFit)
  }

  main()
  def main() =
    val cloudsim = new CloudSim();
    val SaaSDataCenter = createDCs(cloudsim,config)
    val PaaSDataCenter = createDCs(cloudsim,config)
    val IaasDataCenter = createDCs(cloudsim,config)

    val broker0 = new DatacenterBrokerSimple(cloudsim);
    val broker1 = new DatacenterBrokerSimple(cloudsim);
   // when using iaas, the data center will create vms based on user config
    val infrastureVM = createVMList(config, "IaaS")

   // when using saas, the vms are created from the data center config file
    val softwareVM =createVMList(config,  "SaaS")

   // when using platformVM, the user doesnt not worry about scalabity
    val platformVM =createVMList(config,  "PaaS")


     logger.info(s"Created one host: $hostList")


    logger.info(s"Created one virtual machine: ${vmList.size}")


    logger.info(s"Created a list of cloudlets: $cloudletList")

    broker0.submitVmList((infrastureVM ::: softwareVM ::: platformVM).asJava)
    broker0.submitCloudletList(cloudletList.asJava)
    broker0.submitCloudletList((cloudletList ).asJava)
    logger.info("Starting cloud simulation...")

    cloudsim.start();
//    print(cloudletList.head.getUtilizationOfRam())
    print(totalCostOfCloudlet(cloudletList))
    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();



