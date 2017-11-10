package ignite.utils

import ignite.actor.{Iot, IotDevice}
import org.apache.ignite.configuration.{CacheConfiguration, DataStorageConfiguration, IgniteConfiguration}
import org.apache.ignite.lang.IgniteRunnable
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder
import org.apache.ignite.{Ignite, IgniteCache, Ignition}

/**
  * This trait contains appropiated methods to create differents ignite Instance
  * In essence you can create three types of ignite instance:
  * 1) Simple cache : Simple in-memory data base that allows sql operations
  * 2) Clustered instance : Ignite nodes can automatically discover each other,without having to restart the whole cluster,
  * and nodes can bellow to a custom group, for example "remote"
  * 3) Multicast : Uses Multicast to discover other nodes in the grid
  */
object IgniteHelper extends ConfigComponent {

  /**
    * The cache is defined with String value as key, in our case, we use the field 'name' for IotDevice
    *
    */
  def igniteDataGrid(name: String, metrics: Boolean): IgniteCache[String, Iot] = {
    val dataGridType = config.getString("ignite.dataGrid.type")
    dataGridType match {
      case "simple" => igniteSimpleCache(name)(metrics)
      case "cluster" => igniteCluster(name)(metrics)
      case "multicast" => igniteClusterWithMulticast(name)(metrics)
    }
  }

  def configuration(dataGridName: String) = (metrics: Boolean) => {
    val igniteConfig = new IgniteConfiguration()
    igniteConfig.setConsistentId(config.getString("ignite.app.name") + config.getInt("akka.port"))
    // Setting the size of the default memory region to 1GB to achieve this.
    igniteConfig.setDataStorageConfiguration(memConfiguration(1L * 1024 * 1024 * 1024))
    val cacheConfig: CacheConfiguration[String, Iot] = new CacheConfiguration(dataGridName)
    igniteConfig.setCacheConfiguration(cacheConfig)
    cacheConfig.setStatisticsEnabled(metrics)
    indexes(cacheConfig)
    igniteConfig
  }

  /**
    * By default, Ignite nodes consume up to 20% of the RAM available locally
    */
  private def memConfiguration: Long => DataStorageConfiguration =
    size => {
      val storageCfg = new DataStorageConfiguration()
      storageCfg.getDefaultDataRegionConfiguration().setMaxSize(size)
      storageCfg
    }

  /**
    * This is the way to tell ignite that we want to do queries on IotDevice objects
    *
    * @return function : Input[CacheConfiguration] => Output[Unit]
    */
  private def indexes: CacheConfiguration[String, Iot] => Unit = {
    cacheConfig => {
      cacheConfig.setIndexedTypes(Seq(classOf[String], classOf[IotDevice]): _*)
    }
  }

  def igniteSimpleCache(dataGridName: String): Boolean => IgniteCache[String, Iot] = (metrics: Boolean) => {
    val cfg = configuration(dataGridName)(metrics)
    val ignite = Ignition.start(cfg)
    ignite.getOrCreateCache(dataGridName)
  }

  /**
    * Through the IgniteCluster interface you can:
    *  1) Start and stop remote cluster nodes
    *  2) Get a list of all cluster members
    *  3) Create logical Cluster Groups
    *
    *  In this case, the node send a Hello message to other nodes in cluster
    */
  def igniteCluster(dataGridName: String): Boolean => IgniteCache[String, Iot] = (metrics: Boolean) => {
    val ignite: Ignite = Ignition.start(configuration(dataGridName)(metrics))
    val igniteCluster = ignite.cluster() //IgniteCluster interface
    val igniteCache: IgniteCache[String, Iot] = igniteCluster.ignite().getOrCreateCache(dataGridName)
    //Run this code in Only in remote nodes
    igniteCluster.forRemotes().ignite().compute().broadcast(new IgniteRunnable {
      override def run(): Unit = println(s"Hello node ${igniteCluster.localNode().consistentId()}, this message had been send by igniteCompute broadcast")
    })
    igniteCache
  }

  /**
    * See {@link https://apacheignite.readme.io/docs/cluster-config}
    *
    */
  def igniteClusterWithMulticast(dataGridName: String): Boolean => IgniteCache[String, Iot] = (metrics: Boolean) => {
    val cfg: IgniteConfiguration = configuration(dataGridName)(metrics)
    val spi = new TcpDiscoverySpi()
    spi.setLocalPort(48500)
    spi.setLocalPortRange(20)
    val ipFinder = new TcpDiscoveryMulticastIpFinder()
    ipFinder.setMulticastGroup("228.10.10.157")
    spi.setIpFinder(ipFinder)
    cfg.setDiscoverySpi(spi)

    val ignite: Ignite = Ignition.start(cfg)
    ignite.cluster().ignite().getOrCreateCache(dataGridName)
  }

}
