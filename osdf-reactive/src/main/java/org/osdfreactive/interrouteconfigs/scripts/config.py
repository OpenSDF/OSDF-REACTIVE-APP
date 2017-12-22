import os


def config():
        os.system('curl --user onos:rocks -vX POST -H "Content-Type: application/json"'
                                ' http://127.0.0.1:8181/onos/v1/network/configuration/ '
                                '-d @~/OSDF-REACTIVE-APP/osdf-reactive/src/main/java/org/osdfreactive/interrouteconfigs/scripts/config.json')



def main():
    config()


main()
