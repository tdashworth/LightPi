from socket import *
from threading import *
import os, json, colorsys, time, random

class Controller ():
    
    name = ""

    def setup():
        pass

    def startup():
        if name == "":
            setup()

        comsThread = Thread(target=coms).start()

    def coms():
        address = ("", 5625)
        broadcastSocket = socket(AF_INET, SOCK_DGRAM)
        broadcastSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
        broadcastSocket.bind(address)

        while True:
            data, addr = broadcastSocket.recvfrom(2048)
            recvData = data.decode("utf-8").split(",#,")
            print(recvData)

            if recvData[0] == "REQUEST":
                broadcastSocket.sendto(serverName.encode("utf-8"), addr)
            elif recvData[0] == "COL0UR":
                if LEDs.preset != "":
                    broadcastSocket.sendto(str(LEDs.preset).encode("utf-8"), addr)
                else:
                    broadcastSocket.sendto(LEDs.colour.encode("utf-8"), addr)
            elif recvData[0] == "SET PRE":
                LEDS.setPreset(recvData[1], recvData[2])
            elif recvData[0] == "SET COL":
                LEDs.setLED(recvData[1])
            

class LEDs():

    colour = [0,0,0]
    preset = ""

    presetStop = Event()

    def setLED(r = -1,g = -1,b = -1):
        command = ""
        if r >= 0: command += "echo 18="+str(r)+" > /dev/pi-blaster &"
        if g >= 0: command += "echo 23="+str(g)+" > /dev/pi-blaster &"
        if b >= 0: command += "echo 24="+str(b)+" > /dev/pi-blaster"

        if preset != "":
            presetStop.set()
            presetStop = Event()
            preset = ""
            
        #os.system(command)

    def newSetLED(r, g, b): #TO TEST
        command = "echo 18=%s > /dev/pi-blaster & echo 23=%s > /dev/pi-blaster & echo 24=%s > /dev/pi-blaster &" % (r,g,b)

        if preset != "":
            presetStop.set()
            presetStop = Event()
            preset = ""

        #os.system(command)

    def runPreset(name, setting):
        if preset != "":
                presetStop.set()
                presetStop = Event()
                preset = ""

        if name in ["Fade", "Strobe", "Party", "Police"]:
            Thread(target=Presets.exec(name.lower()), args=(int(recv_data[4:]),presetStop)).start()
            preset = name

    class Presets:
    @staticmethod
    def fade(speed, stop_event):
        h = 0
        speed = speed / 200

        while (not stop_event.is_set()):
            rgb = colorsys.hsv_to_rgb(h, 1, 1)
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

