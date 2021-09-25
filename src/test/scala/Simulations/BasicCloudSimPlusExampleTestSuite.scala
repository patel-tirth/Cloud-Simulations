package Simulations

import Simulations.BasicCloudSimPlusExample.config
import Simulations.Simulation1.config
import Simulations.Simulation2.config
import Simulations.Simulation1
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.language.postfixOps
class BasicCloudSimPlusExampleTestSuite extends AnyFlatSpec with Matchers {

  behavior of "number of Vms"

  it should "obtain the number of Vms" in {
    Simulation1.createVM(5).size shouldBe 5
  }

  behavior of "cloudlets"

  it should "obtain the number of cloudlets" in {
    Simulation1.createCloudlets(30).size shouldBe 30
  }

  behavior of "createVm list according to infrastructure"

  it should "obtain the number of vms created in IaaS infrastructure" in {
    Simulation2.createVMList(Simulation2.config, "IaaS").size shouldBe 3
  }

  it should "obtain the number of ram capacity created in IaaS infrastructure" in {
    Simulation2.createVMList(Simulation2.config, "IaaS").head.getRam
//    print( Simulation2.createVMList(Simulation2.config, "IaaS").head.getRam)
  }

  it should "obtain the number time shared cloudlet" in {
    Simulation2.createCloudletsSaas(2).size shouldBe 2
    //    print( Simulation2.createVMList(Simulation2.config, "IaaS").head.getRam)
  }

}
