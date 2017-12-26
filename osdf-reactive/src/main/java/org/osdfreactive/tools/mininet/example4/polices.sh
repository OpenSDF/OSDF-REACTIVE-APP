#!/bin/bash

# Script Configuration:
#
# host     : the controller instance against which this script is run

host=${1:-127.0.0.1}


onos ${host} <<-EOF
   app deactivate org.onosproject.fwd
   intra-route -a PING -region siteA  -sh 00:00:00:00:00:01/None -dh 00:00:00:00:00:02/None  policy1
   intra-route -a WEB -region siteA  -sh 00:00:00:00:00:03/None -dh 00:00:00:00:00:04/None  policy2

EOF
