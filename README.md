## Usage Guideline:

1. First, you need to install and run ONOS on your local machine based on the instructions that have been posted here: [Developer Quick Start](https://wiki.onosproject.org/display/ONOS/Developer+Quick+Start). 
     - #### Note: You will need to export several environment variables. The ONOS source comes with a sample bash_profile that can set these variables for you. To do so, follow the instructions that have been posted here: [Set Enivronment Variables](https://wiki.onosproject.org/display/ONOS/ONOS+from+Scratch#ONOSfromScratch-3.Setupyourbuildenvironment)

2.  Second, clone this repoistory and compile osdf-reactive application using the following commands:
    - git clone https://github.com/OpenSDF/OSDF-REACTIVE-APP.git
    - cd osdf-reactive
    - mvn clean install 
3.  Third, after successfully compiling the application, you should install it using the **onos-app** script.
    - onos-app localhost install target/osdf-reactive-1.0-SNAPSHOT.oar 
4-  Forth, after successfully installing the *osdf-reactive* app, you should activate it using the following command from onos cli.
    - app activate org.osdfreactive
