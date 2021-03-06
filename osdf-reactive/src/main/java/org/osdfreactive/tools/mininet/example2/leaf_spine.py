from mininet.cli import CLI
from mininet.net import Mininet
from mininet.node import RemoteController, Host
from mininet.topo import Topo


class IpHost(Host):
    def __init__(self, name, gateway, *args, **kwargs):
        super(IpHost, self).__init__(name, *args, **kwargs)
        self.gateway = gateway

    def config(self, **kwargs):
        Host.config(self, **kwargs)
        mtu = "ifconfig " + self.name + "-eth0 mtu 1490"
        self.cmd(mtu)
        self.cmd('ip route add default via %s' % self.gateway)


class LeafAndSpine(Topo):
    def __init__(self, spine=2, leaf=2, fanout=2, **opts):
        "Create Leaf and Spine Topo."

        Topo.__init__(self, **opts)

        # Add spine switches


        spines = {}
        for s in range(spine):
            spines[s] = self.addSwitch('spine40%s' % (s + 1))
        # Set link speeds to 100Mb/s
        linkopts = dict()

        # Add Leaf switches
        for ls in range(leaf):
            leafSwitch = self.addSwitch('leaf%s' % (ls + 1))
            # Connect leaf to all spines
            for s in range(spine):
                switch = spines[s]
                self.addLink(leafSwitch, switch, **linkopts)
            # Add hosts under a leaf, fanout hosts per leaf switch
            for f in range(fanout):
                host = self.addHost('h%s' % (ls * fanout + f + 1))  # ,
                # cls=IpHost,
                # gateway='10.0.%s.254' % ((ls + 1)),
                # ip='10.0.%s.%s/24' % ((ls + 1), (f + 1)))

                print(host)
                self.addLink(host, leafSwitch, **linkopts)


if __name__ == '__main__':
    topo = LeafAndSpine(3, 2, 2)
    net = Mininet(topo, autoSetMacs=True, xterms=False, controller=RemoteController)
    net.addController('c', ip='127.0.0.1')  # localhost:127.0.0.1 vm-to-mac:10.0.2.2 server-to-mac:128.112.93.28
    print "\nHosts configured with IPs, switches pointing to OpenVirteX at 128.112.93.28 port 6633\n"
    net.start()
    CLI(net)
    net.stop()
