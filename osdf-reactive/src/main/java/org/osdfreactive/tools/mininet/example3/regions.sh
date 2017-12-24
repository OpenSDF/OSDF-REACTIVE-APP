#!/bin/bash
#
# A simple test topology of three regions, A, B, and C.
#
# Script Configuration:
#
# host     : the controller instance against which this script is run

host=${1:-127.0.0.1}


###------------------------------------------------------
### Start by adding the three regions A, B, and C

# region-add <region-id> <region-name> <region-type> \
#   <lat/Y> <long/X> <locType> <region-master>

onos ${host} <<-EOF
# -- define regions
region-add siteA "siteA" LOGICAL_GROUP 30 20 grid ${host}
region-add siteB "siteB" LOGICAL_GROUP 30 40 grid ${host}
region-add siteC "siteC" LOGICAL_GROUP 30 60 grid ${host}
EOF

### Assign devices to each of their regions

onos ${host} <<-EOF
region-add-devices siteA of:0000000000000002 of:0000000000000003 of:0000000000000001 
region-add-devices siteB of:0000000000000005 of:0000000000000006 of:0000000000000007  of:0000000000000004
region-add-devices siteC of:0000000000000008 of:0000000000000009 of:000000000000000a of:000000000000000b of:000000000000000c
EOF
