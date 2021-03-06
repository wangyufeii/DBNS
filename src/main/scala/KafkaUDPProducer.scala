import java.util.HashMap
import org.apache.kafka.clients.producer.{ProducerConfig, KafkaProducer, ProducerRecord}
import java.util.concurrent.{Executors, ExecutorService}
import java.net.{DatagramPacket,DatagramSocket,InetAddress}
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


object LogUDPProducer{
  
  class Sender(producer:KafkaProducer[String,String],topic:String,fg:Int,queue:BlockingQueue[DatagramPacket]) extends Runnable{
    def run(){
      while (true)
      {
        val packet = queue.take()
        val sentence:String = new String(packet.getData(),0,packet.getLength()-1)
        if (fg == 1) println(Thread.currentThread().getName())
        if (fg == 1) println(topic+": "+sentence)
        val message = new ProducerRecord[String, String](topic, null, sentence)
        producer.send(message)
      }
    }
  }

  def main(args: Array[String]) {
    val Array(broker,flag1,flag2,topic,port) = args

    /*Defualt Configuration
    val topic:String = "test"
    val broker:String = "spark-master:9092"
    val flag1 = 1 //Show the screen flag
    val flag2 = 1 //Use Fixed Pool
    val port:Int = 9999*/

    val portNum:Int = port.toInt
    val fg1:Int = flag1.toInt
    val fg2:Int = flag2.toInt
        
    val props = new HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)// Send some messages
    val serverSocket:DatagramSocket = new DatagramSocket(portNum)
    val receiveData = new Array[Byte](1024)
    val blockingQueue:BlockingQueue[DatagramPacket] = new LinkedBlockingQueue[DatagramPacket](1000)
    val threadPool:ExecutorService=Executors.newFixedThreadPool(8)
    for (i <- 0 to 7){
      threadPool.execute(new Sender(producer,topic,fg1,blockingQueue))
    }

    while(true)
    {
      val receivePacket:DatagramPacket = new DatagramPacket(receiveData, receiveData.length)
      serverSocket.receive(receivePacket)
      blockingQueue.put(receivePacket)
    }
  }
}
