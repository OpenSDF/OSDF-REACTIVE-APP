from mininet.node import Controller, OVSSwitch, UserSwitch
from mininet.net import Mininet
from mininet.log import setLogLevel, info, warn, error, debug
from mininet.cli import CLI
from mininet.util import quietRun, specialClass
from mininet.topo import Topo
from mininet.link import TCLink
from mininet.node import RemoteController, Host


class MyTopo(Topo):
    def __init__(self):

        Topo.__init__(self)

        coreMesh = []

        counter = 0
        for i in range(1, 4):
            sw = self.addSwitch("s%s" % i)
            coreMesh.append(sw)
            for j in range(0, 1):
                host = self.addHost('h' + str(i) + str(j + 1)
                                    , cls=IpHost
                                    , gateway='10.0.%s.254' % ((1))
                                    , ip='10.0.%s.%s/24' % ((1), (counter + 1)))
                self.addLink(host, sw)
                counter = counter + 1

        counter = 0
        remaining = list(coreMesh)

        swRegion1 = remaining[0]
        while True:
            first = remaining[0]
            remaining.remove(first)
            for switch in tuple(remaining):
                if switch is not first:
                    self.addLink(switch, first)

            if not remaining:
                break
        coreMesh = []
        for i in range(4, 8):
            sw = self.addSwitch("s%s" % i)
            coreMesh.append(sw)
            for j in range(0, 1):
                host = self.addHost('h' + str(i) + str(j + 1)
                                    , cls=IpHost
                                    , gateway='10.0.%s.254' % ((2))
                                    , ip='10.0.%s.%s/24' % ((2), (counter + 1)))
                self.addLink(host, sw)
                counter = counter + 1

        remaining = list(coreMesh)
        swRegion2 = remaining[0]
        while True:
            first = remaining[0]
            remaining.remove(first)
            for switch in tuple(remaining):
                if switch is not first:
                    self.addLink(switch, first)

            if not remaining:
                break

        coreMesh = []

        for i in range(8, 13):
            sw = self.addSwitch("s%s" % i)
            coreMesh.append(sw)
            for j in range(0, 1):
                host = self.addHost('h' + str(i) + str(j + 1)
                                    , cls=IpHost
                                    , gateway='10.0.%s.254' % ((3))
                                    , ip='10.0.%s.%s/24' % ((3), (counter + 1)))
                self.addLink(host, sw)
                counter = counter + 1

        remaining = list(coreMesh)
        swRegion3 = remaining[0]
        while True:
            first = remaining[0]
            remaining.remove(first)
            for switch in tuple(remaining):
                if switch is not first:
                    self.addLink(switch, first)

            if not remaining:
                break

        self.addLink(swRegion2, swRegion1)
        self.addLink(swRegion3, swRegion2)


class IpHost(Host):
    def __init__(self, name, gateway, *args, **kwargs):
        super(IpHost, self).__init__(name, *args, **kwargs)
        self.gateway = gateway

    def config(self, **kwargs):
        Host.config(self, **kwargs)
        mtu = "ifconfig " + self.name + "-eth0 mtu 1490"
        self.cmd(mtu)
        self.cmd('ip route add default via %s' % self.gateway)


if __name__ == '__main__':
    topo = MyTopo()
    net = Mininet(topo, autoSetMacs=True, xterms=False, controller=RemoteController)
    net.addController('c', ip='127.0.0.1')  # localhost:127.0.0.1 vm-to-mac:10.0.2.2 server-to-mac:128.112.93.28
    print "\nHosts configured with IPs, switches pointing to OpenVirteX at 128.112.93.28 port 6633\n"
    net.start()
    CLI(net)
    net.stop()
