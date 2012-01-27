/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.eip.dsl
import org.junit.{Assert, Test}
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.core.PollableChannel
import org.springframework.context.support.GenericApplicationContext
import java.lang.Thread

/**
 * @author Oleg Zhurakousky
 */
class CompositionInitializationTests {

  @Test
  def validateEnterableComposition(){
    val compositionA = Channel("foo")
    Assert.assertTrue(compositionA.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionB = Channel("foo").withDispatcher(failover = true)
    Assert.assertTrue(compositionB.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionC = Channel("foo").withQueue()
    Assert.assertTrue(compositionC.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionD = compositionA --> handle.using("spel")
    Assert.assertTrue(compositionD.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionE = compositionB --> handle.using("spel")
    Assert.assertTrue(compositionE.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionF = compositionC --> poll.usingFixedRate(4) --> transform.using("spel")
    Assert.assertTrue(compositionF.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionG = Channel("a") --> Channel("b")
    Assert.assertTrue(compositionG.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionH = Channel("a") --> handle.using("") --> transform.using("")  --> handle.using("")
    Assert.assertTrue(compositionH.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionI = Channel("a") --> (handle.using("") --> transform.using(""))  --> handle.using("")
    Assert.assertTrue(compositionI.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionJ = Channel("a") --> handle.using("spel") --> Channel("b") --> handle.using("spel")
    Assert.assertTrue(compositionJ.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionK = Channel("a")  --> Channel("b") --> handle.using("spel")
    Assert.assertTrue(compositionK.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionL = Channel("a").withQueue() --> poll.usingFixedRate(4)  --> Channel("b")
    Assert.assertTrue(compositionL.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionM = Channel("a").withQueue() --> poll.usingFixedRate(4)  --> Channel("b") --> handle.using("spel")
    Assert.assertTrue(compositionM.isInstanceOf[CompletableEIPConfigurationComposition])

    // non-CompletableComposition
    val compositionAn = Channel("a").withQueue() --> poll.usingFixedDelay(5)
    Assert.assertFalse(compositionAn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionBn = handle.using("") --> transform.using("")
    Assert.assertFalse(compositionBn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionCn = handle.using("") --> Channel("")
    Assert.assertFalse(compositionCn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionDn = handle.using("") --> transform.using("")--> Channel("")
    Assert.assertFalse(compositionDn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionEn = handle.using("") --> Channel("") --> transform.using("")--> Channel("")
    Assert.assertFalse(compositionEn.isInstanceOf[CompletableEIPConfigurationComposition])

    val compositionFn = handle.using("") --> Channel("").withQueue(8) --> poll.usingFixedDelay(3)
    Assert.assertFalse(compositionFn.isInstanceOf[CompletableEIPConfigurationComposition])
  }
//  @Test
//  def validateEIPContext(){
//
//    // the below should be illegal since it is not a completable composition
//    // EIPContext(handle.using("spel"))
//    // EIPContext(handle.using("spel") --> transform.using("spel"))
//
//    //implicit val ac:ApplicationContext = null
//
//    EIPContext(Channel("foo"))
//
//    EIPContext(Channel("foo"), Channel("bar").withQueue())
//
//    EIPContext(
//      Channel("a") -->
//        handle.using("") -->
//        transform.using("spel")  -->
//        handle.using("spel")
//    )
//
//    EIPContext(
//      Channel("a") -->
//        handle.using("") -->
//        transform.using("spel")  -->
//        handle.using("spel"),
//
//      Channel("bar").withQueue(4) --> poll.usingFixedDelay(5) -->
//        handle.using("spel")
//    )
//
//    EIPContext(new GenericApplicationContext)(
//      Channel("a") -->
//        handle.using("") -->
//        transform.using("spel")  -->
//        handle.using("spel"),
//
//      Channel("bar").withQueue(4) --> poll.usingFixedDelay(5) -->
//        handle.using("spel")
//    )
//  }
//
//  @Test
//  def eipChannelInitializationTest() {
//
//    val channelConfigC = Channel("cChannel").withQueue(5)
//
//    val channelConfigD = Channel("dChannel").withDispatcher(failover = true)
//
//    val context = EIPContext(
//      Channel("aChannel") -->
//        handle.using("spel") -->
//        transform.using("spel")  -->
//        Channel("myChannel") -->
//        handle.using("spel"),
//
//      channelConfigC --> poll.usingFixedDelay(5) -->
//        handle.using("spel"),
//
//      channelConfigD -->
//        Channel("hello") -->        // bridge
//        transform.using("spel"),
//
//      Channel("queueChannel").withQueue(5) --> poll.usingFixedDelay(5) -->
//        Channel("fromQueueChannel") -->        // pollable bridge
//        transform.using("spel") -->
//        Channel("foo") -->
//        handle.using("spel") -->
//        transform.using("spel")
//    )
//
//
//
//   // context.send("aChannel")("ghgjgj")
//
//    /*
//    So we can fail on context.send() but. . .
//    we can determine the type of payload and as long as Channels are data-type-channels we cna route internally to the appropriate channel
//    and only fail if ambiguity is detected
//     */
//
////    context.using("aChannel").send("")
////
////    context.send("")
//
////    val channelA = context.channel("aChannel")
////    channelA.send{s:String => s}
////    channelA.send("hello")
////    channelA.send(new Object)
////    Assert.assertTrue(channelA.isInstanceOf[DirectChannel])
////
////    val channelC = context.channel(channelConfigC)
////    Assert.assertTrue(channelC.isInstanceOf[PollableChannel])
//  }

  @Test
  def foo(){
    
//    class Bar
//
    val messageFlowA = Channel("foo") --> handle.using("spel")
    val messageFlowB = transform.using("SPEL") --> handle.using("spel")
    val mergedComp =  messageFlowA --> messageFlowB
    EIPContext(mergedComp)
    println()

//   EIPContext(messageFlowA --> messageFlowB)
//
//    messageFlowA.send("")
//    messageFlowA.send("")
//    messageFlowB.send("")
//
//    val messageFlowC =  handle.using("SPEL-A") --> transform.using("SPEL-B") --> handle.using("SPEL-C")
//    messageFlowC.send("")
//    messageFlowC.send("")
    
//    val compA = Channel("foo") --> handle.using("A")
//    val compB = handle.using("B") --> transform.using("C")
//    val compC = compB.copy(compB.parentComposition, compB.target)
//
//
//    println("compB - " + compB)
//    println("compC - " + compC)
//    val field = classOf[EIPConfigurationComposition].getDeclaredField("parentComposition")
//    field.setAccessible(true)
//    field.set(compC, null)
//
//    println("compB - " + compB)
//    println("compC - " + compC)


//    new Thread(new Runnable{
//       def run(){
//         messageFlowA.send("")
//       }
//    }).start();
    //messageFlowA.sendAndReceive[Int]("hello").to

//    flow.send("")
//
//    flow.name("s").send("")
    //GOOD
//    val flow = Channel("foo") --> handle.using("spel") --> Channel("queue").withQueue(5)
//    flow.send("")
//
//    flow.name("s").send("")
//
//    val reply = flow.receive("")
//
//    val reply = flow.sendAndReceive("")    // an equivalent of MessagingGateway where its even more
//    // since we cna have request only or request/reply gateway (unlike in SI XML)
    // sendAndReceiveLater() would return Future  (a.k.a, asyncSendAndReceive())
//   // GOOD
    
//    val myService = handle.using("spel").where(name="myService")
//
//    val flowA = Channel("foo") --> handle.using("spel")
//
//    val flowB = Channel("bar") --> myService --> Channel("queue").withQueue(5)
//
//    val mainFlow = flowA --> flowB
//
//
//    mainFlow.send()
//
//    mainFlow.using("").send()
//
//    flowB.send()
  }

}

