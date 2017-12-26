## Overview
  - [OSDF Architecutre](https://github.com/OpenSDF/OSDF-REACTIVE-APP/wiki/Overview)
  - [OSDF Policy Lanaguage Syntax](https://github.com/OpenSDF/OSDF-REACTIVE-APP/wiki/OSDF-Policy-Language-Syntax)


## Getting Started

### Installation
1. First, you should install and run ONOS on your local machine using the guideline that have been posted here: [Developer Quick Start](https://wiki.onosproject.org/display/ONOS/Developer+Quick+Start). 
     - #### Note: You will need to export several environment variables. The ONOS source comes with a sample *bash_profile* that can set these variables for you. To do so, follow the instructions that have been posted here: [Set Enivronment Variables](https://wiki.onosproject.org/display/ONOS/ONOS+from+Scratch#ONOSfromScratch-3.Setupyourbuildenvironment)

2.  Second, clone this repoistory and compile OSDF application using the following commands:
    - *git clone https://github.com/OpenSDF/OSDF-REACTIVE-APP.git*
    - *cd osdf-reactive*
    - *mvn clean install* 
3.  Third, after compiling the application successfully, you should install it as an ONOS app using the **onos-app** script as follows:
    - *onos-app localhost install target/osdf-reactive-1.0-SNAPSHOT.oar* 
4.  Forth, after installing the *OSDF* app successfully, you should activate it using the following command from onos cli.
    - *onos 127.0.0.1* 
    - *app activate org.osdfreactive*
    
5- Now, *OSDF* is ready!
    
### Examples
After installing ONOS and OSDF app successfully, you will be able to configure a network using high level abstractions that OSDF provides. To test OSDF application, we provide a set of examples as follows: 
   - [Intra site routing (Example #1)](https://github.com/OpenSDF/OSDF-REACTIVE-APP/wiki/Intra-site-routing-%28Example-1%29)
   - [Intra site routing (Example #2)](https://github.com/OpenSDF/OSDF-REACTIVE-APP/wiki/Intra-site-routing-%28Example-2%29)
   - [Intra site routing (Example #3- Firewall)](https://github.com/OpenSDF/OSDF-REACTIVE-APP/wiki/Intra-site-routing-%28Example-3--Firewall%29)
   - [Inter site routing (Example #1)](https://github.com/OpenSDF/OSDF-REACTIVE-APP/wiki/Inter-site-routing-%28Example-1%29)

#### Note 1: If you plan to use this application for your research, please cite our work: 
https://github.com/OpenSDF/OSDF-REACTIVE-APP/wiki/Conference-papers

#### Note 2: Java doc link:
https://opensdf.github.io/OSDF-REACTIVE-APP/
