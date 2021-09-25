package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}

import scala.language.postfixOps
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*

class BasicCloudSimPlusExample

object BasicCloudSimPlusExample extends App:
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])
  Start()
  def Start() =
    val cloudsim = new CloudSim();
    val broker0 = new DatacenterBrokerSimple(cloudsim);

    val hostPes = List(new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
//      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
//      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
//      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
    )
//    logger.info(s"Created one processing element: $hostPes")

//    def createHost = {
//      val peList = (1 to 2).map { _ =>
//        new PeSimple(1000).asInstanceOf[Pe]
//      }.toList
//      val hosts_ram: Long = 2048
//      val host_bw: Long = 10000
//      val storage: Long = 100000
//
//      new HostSimple(hosts_ram, host_bw, storage, peList.asJava)
//    }
//    val hostList = createHost
    val hostList = List(new HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
      config.getLong("cloudSimulator.host.StorageInMBs"),
      config.getLong("cloudSimulator.host.BandwidthInMBps"),
      hostPes.asJava))
//      val hosts_ram: Long = 2048
//      val host_bw: Long = 10000
//      val storage: Long = 100000
//      val hostList = List(new HostSimple(hosts_ram,host_bw,storage,hostPes.asJava))
//    hostList :+ new HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
//      config.getLong("cloudSimulator.host.StorageInMBs"),
//      config.getLong("cloudSimulator.host.BandwidthInMBps"),
//      hostPes.asJava)
//    val hostSimple = HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
//      config.getLong("cloudSimulator.host.StorageInMBs"),
//      config.getLong("cloudSimulator.host.BandwidthInMBps"),
//      hostPes.asJava);
      def createVM = {
        (1 to 3).map(_ => new VmSimple(1000, 3)
          .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
          .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
          .setSize(config.getLong("cloudSimulator.vm.StorageInMBs"))).toList
      }
      val vmList = createVM

    logger.info(s"Created one host: $hostList")

    val dc0 = new DatacenterSimple(cloudsim, hostList.asJava);
    dc0.setVmAllocationPolicy(new VmAllocationPolicyBestFit);

//    val vmList = List(
//      new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
//      .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
//      .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
//      .setSize(config.getLong("cloudSimulator.vm.StorageInMBs")),
//      new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
//        .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
//        .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
//        .setSize(config.getLong("cloudSimulator.vm.StorageInMBs"))
//    )
//    vmList :+ List(new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
//      .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
//      .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
//      .setSize(config.getLong("cloudSimulator.vm.StorageInMBs")))

    logger.info(s"Created one virtual machine: ${vmList.size}")

    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator.utilizationRatio"));
    val cloudletList = new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel) ::
      new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel) ::
      new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel) :: new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel)
      :: Nil

    logger.info(s"Created a list of cloudlets: $cloudletList")

    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava)
    logger.info("Starting cloud simulation...")
    cloudsim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
