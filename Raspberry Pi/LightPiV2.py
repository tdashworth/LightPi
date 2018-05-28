from socket import *
from threading import *
import os, json, colorsys, time, random

serverName = "Tester"
color = 0
preset = ""

presetStop = Event()

def broadcast():
    # Create socket
    # Start socket
    # Wait for connection
    # Return (send) server name
    # Loop
    global color, serverName

    print("\nLightPi - Controller name: ", serverName)

    global color, preset, presetStop
    address = ("", 5625)
    broadcastSocket = socket(AF_INET, SOCK_DGRAM)
    broadcastSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    broadcastSocket.bind(address)

    print("LightPi: Awaiting data...")
    while True:
        #print("Control Pi - Broadcast: Listening...")
        data, addr = broadcastSocket.recvfrom(2048)
        recv_data = data.decode("utf-8")
        print(recv_data)

        if recv_data == "REQUEST":
            broadcastSocket.sendto(serverName.encode("utf-8"), addr)
        elif recv_data == "COL0UR":
            broadcastSocket.sendto(str(color).encode("utf-8"), addr)
        else:
            if preset != "":
                presetStop.set()
                presetStop = Event()
                preset = ""

            
            if "Fade" in recv_data:
                preset = "Fade"
                Thread(target=Preset.fade, args=(int(recv_data[4:]),presetStop)).start()
            elif "Strobe" in recv_data:
                preset = "Strobe"
                Thread(target=Preset.strobe, args=(int(recv_data[6:]),presetStop)).start()
            elif "Party" in recv_data:
                preset = "Party"
                Thread(target=Preset.party, args=(int(recv_data[5:]),presetStop)).start()
            elif "Police" in recv_data:
                preset = "Police"
                Thread(target=Preset.police, args=(int(recv_data[6:]),presetStop)).start()
            elif "Wake" in recv_data:
                pass
            elif "Dusk" in recv_data:
                pass
            else:
                color = int(recv_data)
                data = toRGB(color)

                setLED(data[0], data[1], data[2])

def setLED(r = -1,g = -1,b = -1):
    command = ""
    if r >= 0: command += "echo 18="+str(r)+" > /dev/pi-blaster &"
    if g >= 0: command += "echo 23="+str(g)+" > /dev/pi-blaster &"
    if b >= 0: command += "echo 24="+str(b)+" > /dev/pi-blaster"

    #os.system(command)

def toRGB(color):
    color = (color * 1)
    r = int(color/256/256 % 256)/255
    g = int(color/256 % 256)/255
    b = int(color % 256)/255

    return [r, g, b]

class Preset:
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



broadcastThread = Thread(target=broadcast).start()
