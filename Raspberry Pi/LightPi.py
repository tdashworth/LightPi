from socket import *
from threading import *
import time, os, colorsys, random, json

serverName = "Tester"
currentColour = -1
current = ""

rPin = 18
gPin = 23
bPin = 24

def broadcast():
    # Create socket
    # Start socket
    # Wait for connection
    # Return (send) server name
    # Loop

    global color
    address = ("", 5625)
    broadcastSocket = socket(AF_INET, SOCK_DGRAM)
    broadcastSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    broadcastSocket.bind(address)

    while True:
        recv_data, addr = broadcastSocket.recvfrom(2048)
        recv_data = recv_data.decode("utf-8")
        print(recv_data)

        if recv_data == "REQUEST":
            broadcastSocket.sendto(serverName.encode("utf-8"), addr)


class Coms:
    receiveQueue = []
    presetThread_stop = Event()
    presetThread = Thread()

    @staticmethod
    def start():
        global current

        print("Light Pi - Name: ", serverName)

        address = ("", 5626)
        commandSocket = socket(AF_INET, SOCK_STREAM)
        commandSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
        commandSocket.setblocking(True)
        commandSocket.bind(address)
        commandSocket.listen(1)

        while True:
            print("Light Pi - Receiver: Waiting connection...")
            (clientSocket, clientAddress) = commandSocket.accept()
            print("Light Pi - Receiver: ", clientAddress)

            reply = {"type": "setup",
                     "current": current}

            Coms.send(reply, clientSocket)

            while 1:
                try:
                    data = clientSocket.recv(2048).decode("utf-8")
                    if data != "":
                        data = json.loads(data)
                        print("Light Pi - Receiver: ", data)
                        Coms.receiveQueue.append(data)
                    else:
                        print("Failed")
                        break
                except:
                    break

    @staticmethod
    def execute():
        while True:
            if len(Coms.receiveQueue) > 0:
                data = Coms.receiveQueue[0]
                Coms.presetThread_stop.set()

                if data["type"] == "setColour":
                    color = colourToRGB(int(data["colour"]))
                    setLED(color[0], color[1], color[2])

                elif data["type"] == "setPreset":
                    Coms.presetThread_stop = Event()

                    if data["preset"] == "fade":
                        Coms.presetThread = Thread(target=Preset.fade, args=(int(data["speed"]), Coms.presetThread_stop), name='thread_function')
                    if data["preset"] == "party":
                        Coms.presetThread = Thread(target=Preset.party, args=(int(data["speed"]), Coms.receiveQueue), name='thread_function')
                    if data["preset"] == "strobe":
                        Coms.presetThread = Thread(target=Preset.strobe, args=(int(data["speed"]), Coms.presetThread_stop), name='thread_function')

                    Coms.presetThread.start()

                Coms.receiveQueue.remove(data)


    @staticmethod
    def send(message, clientSocket):
        try:
            data = json.dumps(message) + '\n'
            clientSocket.send(data.encode("utf-8"))
            pass
        except:
            sys.exc_info()
            pass


def setLED(r=-1, g=-1, b=-1):
    command = ""
    if r >= 0: command += "echo " + str(rPin) + " = " + str(r) + " > /dev/pi-blaster &"
    if g >= 0: command += "echo " + str(gPin) + " = " + str(g) + " > /dev/pi-blaster &"
    if b >= 0: command += "echo " + str(bPin) + " = " + str(b) + " > /dev/pi-blaster"
    
    # os.system(command)


def colourToRGB(color):
    r = int(color / 256 / 256 % 256) / 255
    g = int(color / 256 % 256) / 255
    b = int(color % 256) / 255

    return [r, g, b]


class Preset:
    @staticmethod
    def fade(speed, stop_event):
        h = 0
        speed = speed / 200

        while (not stop_event.is_set()):
            rgb = colorsys.hsv_to_rgb(h, 1, 1)
            print(rgb)
            setLED(rgb[0], rgb[1], rgb[2])

            h += 0.001

            if h > 1:
                h -= 1

            time.sleep(speed)

    @staticmethod
    def strobe(speed, stop_event):
        speed = (101 - speed) / 2000

        while not stop_event.is_set():
            setLED(1, 1, 1)
            time.sleep(speed)
            setLED(0, 0, 0)
            time.sleep(speed)

    @staticmethod
    def police(speed, stop_event):
        speed = (101 - speed) / 300
        print(speed)

        while not stop_event.is_set():
            setLED(1, 0, 0)
            time.sleep(speed)
            setLED(0, 0, 1)
            time.sleep(speed)

    @staticmethod
    def party(speed, stop_event):
        speed = (101 - speed) / 2000

        while not stop_event.is_set():
            h = random.random()
            rgb = colorsys.hsv_to_rgb(h, 1, 1)
            setLED(rgb[0], rgb[1], rgb[2])
            time.sleep(speed)
            setLED(0, 0, 0)
            time.sleep(speed)


broadcastThread = Thread(target=broadcast)
broadcastThread.start()

recieveThread = Thread(target=Coms.start)
recieveThread.start()
