#!/bin/bash

# Script Configuration:
#
# host     : the controller instance against which this script is run

host=${1:-127.0.0.1}


onos ${host} <<-EOF
   app deactivate org.onosproject.fwd
   intra-route -a PING -region siteA  policy1
   intra-route -a PING -region siteB  policy2
   intra-route -a PING -region siteC  policy3
   inter-route -a WEB -srcRegion siteA -dstRegion siteB policy4
   inter-route -a VIDEO_STREAMING -srcRegion siteB -dstRegion siteC policy5
EOF
