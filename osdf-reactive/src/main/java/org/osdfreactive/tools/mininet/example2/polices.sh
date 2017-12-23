#!/bin/bash

# Script Configuration:
#
# host     : the controller instance against which this script is run

host=${1:-127.0.0.1}


onos ${host} <<-EOF
   app deactivate org.onosproject.fwd
   intra-route -a PING -region siteA -psa  ON_DEMAND --via of:0000000000000192/1 policy1
   intra-route -a WEB -region siteA -psa  ON_DEMAND --via of:0000000000000191/1 policy2
EOF
